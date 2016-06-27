package puzzles;

import text.tokenize.BasicTokenizer;

public class Expression {
	static public interface SymbolGetter {
		long getValue(String s);
	}

	private SymbolGetter symget = null;
	//private String expr;
	private BasicTokenizer tok;


	public Expression(String expr, SymbolGetter symget) {
		this.symget = symget;
		//this.expr = expr;
		tok = new BasicTokenizer(expr);
	}
	public Expression(String expr) {
		this(expr,null);
	}

	private long symbol() throws Exception {
		StringBuilder s=new StringBuilder();
		tok.next(s);
		if (symget != null) return symget.getValue(s.toString());
		long sum =0;
		if (Character.isDigit(s.charAt(0))) {
			sum = Long.parseLong(s.toString());
		}
		return sum;
	}
	private long component() throws Exception {
		StringBuilder s=new StringBuilder();
		long sum = 0;
		while (tok.next(s)) {
			if (s.equals("*")) sum*=symbol();
			else if (s.equals("/")) sum/=symbol();
			else {
				tok.unread(s);
				sum=symbol();
			}
		}
		return sum;
	}
	private long expression() throws Exception {
		StringBuilder s=new StringBuilder();
		long sum = 0;
		while (tok.next(s)) {
			if (s.equals("(")) {
				sum += expression();
				tok.next(s);
				if (!s.equals(")")) throw new RuntimeException("expected )");
			}
			else if (s.equals("-")) sum-=component();
			else if (s.equals("+")) sum-=component();
			else {
				tok.unread(s);
				sum+=component();
			}
		}
		return sum;
	}

	public long evaluate() {
		try {
			return expression();
		} catch (Exception e) {
			return -1;
		}
	}
}
