package crypt;

public class Base64 {
	private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	public static String encode(byte[] b) {
		StringBuilder s = new StringBuilder();
		int c = 0, cbits =0;
		//process input bits
		for (int i = 0; i < b.length; ++i) {
			c = (c<<8) | (b[i]&0xff); // 8 bits read
			cbits += 8;
			while (cbits >= 6) {
				cbits -= 6;
				int y = (c >> cbits) & 0x3f; 
				c &= (1 << cbits) - 1;
				s.append(BASE64.charAt(y));
			}
		}
		//final
		if (cbits > 0) {
			c <<= 6 - cbits;
			int y = c & 0x3f;
			s.append(BASE64.charAt(y));
		}
		return s.toString();
	}
}
