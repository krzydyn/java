package netio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.Buffer;
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

import concur.RingArray;
import sys.Log;

public class SelectorThread2 {
	private static final int RWBUFLEN=1*1024; //by default it is 8kB

	public static interface BindListener {
		void accepted(SelSocket sock);
		void closed(SelSocket sock);
	}

	private int sockCnt = 0;
	private final Selector selector;
	private final List<ByteBuffer> bpool=new ArrayList<>();
	private final Thread selThread = 	new Thread("Selector") {
		@Override
		public void run() {
			Log.notice("selector loop started");
			try { loop(); }
			catch (Throwable e) {
				Log.error(e);
			}
			finally {
				Log.notice("selector loop exited");
			}
		}
	};
	private boolean stopReq = true;

	public SelectorThread2() {
		Selector s = null;
		try {
			s = Selector.open();
		} catch (IOException e) {
			Log.error(e);
		}
		selector = s;
	}

	public boolean isRunning() {
		return selThread.isAlive();
	}

	public void start() {
		stopReq = false;
		selThread.start();
	}
	public void stop() {
		if (stopReq) return ;
		stopReq=true;
		selector.wakeup();
	}

	final ByteBuffer getbuf() {
		ByteBuffer b=null;
		synchronized (bpool) {
			int s = bpool.size();
			if (s > 0) {
				b = bpool.remove(s-1);
				((Buffer)b).clear();//pos=0; limit=capa
			}
		}
		if (b == null){
			b = ByteBuffer.allocate(RWBUFLEN);
			if (b==null) throw new NullPointerException("ByteBuffer.allocate");
			//Log.debug("created buf[%d]", bufcnt);
		}
		return b;
	}
	final void releasebuf(ByteBuffer b) {
		synchronized (bpool) {
			bpool.add(b);
		}
	}

	public void bind(String addr, int port, BindListener l) throws IOException {
		Log.debug("binding to %s:%d",addr==null?"*":addr,port);
		if (l == null) throw new NullPointerException("BindListener is null");
		ServerSocketChannel chn = selector.provider().openServerSocketChannel();
		chn.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		chn.configureBlocking(false);
		if (addr == null || addr.isEmpty()) chn.bind(new InetSocketAddress(port), 3);
		else chn.bind(new InetSocketAddress(addr, port), 3);
		SelectionKey sk = registerSocket(chn, SelectionKey.OP_ACCEPT);
		sk.attach(l);
	}
	public SelSocket connect(String addr, int port) throws IOException {
		Log.debug("connecting %s:%d ...", addr, port);
		SocketChannel chn = selector.provider().openSocketChannel();
		chn.configureBlocking(false);
		chn.connect(new InetSocketAddress(addr, port));

		SelectionKey sk = registerSocket(chn, SelectionKey.OP_READ|SelectionKey.OP_CONNECT);
		SelSocket sock = new SelSocket(this, sk, "sock"+sockCnt+":"); ++sockCnt;
		sk.attach(sock);

		return sock;
	}

	private SelectionKey registerSocket(SelectableChannel chn, int ops) throws IOException {
		selector.wakeup();
		return chn.register(selector, ops);
	}

	void wakeup(SelectionKey sk) {
		SelSocket sock = (SelSocket)sk.attachment();
		int ops = sk.interestOps();
		if (!sock.rdqFull()) ops |= SelectionKey.OP_READ;
		if (sock.wrqSize() > 0) ops |= SelectionKey.OP_WRITE;
		sk.interestOps(ops);
		selector.wakeup();
	}

	private void loop() throws Exception {
		while (!stopReq) {

			int n = selector.select(60000);
			if (n == 0) {
				if (stopReq) break;
				Log.debug("select wakeup");
				//Thread.sleep(10);
				continue;
			}

			for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
				SelectionKey sk = i.next();
				i.remove();

				try {
					//SelectableChannel chn = sk.channel();

					if (!sk.isValid()) doDisconnect(sk, null);
					else if (sk.isAcceptable()) doAccept(sk);
					else if (sk.isConnectable()) doConnect(sk);
					else {
						if (sk.isWritable()) doWrite(sk);
						if (sk.isReadable()) doRead(sk);
					}
				}
				catch (Throwable e) {
					doDisconnect(sk, e);
				}
			}

		}
	}

	private void doAccept(SelectionKey sk) throws IOException {
		Log.debug();
		ServerSocketChannel schn = (ServerSocketChannel)sk.channel();
		SocketChannel chn = schn.accept();
		chn.configureBlocking(false);
		Log.debug("new connection accepted from %s", chn.getRemoteAddress());

		SelectionKey nsk = registerSocket(chn, SelectionKey.OP_READ);
		SelSocket sock = new SelSocket(this, nsk, "sock"+sockCnt + ":"); ++sockCnt;
		nsk.attach(sock);

		// TODO move to worker thread
		BindListener listener = (BindListener)sk.attachment();
		if (listener != null) listener.accepted(sock);
	}

	private void doConnect(SelectionKey sk) throws IOException {
		Log.debug("doConnect");
		SocketChannel chn = (SocketChannel)sk.channel();
		if (!chn.finishConnect()) return ;
		SelSocket sock = (SelSocket)sk.attachment();
		int ops = (sk.interestOps() | SelectionKey.OP_READ) & ~SelectionKey.OP_CONNECT;
		if (sock.wrqSize() > 0) ops |= SelectionKey.OP_WRITE;
		sk.interestOps(ops);
	}


	private void doRead(SelectionKey sk) throws IOException {
		Log.debug("doRead");
		ReadableByteChannel c = (ReadableByteChannel)sk.channel();
		ByteBuffer b = getbuf();
		SelSocket sock = (SelSocket)sk.attachment();
		if (c.read(b) == -1) {
			Log.error("received EOF");
			sock.setEOF();
			sk.cancel(); // throw away
			//c.close();
			//throw new EOFException("End of stream");
			return ;
		}
		((Buffer)b).flip();
		Log.debug("%s received %d bytes", sock.getName(), b.remaining());
		if (!sock.received(b)) {
			int ops = sk.interestOps() & ~SelectionKey.OP_READ;
			sk.interestOps(ops);
		}
	}

	private void doWrite(SelectionKey sk) throws IOException {
		Log.debug("doWrite");
		WritableByteChannel c = (WritableByteChannel)sk.channel();
		SelSocket sock = (SelSocket)sk.attachment();
		ByteBuffer b;
		RingArray<ByteBuffer> wrq = sock.get_wrq();
		int len = wrq.size();
			for (int i = 0; i < len; ++i) {
				b = wrq.peek();
				Log.debug("%s writing %d bytes", sock.getName(), b.remaining());
				int r = c.write(b);
				if (b.remaining() != 0) {
					Log.error("Not all bytes written, r=%d %d/%d", r, b.position(), b.limit());
					break;
				}
				releasebuf(b);
			}
			if (len == wrq.size()) {
				wrq.clear();
				int ops = sk.interestOps() & ~SelectionKey.OP_WRITE;
				sk.interestOps(ops);
				return ;
			}
			while (len > 0) {
				wrq.poll();
				--len;
			}
	}

	private void doDisconnect(SelectionKey sk, Throwable e) {
		if (e != null) Log.error(e, "doDisconnect"); else Log.debug("doDisconnect");
		SelectableChannel ch = sk.channel();
		if (ch instanceof ServerSocketChannel) {
			BindListener l = (BindListener)sk.attachment();
			l.closed(null);
		}
		else {
			SelSocket sock = (SelSocket)sk.attachment();
			if (sock == null) {
				Log.warn("sock is already detached");
				return ;
			}
			if (e instanceof IOException)
				sock.setError((IOException)e);
		}
	}
}
