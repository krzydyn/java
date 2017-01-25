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
import sys.XThread;

public class SelectorThread2 {
	private final Selector selector;
	private static final int RWBUFLEN=4*1024; //by default it is 8kB
	private final List<ByteBuffer> bpool=new ArrayList<ByteBuffer>();
	private boolean running = false;
	private boolean stopReq = false;
	private boolean registerReq = false;

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

		public boolean isOpen() {return chn.isOpen();}
		public void write(ByteBuffer b) {
			if (!chn.isOpen()) {
				throw new RuntimeException("chn is not opened");
			}
			sel.write(chn, b);
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
		registerReq=true;
		if (running) selector.wakeup();
		SelectionKey sk = chn.register(selector, ops, new QueueChannel(this, chn, d));
		registerReq=false;
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
		//sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
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
			synchronized (writeFlag) {
				for (int i = writeFlag.size(); i > 0;) {
					--i;
					writeFlag.get(i).interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
				}
				writeFlag.clear();
			}
			int n=selector.select(1000);
			if (n==0) {
				if (registerReq) XThread.sleep(10); //this sleep allows to register new channel
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
		SocketAddress addr = null;
		try { addr = c.getRemoteAddress(); } catch (Exception e) {}
		if (thr == null) ;
		else if (thr instanceof EOFException)
			Log.error("%s: peer closed connection", addr);
		else
			Log.error("%s(%s): %s", thr.getClass().getName(), addr, thr.getMessage());
		try {c.close();} catch (IOException e) { Log.error(e);}
		qchn.hnd.disconnected(qchn);
	}

	private void accept(SelectionKey sk) throws IOException {
		ServerSocketChannel chn = (ServerSocketChannel)sk.channel();
		QueueChannel qchn = (QueueChannel)sk.attachment();
		SocketChannel client = chn.accept();
		sk = addChannel(client, SelectionKey.OP_READ, qchn.hnd.createFilter());
		qchn = (QueueChannel)sk.attachment();
		qchn.hnd.connected(qchn);
	}
	private void finishConnect(SelectionKey sk) throws IOException {
		SocketChannel chn = (SocketChannel)sk.channel();
		chn.finishConnect();
		QueueChannel chnst = (QueueChannel)sk.attachment();
		chnst.hnd.connected(chnst);
		sk.interestOps(SelectionKey.OP_READ);
	}
	private void read(SelectionKey sk) throws IOException {
		ReadableByteChannel c=(ReadableByteChannel)sk.channel();
		ByteBuffer b=getbuf();
		if (c.read(b) == -1) {
			throw new EOFException("End of stream");
		}
		b.flip();
		QueueChannel chnst = (QueueChannel)sk.attachment();
		chnst.hnd.received(chnst, b);
		releasebuf(b);
	}
	private void write(SelectionKey sk) throws IOException {
		WritableByteChannel c=(WritableByteChannel)sk.channel();
		QueueChannel chnst = (QueueChannel)sk.attachment();
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
