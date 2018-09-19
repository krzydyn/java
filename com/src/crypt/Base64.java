/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package crypt;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Base64 {
	private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String URL64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"; //url
	private static final String RADIX64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; //linux passwords/Gedcom
	private static final String XX64 = "+-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"; //xxencoding
	private static final String UU64 = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"; //uuencoding

	private static final char BASE64_PAD = '=';
	private static final Map<Character, Integer> BASE64inv = new HashMap<Character, Integer>();
	static {
		for (int i = 0; i < BASE64.length(); ++i)
			BASE64inv.put(BASE64.charAt(i), i);
	}

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
		if ((s.length()&0x3) > 0) {
			for (int i = s.length()&0x3; i < 4; ++i)
				s.append(BASE64_PAD);
		}
		return s.toString();
	}

	public static byte[] decode(String s) {
		int c = 0, cbits =0;
		ByteArrayOutputStream b = new ByteArrayOutputStream(s.length());
		//process input bits
		for (int i = 0; i < s.length(); ++i) {
			char ch = s.charAt(i);
			Integer x = BASE64inv.get(ch);
			if (x == null) {
				if (ch == BASE64_PAD) break;
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
