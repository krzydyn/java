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
import netio.SelSocket.SocketListener;
import sys.Log;

public class SelectorThread2 {
	private static final int RWBUFLEN=4*1024; //by default it is 8kB

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
	private final List<SelSocket> addSocklList = new ArrayList<>();

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
		if (selThread.isAlive()) return ;
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

	public SelSocket bind(String addr, int port) throws IOException {
		Log.debug("binding to %s:%d",addr==null?"*":addr,port);
		ServerSocketChannel chn = selector.provider().openServerSocketChannel();
		chn.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		chn.configureBlocking(false);
		if (addr == null || addr.isEmpty()) chn.bind(new InetSocketAddress(port), 3);
		else chn.bind(new InetSocketAddress(addr, port), 3);
		return addsocket(chn);
	}
	public SelSocket connect(String addr, int port) throws IOException {
		Log.debug("connecting %s:%d ...", addr, port);
		SocketChannel chn = selector.provider().openSocketChannel();
		chn.configureBlocking(false);
		chn.connect(new InetSocketAddress(addr, port));
		return addsocket(chn);
	}

	private SelSocket addsocket(SelectableChannel chn) {
		SelSocket sock;
		synchronized (addSocklList) {
			sock = new SelSocket(this, chn, "sock"+sockCnt+":"); ++sockCnt;
			addSocklList.add(sock);
		}
		if (Thread.currentThread() != selThread)
			selector.wakeup();
		return sock;
	}

	void wakeup(SelectionKey sk) {
		SelSocket sock = (SelSocket)sk.attachment();
		int ops = sk.interestOps()&~(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		//int ops = 0;
		if (!sock.rdqFull()) ops |= SelectionKey.OP_READ;
		if (sock.wrqSize() > 0) ops |= SelectionKey.OP_WRITE;
		sk.interestOps(ops);
		selector.wakeup();
	}

	private void loop() throws Exception {
		while (!stopReq) {
			synchronized (addSocklList) {
				for (SelSocket sock : addSocklList) {
					SelectableChannel chn = sock.channel();
					SelectionKey sk = null;
					if (chn instanceof ServerSocketChannel) {
						sk = chn.register(selector, SelectionKey.OP_ACCEPT, sock);
					} else if (chn instanceof SocketChannel) {
						sk = chn.register(selector, SelectionKey.OP_READ|SelectionKey.OP_CONNECT, sock);
					}
					sock.setSelection(sk);
				}
				addSocklList.clear();
			}
			int n = selector.select();
			if (n == 0) {
				if (stopReq) break;
				continue;
			}

			for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
				SelectionKey sk = i.next();
				i.remove();

				try {
					//SelectableChannel chn = sk.channel();

					if (!sk.isValid()) doCancel(sk, null);
					else if (sk.isAcceptable()) doAccept(sk);
					else if (sk.isConnectable()) doConnect(sk);
					else {
						if (sk.isWritable()) doWrite(sk);
						if (sk.isReadable()) doRead(sk);
					}
				}
				catch (Throwable e) {
					doCancel(sk, e);
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

		SelSocket sock = (SelSocket)sk.attachment();
		// TODO move to worker thread
		SelSocket c_sock = addsocket(chn);

		List<SocketListener> listeners = sock.getListeners();
		for (SocketListener l : listeners)
			l.connected(c_sock);
	}

	private void doConnect(SelectionKey sk) throws IOException {
		Log.debug("doConnect");
		SocketChannel chn = (SocketChannel)sk.channel();
		if (!chn.finishConnect()) return ;
		SelSocket sock = (SelSocket)sk.attachment();
		int ops = sk.interestOps()&~(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
		if (!sock.rdqFull()) ops |= SelectionKey.OP_READ;
		if (sock.wrqSize() > 0) ops |= SelectionKey.OP_WRITE;
		sk.interestOps(ops);

		List<SocketListener> listeners = sock.getListeners();
		for (SocketListener l : listeners)
			l.connected(sock);
	}


	private void doRead(SelectionKey sk) throws IOException {
		ReadableByteChannel c = (ReadableByteChannel)sk.channel();
		ByteBuffer b = getbuf();
		SelSocket sock = (SelSocket)sk.attachment();
		if (c.read(b) == -1) {
			Log.error("doRead %s: received EOF", sock.getName());
			sock.setEOF();
			sk.cancel(); // throw away
			//c.close();
			//throw new EOFException("End of stream");
			return ;
		}
		((Buffer)b).flip();
		Log.debug("doRead %s: %d", sock.getName(), b.remaining());
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
				sock.sent(r);
				if (b.remaining() != 0) {
					Log.warn("Not all bytes written, r=%d %d/%d", r, b.position(), b.limit());
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

	private void doCancel(SelectionKey sk, Throwable e) {
		Log.debug("doDisconnect");
		SelSocket sock = (SelSocket)sk.attachment();
		if (sock == null) {
			Log.warn("sock is already detached");
			return ;
		}
		if (e instanceof IOException) {
			sock.setError((IOException)e);
		}
		List<SocketListener> listeners = sock.getListeners();
		for (SocketListener l : listeners)
			l.closed(sock);

	}
}
