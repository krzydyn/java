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

public class Token {
	public final int cla;
	public final int ln;
	public final String rep;
	public Token(int c,int l, String s) {
		cla=c; ln=l; rep=s;
	}

	@Override
	public String toString() {
		return String.format("token %d: ln(%d) '%s'",cla,ln,rep);
	}

	@SuppressWarnings("serial")
	static public class TokenException extends RuntimeException {
		TokenException(Token tok) {
			super("wrong "+tok.toString());
		}
		TokenException(Token tok, String msg) {
			super(String.format("wrong %s; %s",tok.toString(),msg));
		}
	}
}
