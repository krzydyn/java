package crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		public void write(OutputStream os) throws IOException {
			super.write(os);
			os.write(v);
		}

		@Override
		public int length() {return v.length;}
		@Override
		public void add(TLV t) {
			throw new RuntimeException();
		}
		@Override
		public TLV setValue(byte[] v, int offs, int len) {
			if (len == -1) this.v = v;
			else this.v = Arrays.copyOfRange(v, offs, offs + len);
			return this;
		}
	}

	private static class TLV_Constr extends TLV {
		private List<TLV> sub = new ArrayList<>();

		protected TLV_Constr(Tag tag) {
			super(tag);
		}

		@Override
		public void write(OutputStream os) throws IOException {
			super.write(os);
			for (TLV t : sub) t.write(os);
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
		public TLV setValue(byte[] v, int offs, int len) {
			throw new RuntimeException();
		}
	}

	protected TLV(Tag t) { this.t = t; }

	static public TLV create(int tag) {
		Tag t = new Tag(tag);
		if ((t.bytes[0]&0x20) != 0) return new TLV_Constr(t);
		return new TLV_Primary(t);
	}
	static public TLV create(byte[] tag, int offs, int len) {
		Tag t = new Tag(tag, offs, len);
		if ((t.bytes[0]&0x20) != 0) return new TLV_Constr(t);
		return new TLV_Primary(t);
	}
	static public TLV craete(byte[] tag) {
		return TLV.create(tag, 0, tag.length);
	}

	public final TLV setValue(byte[] v) { return setValue(v, 0, v.length); }

	public abstract void add(TLV t);
	public abstract TLV setValue(byte[] v, int offs, int len);
	public abstract int length();

	protected void write(OutputStream os) throws IOException {
		os.write(t.bytes);
		TLV_BER.lengthWrite(os, length());
	}

	public byte[] toByteArray() {
		int len = length();
		len = t.bytes.length + TLV_BER.lengthBytes(len) + len;
		ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
		try {
			write(ba);
			return ba.toByteArray();
		}
		catch (IOException e) {
			return null;
		}
	}
}
