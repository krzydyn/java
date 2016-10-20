package crypt;

import sys.Log;

public class GenPort {

	static int genInt(String s, int lo, int hi) {
		long x = s.hashCode()&0xffffffffL;
		int m = hi-lo+1;
		Log.debug("hash = %d",x);
		return (int)(x%m + lo);
	}

	public static void main(String[] args) {
		for (String s : args) {
			Log.prn("Port num: %d", genInt(s, 1025, 9999));
		}
	}
}
