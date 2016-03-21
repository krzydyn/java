package crypt;

import sys.Log;

public class Base64 {
	private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	public static String encode(byte[] b) {
		StringBuilder s = new StringBuilder();
		int c = 0, cbits =0;
		for (int i = 0; i < b.length; ++i) {
			c = (c<<8) | (b[i]&0xff); // 8 bits read
			Log.debug("read byte %02x", b[i]);
			cbits += 8;
			while (cbits >= 6) {
				cbits -= 6;
				int y = (c >> cbits) & 0x3f; 
				c &= (1 << cbits) - 1;
				s.append(BASE64.charAt(y));
				Log.debug("y=%02x, carry %02x/%d", y, c, cbits);
			}
		}
		if (cbits > 0) {
			if (cbits < 6) {
				c <<= 6 - cbits;
				cbits = 6;
			}
			while (cbits >= 6) {
				cbits -= 6;
				int y = (c >> cbits) & 0x3f; 
				c &= (1 << cbits) - 1;
				s.append(BASE64.charAt(y));
			}
		}
		return s.toString();
	}
}
