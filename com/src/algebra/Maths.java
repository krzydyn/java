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

package algebra;

public class Maths {
	public static float pow(final float a, final float b) {
		final int x = Float.floatToIntBits(a) >> 32;
		final int y = (int)(b * (x - 1072632447) + 1072632447);
		return Float.intBitsToFloat(y << 32);
	}
	public static double power_approx(final double a, final double b) {
		final long tmp = Double.doubleToLongBits(a);
		final long tmp2 = (long)(b * (tmp - 4606921280493453312L)) + 4606921280493453312L;
		return Double.longBitsToDouble(tmp2);
	}
	static public long power(int x, int n) {
		long r=1;
		while (n!=0) {
			if ((n&1)!=0) r*=x;
			x*=x;
			n>>=1;
		}
		return r;
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
