package puzzles;

import java.util.ArrayList;
import java.util.List;

import puzzles.Expression.SymbolGetter;
import sys.Log;
import algebra.Permutate;

public class Cryptarithm {
	final List<Integer> values = new ArrayList<Integer>();
	final List<Character> symbols = new ArrayList<Character>();
	final List<String> exprs = new ArrayList<String>();

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
	void clear() {
		values.clear();
		symbols.clear();
		exprs.clear();
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
		exprs.add(expr);
	}
	private boolean verify(String expr) {
		return new Expression(expr,symval).evaluate() != 0;
	}
	public void solve() {
		for (int i=0; i < 10; ++i) values.add(i);
		do {
			boolean r=true;
			for (String e : exprs) {
				if (!verify(e)) {r=false;break;}
			}
			if (r) {
				for (int i=0; i < symbols.size(); ++i) {
					char symb = symbols.get(i);
					System.out.printf("%c:%d ", symb, getValue(symb));
				}
				System.out.println();
			}
		} while (Permutate.nextPermutation(values));
	}
}
