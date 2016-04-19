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

package text.tokenize;

import java.io.IOException;

public class CppTokenizer {
	static public final int TOKEN_NONE = 0;
	static public final int TOKEN_WHILESPACE = 1;
	static public final int TOKEN_PREPROC = 2; //preprocessor directive
	static public final int TOKEN_COMMENT = 3;
	static public final int TOKEN_COMMENT_LN = 4;
	static public final int TOKEN_QUOTE = 5;
	static public final int TOKEN_DBLQUOTE = 6;
	static public final int TOKEN_NUMBER = 7;
	static public final int TOKEN_NAME = 8;
	static public final int TOKEN_SPECIAL = 9;
	static public final int TOKEN_BLKSTART = 10;
	static public final int TOKEN_BLKEND = 11;

	private BasicTokenizer tokenizer;
	public CppTokenizer(BasicTokenizer t) {
		tokenizer=t;
	}

	public int getLineNo() {return tokenizer.getLineNo();}

	public void unread(CharSequence s) {
		tokenizer.unread(s);
	}

	public Token next(StringBuilder b) throws IOException {
		int cla=TOKEN_NONE;
		StringBuilder cpptok=new StringBuilder();
		int ln0=getLineNo();
		if (tokenizer.next(b)) {
			cpptok.append(b);
			if (Character.isDigit(b.charAt(0))) {
				cla=TOKEN_NUMBER;
			}
			else if (Character.isWhitespace(b.charAt(0))) {
				cla=TOKEN_WHILESPACE;
			}
			else if (Character.isLetter(b.charAt(0))) {
				cla=TOKEN_NAME;
			}
			else if (b.length()==1) {
				if (b.charAt(0)=='"') {
					cla=TOKEN_DBLQUOTE;
					while (tokenizer.next(b)) {
						cpptok.append(b);
						if (b.charAt(0)=='"') break;
					}
				}
				else if (b.charAt(0)=='\'') {
					cla=TOKEN_QUOTE;
					while (tokenizer.next(b)) {
						cpptok.append(b);
						if (b.charAt(0)=='\'') break;
					}
				}
				else if (b.charAt(0)=='#') {
					cla=TOKEN_PREPROC;
					while (tokenizer.next(b)) {
						if (b.charAt(0)=='\n'){
							if (!cpptok.toString().endsWith("\\")) {
								unread(b);
								break;
							}
							cpptok.setLength(cpptok.length()-1);
						}
						cpptok.append(b);
						if (b.charAt(0)=='/' && cpptok.toString().endsWith("//")) {
							int i=cpptok.length()-2;
							while (i>0 && tokenizer.isSpace(cpptok.charAt(i-1)))
								--i;
							unread(cpptok.substring(i));
							cpptok.setLength(i);
							break;
						}
					}
				}
				else if (b.charAt(0)=='/') {
					tokenizer.next(b);
					if (b.charAt(0)=='*') {
						cla=TOKEN_COMMENT;
						cpptok.append(b);
						while (tokenizer.next(b)) {
							cpptok.append(b);
							if (b.charAt(0)=='/' && cpptok.toString().endsWith("*/")) break;
						}
					}
					else if (b.charAt(0)=='/') {
						cla=TOKEN_COMMENT_LN;
						cpptok.append(b);
						while (tokenizer.next(b)) {
							if (b.toString().endsWith("\n")) {
								unread(b);
								break;
							}
							cpptok.append(b);
						}
					}
					else {
						cla=TOKEN_SPECIAL;
						unread(b);
					}
				}
				else if (b.charAt(0)=='{') {
					cla=TOKEN_BLKSTART;
				}
				else if (b.charAt(0)=='}') {
					cla=TOKEN_BLKEND;
				}
				else {
					String dbl="|&^:<>=-+";
					cla=TOKEN_SPECIAL;
					if (dbl.indexOf(b.charAt(0))>=0) {
						tokenizer.next(b);
						if (b.charAt(0) == cpptok.charAt(0))
							cpptok.append(b);
						else
							unread(b);
					}
				}
			}
			else
				cla=TOKEN_NAME;
		}
		if (cla==TOKEN_NONE && cpptok.length()>0)
			throw new RuntimeException(String.format("unknown token: ln=%d '%s'",ln0,cpptok.toString()));
		return cla!=0?new Token(cla, ln0, cpptok.toString()):null;
	}
}
