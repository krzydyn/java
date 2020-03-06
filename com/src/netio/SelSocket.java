package netio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import concur.RingArray;
import sys.Log;

public class SelSocket {
	final private String name;
	final private RingArray<ByteBuffer> wrq;
	final private RingArray<ByteBuffer> rdq;
	final private SelectorThread2 selector;
	final private SelectionKey sk;

	private IOException ioerror = null;
	private boolean eof = false;
	private ByteBuffer hold;

	// package private
	SelSocket(SelectorThread2 selector, SelectionKey sk) {
		this(selector, sk, "");
	}
	SelSocket(SelectorThread2 selector, SelectionKey sk, String name) {
		this.name = name;
		wrq = new RingArray<>(name+"wrq", 10);
		rdq = new RingArray<>(name+"rdq", 10);
		this.selector = selector;
		this.sk = sk;
	}


	final RingArray<ByteBuffer> get_wrq() { return wrq; }

	final void setError(IOException err) {
		ioerror = err;
		rdq.setInterrupted();
	}
	final void setEOF() {
		eof = true;
		rdq.setInterrupted();
	}
	final boolean received(ByteBuffer b) {
		if (hold != null) {
			Log.error("hold in use, packet dropped");
			System.exit(1);
			hold = null;
		}
		if (rdq.add(b)) return true;
		hold = b;
		return false;
	}

	final public String getName() {return  name;}
	final public int wrqSize() { return wrq.size(); }
	final public int rdqSize() { return rdq.size(); }
	final public boolean rdqFull() { return rdq.isFull(); }

	public void close() {
		SelectableChannel chn = sk.channel();
		sk.attach(null);
		sk.cancel();
		if (chn != null) {
			try { chn.close(); } catch (Throwable e) {}
		}
	}

	public int read(byte[] buf, int offs, int len) throws IOException {
		if (ioerror != null) throw ioerror;

		try {
			rdq.peekw();
		} catch (InterruptedException e) {
			Log.error("%s: peekw interrupted", name);
			setEOF();
		}
		if (rdq.isInterrupted()) {
			if (eof) {
				Log.error("read =  EOF");
				return -1;
			}
		}

		int i;
		for (i = 0; rdq.size() > 0 & i < len; ) {
			ByteBuffer b = rdq.peek();
			int n = b.remaining();
			if (n > 0) {
				if (n > len - i) n = len - i;
				b.get(buf, offs + i, n);
				if (!b.hasRemaining()) {
					rdq.poll();
					if (hold != null) {
						rdq.add(hold);
						hold = null;
					}
				}
				i += n;
			}
			else {
				Log.error("empty buffer");
				rdq.poll();
				selector.releasebuf(b);
			}
			selector.wakeup(sk);
		}
		Log.debug("%s.read %d: ", name, i);
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
