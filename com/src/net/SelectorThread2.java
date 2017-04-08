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
package net;

import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
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

public class SelectorThread2 {
	private final Selector selector;
	private static final int RWBUFLEN=8*1024; //by default it is 8kB
	private final List<ByteBuffer> bpool=new ArrayList<ByteBuffer>();
	private boolean running = false;
	private boolean stopReq = false;
	private boolean registerReq = false;
	private final Object registerLock = new Object();

	private final List<SelectionKey> writeFlag=new ArrayList<SelectionKey>();

	final static public class QueueChannel {
		private QueueChannel(SelectorThread2 s, SelectableChannel c, ChannelHandler h) {
			sel=s;
			chn=c;
			hnd=h;
		}

		private final SelectorThread2 sel;
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

	public SelectorThread2() throws IOException {
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
				try { loop(); }
				catch (Throwable e) {
					Log.error(e);
				}
				finally {
					running=false;
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
				disconect(sk, null);
		}
	}

	public SelectionKey bind(String addr, int port, ChannelHandler d) throws IOException {
		Log.debug("binding to %s:%d",addr==null?"*":addr,port);
		ServerSocketChannel chn=selector.provider().openServerSocketChannel();
		chn.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		if (addr == null || addr.isEmpty()) chn.bind(new InetSocketAddress(port), 3);
		else chn.bind(new InetSocketAddress(addr, port), 3);
		return addChannel(chn, SelectionKey.OP_ACCEPT, d);
	}
	public SelectionKey connect(String addr, int port, ChannelHandler d) throws IOException {
		SocketChannel chn=selector.provider().openSocketChannel();
		chn.configureBlocking(false);
		Log.debug("connecting ... %s:%d", addr, port);
		chn.connect(new InetSocketAddress(addr, port));
		return addChannel(chn, SelectionKey.OP_CONNECT|SelectionKey.OP_READ, d);
	}

	//low level
	public SelectionKey addChannel(SelectableChannel chn, int ops, ChannelHandler d) throws IOException {
		if (chn.isBlocking()) chn.configureBlocking(false);//must be non blocking !!!
		Log.debug("addChannel ...");
		if (running) {
			registerReq=true;
			Log.debug("wakeup selector");
			selector.wakeup();
			Thread.yield();
		}
		SelectionKey sk = chn.register(selector, ops, new QueueChannel(this, chn, d));
		if ((ops&SelectionKey.OP_CONNECT)!=0)
			((QueueChannel)sk.attachment()).addr=((SocketChannel)chn).getRemoteAddress();

		if (registerReq) {
			Log.debug("notifying selector, chn registered");
			synchronized (registerLock) {
				registerReq=false;
				registerLock.notify();
			}
		}
		return sk;
	}

	private void write(SelectableChannel chn, ByteBuffer buf) {
		SelectionKey sk = chn.keyFor(selector);
		QueueChannel qchn = (QueueChannel)sk.attachment();
		//Log.debug("writing to queue " + buf);
		while (buf.position() < buf.limit()) {
			ByteBuffer dst =  getbuf();
			int maxbytes = Math.min(dst.remaining(), buf.remaining());
			dst.put(buf.array(), buf.position(), maxbytes);
			buf.position(buf.position() + maxbytes);
			dst.flip();
			synchronized (qchn) {
				if (qchn.writeq == null) qchn.writeq = new ArrayList<ByteBuffer>();
				/*if (qchn.writeq.size() > 0) {
					ByteBuffer lst = qchn.writeq.get(qchn.writeq.size()-1);
					if (lst.capacity() - lst.limit() >= dst.remaining()) {
						int oldp = lst.position();
						lst.position(lst.limit());
						lst.limit(lst.capacity());
						lst.put(dst);
						lst.limit(lst.position());
						lst.position(oldp);
					}
				}*/
				if (dst!=null)
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
				b.clear();//pos=0; limit=capa
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
		Log.debug("loop started");
		while (!stopReq) {
			if (registerReq) {
				Log.debug("selector wait for chn registered");
				 // wait, so other thread can register new channel
				synchronized (registerLock) {
					registerLock.wait();
				}
			}
			synchronized (writeFlag) {
				for (int i = writeFlag.size(); i > 0;) {
					--i;
					SelectionKey sk = writeFlag.get(i);
					int ops = sk.interestOps();
					sk.interestOps(ops|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
				}
				writeFlag.clear();
			}
			int n=selector.select(10000);
			if (n==0) {
				continue;
			}

			for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
				SelectionKey sk = i.next();
				i.remove();
				//Log.debug("processing selected channel %s", sk.channel());
				try {
					if (!sk.isValid()) disconect(sk, null);
					else if (sk.isAcceptable()) accept(sk);
					else if (sk.isConnectable()) finishConnect(sk);
					else {
						if (sk.isWritable()) write(sk);
						if (sk.isReadable()) read(sk);
					}
				}
				catch (Throwable e) {
					disconect(sk, e);
				}
			}
		}
		Log.debug("loop finished");
	}

	private void disconect(SelectionKey sk, Throwable thr) {
		QueueChannel qchn = (QueueChannel)sk.attachment();
		sk.attach(null);  //unref qchn
		sk.cancel();      //remove from selector
		SocketChannel c = (SocketChannel)sk.channel();
		SocketAddress addr = qchn.addr;
		if (thr == null) ;
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
		qchn.hnd.disconnected(qchn);
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
		sk = addChannel(chn, SelectionKey.OP_READ, qchn.hnd.createFilter());
		qchn = (QueueChannel)sk.attachment();
		qchn.connected=true;
		qchn.addr=chn.getRemoteAddress();
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
		qchn.addr=chn.getRemoteAddress();
		qchn.hnd.connected(qchn);
	}
	private void read(SelectionKey sk) throws IOException {
		ReadableByteChannel c=(ReadableByteChannel)sk.channel();
		ByteBuffer b=getbuf();
		if (c.read(b) == -1) {
			throw new EOFException("End of stream");
		}
		b.flip();
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
					int ops = sk.interestOps()&~SelectionKey.OP_WRITE;
					sk.interestOps(ops);
				}
			}
			else {
				Log.error("Not all bytes written, r=%d %d/%d", r, b.position(), b.limit());
			}
		}
	}
}
