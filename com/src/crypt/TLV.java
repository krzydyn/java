package crypt;

import java.util.ArrayList;
import java.util.List;

import sys.Log;

public abstract class TLV {
	protected int t;

	private static class TLV_Primary extends TLV {
		private byte[] v;

		protected TLV_Primary(int tag) {
			super(tag);
		}

		@Override
		public int length() {return v.length;}

		@Override
		public void add(TLV t) {
			throw new RuntimeException();
		}
	}

	private static class TLV_Constr extends TLV {
		private List<TLV> sub = new ArrayList<>();

		protected TLV_Constr(int tag) {
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
			if ((this.t&0x20) == 0) throw new RuntimeException();
			sub.add(t);
		}
	}

	protected TLV(int tag) { this.t = tag; }

	static public TLV craete(int tag) {
		if ((tag&0x20) != 0) return new TLV_Constr(tag);
		return new TLV_Primary(tag);
	}

	public abstract void add(TLV t);
	public abstract int length();

	public byte[] toByteArray() {
		int l = length();
		Log.debug("buffer len %d",l);
		return null;
	}
}
