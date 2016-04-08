package text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Text {
	public static String HEX_DIGITS = "0123456789ABCDEF";
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

	private static String join_l(List<?> a, String sep) {
		if (a.size()==0) return "";
		StringBuilder b=new StringBuilder(a.size()*(sep.length()+2));
		b.append(a.get(0));
		for (int i = 1; i < a.size(); ++i) {
			b.append(sep);
			b.append(a.get(i));
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
		if (o instanceof int[]) return join_i((int[])o, sep);
		if (o instanceof Object[]) return join_o((Object[])o, sep);
		if (o instanceof List) return join_l((List<?>)o, sep);
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

	public static StringBuilder hex(StringBuilder b, byte[] s) {
		b.ensureCapacity(b.length()+s.length);
		for (int i=0; i<s.length; ++i) {
			b.append(String.format("%02X", s[i]&0xff));
		}
		return b;
	}
	public static String hex(byte[] s) {
		StringBuilder b=new StringBuilder(s.length);
		return hex(b, s).toString();
	}

	public static StringBuilder vis(StringBuilder b, byte[] s, int off, int len) {
		b.ensureCapacity(b.length()+s.length);
		for (int i=0; i<len; ++i) {
			if (off+i >= s.length) throw new IndexOutOfBoundsException();
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

}
