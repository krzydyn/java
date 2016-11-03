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
import java.io.Reader;
import java.io.StringReader;

public class BasicTokenizer {
	private final Reader rd;
	private final StringBuilder pushback=new StringBuilder();

	private int line;
	private int maxunread;

	public BasicTokenizer(Reader r) {
		rd=r;
		line=1;
		maxunread=0;
	}
	public BasicTokenizer(String s) {this(new StringReader(s));}

	private void unread(int c) {
		if (c==-1) return ;
		if (c=='\n') --line;
		pushback.append((char)c);
		if (maxunread < pushback.length()) maxunread=pushback.length();
	}
	private int readc() throws IOException {
		int r,l=pushback.length();
		if (l>0) {
			r=pushback.charAt(l-1);
			pushback.setLength(l-1);
		}
		else {
			r=rd.read();
		}
		if (r=='\n') {++line;}
		return r;
	}

	public void unread(CharSequence s) {
		for (int i=s.length(); i > 0; ) {
			--i;
			char c = s.charAt(i);
			if (c=='\n') --line;
			pushback.append(c);
		}
		if (maxunread < pushback.length()) maxunread=pushback.length();
	}
	protected boolean isSpace(char c) {
		return Character.isWhitespace(c);
	}
	protected boolean isAlnum(char c) {
		return Character.isLetterOrDigit(c)||c=='_';
	}
	public int getLineNo() {return line;}

	public boolean next(StringBuilder s) throws IOException {
		int c;
		boolean prevsp=true;
		s.setLength(0);
		while ((c=readc())>=0) {
			if (isSpace((char)c)) {
				if (prevsp) {
					s.append((char)c);
					if (c=='\n') break;
				}
				else { unread(c); break; }
			}
			else if (isAlnum((char)c)) {
				if (prevsp && s.length()>0) { unread(c); break; }
				s.append((char)c);
				prevsp=false;
			}
			else {
				if (s.length()>0) unread(c);
				else s.append((char)c);
				break;
			}
		}
		return s.length() > 0;
	}
	public String next() throws IOException {
		StringBuilder s=new StringBuilder();
		return next(s) ? s.toString() : null;
	}
}
