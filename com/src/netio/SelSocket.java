package netio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import concur.RingArray;
import sys.Log;

public class SelSocket {
	final private RingArray<ByteBuffer> wrq = new RingArray<>("wrq", 10);
	final private RingArray<ByteBuffer> rdq = new RingArray<>("rdq", 10);
	final private SelectorThread2 selector;
	final private SelectionKey sk;

	private IOException ioerror = null;
	private boolean eof = false;

	// package private
	SelSocket(SelectorThread2 selector, SelectionKey sk) {
		this.selector = selector;
		this.sk = sk;
	}

	final RingArray<ByteBuffer> get_wrq() { return wrq; }
	final RingArray<ByteBuffer> get_rdq() { return rdq; }
	final void setError(IOException err) {
		ioerror = err;
		rdq.setInterrupted();
	}
	final void setEOF() {
		eof = true;
		rdq.setInterrupted();
	}
	final void received(ByteBuffer b) {
		try {
			//TODO stop reading nio channel
			if (!rdq.addw(b, 100))
				Log.error("FATAL: dopped packet");
		} catch (InterruptedException e) {
			Log.error("FATAL: dopped packet (intr)");
		}
	}


	public int wrqSize() {
		return wrq.size();
	}
	public int rdqSize() {
		return rdq.size();
	}

	public void close() {
		Log.debug("sock.close");
		SelectableChannel chn = sk.channel();
		sk.attach(null);
		sk.cancel();
		if (chn != null) {
			try { chn.close(); } catch (Throwable e) {}
		}
	}

	public int read(byte[] buf, int offs, int len) throws IOException {
		if (ioerror != null) throw ioerror;

		while (rdq.size() == 0) {
			try {
				rdq.peekw();
			} catch (InterruptedException e) {
				Log.error("peekw interrupted");
			}
			if (rdq.isInterrupted()) {
				if (eof) {
					Log.error("read =  EOF");
					return -1;
				}
			}
		}

		Log.debug("reading rdq rdq.size = %d", rdq.size());
		int i;
		for (i = 0; rdq.size() > 0 & i < len; ) {
			ByteBuffer b = rdq.peek();
			int n = b.remaining();
			if (n > 0) {
				if (n > len - i) n = len - i;
				b.get(buf, offs + i, n);
				if (!b.hasRemaining()) rdq.poll();
				i += n;
			}
			else {
				rdq.poll();
				selector.releasebuf(b);
			}
		}
		return i;
	}
	public void write(byte[] buf) throws IOException {
		write(buf, 0, buf.length);
	}
	public void write(byte[] buf, int offs, int len) throws IOException {
		write(ByteBuffer.wrap(buf, offs, len));
	}

	public void write(ByteBuffer buf) throws IOException {
		if (ioerror != null) throw ioerror;

		int len = buf.remaining();
		if (wrq.size() > 0) {
			ByteBuffer b = wrq.peek();
			b.compact();
			if (len < b.capacity() - b.limit()) {
				b.position(b.limit()).limit(b.capacity());
				b.put(buf);
				b.flip();
				return ;
			}
		}

		for (int i = 0; i < len; ) {
			ByteBuffer b = selector.getbuf();
			int n = b.capacity();
			if (n > len - i) n = len - i;
			b.put(buf.array(), buf.arrayOffset()+buf.position() + i, len - i);
			b.flip();
			wrq.add(b);
			i += n;
		}
		selector.wakeup(sk);
	}
}
