/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package algebra;

import java.util.Comparator;
import java.util.List;

public class Maths {

	public static float power_approx(final float a, final float b) {
		final int x = Float.floatToIntBits(a) >> 32;
		final int y = (int)(b * (x - 1072632447) + 1072632447);
		return Float.intBitsToFloat(y << 32);
	}
	public static double power_approx(final double a, final double b) {
		final long tmp = Double.doubleToLongBits(a);
		final long tmp2 = (long)(b * (tmp - 4606921280493453312L)) + 4606921280493453312L;
		return Double.longBitsToDouble(tmp2);
	}
	static public long power(int a, int b) {
		if (b < 0) {
			if (a==1) return 1L;
			return 0L;
		}
		if (b==0) return 1L;
		if (b==1) return a;

		long r=1;
		long aa=a;
		while (b!=0) {
			if ((b&1)!=0) r*=aa;
			aa*=aa;
			b>>>=1;
		}
		return r;
	}
	static public long gcd_simple(long a, long b) {
		if (a==0L) return b;
		while (b != 0) {
			if (a > b) a = a - b;
			else b = b - a;
		}
		return a;
	}
	static public long gcd(long a, long b) {
		if (a==0L) return b;
		long c;
		while (b != 0) {
			c = a % b;
			a = b;
			b = c;
		}
		return a;
	}
	static public int modInv(int a,int b) {
		a %= b;
		long aa=a;
		for (int x = 1; x < b; ++x) {
			if ((aa*x)%b == 1) {
				return x;
			}
		}
		return 0;
	}
	static public int modInv2(int a,int b) {
		int r=1,x=0,b0=b;
		a %= b;
		while (a>1) {
			int q = a/b;
			a = a % b;
			int t=a; a=b; b=t;//swap(a,b)
			r = r - q*x;
			t=r; r=x; x=t; //swap(r,x)
		}
		if (r<0) r+=b0;
		return r;
	}
	static public int modPow(int a,int b, int m) {
		long r=1,aa=a;
		while (b!=0) {
			if ((b&1)!=0) r=(r*aa)%m;
			aa=(aa*aa)%m;
			b>>>=1;
		}
		return (int)r;
	}
	static public <T extends Comparable<T>> T min(List<T> list) {
		T m = list.get(0);
		for (int i=1; i < list.size(); ++i) {
			if (m.compareTo(list.get(i)) > 0) m=list.get(i);
		}
		return m;
	}
	static public <T extends Comparable<T>> T max(List<T> list) {
		T m = list.get(0);
		for (int i=1; i < list.size(); ++i) {
			if (m.compareTo(list.get(i)) < 0) m=list.get(i);
		}
		return m;
	}
	static public <T> T min(List<T> list, Comparator<T> cmp) {
		T m = list.get(0);
		for (int i=1; i < list.size(); ++i) {
			if (cmp.compare(m,list.get(i)) > 0) m=list.get(i);
		}
		return m;
	}
	static public <T> T max(List<T> list, Comparator<T> cmp) {
		T m = list.get(0);
		for (int i=1; i < list.size(); ++i) {
			if (cmp.compare(m,list.get(i)) < 0) m=list.get(i);
		}
		return m;
	}
	@SuppressWarnings("unchecked")
	static public <T extends Comparable<T>> T max(T... a) {
		T m = a[0];
		for (int i=1; i < a.length; ++i) {
			if (m.compareTo(a[i]) < 0) m=a[i];
		}
		return m;
	}
	static public int max(int... a) {
		int m = a[0];
		for (int i=1; i < a.length; ++i) {
			if (m < a[i]) m=a[i];
		}
		return m;
	}
	static public int min(int... a) {
		int m = a[0];
		for (int i=1; i < a.length; ++i) {
			if (m > a[i]) m=a[i];
		}
		return m;
	}
	static public int blackjackPoints(CharSequence s) {
		int sum = 0;
		int aces = 0;
		for (int i=0; i < s.length(); ++i) {
			char c=s.charAt(i);
			if (c >= '2' && c <= '9') sum+=c-'0';
			else if (c=='A') ++aces;
			else sum+=10; //other fig
		}
		if (aces>0) {
			sum+=aces;
			while (aces>0 && sum+10<=21) {
				 sum+=10; --aces;
			}
		}
		return sum;
	}

}
