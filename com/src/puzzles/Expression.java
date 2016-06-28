package puzzles;

import sys.Log;
import text.tokenize.BasicTokenizer;

public class Expression {
	static public interface SymbolGetter {
		long getValue(String s);
	}

	private String expr;
	private SymbolGetter symget = null;
	private BasicTokenizer tok;

	public Expression(String expr, SymbolGetter symget) {
		this.symget = symget;
		this.expr = expr;
	}
	public Expression(String expr) {
		this(expr,null);
	}

	private long symbol() throws Exception {
		StringBuilder s=new StringBuilder();
		tok.next(s);
		long sum=0;
		if (Character.isDigit(s.charAt(0))) {
			sum = Long.parseLong(s.toString());
		}
		else if (symget != null) return symget.getValue(s.toString());
		return sum;
	}
	private long factor() throws Exception {
		StringBuilder s=new StringBuilder();
		while (tok.next(s)) {
			if (s.toString().equals("!")) return factor()==0?1:0;
			if (s.toString().equals("-")) return -factor();
			if (s.toString().equals("+")) return factor();
			if (s.toString().equals("(")) {
				long sum = expression();
				tok.next(s); //should be ')'
				return sum;
			}
			else {
				tok.unread(s);
				return symbol();
			}
		}
		return 0;
	}
	//components = product of symbols
	private long component() throws Exception {
		StringBuilder s=new StringBuilder();
		long sum = factor();
		while (tok.next(s)) {
			if (s.toString().equals("*")) sum*=factor();
			else if (s.toString().equals("/")) sum/=factor();
			else {
				tok.unread(s);
				break;
			}
		}
		return sum;
	}
	//expression = sum of components
	private long expression() throws Exception {
		StringBuilder s=new StringBuilder();
		long sum = component();
		while (tok.next(s)) {
			if (s.toString().equals("-")) sum-=component();
			else if (s.toString().equals("+")) sum+=component();
			else if (s.toString().equals("=")) {
				long sum2=expression();
				return sum == sum2 ? 1 : 0;
			}
			else if (s.toString().equals("<")) {
				return sum < expression() ? 1 : 0;
			}
			else if (s.toString().equals(">")) {
				return sum > expression() ? 1 : 0;
			}
			else {
				tok.unread(s);
				break;
			}
		}
		return sum;
	}

	public long evaluate() {
		tok = new BasicTokenizer(expr);
		try {
			return expression();
		} catch (Exception e) {
			Log.error(e);
			return 0;
		}
	}
}
