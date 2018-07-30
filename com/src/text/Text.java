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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import algebra.Maths;
import sys.Log;

public class Text {
	final public static String HEX_DIGITS = "0123456789ABCDEF";

	public static boolean isAnagram(String s1, String s2) {
		if (s1.length() != s2.length()) return false;
		Map<Character, Integer> chars=new HashMap<>();
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
	private static String join_i(String sep, long[] a, int off,int len) {
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
	private static String join_it(String sep, Iterable<?> a, int off,int len) {
		Iterator<?> it = a.iterator();
		if (!it.hasNext()) return "";
		StringBuilder b=new StringBuilder((len<0?10:len)*(sep.length()+2));
		for (int i = 0; it.hasNext();) {
			Object o = it.next();
			if (i < off) {o=null;continue;}
			if (o.getClass().isArray()) {
				b.append("[");
				b.append(join(sep, o, 0, -1));
				b.append("]");
			}
			else if (o instanceof Iterable) {
				b.append("[");
				b.append(join_it(sep, (Iterable<?>)o, 0, -1));
				b.append("]");
			}
			else b.append(o);
			if (len > 0 && ++i == off+len) break;
			b.append(sep);
		}
		return b.toString();
	}
	private static String join_o(String sep, Object[] a, int off,int len) {
		if (a.length==0) return "";
		if (len < 0) len = a.length;
		StringBuilder b=new StringBuilder(len*(sep.length()+2));
		for (int i = 0; ; ) {
			Object o = a[off+i];
			if (o.getClass().isArray()) {
				b.append("[");
				b.append(join(sep, o, 0, -1));
				b.append("]");
			}
			else if (o instanceof Iterable) {
				b.append("[");
				b.append(join_it(sep, (Iterable<?>)o, 0, -1));
				b.append("]");
			}
			else b.append(o.toString());
			if (++i == len) break;
			b.append(sep);
		}
		return b.toString();
	}
	public static String join(String sep,Object o, int off,int len) {
		if (o == null) return null;
		if (o.getClass().isArray()) {
			if (o instanceof byte[]) return join_b(sep, (byte[])o,off,len);
			if (o instanceof short[]) return join_i(sep, (short[])o,off,len);
			if (o instanceof int[]) return join_i(sep, (int[])o,off,len);
			if (o instanceof long[]) return join_i(sep, (long[])o,off,len);
			if (o instanceof Object[]) return join_o(sep, (Object[])o,off,len);
			if (o instanceof Iterable) return join_it(sep, (Iterable<?>)o,off,len);
		}
		return o.toString();
	}

	public static String join(String sep,Object o, int len) {
		return join(sep,o,0,len);
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
		if (s == null) return null;
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
			b.append(Ansi.toString((char)(s[off+i]&0xff)));
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
			char c = s.charAt(i);
			if (c >= '0' && c <= '9') bt|=c-'0';
			else if (c >= 'a' && c <= 'f') bt|=c-'a'+10;
			else if (c >= 'A' && c <= 'F') bt|=c-'A'+10;
			else {continue;}
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
				if (vovels.indexOf(n) >= 0)
					n = Character.toUpperCase(n);
				if (n=='z') n='a';
				else if (n=='Z') n='A';
				else ++n;
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

	public static int findDiffIndex(String s1, String s2) {
		int i;
		for (i=0; i < s1.length() && i < s2.length(); ++i) {
			if (s1.charAt(i)!=s2.charAt(i)) return i;
		}
		return i;
	}

	//Simple alg O(n^2*m)
	public static List<String> lcsub_simple(String s1, String s2) {
		int n=s1.length(), m=s2.length();
		int z=0;
		List<String> ret = new ArrayList<>();
		for (int i=0; i < n; ++i) {
			for (int j=0; j < m; ++j) {
				int k;
				for (k=0; k+i < n && k+j < m; ++k) {
					if (s1.charAt(k+i) != s2.charAt(k+j)) break;
				}
				if (k==0) continue;
				if (k > z) {
					ret.clear();
					z=k;
					ret.add(s1.substring(i, i+z));
				}
				else if (k == z) {
					ret.add(s1.substring(i, i+z));
				}
			}
		}
		return ret;
	}

	//Dynamic programming O(n*m) (suffix array would be more efficient)
	public static List<String> lcsub(String s1, String s2) {
		int n=s1.length(), m=s2.length();
		if (m > n) {
			String t=s1; s1=s2; s2=t;
			n=s1.length(); m=s2.length();
		}
		int[] prev = new int[m];
		int[] curr = new int[m];
		int z=0;
		List<String> ret = new ArrayList<>();
		for (int i=0; i < n; ++i) {
			for (int j=0; j < m; ++j) {
				if (s1.charAt(i) != s2.charAt(j)) curr[j]=0;
				else {
					if (i==0 || j==0) curr[j] = 1;
					else curr[j] = prev[j-1]+1;
					if (curr[j]==0) continue;
					if (curr[j] > z) {
						ret.clear();
						z=curr[j];
						ret.add(s1.substring(i-z+1, i+1));
					}
					else if (curr[j] == z) {
						ret.add(s1.substring(i-z+1, i+1));
					}
				}
			}
			int[] t = curr;
			curr = prev;
			prev = t;
		}

		return ret;
	}

	public static String diff(String s1, String s2) {
		if (s1==null) return s2;
		if (s2==null) return s1;
		int i=findDiffIndex(s1, s2);
		if (i==s1.length() && i==s2.length()) return "";

		StringBuilder b=new StringBuilder();
		for (;;) {
			int i0 = i-50;
			if (i0 < 0) i0=0;
			if (i == s1.length()) {
				if (i < s2.length()) b.append(">>>>"+s2.substring(i));
				break;
			}
			if (i == s2.length()) {
				if (i < s1.length()) b.append("<<<<"+s1.substring(i));
				break;
			}

			int p=i, p1=i,p2=i;
			Log.debug("diffs at %d   %c != %c", p, s1.charAt(p1), s2.charAt(p2));
			boolean chg;
			do {
				chg=false;
				char c1=s1.charAt(p1);
				char c2=s2.charAt(p2);
				i = s2.indexOf(c1,p2);
				if ((i==-1 || i-p2>5) && p2+1 < s2.length()) {chg=true; ++p2;}
				i = s1.indexOf(c2,p1);
				if ((i==-1 || i-p1>5) && p1+1 < s1.length()) {chg=true; ++p1;}
			} while (chg);
			b.append("<<<<"+s1.substring(p,p1));
			b.append(">>>>"+s2.substring(p,p2));
			s1=s1.substring(p1+1);
			s2=s2.substring(p2+1);
			i=findDiffIndex(s1, s2);
		}
		return b.toString();
	}


	public static int levenshteinDistance(String s1, String s2) {
		int l1=s1.length(), l2=s2.length();
		if (l2 > l1) {
			String t=s1; s1=s2; s2=t;
			l1=s1.length(); l2=s2.length();
		}
		if (l2 == 0) return l1;

		int[] prev = new int[l2+1];
		int[] curr = new int[l2+1];
		for (int i=0; i <= l2; ++i) prev[i]=i;

		for (int i=0; i < l1; ++i) {
			curr[0]=i+1;
			for (int j=0; j < l2; ++j) {
				int c = s1.charAt(i)==s2.charAt(j) ? 0 : 1;
				curr[j+1] = Maths.min(
						curr[j]+1,     // cost delete
						prev[j+1]+1,   // cost insert
						prev[j] + c    // cost replace
				);
			}
			int[] t = curr;
			curr = prev;
			prev = t;
		}
		return prev[l2];
	}
}
/*
 >>>>60281
_NET_WM_ICON_GEOMETRY(CARDINAL) = 1925, 416, 38, 38
 <<<<55409
_NET_WM_ICON_GEOMETRY(CARDINAL) = 1925, 416, 38, 38
*/
