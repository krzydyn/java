package puzzles;

public class Sudoku {
	final private String values = ".123456789ABCDEFGHIJK";
	final private int ORDER;
	final private int DIM;
	final private int[][] a;
	
	public Sudoku(int order) {
		ORDER = order;
		DIM = order*order;
		a = new int[DIM][DIM];
	}
	public void clear() {
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) a[y][x]=0;
	}
	public void set(int x,int y,int v) {a[y][x]=v;}
	public int get(int x,int y) {return a[y][x];}
	public boolean isEmpty(int x,int y) {return a[y][x]==0;}
	
	public void parse(String s) {
		String sep = "-|= ,";
		clear();
		int p=0;
		for (int i=0; i < s.length(); ++i) {
			char c=s.charAt(i);
			if (c <= ' ' || sep.indexOf(c) >= 0) continue;
			int x=p%DIM;
			int y=p/DIM;
			a[y][x] = values.indexOf(c,1)+1;
			++p;
		}
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
	public void printshort() {
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x)
				System.out.printf(" %s",values.charAt(a[y][x]));
		System.out.println();
	}
	
	boolean isAllowed(int x,int y,int v) {
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
		boolean[][] fixed = new boolean[DIM][DIM];
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				fixed[y][x] = a[y][x] != 0;
			}
		
		int p=0,x,y;
		while (p < DIM*DIM) {
			x=p%DIM; y=p/DIM;
			if (fixed[y][x]) {++p; continue;}
			int v=a[y][x];
			for (; v<DIM; ++v) {
				if (isAllowed(x, y, v+1)) break;
			}
			
			if (v < DIM) {
				a[y][x] = v+1; ++p;
				continue;
			}

			//rollback
			if (p == 0) return false;					
			a[y][x] = 0; --p;
			while (p > 0) {
				x=p%DIM; y=p/DIM;
				if (fixed[y][x]) {--p;continue;}
				if (a[y][x] == DIM) {a[y][x]=0; --p; continue;}
				break;
			}
		}
		
		return true;
	}
}
