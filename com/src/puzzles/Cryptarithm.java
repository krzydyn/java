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

import puzzles.Expression.SymbolGetter;
import algebra.Permutate;

public class Cryptarithm {
	final List<Integer> values = new ArrayList<Integer>();
	final List<Character> symbols = new ArrayList<Character>();
	final List<Expression> exprs = new ArrayList<Expression>();

	private int getValue(char symb) {
		int i = symbols.indexOf(symb);
		return values.get(i);
	}
	final SymbolGetter symval = new SymbolGetter() {
		@Override
		public long getValue(String s) {
			long x=0;
			for (int i=0; i < s.length(); ++i) {
				x*=10;
				x += Cryptarithm.this.getValue(s.charAt(i));
			}
			return x;
		}
	};

	public Cryptarithm() {
		clear();
	}
	@Override
	public String toString() {
		return values.toString();
	}
	void clear() {
		symbols.clear();
		exprs.clear();
	}
	public long getValue(String s) {
		return symval.getValue(s);
	}
	public List<Character> getSymbols() {
		return new ArrayList<Character>(symbols);
	}
	private void addSymbols(String s) {
		for (int i=0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (!Character.isLetter(c)) continue;
			if (!symbols.contains(c)) symbols.add(c);
		}
	}
	public void addExpr(String expr) {
		addSymbols(expr);
		exprs.add(new Expression(expr,symval));
	}
	public void prepare(int[] init) {
		values.clear();
		if (init==null)
			for (int i=0; i < 10; ++i) values.add(i);
		else
			for (int i=0; i < init.length; ++i) values.add(init[i]);
	}
	public boolean verify() {
		for (Expression e : exprs) {
			if (e.evaluate() == 0) {
				return false;
			}
		}
		return true;
	}
	public boolean next() {
		return Permutate.nextPermutation(values);
	}
	public void solve() {
		prepare(null);
		do {
			if (verify()) {
				System.out.println(symbols);
				System.out.println(values);
			}
		} while (next());
	}
}
