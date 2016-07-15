package puzzles;

public class SudokuSimple {
	final private String values = ".123456789ABCDEFGHIJKLMNOPQRSTUWXYZ@%";
	final private int ORDER;
	final private int DIM;
	final private int[][] a;
	final private boolean[][] fixed;
	private boolean doInit = true;

	public SudokuSimple(int order) {
		ORDER = order;
		DIM = order*order;
		a = new int[DIM][DIM];
		fixed = new boolean[DIM][DIM];
	}
	public void clear() {
		doInit=true;
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				a[y][x]=0;
			}
	}
	public void reset() {
		if (doInit) clear();
		else {
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) {
					if (!fixed[y][x]) a[y][x]=0;
				}
		}
	}
	public void set(int x,int y,int v) {a[y][x]=v;}
	public int get(int x,int y) {return a[y][x];}
	public boolean isEmpty(int x,int y) {return a[y][x]==0;}

	public void parse(String s) {
		String ignore = "-|= ";
		clear();
		int p=0;
		for (int i=0; i < s.length(); ++i) {
			char c=s.charAt(i);
			if (c <= ' ' || ignore.indexOf(c) >= 0) continue;
			int x=p%DIM;
			int y=p/DIM;
			int v = values.indexOf(c);
			a[y][x] = v<0 ? v=0 : v;
			++p;
		}
	}
	@Override
	public String toString() {
		StringBuilder b=new StringBuilder();
		int s=0;
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				b.append(String.format("%s",values.charAt(a[y][x])));
				if (a[y][x]>0) ++s;
			}
		b.append(" s="+s);
		return b.toString();
	}
	public void print() {
		for (int y=0; y<DIM; ++y) {
			if (y>0 && y%ORDER == 0)
				System.out.println("-----------------------");
			for (int x=0; x<DIM; ++x) {
				if (x>0 && x%ORDER == 0) System.out.print(" |");
				System.out.printf(" %s",values.charAt(a[y][x]));
			}
			System.out.println();
		}
		System.out.println();
	}
	private boolean setCheck(int x,int y,int v) {
		//horizontal
		for (int i=0; i < DIM; ++i) {
			if (a[y][i] == v) return false;
		}
		//vertical
		for (int i=0; i < DIM; ++i) {
			if (a[i][x] == v) return false;
		}
		//box
		int x0 = x - x%ORDER;
		int y0 = y - y%ORDER;
		for (y=0; y < ORDER; ++y) {
			for (x=0; x < ORDER; ++x) {
				if (a[y0+y][x0+x] == v) return false;
			}
		}
		return true;
	}

	public boolean solve() {
		int p,x,y;
		if (doInit) {
			for (y=0; y<DIM; ++y)
				for (x=0; x<DIM; ++x) {
					fixed[y][x] = a[y][x] != 0;
				}
			doInit=false;
			p=0;
		}
		else {
			p=DIM*DIM-1;
			while (p > 0) {
				x=p%DIM; y=p/DIM;
				if (!fixed[y][x]) break;
				--p;
			}
		}

		while (p < DIM*DIM) {
			x=p%DIM; y=p/DIM;
			if (fixed[y][x]) {++p; continue;}
			int v=a[y][x];
			a[y][x] = 0;
			for (; v<DIM; ++v) {
				if (setCheck(x, y, v+1)) break;
			}

			if (v < DIM) {
				a[y][x] = v+1; ++p;
				continue;
			}

			//backtrack
			--p;
			while (p >= 0) {
				x=p%DIM; y=p/DIM;
				if (fixed[y][x]) {--p;continue;}
				if (a[y][x] == DIM) {
					a[y][x]=0; --p;
					continue;
				}
				break;
			}
			if (p<0) return false;
		}
		return true;
	}
}
