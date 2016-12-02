package puzzles;

public abstract class GameBoard<T> {

	int w,h;
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
}
