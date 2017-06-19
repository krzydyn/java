package crypt;

import sys.Log;

public class GenPort {

	static int genInt(String s, int lo, int hi) {
		long x = s.hashCode();
		int m = hi-lo+1;
		//Log.debug("hash = %d",x);
		if (x < 0) return (int)(m-(-x)%m + lo);
		return (int)(x%m + lo);
	}

	public static void main(String[] args) {
		//int lo=1025, hi=0xffff-1;
		int lo=1025, hi=9999;
		for (String s : args) {
			Log.prn("Port num (%d...%d): %d", lo, hi, genInt(s, lo, hi));
		}
	}
}
