package crypt;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import sys.Log;

public class Encoder64 {
	public static enum Mode {
		BASE64,
		URL64,
		XX64,
		UU64,
		BINHEX,
		RADIX64,
	}
	private static final String BASE64  = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String URL64   = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
	private static final String XX64    = "+-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String UU64    = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_";
	private static final String BINHEX  = "!\"#$%&'()*+,-012345689@ABCDEFGHIJKLMNPQRSTUVXYZ[`abcdefhijklmpqr";
	private static final String RADIX64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private final String alphabet;
	private final char pad;
	private final Map<Character, Integer> invalphabet = new HashMap<>(64);

	public Encoder64(Mode m) {
		String x = null;
		char p = 0;
		switch (m) {
		case BASE64:
			x = Encoder64.BASE64;
			p = '=';
			break;
		case URL64:
			x = Encoder64.URL64;
			break;
		case XX64:
			x = Encoder64.XX64;
			break;
		case UU64:
			x = Encoder64.UU64;
			p = '`';
			break;
		case BINHEX:
			x = Encoder64.BINHEX;
			p = '-';
			break;
		case RADIX64:
			x = Encoder64.RADIX64;
			p = '-';
			break;
		default:
			throw new RuntimeException("Mode not supported "+m);
		}
		alphabet = x;
		pad = p;
		for (int i = 0; i < x.length(); ++i)
			invalphabet.put(x.charAt(i), i);
	}

	public String bencode(byte[] b) {
		StringBuilder s = new StringBuilder(b.length*8/6);
		int c = 0, cbits =0;
		//process input bits
		for (int i = 0; i < b.length; ++i) {
			c = (c<<8) | (b[i]&0xff); // 8 bits read
			cbits += 8;
			while (cbits >= 6) {
				cbits -= 6;
				int y = (c >> cbits) & 0x3f;
				c &= (1 << cbits) - 1;
				s.append(alphabet.charAt(y));
			}
		}
		//final
		if (cbits > 0) {
			c <<= 6 - cbits;
			int y = c & 0x3f;
			s.append(alphabet.charAt(y));
		}
		if (pad != 0) {
			if ((s.length()&0x3) > 0) {
				for (int i = s.length()&0x3; i < 4; ++i)
					s.append(pad);
			}
		}
		return s.toString();
	}

	public byte[] bdecode(String s) {
		int c = 0, cbits =0;
		ByteArrayOutputStream b = new ByteArrayOutputStream(s.length()*6/8);
		//process input bits
		for (int i = 0; i < s.length(); ++i) {
			char ch = s.charAt(i);
			Integer x = invalphabet.get(ch);
			if (x == null) {
				if (ch == pad) break;
				if (!Character.isSpaceChar(ch))
					Log.error("invalid input char <#%02X>", (int)ch);
				continue;
			}
			c = (c<<6) | x.intValue(); // 6 bits read
			cbits += 6;
			while (cbits >= 8) {
				cbits -= 8;
				int y = (c >> cbits) & 0xff;
				c &= (1 << cbits) - 1;
				b.write(y);
			}
		}
		return b.toByteArray();
	}
}
