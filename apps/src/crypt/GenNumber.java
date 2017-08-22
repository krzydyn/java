package crypt;

import sys.Log;

public class GenNumber {

	static int genInt(String s, int lo, int hi) {
		int x = s.hashCode();
		int m = hi-lo;
		//Log.debug("hash = %d",x);
		if (x < 0) return m-(-x)%m + lo;
		return x%m + lo;
	}

	public static void main(String[] args) {
		//int lo=1025, hi=0xffff;
		int lo=1025, hi=10000;
		for (String s : args) {
			Log.prn("Generated number (%s in %d...%d): %d", s, lo, hi, genInt(s, lo, hi));
		}
	}
}
