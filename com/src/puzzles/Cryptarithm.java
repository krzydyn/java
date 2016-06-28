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
	@Override
	public String toString() {
		return values.toString();
	}
	void clear() {
		symbols.clear();
		exprs.clear();
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
		exprs.add(expr);
	}
	public void prepare(int[] init) {
		values.clear();
		if (init==null)
			for (int i=0; i < 10; ++i) values.add(i);
		else
			for (int i=0; i < init.length; ++i) values.add(init[i]);
	}
	public boolean verify() {
		for (String e : exprs) {
			if (new Expression(e,symval).evaluate() == 0) {
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
