/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com
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

import sys.Log;
import text.tokenize.BasicTokenizer;

public class Expression {
	static public interface SymbolMapper {
		Object getValue(String s);
	}

	//token type
	final private static int TYPE_OP = 0;    //operator
	final private static int TYPE_CONST = 1; //constant
	final private static int TYPE_VAR = 2;   //variable
	final private static int TYPE_FUNC = 3;  //function

	//operator type
	static enum OpType {
		OP_NOT(1,0){    // !
		},
		OP_AND(2,0){    // &&
		},
		OP_OR(2,0){     // ||
		},
		OP_XOR(2,0){    // ^^
		},

		OP_ASGN(1,0){    // a = b
			@Override
			public Object calc(Object[] a) { return a[1]; }
		},
		OP_ADD(2,1){    // +
			@Override
			public Object calc(Object[] a) {
				if (a[0] instanceof Long && a[1] instanceof Long)
					return (long)a[0]+(long)a[1];
				if (a[0] instanceof String && a[1] instanceof String)
					return (String)a[0]+(String)a[1];
				return null;
			}
		},
		OP_SUB(2,1){    // -
			@Override
			public Object calc(Object[] a) {
				if (a[0] instanceof Long && a[1] instanceof Long)
					return (long)a[0]-(long)a[1];
				return null;
			}
		},
		OP_MUL(2,2){    // *
			@Override
			public Object calc(Object[] a) { return (long)a[0]*(long)a[1]; }
		},
		OP_DIV(2,2){    // /
			@Override
			public Object calc(Object[] a) {
				return (long)a[0]/(long)a[1];
			}
		},
		OP_REM(2,2){    // %
			@Override
			public Object calc(Object[] a) { return (long)a[0]%(long)a[1]; }
		},
		OP_INC(1,3){    // ++
			@Override
			public Object calc(Object[] a) { return (long)a[0]+1; }
		},
		OP_DEC(1,3){    // --
			@Override
			public Object calc(Object[] a) { return (long)a[0]-1; }
		},
		OP_INCN(2,4){   // +=
			@Override
			public Object calc(Object[] a) { return (long)a[0]+(long)a[1]; }
		},
		OP_DECN(2,4){   // -=
			@Override
			public Object calc(Object[] a) { return (long)a[0]-(long)a[1]; }
		},
		OP_EQ(2,0){     // ==
			@Override
			public Object calc(Object[] a) { return a[0].equals(a[1]) ? 1l : 0l; }
		},
		OP_NEQ(2,0){    // !=
			@Override
			public Object calc(Object[] a) { return a[0].equals(a[1]) ? 0l : 1l; }
		},
		OP_GT(2,0){     // >
		},
		OP_GE(2,0){     // >=
		},
		OP_LT(2,0){     // <
		},
		OP_LE(2,0){     // <=
		},

		OP_BNOT(1,0){   // ~
		},
		OP_BAND(2,0){   // &
		},
		OP_BOR(2,0){    // |
		},
		OP_BXOR(2,0){   // ^
		},
		OP_LSH(2,0){    // <<
		},
		OP_RSH(2,0){    // >>
		},
		OP_RSHU(2,0){   // >>>
		},

		OP_POW(2,0);    // power

		final int args;
		final int prio;
		private OpType(int a, int p) {args=a;prio=p;}
		public Object calc(Object[] args) {
			throw new RuntimeException("not implemented");
		}
	}

	// Operands
	private static Map<String,OpType> opMap = new HashMap<>();
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
	/** RPN (postfix) representation
	 */
	private final List<Token> rpn = new ArrayList<>();

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

	private boolean isOperand(char c) {
		return c=='=' || c=='+' || c=='-' || c=='|' || c=='&';
	}

	private void fromInfix(String expr) throws Exception {
		BasicTokenizer tok = new BasicTokenizer(expr);
		StringBuilder s=new StringBuilder();
		rpn.clear();
		List<String> op = new ArrayList<>();
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
					if (t.type == TYPE_VAR) {
						t.type = TYPE_FUNC;
						//TODO parse function args
					}
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
					tok.unread(s2); // return S2 to tokenizer
				}

				while (op.size() > 0) {
					String x = op.get(op.size()-1);
					if (x.charAt(0) == '(') break;
					//Log.debug("operand: prev='%s'   curr='%s'", x, s);
					OpType curr_op = opMap.get(s.toString());
					if (curr_op == null) {
						Log.error("no op for '%s'", s);
						break;
					}
					OpType prev_op = opMap.get(x);
					if (curr_op.prio > prev_op.prio) break;
					op.remove(op.size()-1);
					Token t = new Token();
					t.type = TYPE_OP;
					t.rep = prev_op;
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
		List<Object> stack = new ArrayList<>();
		for (Token t : rpn) {
			if (t.type == TYPE_OP) {
				OpType op = (OpType)t.rep;
				Object[] a = new Object[op.args];
				for (int i = 0; i < op.args; ++i)
					a[op.args - i -1] = stack.remove(stack.size()-1);
				Object r = op.calc(a);
				if (r != null) stack.add(r);
			}
			else if (t.type == TYPE_CONST) {
				stack.add(t.rep);
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
		return (long)stack.remove(0);
	}
}
