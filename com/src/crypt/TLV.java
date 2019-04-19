package crypt;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sys.Log;

public abstract class TLV {
	public static class Tag {
		byte[] bytes;
		public Tag(int tag) {
			bytes = new byte[TLV_BER.tagBytes(tag)];
			for (int i = 0; i < bytes.length; ++i)
				bytes[i] = (byte)((tag >>> i*8)&0xff);
		}
		public Tag(byte[] tag, int offs, int len) {
			bytes = Arrays.copyOfRange(tag, offs, offs + len);
		}
	}
	protected Tag t;

	private static class TLV_Primary extends TLV {
		private byte[] v;

		protected TLV_Primary(Tag tag) {
			super(tag);
		}

		@Override
		public int length() {return v.length;}
		@Override
		public void add(TLV t) {
			throw new RuntimeException();
		}
		@Override
		public TLV set(byte[] v) {
			this.v = v;
			return this;
		}
	}

	private static class TLV_Constr extends TLV {
		private List<TLV> sub = new ArrayList<>();

		protected TLV_Constr(Tag tag) {
			super(tag);
		}

		@Override
		public int length() {
			int l = 0;
			for (TLV t : sub) l += t.length();
			return l;
		}
		@Override
		public void add(TLV t) {
			if ((this.t.bytes[0]&0x20) == 0) throw new RuntimeException();
			sub.add(t);
		}
		@Override
		public TLV set(byte[] v) {
			throw new RuntimeException();
		}
	}

	protected TLV(Tag t) { this.t = t; }

	static public TLV craete(int tag) {
		Tag t = new Tag(tag);
		if ((t.bytes[0]&0x20) != 0) return new TLV_Constr(t);
		return new TLV_Primary(t);
	}
	static public TLV craete(byte[] tag, int offs, int len) {
		Tag t = new Tag(tag, offs, len);
		if ((t.bytes[0]&0x20) != 0) return new TLV_Constr(t);
		return new TLV_Primary(t);
	}

	public abstract void add(TLV t);
	public abstract TLV set(byte[] v);
	public abstract int length();

	void write(OutputStream os) {

	}

	public byte[] toByteArray() {
		int len = length();
		int totlen = t.bytes.length + TLV_BER.lengthBytes(len) + len;
		Log.debug("buffer len %d", totlen);
		return null;
	}
}
