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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Text {
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

	private static String join_iter(Iterable<?> a, String sep) {
		Iterator<?> it = a.iterator();
		if (!it.hasNext()) return "";
		StringBuilder b=new StringBuilder(30*(sep.length()+2));
		b.append(it.next());
		while (it.hasNext()) {
			b.append(sep);
			b.append(it.next());
		}
		return b.toString();
	}
	private static String join_b(byte[] a, String sep) {
		if (a.length==0) return "";
		StringBuilder b=new StringBuilder(a.length*(sep.length()+2));
		for (int i = 0; ; ) {
			b.append(String.format("%02X", a[i]&0xff));
			if (++i == a.length) break;
			b.append(sep);
		}
		return b.toString();
	}
	private static String join_i(int[] a, String sep) {
		if (a.length==0) return "";
		StringBuilder b=new StringBuilder(a.length*(sep.length()+2));
		b.append(a[0]);
		for (int i = 1; i < a.length; ++i) {
			b.append(sep);
			b.append(a[i]);
		}
		return b.toString();
	}
	private static String join_o(Object[] a, String sep) {
		if (a.length==0) return "";
		StringBuilder b=new StringBuilder(a.length*(sep.length()+2));
		b.append(a[0]);
		for (int i = 1; i < a.length; ++i) {
			b.append(sep);
			b.append(a[i]);
		}
		return b.toString();
	}
	public static String join(Object o, String sep) {
		if (o == null) return null;
		if (o instanceof byte[]) return join_b((byte[])o, sep);
		if (o instanceof int[]) return join_i((int[])o, sep);
		if (o instanceof Object[]) return join_o((Object[])o, sep);
		if (o instanceof Iterable) return join_iter((List<?>)o, sep);
		return o.toString();
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
}
