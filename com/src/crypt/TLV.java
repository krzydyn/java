package crypt;

import java.util.ArrayList;
import java.util.List;

public class TLV {
	private List<TLV> sub = new ArrayList<>();
	private int t;

	static class TLV_Prim extends TLV {
		private int l;
		private byte[] v;
	}

	public TLV() {}

	public byte[] toByteArray() {
		return null;
	}
}
