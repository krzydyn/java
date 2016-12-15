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

import java.util.ArrayList;
import java.util.List;

import sys.Log;
import text.tokenize.BasicTokenizer;

public class Expression {
	static public interface SymbolGetter {
		long getValue(String s);
	}

	//token type
	final private static int TYPE_OP = 0;    //operator
	final private static int TYPE_CONST = 1; //constant
	final private static int TYPE_VAR = 2;   //variable
	final private static int TYPE_FUNC = 3;  //function

	//operator type
	static enum OpType {
		OP_ASGN,   // =
		OP_ADD,    // +
		OP_SUB,    // -
		OP_MUL,    // *
		OP_DIV,    // /
		OP_REM,    // %
		OP_INC,    // ++
		OP_DEC,    // --
		OP_INCN,   // +=
		OP_DECN,   // -=
		OP_EQ,     // ==
		OP_NEQ,    // !=
		OP_GT,     // >
		OP_GE,     // >=
		OP_LT,     // <
		OP_LE,     // <=

		OP_NOT,    // !
		OP_AND,    // &&
		OP_OR,     // ||
		OP_XOR,    // ^^

		OP_BNOT,   // ~
		OP_BAND,   // &
		OP_BOR,    // |
		OP_BXOR,   // ^
		OP_LSH,    // <<
		OP_RSH,    // >>
		OP_RSHU,   // >>>

		OP_POW,    // ?
	}

	final private static class Token {
		int type;
		Object rep;
	}
	private SymbolGetter symget = null;
	//RPN (postfix) representation
	private final List<Token> rpn = new ArrayList<Token>();

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
		if (ch == '/' || ch == '*' || ch == '%') return 2;
		if (ch == '+' || ch == '-') return 1;
		return 0;
	}
	private boolean isOp(char c) {
		return c=='=' || c=='+' || c=='-' || c=='|' || c=='&';
	}
	private void fromInfix(String expr) throws Exception {
		BasicTokenizer tok = new BasicTokenizer(expr);
		StringBuilder s=new StringBuilder();
		rpn.clear();
		List<String> op = new ArrayList<String>();
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
				op.add(s.toString());
			}
			else if (c == ')') {
				while (op.size() > 0) {
					String x = op.remove(op.size()-1);
					c = x.charAt(0);
					if (c=='(') break;
					Token t = new Token();
					t.type = TYPE_OP;
					t.rep = x;
					rpn.add(t);
				}
			}
			else {
				StringBuilder s2 = new StringBuilder(2);
				if (isOp(c)) {
					while (tok.next(s2)) {
						c = s2.charAt(0);
						if (!isOp(c)) break;
						s.append(s2);
					}
					tok.unread(s2);
				}

				while (op.size() > 0) {
					String x = op.get(op.size()-1);
					if (x.charAt(0) == '(') break;
					if (priority(c) > priority(x.charAt(0))) break;
					Token t = new Token();
					t.type = TYPE_OP;
					t.rep = op.remove(op.size()-1);
					rpn.add(t);
				}
				Log.debug("add op %s",s);
				op.add(s.toString());
			}
		}
		while (op.size() > 0) {
			String x = op.remove(op.size()-1);
			if (x.charAt(0) == '(') continue;
			Token t = new Token();
			t.type = TYPE_OP;
			t.rep = x;
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
		Log.prn("===============");
		List<Long> stack = new ArrayList<Long>();
		for (Token t : rpn) {
			if (t.type == TYPE_OP) {
				String op = (String)t.rep;
				long b = stack.remove(stack.size()-1);
				long a = stack.remove(stack.size()-1);
				Log.prn("perform %s on %d %d", op, a, b);
				if (op.equals("+")) a = a+b;
				else if (op.equals("-")) a = a-b;
				else if (op.equals("*")) a = a*b;
				else if (op.equals("/")) a = a/b;
				else if (op.equals("%")) a = a%b;
				else if (op.equals("==")) a = (a==b ? 1 : 0);
				else throw new RuntimeException("Usuported operand "+op);
				stack.add(a);
			}
			else if (t.type == TYPE_CONST) {
				stack.add((Long)t.rep);
			}
			else if (t.type == TYPE_VAR) {
				if (symget == null) stack.add((long)0);
				else stack.add(symget.getValue((String)t.rep));
			}
		}
		return stack.remove(0);
	}
}
