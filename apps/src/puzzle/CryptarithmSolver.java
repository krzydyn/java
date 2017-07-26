package puzzle;

import puzzles.Cryptarithm;

public class CryptarithmSolver {
	static private final String[] ex1 = {"3*mroz==zima", "dcxciii+dcccxcv+mdcccxv==mmmcdiii"};
	static private final String[] ex2 = {"-x0", "vive*la ==france"};

	private static void solve(String[] args) {
		Cryptarithm c=new Cryptarithm();
		for (int i=0; i < args.length; ++i) {
			String s = args[i];
			if (s.length() < 2) continue;
			if (s.charAt(0)=='-') {
				if (s.charAt(1)=='x') {
					int v = Integer.parseInt(s.substring(2));
					c.removeFigure(v);
				}
				else if (s.charAt(1)=='e') {
					int v = Integer.parseInt(s.substring(2));
					if (v==1) solve(ex1);
					else if (v==2) solve(ex2);
				}
				continue;
			}
			c.addExpr(s);
		}
		if (c.getSymbols().isEmpty()) return ;

		System.out.println("Solving: "+c.toString());
		System.out.println(c.getSymbols());
		System.out.println("-----------------------");
		do {
			if (c.verify()) {
				System.out.println(c.toString());
			}
		} while (c.next());
	}

	public static void main(String[] args) {
		solve(args);
	}
}
