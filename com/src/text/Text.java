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

package text;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Text {
	final public static Charset UTF8_Charset = Charset.forName("UTF-8");
	final public static String HEX_DIGITS = "0123456789ABCDEF";

	public static boolean isAnagram(String s1, String s2) {
		if (s1.length() != s2.length()) return false;
		Map<Character, Integer> chars=new HashMap<Character, Integer>();
		//calc histogram
		for (char c:s1.toCharArray()) chars.put(c, 0);
		for (char c:s1.toCharArray()) chars.put(c, chars.get(c)+1);
		for (char c:s2.toCharArray()) {
			if (!chars.containsKey(c)) return false;
			chars.put(c, chars.get(c)-1);
		}
		for (char c:chars.keySet())
			if (chars.get(c)!=0) return false;
		return true;
	}

	private static String join_it(String sep, Iterable<?> a, int off,int len) {
		Iterator<?> it = a.iterator();
		if (!it.hasNext()) return "";
		StringBuilder b=new StringBuilder(30*(sep.length()+2));
		for (int i = 0; ;) {
			if (i < off) {
				it.next();
				continue;
			}
			b.append(it.next());
			if (len==-1 && !it.hasNext()) break;
			if (++i == off+len) break;
			b.append(sep);
		}
		return b.toString();
	}
	private static String join_b(String sep, byte[] a, int off,int len) {
		if (a.length==0) return "";
		if (len < 0) len = a.length;
		StringBuilder b=new StringBuilder(len*(sep.length()+2));
		for (int i = 0; ; ) {
			b.append(String.format("%02X", a[off+i]&0xff));
			if (++i == len) break;
			b.append(sep);
		}
		return b.toString();
	}
	private static String join_i(String sep, int[] a, int off,int len) {
		if (a.length==0) return "";
		if (len < 0) len = a.length;
		StringBuilder b=new StringBuilder(len*(sep.length()+2));
		for (int i = 0; ;) {
			b.append(a[off+i]);
			if (++i == len) break;
			b.append(sep);
		}
		return b.toString();
	}
	private static String join_i(String sep, short[] a, int off,int len) {
		if (a.length==0) return "";
		if (len < 0) len = a.length;
		StringBuilder b=new StringBuilder(len*(sep.length()+2));
		for (int i = 0; ;) {
			b.append(a[off+i]);
			if (++i == len) break;
			b.append(sep);
		}
		return b.toString();
	}
	private static String join_o(String sep, Object[] a, int off,int len) {
		if (a.length==0) return "";
		if (len < 0) len = a.length;
		StringBuilder b=new StringBuilder(len*(sep.length()+2));
		b.append(a[0]);
		for (int i = 0; ; ) {
			b.append(a[off+i]);
			if (++i == len) break;
			b.append(sep);
		}
		return b.toString();
	}
	public static String join(String sep,Object o, int off,int len) {
		if (o == null) return null;
		if (o instanceof byte[]) return join_b(sep, (byte[])o,off,len);
		if (o instanceof short[]) return join_i(sep, (short[])o,off,len);
		if (o instanceof int[]) return join_i(sep, (int[])o,off,len);
		if (o instanceof Object[]) return join_o(sep, (Object[])o,off,len);
		if (o instanceof Iterable) return join_it(sep, (List<?>)o,off,len);
		return o.toString();
	}

	public static String join(String sep,Object o) {
		return join(sep,o,0,-1);
	}

	public static StringBuilder repeat(StringBuilder b, CharSequence s, int n) {
		b.ensureCapacity(b.length()+s.length()*n);
		for (int i=0; i < n; ++i) b.append(s);
		return b;
	}
	public static String repeat(CharSequence s, int n) {
		StringBuilder b=new StringBuilder(s.length()*n);
		return repeat(b, s, n).toString();
	}

	public static StringBuilder hex(StringBuilder b, byte[] s, int off, int len) {
		b.ensureCapacity(b.length()+2*len);
		for (int i=0; i<len; ++i) {
			b.append(String.format("%02X", s[off+i]&0xff));
		}
		return b;
	}
	public static String hex(byte[] s, int off, int len) {
		StringBuilder b=new StringBuilder(s.length);
		return hex(b, s, off, len).toString();
	}
	public static String hex(byte[] s) {
		StringBuilder b=new StringBuilder(s.length);
		return hex(b, s, 0 , s.length).toString();
	}

	public static StringBuilder hexstr(StringBuilder b, byte[] s, int off, int len) {
		for (int i=0; i<len; ++i) {
			b.append(String.format("\\x%02X", s[off+i]&0xff));
		}
		return b;
	}
	public static String hexstr(byte[] s, int off, int len) {
		StringBuilder b=new StringBuilder(s.length);
		return hexstr(b, s, off, len).toString();
	}
	public static String hexstr(byte[] s) {
		StringBuilder b=new StringBuilder(s.length);
		return hexstr(b, s, 0 , s.length).toString();
	}

	public static StringBuilder vis(StringBuilder b, byte[] s, int off, int len) {
		b.ensureCapacity(b.length()+len);
		for (int i=0; i<len; ++i) {
			b.append(Ansi.toString((char)s[off+i]));
		}
		return b;
	}
	public static String vis(byte[] s, int off, int len) {
		StringBuilder b=new StringBuilder(s.length);
		return vis(b, s, off, len).toString();
	}
	public static String vis(byte[] s) {
		StringBuilder b=new StringBuilder(s.length);
		return vis(b, s, 0, s.length).toString();
	}

	public static StringBuilder vis(StringBuilder b, CharSequence s) {
		b.ensureCapacity(b.length()+s.length());
		for (int i=0; i<s.length(); ++i) {
			b.append(Ansi.toString(s.charAt(i)));
		}
		return b;
	}
	public static String vis(CharSequence s) {
		StringBuilder b=new StringBuilder(s.length());
		return vis(b, s).toString();
	}

	public static byte[] bin(CharSequence s) {
		ByteArrayOutputStream ba = new ByteArrayOutputStream(s.length()/2);
		byte bt=0;
		int bc=0;
		for (int i=0; i<s.length(); ++i) {
			bt<<=4;
			char c = Character.toUpperCase(s.charAt(i));
			if (c >= '0' && c <= '9') bc|=c-'0';
			else if (c >= 'A' && c <= 'F') bc|=c-'A'+10;
			else continue;
			++bc;
			if (bc==2) {
				ba.write(bt);
				bc=bt=0;
			}
		}
		if (bc>0) ba.write(bt<<4);

		return ba.toByteArray();
	}

	public static String reverse(String s) {
		StringBuilder b = new StringBuilder(s.length());
		for (int i=b.length(); i>0; )
			b.append(s.charAt(--i));
		return b.toString();
	}
	public static String nextLetterEncode(String str) {
		final String vovels="aeiou";
		StringBuilder b = new StringBuilder(str.length());
		for (int i=0; i<str.length(); ++i) {
			char n=str.charAt(i);
			if (Character.isLetter(n)) {
				if (n=='z') n='a';
				else if (n=='Z') n='A';
				else ++n;
				if (vovels.indexOf(n) >= 0)
					n = Character.toUpperCase(n);
			}
			b.append(n);
		}
		return b.toString();
	}
	public static String longestWord(String str) {
		int li=0,lm=0,l;
		for (int i=0; i < str.length(); ++i) {
			if (!Character.isLetterOrDigit(str.charAt(i))) continue;
			l=1;
			while (i+l < str.length() && Character.isLetterOrDigit(str.charAt(i+l))) ++l;
			if (lm<l) {li=i;lm=l;}
		}
		return str.substring(li,li+lm);
	}
}
