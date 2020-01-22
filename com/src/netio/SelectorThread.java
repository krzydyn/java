/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package netio;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

import sys.Log;

public class SelectorThread {
	private final Selector selector;
	private static final int RWBUFLEN=8*1024; //by default it is 8kB
	private final List<ByteBuffer> bpool=new ArrayList<>();
	private boolean running = false;
	private boolean stopReq = false;

	private final List<SelectionKey> writeFlag = new ArrayList<>();
	private final List<AddChannel> addChannelList = new ArrayList<>();

	final static private class AddChannel {
		int ops;
		QueueChannel chn;
		public AddChannel(int ops, QueueChannel chn) {
			this.ops = ops;
			this.chn = chn;
		}
	}
	final static public class QueueChannel {
		private QueueChannel(SelectorThread s, SelectableChannel c, ChannelHandler h) {
			sel=s;
			chn=c;
			hnd=h;
		}

		private final SelectorThread sel;
		private final SelectableChannel chn;
		public final ChannelHandler hnd;
		private List<ByteBuffer> writeq;
		private Object userData;
		private SocketAddress addr;
		private boolean connected=false;

		public void setUserData(Object o) {userData=o;}
		public Object getUserData() {return userData;}
		public int queueSize() { return writeq==null?0:writeq.size(); }

		public boolean isOpen() {return chn.isOpen();}
		public boolean isConnected() {return connected;}
		public void close() {
			SelectionKey sk = chn.keyFor(sel.selector);
			if (sk != null) {
				//TODO postpone closing to Selector loop
				Log.debug("closing QueueChannel");
				sel.disconnect(sk, null);
			}
			else {
				Log.warn("closing non selector Channel");
				if (chn instanceof Closeable) {
					try {
						((Closeable)chn).close();
						connected=false;
						hnd.disconnected(this, null);
					} catch (IOException e) {}
				}
			}
		}
		public void write(ByteBuffer b,boolean part) {
			if (!part && writeq != null &&  b.limit() > RWBUFLEN) {
				int limit = (b.limit()/RWBUFLEN+1)*2+10;
				if (writeq.size() > limit)
					throw new RuntimeException("Output buffer overload, limit="+limit);
			}
			sel.write(chn, b);
		}
		public void write(ByteBuffer b) {
			write(b, false);
		}
	};

	//private final Map<Channel,ChannelState> chns=new HashMap<Channel, ChannelState>();

	public SelectorThread() throws IOException {
		selector = Selector.open();
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		new Thread("Selector") {
			@Override
			public void run() {
				running=true;
				Log.notice("selector loop started");
				try { loop(); }
				catch (Throwable e) {
					Log.error(e);
				}
				finally {
					running=false;
					if (stopReq) Log.notice("selector loop finished, sockets = %d",selector.keys().size());
					else Log.warn("selector loop finished, sockets = %d",selector.keys().size());
					closeAll();
				}
			}
		}.start();
	}
	public void stop() {
		stopReq=true;
		selector.wakeup();
	}

	private void closeAll() {
		for (Iterator<SelectionKey> i = selector.keys().iterator(); i.hasNext(); ) {
			SelectionKey sk = i.next();
			Object o = sk.attachment();
			if (o instanceof SocketChannel)
				disconnect(sk, null);
		}
	}
	public QueueChannel bind(String addr, int port, ChannelHandler d) throws IOException {
		Log.debug("binding to %s:%d",addr==null?"*":addr,port);
		if (d == null) throw new NullPointerException("ChannelHandler is null");
		ServerSocketChannel chn=selector.provider().openServerSocketChannel();
		chn.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		if (addr == null || addr.isEmpty()) chn.bind(new InetSocketAddress(port), 3);
		else chn.bind(new InetSocketAddress(addr, port), 3);
		return addChannel(chn, SelectionKey.OP_ACCEPT, d);
	}
	public QueueChannel connect(String addr, int port, ChannelHandler d) throws IOException {
		Log.debug("connecting ... %s:%d", addr, port);
		if (d == null) throw new NullPointerException("ChannelHandler is null");
		SocketChannel chn = selector.provider().openSocketChannel();
		chn.configureBlocking(false);
		chn.connect(new InetSocketAddress(addr, port));
		return addChannel(chn, SelectionKey.OP_CONNECT|SelectionKey.OP_READ, d);
	}

	private QueueChannel addChannel(SelectableChannel chn, int ops, ChannelHandler hnd) throws IOException {
		if (chn.isBlocking()) chn.configureBlocking(false);//must be non blocking !!!

		QueueChannel c = new QueueChannel(this, chn, hnd);
		synchronized (addChannelList) {
			addChannelList.add(new AddChannel(ops, c));
		}
		if (running) {
			selector.wakeup();
			Thread.yield();
		}
		return c;
	}

	private void write(SelectableChannel chn, ByteBuffer buf) {
		SelectionKey sk = chn.keyFor(selector);
		if (sk == null) {
			Log.error("no key for channel");
			try {chn.close();} catch (IOException e) {}
			return ;
		}
		QueueChannel qchn = (QueueChannel)sk.attachment();
		//Log.debug("writing to queue " + buf);
		while (buf.position() < buf.limit()) {
			ByteBuffer dst =  getbuf();
			int maxbytes = Math.min(dst.remaining(), buf.remaining());
			dst.put(buf.array(), buf.position(), maxbytes);
			((Buffer)buf).position(((Buffer)buf).position() + maxbytes);
			((Buffer)dst).flip();
			synchronized (qchn) {
				if (qchn.writeq == null) qchn.writeq = new ArrayList<>();
				Log.debug("adding buffer to queue");
				qchn.writeq.add(dst);
			}
		}
		synchronized (writeFlag) {
			writeFlag.add(sk);
		}
		if (running) selector.wakeup();
	}

	final private ByteBuffer getbuf() {
		ByteBuffer b=null;
		synchronized (bpool) {
			int s=bpool.size();
			if (s > 0) {
				b=bpool.remove(s-1);
				((Buffer)b).clear();//pos=0; limit=capa
			}
		}
		if (b==null){
			b=ByteBuffer.allocate(RWBUFLEN);
			if (b==null) throw new NullPointerException("ByteBuffer.allocate");
			//Log.debug("created buf[%d]", bufcnt);
		}
		return b;
	}
	final private void releasebuf(ByteBuffer b) {
		synchronized (bpool) {
			bpool.add(b);
		}
	}

	private void loop() throws Exception {
		while (!stopReq) {
			if (addChannelList.size() > 0) {
				int l = addChannelList.size();
				for (int i = 0; i < l; ++i) {
					AddChannel c = addChannelList.get(i);
					SelectableChannel chn = c.chn.chn;
					chn.register(selector, c.ops, c.chn);
					if ((c.ops&SelectionKey.OP_CONNECT)!=0)
						c.chn.addr = ((SocketChannel)chn).getRemoteAddress();
					Log.debug("channel registered");
				}
				synchronized (addChannelList) {
					if (addChannelList.size() == l) addChannelList.clear();
					else while (l > 0) {
						--l;
						addChannelList.remove(0);
					}
				}
			}

			if (writeFlag.size() > 0) {
				synchronized (writeFlag) {
					for (int i = writeFlag.size(); i > 0;) {
						--i;
						SelectionKey sk = writeFlag.get(i);
						if (!sk.isValid()) {
							Log.warn("key %d is invalid (was closed)", i);
							continue;
						}
						int ops = sk.interestOps();
						sk.interestOps(ops|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
					}
					writeFlag.clear();
				}
			}

			//Log.debug("select on %d", selector.keys().size());
			int n=selector.select(10000);
			if (n==0) {
				//Log.debug("select wakeup");
				continue;
			}

			for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
				SelectionKey sk = i.next();
				i.remove();
				//Log.debug("processing selected channel %s", sk.channel());
				try {
					if (!sk.isValid()) disconnect(sk, null);
					else if (sk.isAcceptable()) accept(sk);
					else if (sk.isConnectable()) finishConnect(sk);
					else {
						if (sk.isWritable()) write(sk);
						if (sk.isReadable()) read(sk);
					}
				}
				catch (Throwable e) {
					disconnect(sk, e);
				}
			}
		}
	}

	private void disconnect(SelectionKey sk, Throwable thr) {
		writeFlag.remove(sk);
		QueueChannel qchn = (QueueChannel)sk.attachment();
		while (running && qchn.writeq.size() > 0) {
			Log.debug("waiting wrq %d", qchn.writeq.size());
			try {
				write(sk);
			} catch (IOException e) {
				Log.error(e);
			}
		}

		sk.attach(null);  //unref qchn
		sk.cancel();      //remove from selector
		if (sk.channel() instanceof ServerSocketChannel) {
			Log.error(thr, "server socket");
			return ;
		}
		SocketChannel c = (SocketChannel)sk.channel();
		SocketAddress addr = qchn.addr;
		if (thr == null) {
		}
		else if (thr instanceof EOFException)
			Log.error("%s: peer closed connection", addr);
		else if (thr instanceof ConnectException)
			Log.error("%s: %s", addr, thr.getMessage());
		else if (thr instanceof IOException)
			Log.error("%s(%s): %s", thr.getClass().getName(), addr, thr.getMessage());
		else
			Log.error(thr, "addr: %s", addr);
		try {c.close();} catch (IOException e) { Log.error(e);}
		qchn.connected=false;
		qchn.hnd.disconnected(qchn, thr);
		if (qchn.writeq != null) {
			qchn.writeq.clear();
			qchn.writeq=null;
		}
	}

	private void accept(SelectionKey sk) throws IOException {
		ServerSocketChannel schn = (ServerSocketChannel)sk.channel();
		QueueChannel qchn = (QueueChannel)sk.attachment();
		SocketChannel chn = schn.accept();
		Log.debug("new connection accepted");
		qchn = addChannel(chn, SelectionKey.OP_READ, qchn.hnd.createFilter());
		qchn.connected=true;
		qchn.addr = chn.getRemoteAddress();
		qchn.hnd.connected(qchn);
	}
	private void finishConnect(SelectionKey sk) throws IOException {
		SocketChannel chn = (SocketChannel)sk.channel();
		if (!chn.finishConnect()) return ;
		QueueChannel qchn = (QueueChannel)sk.attachment();
		int ops = sk.interestOps() & ~SelectionKey.OP_CONNECT;
		if (qchn.queueSize() == 0) ops|=SelectionKey.OP_READ;
		else ops|=SelectionKey.OP_READ|SelectionKey.OP_WRITE;
		sk.interestOps(ops);
		qchn.connected=true;
		qchn.addr = chn.getRemoteAddress();
		qchn.hnd.connected(qchn);
	}
	private void read(SelectionKey sk) throws IOException {
		ReadableByteChannel c=(ReadableByteChannel)sk.channel();
		ByteBuffer b=getbuf();
		if (c.read(b) == -1) {
			throw new EOFException("End of stream");
		}
		((Buffer)b).flip();
		QueueChannel qchn = (QueueChannel)sk.attachment();
		qchn.hnd.received(qchn, b);
		releasebuf(b);
	}
	private void write(SelectionKey sk) throws IOException {
		WritableByteChannel c=(WritableByteChannel)sk.channel();
		QueueChannel qchn = (QueueChannel)sk.attachment();
		ByteBuffer b;
		synchronized (qchn) {
			if (qchn.writeq.size()==0) return ;
			b = qchn.writeq.get(0);
			int r = c.write(b);
			if (b.remaining() == 0) {
				releasebuf(b);
				qchn.writeq.remove(0);
				if (qchn.writeq.isEmpty()) {
					int ops = sk.interestOps() & ~SelectionKey.OP_WRITE;
					sk.interestOps(ops);
				}
			}
			else {
				Log.error("Not all bytes written, r=%d %d/%d", r, b.position(), b.limit());
			}
		}
	}
}
