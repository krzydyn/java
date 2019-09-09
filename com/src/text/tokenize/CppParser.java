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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import sys.Log;

public class CppParser {
	CppTokenizer ct=null;
	int line;
	public int getLineNo() {return line; }

	public Cpp.Node parse(String f) throws Exception {
		System.out.printf("parsing file \"%s\"\n",f);
		FileReader rd=new FileReader(f);
		try {return parse(rd);}
		finally {
			rd.close();
			System.out.printf("parsing done\n");
		}
	}
	public Cpp.Node parse(Reader rd) throws Exception {
		BasicTokenizer t=new BasicTokenizer(rd);

		ct = new CppTokenizer(t);

		return readRootNode(new Cpp.RootNode());
	}

	private Token next(StringBuilder b) throws IOException {
		Token t = ct.next(b);
		line = ct.getLineNo();
		return t;
	}

	private Cpp.Node readRootNode(Cpp.RootNode node) throws Exception {
		Token tok;
		StringBuilder b=new StringBuilder();
		while ((tok=next(b))!=null) {
			if (tok.cla==CppTokenizer.TOKEN_WHILESPACE) continue;

			if (tok.cla==CppTokenizer.TOKEN_PREPROC) {
				node.nodes.add(new Cpp.Preproc(tok.rep));
			}
			else if (tok.cla==CppTokenizer.TOKEN_COMMENT || tok.cla==CppTokenizer.TOKEN_COMMENT_LN) {
				node.nodes.add(new Cpp.Comment(tok.rep,tok.cla==CppTokenizer.TOKEN_COMMENT_LN));
			}
			else if (tok.cla==CppTokenizer.TOKEN_QUOTE) {
				node.nodes.add(new Cpp.CharQuote(tok.rep.substring(1, tok.rep.length()-1)));
			}
			else if (tok.cla==CppTokenizer.TOKEN_DBLQUOTE) {
				node.nodes.add(new Cpp.StringQuote(tok.rep.substring(1, tok.rep.length()-1)));
			}
			else if (tok.cla==CppTokenizer.TOKEN_NAME) {
				if (tok.rep.equals("namespace")) {
					node.nodes.add(readNamespace(new Cpp.Namespace()));
				}
				else node.nodes.add(readFragment(new Cpp.SourceFragment(tok.rep)));
			}
			else if (tok.cla==CppTokenizer.TOKEN_BLKSTART) {
				node.nodes.add(readBlock(new Cpp.CodeBlock()));
			}
			else if (tok.cla==CppTokenizer.TOKEN_NUMBER) {
				node.nodes.add(readFragment(new Cpp.SourceFragment(tok.rep)));
			}
			else throw new Token.TokenException(tok);
		}
		return node;
	}

	private Cpp.Node readBlock(Cpp.CodeBlock node) throws Exception {
		Token tok;
		StringBuilder b=new StringBuilder();
		StringBuilder blk=new StringBuilder();
		int lcla=CppTokenizer.TOKEN_NONE;
		while ((tok=next(b))!=null) {
			if (tok.cla==CppTokenizer.TOKEN_WHILESPACE) continue;

			if (tok.cla==CppTokenizer.TOKEN_PREPROC) {
				node.nodes.add(new Cpp.Preproc(tok.rep));
			}
			else if (tok.cla==CppTokenizer.TOKEN_COMMENT || tok.cla==CppTokenizer.TOKEN_COMMENT_LN) {
				if (blk.length()>0) {
					node.nodes.add(new Cpp.SourceFragment(blk.toString()));
					blk.setLength(0); lcla=CppTokenizer.TOKEN_NONE;
				}
				node.nodes.add(new Cpp.Comment(tok.rep,tok.cla==CppTokenizer.TOKEN_COMMENT_LN));
			}
			else if (tok.cla==CppTokenizer.TOKEN_COMMENT) {
				node.nodes.add(new Cpp.Comment(tok.rep,false));
			}
			else if (tok.cla==CppTokenizer.TOKEN_BLKEND) {
				if (blk.length()>0) {
					node.nodes.add(new Cpp.SourceFragment(blk.toString()));
					blk.setLength(0); lcla=CppTokenizer.TOKEN_NONE;
				}
				break;
			}
			else if (tok.cla==CppTokenizer.TOKEN_BLKSTART) {
				if (blk.length()>0) {
					node.nodes.add(new Cpp.SourceFragment(blk.toString()));
					blk.setLength(0); lcla=CppTokenizer.TOKEN_NONE;
				}
				node.nodes.add(readBlock(new Cpp.CodeBlock()));
			}
			else if (tok.cla==CppTokenizer.TOKEN_NAME && tok.rep.equals("namespace")) {
				if (blk.length()>0) {
					node.nodes.add(new Cpp.SourceFragment(blk.toString()));
					blk.setLength(0); lcla=CppTokenizer.TOKEN_NONE;
				}
				node.nodes.add(readNamespace(new Cpp.Namespace()));
			}
			else if (tok.cla==CppTokenizer.TOKEN_NAME && tok.rep.equals("using")) {
				if (blk.length()>0) {
					node.nodes.add(new Cpp.SourceFragment(blk.toString()));
					blk.setLength(0); lcla=CppTokenizer.TOKEN_NONE;
				}
				node.nodes.add(readFragment(new Cpp.SourceFragment(tok.rep)));
			}
			else {
				if (lcla!=CppTokenizer.TOKEN_SPECIAL && lcla==tok.cla) blk.append(' ');
				lcla=tok.cla;
				blk.append(tok.rep);
				if (lcla==CppTokenizer.TOKEN_SPECIAL && tok.rep.equals(";")) {
					node.nodes.add(new Cpp.SourceFragment(blk.toString()));
					blk.setLength(0); lcla=CppTokenizer.TOKEN_NONE;
				}
			}
		}
		return node;
	}

	Cpp.Node readNamespace(Cpp.Namespace node) throws Exception {
		Token tok;
		StringBuilder b=new StringBuilder();

		while ((tok=next(b))!=null) {
			if (tok.cla==CppTokenizer.TOKEN_WHILESPACE) continue;

			if (tok.cla==CppTokenizer.TOKEN_PREPROC) {
				node.nodes.add(new Cpp.Preproc(tok.rep));
			}
			else if (tok.cla==CppTokenizer.TOKEN_COMMENT || tok.cla==CppTokenizer.TOKEN_COMMENT_LN) {
				node.nodes.add(new Cpp.Comment(tok.rep,tok.cla==CppTokenizer.TOKEN_COMMENT_LN));
			}
			else if (tok.cla==CppTokenizer.TOKEN_BLKSTART) {
				return readBlock(node);
			}
			else if (tok.cla==CppTokenizer.TOKEN_NAME) {
				if (node.name==null) node.name=tok.rep;
				else node.name+=tok.rep;
			}
			else if (tok.cla==CppTokenizer.TOKEN_SPECIAL) {
				if (node.name==null) throw new Token.TokenException(tok, "namespace name expected");
				if (tok.rep.equals("=")) node.nodes.add(new Cpp.SourceFragment(tok.rep));
				else node.name+=tok.rep;
			}
			else throw new Token.TokenException(tok);
		}
		return node;
	}
	Cpp.Node readFragment(Cpp.SourceFragment node) throws Exception {
		Token tok;
		StringBuilder b=new StringBuilder();
		StringBuilder blk=new StringBuilder();
		int lcla=CppTokenizer.TOKEN_NONE;
		if (node.text!=null && !node.text.isEmpty()) {
			lcla=CppTokenizer.TOKEN_NAME;
			blk.append(node.text);
		}
		while ((tok=next(b))!=null) {
			if (tok.cla==CppTokenizer.TOKEN_WHILESPACE) continue;

			if (tok.cla==CppTokenizer.TOKEN_NAME || tok.cla==CppTokenizer.TOKEN_SPECIAL || tok.cla==CppTokenizer.TOKEN_DBLQUOTE) {
				if (lcla!=CppTokenizer.TOKEN_SPECIAL && lcla==tok.cla) blk.append(' ');
				lcla=tok.cla;
				blk.append(tok.rep);
				if (lcla==CppTokenizer.TOKEN_SPECIAL && tok.rep.equals(";")) {
					node.text=blk.toString();
					break;
				}
			}
			else {
				Log.debug("unread %s",tok.toString());
				ct.unread(tok.rep);
				break;
			}
		}
		return node;
	}
}
/*
 * 103/18 ln/ms, 5.722 kln/s [5.722 kln/s]
 * 577/40 ln/ms, 14.425 kln/s [14.425 kln/s]
 *
 */
