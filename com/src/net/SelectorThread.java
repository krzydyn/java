package net;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sys.Log;
import sys.XThread;

public class SelectorThread {
	private final Selector selector;
	private static final int RWBUFLEN=4*1024; //by default it is 8kB
	private final List<ByteBuffer> bpool=new ArrayList<ByteBuffer>();
	private int bufcnt = 0;
	private boolean running;
	
	private final List<SelectionKey> writeFlag=new ArrayList<SelectionKey>();
	
	static class ChannelState {
		public ChannelState(SelectableChannel c, ChannelStatusHandler h) {
			chn=c;
			hnd=h;
		}
		
		final SelectableChannel chn;
		final ChannelStatusHandler hnd;
		List<ByteBuffer> writeq;
		
		ChannelWriter write = new ChannelWriter() {
			public void write(SelectorThread st, ByteBuffer b) {
				st.write(chn, b);
			}
		};
	};
	
	//private final Map<Channel,ChannelState> chns=new HashMap<Channel, ChannelState>();
	
	public SelectorThread() throws IOException {
		selector = Selector.open();
	}
	
	public void start() {
		new Thread("Selector") {
			public void run() {
				running=true;
				try { loop(); }
				catch (Throwable e) {}
				finally {running=false;}
			}
		}.start();
	}
	public void stop() {
		if (running) {
			Log.debug("stopping");
			running=false;
			selector.wakeup();
			XThread.sleep(500);
		}
	}
	
	public void bind(String addr, int port, ChannelStatusHandler d) throws IOException {
		ServerSocketChannel chn=ServerSocketChannel.open();
		chn.bind(new InetSocketAddress(port), 3);
		addChannel(chn, SelectionKey.OP_ACCEPT, d);
		Log.debug("bind registered " + addr);
	}
	public void connect(String host, int port, ChannelStatusHandler d) throws IOException {
		SocketChannel chn=SocketChannel.open();
		chn.configureBlocking(false);
		Log.debug("connecting ... %s:%d", host, port);
		Log.debug("resolving host ... " + host);
		chn.connect(new InetSocketAddress(host, port));
		addChannel(chn, SelectionKey.OP_CONNECT|SelectionKey.OP_READ, d);
	}
	
	//low level
	public SelectionKey addChannel(SelectableChannel chn, int ops, ChannelStatusHandler d) throws IOException {
		chn.configureBlocking(false);//must be non blocked !!!
		SelectionKey sk = chn.register(selector, ops, new ChannelState(chn, d));
		if (running) selector.wakeup();
		return sk;
	}
	
	public void write(SelectableChannel chn, byte[] buf, int offs, int len) {
		write(chn, ByteBuffer.wrap(buf, offs, len));
	}
	
	public void write(SelectableChannel chn, ByteBuffer buf) {
		SelectionKey sk = chn.keyFor(selector);
		ChannelState chnst = (ChannelState)sk.attachment();
		Log.debug("writing to queue " + buf);
		while (buf.position() < buf.limit()) {
			ByteBuffer dst = getbuf();
			int maxbytes = Math.min(dst.remaining(), buf.remaining());
			dst.put(buf.array(), buf.position(), maxbytes);
			buf.position(buf.position() + maxbytes);
			dst.flip();
			synchronized (chnst) {
				if (chnst.writeq == null) chnst.writeq = new ArrayList<ByteBuffer>();
				chnst.writeq.add(dst);
			}
		}
		synchronized (writeFlag) {
			writeFlag.add(sk);	
		}
		//sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		if (running) selector.wakeup();
	}
	
	final private ByteBuffer getbuf() {
		ByteBuffer b=null;
		synchronized (bpool) {
			int s=bpool.size();
			if (s > 0) {
				b=bpool.remove(s-1);
				b.clear();
			}
		}
		if (b==null){
			b=ByteBuffer.allocate(RWBUFLEN);
			if (b==null) throw new NullPointerException("ByteBuffer.allocate");
			++bufcnt;
			Log.debug("created buf[%d]", bufcnt);
		}
		return b;
	}
	final private void releasebuf(ByteBuffer b) {
		synchronized (bpool) {
			bpool.add(b);
		}
	}
	
	private void idle() {}
	private void loop() throws Exception {
		Log.debug("loop started");
		while (running) {
			synchronized (writeFlag) {
				for (int i = writeFlag.size(); i > 0;) {
					--i;
					writeFlag.get(i).interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
				}
				writeFlag.clear();
			}
			int n=selector.select(1000);
			if (n==0) {
				Log.debug("idle: no active channels");
				idle();
				continue;
			}
			
			for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
				SelectionKey sk = i.next();
				i.remove();
				//Log.debug("processing selected channel %s", sk.channel());
				try {
					if (!sk.isValid()) ;
					else if (sk.isAcceptable()) accept(sk);
					else if (sk.isConnectable()) finishConnect(sk);
					else {
						if (sk.isWritable()) write(sk);
						if (sk.isReadable()) read(sk);
					}
				} catch (IOException e) {
					SelectableChannel c = sk.channel();
					Log.error("%s: %s", c, e.getMessage());
					c.close();
					sk.cancel(); //remove from selecif (writeFlag.size() > 0) {tor
				} catch (Throwable e) {
				}
			}
		}
		Log.debug("loop finished");
	}

	private void accept(SelectionKey sk) throws IOException {
		ServerSocketChannel chn = (ServerSocketChannel)sk.channel();
		ChannelState chnst = (ChannelState)sk.attachment();
		SocketChannel client = chn.accept();
		sk = addChannel(client, SelectionKey.OP_READ, chnst.hnd);
		chnst = (ChannelState)sk.attachment();
		chnst.hnd.connected(this, chnst.write);
	}
	private void finishConnect(SelectionKey sk) throws IOException {
		SocketChannel chn = (SocketChannel)sk.channel();
		chn.finishConnect();
		ChannelState chnst = (ChannelState)sk.attachment();
		chnst.hnd.connected(this, chnst.write);
		sk.interestOps(SelectionKey.OP_READ);
	}
	private void read(SelectionKey sk) throws IOException {
		ReadableByteChannel c=(ReadableByteChannel)sk.channel();
		ByteBuffer b=getbuf();
		if (c.read(b) == -1) {
			throw new EOFException("End of stream");
		}
		b.flip();
		Log.debug("read " + b.remaining());
		ChannelState chnst = (ChannelState)sk.attachment();
		chnst.hnd.received(this, chnst.write, b);
		releasebuf(b);
	}
	private void write(SelectionKey sk) throws IOException {
		WritableByteChannel c=(WritableByteChannel)sk.channel();
		ChannelState chnst = (ChannelState)sk.attachment();
		ByteBuffer b;
		synchronized (chnst) {
			b = chnst.writeq.get(0);
			int r = c.write(b);
			if (b.remaining() == 0) {
				releasebuf(b);
				chnst.writeq.remove(0);
				if (chnst.writeq.isEmpty()) {
					sk.interestOps(SelectionKey.OP_READ);
				}
			}
			else {
				Log.error("Not all bytes written, r=%d %d/%d", r, b.position(), b.limit());
			}
		}
	}
}
