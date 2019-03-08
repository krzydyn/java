package text.tokenize;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

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
		public void writeif(int c) {
			if (lastc==-1) return ;
			if (lastc ==' ' || lastc == '\n') return ;
			write(c);
		}
	}

	private FWriter wr;

	public CppBuilder(Writer wr) {
		this.wr=new FWriter(wr);
	}
	private void writeNode(Cpp.Node n) {
		if (n instanceof Cpp.TopNode) {
			for (int i=0; i < n.nodes.size(); ++i) {
				Cpp.Node nn=n.nodes.get(i);
				writeNode(nn);
			}
		}
		else if (n instanceof Cpp.CodeBlock) {
			n.write(wr);
			wr.writeif(' ');
			wr.write("{\n");
			if (!(n instanceof Cpp.Namespace)) wr.indent(1);
			for (int i=0; i < n.nodes.size(); ++i) {
				Cpp.Node nn=n.nodes.get(i);
				writeNode(nn);
			}
			wr.writeif('\n');
			if (!(n instanceof Cpp.Namespace)) wr.indent(-1);
			wr.write("}");
		}
		else {
			Cpp.SourceFragment f = (Cpp.SourceFragment)n;
			if ((f instanceof Cpp.Comment)) wr.writeif(' ');
			else if (wr.lastc=='}') {
				if (f.str.length()>1) {
					wr.writeif('\n');
					if (wr.indent==0) wr.write('\n');
				}
			}
			n.write(wr);
			if ((f instanceof Cpp.Comment)) ;
			else {
				if (f.str.endsWith(";") || f.str.endsWith(","))
					wr.writeif('\n');
			}
		}
	}

	static public void write(Cpp.Node n, String f) throws Exception {
		Writer wr=new FileWriter(f);
		try {write(n,wr);}
		finally {wr.close();}
	}
	static public void write(Cpp.Node n, Writer wr) throws Exception {
		CppBuilder build = new CppBuilder(wr);
		build.writeNode(n);
		wr.flush();
	}

}
