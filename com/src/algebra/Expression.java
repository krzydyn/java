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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import text.tokenize.BasicTokenizer;

public class Expression {
	static public interface SymbolMapper {
		long getValue(String s);
	}

	//token type
	final private static int TYPE_OP = 0;    //operator
	final private static int TYPE_CONST = 1; //constant
	final private static int TYPE_VAR = 2;   //variable
	final private static int TYPE_FUNC = 3;  //function

	//operator type
	static enum OpType {
		OP_ASGN(1){    // =
			@Override
			public long calc(long ...a) { return a[0]; }
		},
		OP_ADD{    // +
			@Override
			public long calc(long ...a) { return a[0]+a[1]; }
		},
		OP_SUB{    // -
			@Override
			public long calc(long ...a) { return a[0]-a[1]; }
		},
		OP_MUL{    // *
			@Override
			public long calc(long ...a) { return a[0]*a[1]; }
		},
		OP_DIV{    // /
			@Override
			public long calc(long ...a) { return a[0]/a[1]; }
		},
		OP_REM{    // %
			@Override
			public long calc(long ...a) { return a[0]%a[1]; }
		},
		OP_INC(1){    // ++
			@Override
			public long calc(long ...a) { return a[0]+1; }
		},
		OP_DEC(1){    // --
			@Override
			public long calc(long ...a) { return a[0]-1; }
		},
		OP_INCN{   // +=
			@Override
			public long calc(long ...a) { return a[0]+a[1]; }
		},
		OP_DECN{   // -=
			@Override
			public long calc(long ...a) { return a[0]-a[1]; }
		},
		OP_EQ{     // ==
			@Override
			public long calc(long ...a) { return a[0]==a[1]?1:0; }
		},
		OP_NEQ{    // !=
			@Override
			public long calc(long ...a) { return a[0]!=a[1]?1:0; }
		},
		OP_GT{     // >
		},
		OP_GE{     // >=
		},
		OP_LT{     // <
		},
		OP_LE{     // <=
		},

		OP_NOT(1){    // !
		},
		OP_AND{    // &&
		},
		OP_OR{     // ||
		},
		OP_XOR{    // ^^
		},

		OP_BNOT(1){   // ~
		},
		OP_BAND{   // &
		},
		OP_BOR{    // |
		},
		OP_BXOR{   // ^
		},
		OP_LSH{    // <<
		},
		OP_RSH{    // >>
		},
		OP_RSHU{   // >>>
		},

		OP_POW;    // power

		final int args;
		private OpType() {args=2;}
		private OpType(int a) {args=a;}
		public long calc(long ...args) {
			throw new RuntimeException("not implemented");
		}

	}
	private static Map<String,OpType> opMap = new HashMap<String, Expression.OpType>();
	static {
		opMap.put("=", OpType.OP_ASGN);
		opMap.put("+", OpType.OP_ADD);
		opMap.put("-", OpType.OP_SUB);
		opMap.put("*", OpType.OP_MUL);
		opMap.put("/", OpType.OP_DIV);
		opMap.put("%", OpType.OP_REM);
		opMap.put("++", OpType.OP_INC);
		opMap.put("--", OpType.OP_DEC);
		opMap.put("==", OpType.OP_EQ);
		opMap.put("!=", OpType.OP_NEQ);
	}

	final private static class Token {
		int type;
		Object rep;
	}
	private SymbolMapper symbols = null;
	//RPN (postfix) representation
	private final List<Token> rpn = new ArrayList<Token>();

	public Expression(String expr, SymbolMapper symmap) {
		this.symbols = symmap;
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
	private boolean isOperand(char c) {
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
					t.rep = opMap.get(x);
					rpn.add(t);
				}
			}
			else {
				StringBuilder s2 = new StringBuilder(2);
				if (isOperand(c)) {
					while (tok.next(s2)) {
						c = s2.charAt(0);
						if (!isOperand(c)) break;
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
					t.rep = opMap.get(op.remove(op.size()-1));
					rpn.add(t);
				}
				op.add(s.toString());
			}
		}
		while (op.size() > 0) {
			String x = op.remove(op.size()-1);
			if (x.charAt(0) == '(') continue;
			Token t = new Token();
			t.type = TYPE_OP;
			t.rep = opMap.get(x);
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
		//Log.prn("eval rpn %s", toString());
		List<Long> stack = new ArrayList<Long>();
		for (Token t : rpn) {
			if (t.type == TYPE_OP) {
				OpType op = (OpType)t.rep;
				long a;
				if (op.args==1) {
					a = stack.remove(stack.size()-1);
					a=op.calc(a);
				}
				else if (op.args==2){
					long b = stack.remove(stack.size()-1);
					a = stack.remove(stack.size()-1);
					a=op.calc(a,b);
				}
				else throw new RuntimeException("Usuported operand "+op);
				stack.add(a);
			}
			else if (t.type == TYPE_CONST) {
				stack.add((Long)t.rep);
			}
			else if (t.type == TYPE_VAR) {
				if (symbols == null) stack.add((long)0);
				else stack.add(symbols.getValue((String)t.rep));
			}
			else if (t.type == TYPE_FUNC) {
				throw new RuntimeException("not implemented yet");
			}
		}
		if (stack.size() > 1) {
			throw new RuntimeException("too many values on stack");
		}
		return stack.remove(0);
	}
}
