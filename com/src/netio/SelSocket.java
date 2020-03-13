package netio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import concur.RingArray;
import sys.Log;

public class SelSocket {
	public static interface SocketListener {
		void connected(SelSocket sock);
		void closed(SelSocket sock);
	}

	final private String name;
	final private List<SocketListener> listeners = new ArrayList<>();
	final private RingArray<ByteBuffer> wrq;
	final private RingArray<ByteBuffer> rdq;
	final private SelectorThread2 selector;
	final private SelectableChannel chn;
	private SelectionKey sk;

	private long bytes_recv = 0, bytes_read = 0;
	private long bytes_sent = 0, bytes_write = 0;
	private IOException ioerror = null;
	private boolean eof = false;
	private ByteBuffer hold;

	// package private
	SelSocket(SelectorThread2 selector, SelectableChannel chn, String name) {
		this.chn = chn;
		this.name = name;
		wrq = new RingArray<>(name+"wrq", 10);
		rdq = new RingArray<>(name+"rdq", 10);
		this.selector = selector;
	}

	final List<SocketListener> getListeners() { return listeners; }
	final SelectableChannel channel() { return chn; }
	final void setSelection(SelectionKey sk) { this.sk = sk; }

	final RingArray<ByteBuffer> get_wrq() { return wrq; }

	final void setError(IOException err) {
		ioerror = err;
		rdq.setInterrupted();
	}
	final void setEOF() {
		eof = true;
		rdq.setInterrupted();
	}
	final void sent(int n) {
		bytes_sent += n;
	}
	final boolean received(ByteBuffer b) {
		if (hold != null) {
			if (rdq.add(hold)) {
				hold = null;
			}
			else {
				Log.error("hold in use, packet dropped");
				selector.releasebuf(b);
				close();
				return false;
			}
		}
		bytes_recv += b.remaining();
		if (rdq.add(b)) return true;
		Log.debug("%s received %d bytes (hold)", name, b.remaining());
		hold = b;
		return false;
	}

	final public void addListener(SocketListener l) {
		listeners.add(l);
	}
	final public String getName() {return  name;}
	final public int wrqSize() { return wrq.size(); }
	final public int rdqSize() { return rdq.size(); }
	final public boolean rdqFull() { return rdq.isFull(); }

	public void close() {
		if (chn != null) {
			try { chn.close(); } catch (Throwable e) {}
		}
		stat();
	}

	public void stat() {
		if (bytes_read != bytes_recv)
			Log.error("%s: rcv %d/%d   snd %d/%d", name, bytes_read, bytes_recv, bytes_write, bytes_sent);
		else
			Log.debug("%s: rcv %d/%d   snd %d/%d", name, bytes_read, bytes_recv, bytes_write, bytes_sent);
	}

	public int read(byte[] buf, int offs, int len) throws IOException {
		if (ioerror != null) throw ioerror;

		if (hold != null) {
			if (rdq.add(hold))
				hold = null;
		}
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
			boolean full = rdq.isFull();
			ByteBuffer b = rdq.peek();
			int n = b.remaining();
			if (n > 0) {
				if (n > len - i) n = len - i;
				b.get(buf, offs + i, n);
				if (!b.hasRemaining()) {
					rdq.poll();
				}
				i += n;
			}
			else {
				Log.error("empty buffer");
				rdq.poll();
				selector.releasebuf(b);
			}
			if (full) selector.wakeup(sk);
		}

		bytes_read += i;
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
		if (!chn.isOpen()) throw new ClosedChannelException();

		int len = buf.remaining();
		bytes_write += len;
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
		if (sk != null)
			selector.wakeup(sk);
	}
}
