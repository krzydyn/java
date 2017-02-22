package text.tokenize;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

import text.tokenize.CppParser.CodeBlock;
import text.tokenize.CppParser.Comment;
import text.tokenize.CppParser.CppNode;
import text.tokenize.CppParser.Namespace;
import text.tokenize.CppParser.SourceFragment;
import text.tokenize.CppParser.TopNode;

public class CppBuilder {

	static class FWriter extends PrintWriter {
		int indent=0;
		int lastc=-1;
		private String indentStr="    ";

		private void writeIndent() {
			lastc=' ';
			for (int i=0; i < indent; ++i) super.write(indentStr);
		}
		public FWriter(Writer out) {
			super(out);
		}

		public void indent(int d) {
			indent += d;
		}

		@Override
		public void write(int c) {
			if (lastc == '\n' && c != '\n') {
				writeIndent();
			}
			lastc=c;
			super.write(c);
		}
		@Override
		public void write(String s, int off, int len) {
			for (int i=0; i < len; ++i) write(s.charAt(off+i));
		}
		@Override
		public void write(char[] buf, int off, int len) {
			for (int i=0; i < len; ++i) write(buf[off+i]);
		}
	}

	private FWriter wr;

	public CppBuilder(Writer wr) {
		this.wr=new FWriter(wr);
	}
	private void writeNode(CppNode n) {
		if (n instanceof TopNode) {
			for (int i=0; i < n.nodes.size(); ++i) {
				CppNode nn=n.nodes.get(i);
				writeNode(nn);
			}
		}
		else if (n instanceof CodeBlock) {
			n.write(wr);
			wr.write(" {\n");
			if (!(n instanceof Namespace)) wr.indent(1);
			for (int i=0; i < n.nodes.size(); ++i) {
				CppNode nn=n.nodes.get(i);
				writeNode(nn);
			}
			if (wr.lastc != '\n') wr.write("\n");
			if (!(n instanceof Namespace)) wr.indent(-1);
			wr.write("}");
		}
		else {
			SourceFragment f = (SourceFragment)n;
			boolean addnl = !f.str.endsWith(";") && !(f instanceof Comment);
			if (addnl && wr.lastc!='\n') {
				wr.write("\n");
			}
			if ((f instanceof Comment) && wr.lastc != '\n') wr.write(" ");
			n.write(wr);
			if (f.str.endsWith(";")) wr.write("\n");
		}
	}

	static public void write(CppNode n, String f) throws Exception {
		Writer wr=new FileWriter(f);
		try {write(n,wr);}
		finally {wr.close();}
	}
	static public void write(CppNode n, Writer wr) throws Exception {
		CppBuilder build = new CppBuilder(wr);
		build.writeNode(n);
		wr.flush();
	}

}
