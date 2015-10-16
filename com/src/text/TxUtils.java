package text;

import java.util.HashMap;
import java.util.Map;

public class TxUtils {
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
	public static StringBuilder vis(CharSequence s,StringBuilder b) {
		for (int i=0; i<s.length(); ++i) {
			char c=s.charAt(i);
			if (c>0x20 && c<0x80) b.append(c);
			else b.append(String.format("<%X>", (int)c));
		}
		return b;
	}
	public static String vis(CharSequence s) {
		StringBuilder b=new StringBuilder(s.length());
		return vis(s,b).toString();
	}
	public static String repeat(CharSequence s, int n) {
		StringBuilder b=new StringBuilder(s.length()*n);
		for (int i=0; i < n; ++i)
			b.append(s);
		return b.toString();
	}
}
