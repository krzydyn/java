package puzzles;

import java.util.ArrayList;
import java.util.List;

import text.tokenize.BasicTokenizer;

public class Expression {
	static public interface SymbolGetter {
		long getValue(String s);
	}

	final private static int TYPE_OP = 0;
	final private static int TYPE_CONST = 1;
	final private static int TYPE_VAR = 2;
	final private static int TYPE_FUNC = 3;
	final private static class Token {
		int type;
		Object rep;
	}
	private SymbolGetter symget = null;
	//RPN (postfix) representation
	private List<Token> rpn = new ArrayList<Token>();

	public Expression(String expr, SymbolGetter symget) {
		this.symget = symget;
		try {
			fromInfix(expr);
			//Log.debug("RPN(%s): %s", expr, toString());
		} catch (Exception e) { throw new RuntimeException(e); }
	}
	public Expression(String expr) {
		this(expr,null);
	}

	private int priority(char ch) {
		if (ch == '^') return 3;
		if (ch == '/' || ch == '*' || ch == '%') return 2;
		if (ch == '+' || ch == '-') return 1;
		return 0;
	}
	private void fromInfix(String expr) throws Exception {
		BasicTokenizer tok = new BasicTokenizer(expr);
		StringBuilder s=new StringBuilder();
		rpn.clear();
		List<Character> op = new ArrayList<Character>();
		while (tok.next(s)) {
			char c = s.charAt(0);
			if (Character.isWhitespace(c)) continue;

			if (Character.isDigit(c)) {
				Token t = new Token();
				t.type = TYPE_CONST;
				t.rep = Long.parseLong(s.toString());
				rpn.add(t);
			}
			else if (Character.isLetter(c)) {
				Token t = new Token();
				t.type = TYPE_VAR;
				t.rep = s.toString();
				rpn.add(t);
			}
			else if (c == '(') {

				if (rpn.size() > 0) {
					Token t = rpn.get(rpn.size()-1);
					if (t.type == TYPE_VAR) t.type = TYPE_FUNC;
				}
				op.add(c);
			}
			else if (c == ')') {
				while (op.size() > 0) {
					c = op.remove(op.size()-1);
					if (c=='(') break;
					Token t = new Token();
					t.type = TYPE_OP;
					t.rep = c;
					rpn.add(t);
				}
			}
			else {
				while (op.size() > 0) {
					if (priority(c) > priority(op.get(op.size()-1))) break;
					Token t = new Token();
					t.type = TYPE_OP;
					t.rep = op.remove(op.size()-1);
					rpn.add(t);
				}
				op.add(c);
			}
		}
		while (op.size() > 0) {
			char c = op.remove(op.size()-1);
			if (c == '(') continue;
			Token t = new Token();
			t.type = TYPE_OP;
			t.rep = c;
			rpn.add(t);
		}
	}

	@Override
	public String toString() {
		if (rpn.size()==0) return "";

		StringBuilder s = new StringBuilder();
		for (Token t : rpn) {
			s.append(String.format("%s,",t.rep));
		}
		s.setLength(s.length()-1);
		return s.toString();
	}

	public long evaluate() {
		List<Long> stack = new ArrayList<Long>();
		for (Token t : rpn) {
			if (t.type == TYPE_OP) {
				long b = stack.remove(stack.size()-1);
				long a = stack.remove(stack.size()-1);
				char op = (char)t.rep;
				if (op == '+') a = a+b;
				else if (op == '-') a = a-b;
				else if (op == '*') a = a*b;
				else if (op == '/') a = a/b;
				else if (op == '%') a = a%b;
				else if (op == '=') a = (a==b ? 1 : 0);
				stack.add(a);
			}
			else if (t.type == TYPE_CONST) {
				stack.add((long)t.rep);
			}
			else if (t.type == TYPE_VAR) {
				if (symget == null) stack.add((long)0);
				else stack.add(symget.getValue((String)t.rep));
			}
		}
		return stack.remove(0);
	}
}
