package text.tokenize;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import text.Text;

public class Cpp {
	public static abstract class Node {
		protected final List<Node> nodes = new ArrayList<>();
		public void write(PrintWriter wr) {}
	}

	static class RootNode extends Node {
		RootNode() {}
	}
	/**
	 * Already formated code fragment
	 *
	 */
	static class SourceFragment extends Node {
		protected String str;

		public SourceFragment(String s) { str=s;}
		@Override
		public void write(PrintWriter wr) {
			wr.print(str);
		}
	}
	/**
	 * CodeBlock content from block-begin to block-end sentinel
	 *
	 */
	static class CodeBlock extends Node {
		CodeBlock() {}
	}

	static class Preproc extends SourceFragment {
		Preproc(String c){super(c);}
		@Override
		public void write(PrintWriter wr) {
			wr.printf("%s\n",str.replace("\n", "\\\n"));
		}
	}
	static class Comment extends SourceFragment {
		Comment(String c, boolean oneln){
			super(c.replaceAll("(?m)^ *\\* {0,1}", "").trim());
			this.oneln=oneln;
		}
		boolean oneln;
		@Override
		public void write(PrintWriter wr) {
			if (oneln) wr.printf("// %s\n", str);
			else {
				if (!str.contains("\n")) wr.printf("/* %s */ ", str);
				else {
					wr.printf("/*%s\n */\n", ("\n"+str).replace("\n", "\n * "));
				}
			}
		}
	}

	static class CharQuote extends SourceFragment {
		public CharQuote(String s) {
			super(s);
		}
		@Override
		public void write(PrintWriter wr) {
			wr.printf("'%s'",str);
		}
	}

	static class StringQuote extends SourceFragment {
		public StringQuote(String s) {
			super(s);
		}
		@Override
		public void write(PrintWriter wr) {
			wr.printf("\"%s\"",str);
		}
	}

	static class Method extends CodeBlock {
		String retType;
		String name;
		List<String> modifiers; //{public,final,static}
		List<String> exception; //declared exceptions

		@Override
		public void write(PrintWriter wr) {
			wr.printf("%s %s : %s", Text.join(",", modifiers), retType, name, Text.join(",", exception));
		}
	}

	static class Class extends CodeBlock {
		String name;
		final List<String> bases=new ArrayList<>(); //base classes
		@Override
		public void write(PrintWriter wr) {
			wr.printf("class %s : %s", name, Text.join(",", bases));
		}
	}

	static class Namespace extends CodeBlock {
		Namespace() {}
		String name;
		@Override
		public void write(PrintWriter wr) {
			wr.printf("namespace %s", name);
		}
	}

	//print internal structures
	static private void printNode(Cpp.Node n, int l) {
		String indent = Text.repeat("    ", l);
		if (n instanceof Cpp.SourceFragment) {
			System.out.printf("%s: '",n.getClass().getSimpleName());
			PrintWriter p=new PrintWriter(System.out);
			n.write(p);
			p.flush();
			System.out.println("'");
		}
		else {
			boolean cb = n instanceof Cpp.CodeBlock;
			int l1 = cb ? l+1 : l;
			if (n instanceof Cpp.Namespace) {
				l1=l;
				System.out.println();
				System.out.printf("namespace %s ", ((Cpp.Namespace) n).name);
			}
			if (cb) System.out.println("{");
			boolean lcb=true;
			for (int i=0; i < n.nodes.size(); ++i) {
				Cpp.Node nn=n.nodes.get(i);
				boolean iscb = nn instanceof Cpp.CodeBlock;
				if (!iscb && !lcb) System.out.println();
				printNode(nn,l1);
				lcb=iscb;
			}
			if (cb) System.out.print("\n"+indent+"}");
		}
	}
	static public void printNode(Cpp.Node n) {
		printNode(n,0);
	}

}
