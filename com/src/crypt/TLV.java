package crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sys.Log;
import text.Text;

public abstract class TLV {
	public static class Tag {
		byte[] bytes;
		public Tag(int tag) {
			bytes = new byte[TLV_BER.tagBytes(tag)];
			for (int i = 0; i < bytes.length; ++i)
				bytes[i] = (byte)((tag >>> i*8)&0xff);
			if (!verify()) throw new RuntimeException();
		}
		public Tag(byte[] tag, int offs, int len) {
			bytes = Arrays.copyOfRange(tag, offs, offs + len);
			if (!verify()) throw new RuntimeException();
		}

		public boolean verify() {
			if (bytes.length == 1) {
				return (bytes[0]&TLV_BER.TAG_SUBSEQ) != TLV_BER.TAG_SUBSEQ;
			}
			if ((bytes[0]&TLV_BER.TAG_SUBSEQ) != TLV_BER.TAG_SUBSEQ) return false;
			if ((bytes[bytes.length-1]&TLV_BER.TAG_NEXT) != 0) return false;
			for (int i = 1; i < bytes.length-1; ++i) {
				if ((bytes[i]&TLV_BER.TAG_NEXT) == 0) return false;
			}
			return true;
		}
	}
	protected TLV_Constr parent;
	protected Tag t;

	private static class TLV_Primitive extends TLV {
		static byte[] empty = {};
		private byte[] v;

		protected TLV_Primitive(Tag tag) {
			super(tag);
		}

		@Override
		public void write(OutputStream os) throws IOException {
			writeTL(os);
			os.write(v);
		}

		@Override
		public int length() {return v.length;}
		@Override
		public byte[] value() {return v;}
		@Override
		public void add(TLV t) {
			throw new RuntimeException();
		}
		@Override
		public TLV get(int idx) {
			throw new RuntimeException();
		}
		@Override
		public TLV find(int idx, Tag tag) {
			throw new RuntimeException();
		}
		@Override
		public TLV setValue(byte[] v, int offs, int len) {
			if (len == 0) this.v = empty;
			else this.v = Arrays.copyOfRange(v, offs, offs + len);
			return this;
		}
	}

	private static class TLV_Constr extends TLV {
		private List<TLV> sub = new ArrayList<>();
		private int cashedLen = -1;

		protected TLV_Constr(Tag tag) {
			super(tag);
		}
		protected void writeValue(OutputStream os) throws IOException {
			for (TLV t : sub) t.write(os);
		}

		@Override
		public void write(OutputStream os) throws IOException {
			writeTL(os);
			writeValue(os);
		}

		@Override
		public int length() {
			if (cashedLen >= 0) {
				//Log.debug("return cashed value " + cashedLen);
				return cashedLen;
			}
			int l = 0;
			for (TLV t : sub) l += t.opaqueLength();
			cashedLen = l;
			return l;
		}
		@Override
		public byte[] value() {
			ByteArrayOutputStream ba = new ByteArrayOutputStream(opaqueLength());
			try {
				writeValue(ba);
			}catch (Exception e) {
				return null;
			}
			return ba.toByteArray();
		}
		@Override
		public void add(TLV t) {
			if (t.parent != null)
				t.parent.remove(t);
			t.parent = this;
			sub.add(t);
			cashedLen = -1;
			for (TLV_Constr p = parent; p != null; p = p.parent)
				p.cashedLen = -1;
		}
		public void remove(TLV tlv) {
			sub.remove(tlv);
			tlv.parent = null;
			cashedLen = -1;
			for (TLV_Constr p = parent; p != null; p = p.parent)
				p.cashedLen = -1;
		}
		@Override
		public TLV get(int idx) {
			return sub.get(idx);
		}
		@Override
		public TLV find(int idx, Tag tag) {
			for (int i=idx; i < sub.size(); ++i) {
				TLV t = sub.get(i);
				if (tag.equals(t.t)) return t;
			}
			return null;
		}
		@Override
		public TLV setValue(byte[] v, int offs, int len) {
			throw new RuntimeException();
		}
	}

	protected TLV(Tag tag) { this.t = tag; }
	public boolean isConstructed() {
		return (t.bytes[0]&TLV_BER.TAG_CONSTR) != 0;
	}

	static public TLV create(int tag) {
		Tag t = new Tag(tag);
		if ((t.bytes[0]&0x20) != 0) return new TLV_Constr(t);
		return new TLV_Primitive(t);
	}
	static public TLV create(byte[] tag, int offs, int len) {
		Tag t = new Tag(tag, offs, len);
		if ((t.bytes[0]&0x20) != 0) return new TLV_Constr(t);
		return new TLV_Primitive(t);
	}
	static public TLV create(byte[] tag) {
		return TLV.create(tag, 0, tag.length);
	}

	public abstract void add(TLV t);
	public abstract TLV get(int idx);
	public abstract TLV find(int idx, Tag tag);
	public abstract TLV setValue(byte[] v, int offs, int len);

	public abstract int length();
	public abstract byte[] value();
	public abstract void write(OutputStream os) throws IOException;

	public final byte[] tag() { return t.bytes; }
	public final int opaqueLength() {
		int len = length();
		return t.bytes.length + TLV_BER.lengthBytes(len) + len;
	}
	public final TLV setValue(byte[] v) { return setValue(v, 0, v.length); }
	public final TLV find(int idx, int tag) { return find(idx, new Tag(tag)); }

	public byte[] toByteArray() {
		int len = length();
		len = t.bytes.length + TLV_BER.lengthBytes(len) + len;
		ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
		try {
			write(ba);
		}
		catch (IOException e) {}
		return ba.toByteArray();
	}

	protected void writeTL(OutputStream os) throws IOException {
		os.write(t.bytes);
		TLV_BER.lengthWrite(os, length());
	}


	static public TLV load(InputStream is) throws IOException {
		int r;
		while ((r=is.read())==0) ;
		if (r == -1) return null;

		// TAG
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		ba.write(r);
		if ((r&TLV_BER.TAG_SUBSEQ) == TLV_BER.TAG_SUBSEQ){
			while ((r=is.read()) >= 0) {
				ba.write(r);
				if ((r&TLV_BER.TAG_NEXT) == 0) break;
			}
		}

		TLV tlv = create(ba.toByteArray());
		ba.reset();
		Log.debug("TAG=%s", Text.hex(tlv.tag()));

		// LENGTH
		r=is.read();
		if (r == -1) return null;
		int vl = r;
		if ((r&TLV_BER.LEN_BYTE) != 0) {
			int ll = vl&0x7f;
			vl=0;
			for (int i=0; i < ll && (r=is.read()) >= 0; ++i) {
				vl <<= 8; vl |= r;
			}
		}
		Log.debug("LENGTH=%d", vl);

		// VALUE
		if (tlv.isConstructed()) {
			while (tlv.length() < vl) {
				TLV t = load(is);
				if (t == null) break;
				Log.debug("add tlv t=%s l=%d", Text.hex(t.tag()), t.length());
				tlv.add(t);
			}
			if (tlv.length() < vl) {
				Log.error("constr data too short, %d < %d", tlv.length(), vl);
			}
		}
		else {
			for (int i=0; i < vl && (r=is.read()) >= 0; ++i) {
				ba.write(r);
			}
			tlv.setValue(ba.toByteArray());
			if (tlv.length() < vl) {
				Log.error("data too short, %d < %d", tlv.length(), vl);
			}
		}
		ba.reset();
		Log.debug("VALUE=%s", Text.hex(tlv.value()));

		return tlv;
	}
}
