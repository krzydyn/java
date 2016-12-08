package puzzles;

public abstract class GameBoard<T> {

	final public int w,h;
	public GameBoard(int w, int h) {
		this.w=w; this.h=h;
	}
	final public int getN() { return w*h;}
	final public T get(int x, int y) {
		if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException(String.format("%d,%d",x,y));
		return get(y*w+x);
	}
	final public void set(int x, int y, T b){
		if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException(String.format("%d,%d",x,y));
		set(y*w+x,b);
	}
	public void print() {
		for (int y=0; y < h; ++y) {
			for (int x=0; x < w; ++x) {
				System.out.print(get(x,y));
			}
			System.out.println();
		}
	}
	abstract public T get(int i);
	abstract public void set(int i, T b);

	static public class Bool extends GameBoard<Boolean> {
		final boolean[] board;
		public Bool(int w, int h) {
			super(w,h);
			board = new boolean[w*h];
		}
		@Override
		public Boolean get(int i) {
			return board[i];
		}
		@Override
		public void set(int i, Boolean b) {
			board[i] = b;
		}
	}
	static public class Char extends GameBoard<Character> {
		final char[] board;
		public Char(int w, int h) {
			super(w,h);
			board = new char[w*h];
		}
		@Override
		public Character get(int i) {
			return board[i];
		}
		@Override
		public void set(int i, Character b) {
			board[i] = b;
		}
	}

	static public class Generic<T> extends GameBoard<T> {
		final Object[] board;
		public Generic(int w, int h) {
			super(w,h);
			board = new Object[w*h];
		}
		@SuppressWarnings("unchecked")
		@Override
		public T get(int i) {
			return (T)board[i];
		}
		@Override
		public void set(int i, T b) {
			board[i] = b;
		}
	}

	public static class Sheet {
		public int w, h;
		public Sheet(int w,int h) {set(w,h);}
		public Sheet(Sheet d) {set(d.w,d.h);}
		public void set(int w,int h) {this.w=w; this.h=h;}
		public void rot() { int t=w; w=h; h=t;}
		public Sheet rotated() { Sheet d=new Sheet(this); d.rot(); return d;}
		public boolean contains(Rect r) {
			return 0<=r.x && 0<=r.y && r.x+r.s.w<=w && r.y+r.s.h<=h;
		}
		public boolean contains(Sheet r) {return r.w<=w && r.h<=h;}
	}
	public static class Rect {
		public int x, y;
		public Sheet s;
		public Rect(int x,int y,Sheet s) {
			set(x, y, s);
		}
		Rect(Rect r) {set(r);}
		void set(int x,int y,Sheet s) {
			this.x=x; this.y=y; this.s=s;
		}
		void set(Rect r) {
			set(r.x,r.y,r.s);
		}
		@Override
		public String toString() {
			return String.format("(%d,%d,%d,%d)", x,y,s.w,s.h);
		}
		public boolean intersects(Rect r) {
			return x < r.x+r.s.w && r.x < x+s.w && y < r.y+r.s.h && r.y < y+s.h;
		}
	}


}
