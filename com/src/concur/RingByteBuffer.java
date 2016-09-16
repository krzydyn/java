package concur;

import sys.Log;

//TODO wrap java.nio.ByteBuffer and add concurrency checks
public class RingByteBuffer extends RingCollection {
	private byte buf[];

	public RingByteBuffer() {
		this(1024);
	}
	public RingByteBuffer(int capacity) {
		buf = new byte[capacity];
	}
	final public int write(byte[] b) {
		return write(b,0,b.length);
	}
	final public int read(byte[] b) {
		return read(b,0,b.length);
	}
	synchronized public int write(byte[] b, int o, int l) {
		if (l < 0) throw new RuntimeException("wrong length "+l);
		if (o < 0 || o+l > b.length) throw new RuntimeException("wrong region o="+o+" l="+l);
		if (len+l > buf.length) l = buf.length-len;
		if (l == 0) return 0;

		int i0=(idx+len)%buf.length;
		if (i0+l < buf.length) {
			Log.debug("writing1: idx=%d, len=%d, l=%d, end=%d",idx,len,l,i0);
			System.arraycopy(b, o, buf, i0, l);
		}
		else {
			int rem = buf.length-i0;
			Log.debug("writing2: idx=%d, len=%d, l=%d, end=%d",idx,len,l,i0);
			System.arraycopy(b, o, buf, i0, rem);
			System.arraycopy(b, o+rem, buf, 0, l-rem);
		}
		len+=l;
		return l;
	}
	synchronized public int read(byte[] b, int o, int l) {
		if (l < 0) throw new RuntimeException("wrong length "+l);
		if (o < 0 || o+l > b.length) throw new RuntimeException("wrong region o="+o+" l="+l);
		if (l > len) l = len;
		if (l == 0) return 0;

		int i0=idx;
		if (i0+l < buf.length) {
			System.arraycopy(buf, idx, b, o, l);
		}
		else {
			int rem = buf.length-i0;
			System.arraycopy(buf, idx, b, o, rem);
			System.arraycopy(buf, 0, b, o+rem, l-rem);
		}
		idx=(idx+l)%buf.length;
		len-=l;
		return l;
	}
}
