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
package puzzles;

import java.util.ArrayList;
import java.util.List;

import algebra.Permutate;

public class TestCryptarithm {
	final static List<Character> symbols = new ArrayList<Character>();
	final static List<Integer> values = new ArrayList<Integer>();

	static int getValue(char symb) {
		int i = symbols.indexOf(symb);
		return values.get(i);
	}
	static long value(String t) {
		long x=0;
		for (int i=0; i < t.length(); ++i) {
			x*=10;
			x += getValue(t.charAt(i));
		}
		return x;
	}

	static void addSymbols(String s) {
		for (int i=0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (!symbols.contains(c)) {
				symbols.add(c);
			}
		}
	}

	static boolean nextValues() {
		return Permutate.nextPermutation(values);
	}

	static void test() {
		String a = "dcxciii";
		String b = "dcccxcv";
		String c = "mdcccxv";
		String d = "mmmcdiii";
		String a1 = "mroz";
		String b1 = "zima";

		//addSymbols("mroziadcxv");
		addSymbols(a1+b1+a+b+c+d);
		for (int i=0; i < 10; ++i)
			values.add(i);

		String[] vars = {a1,b1,a,b,c,d};
		do {

			if (3*value(a1)==value(b1) && value(a)+value(b)+value(c) == value(d)) {
				System.out.println(symbols.toString());
				System.out.println(values.toString());
				for (String v : vars) {
					System.out.printf("%s = %d\n",v,value(v));
				}
			}
		} while (nextValues());
	}

	public static void main(String[] args) {
		long t0 = System.currentTimeMillis();
		test();
		System.out.printf("Elapsed time: %d ms\n",System.currentTimeMillis()-t0);
	}
}
