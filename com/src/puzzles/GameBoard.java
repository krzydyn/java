package puzzles;

public abstract class GameBoard<T> {

	int w,h;
	public GameBoard(int w, int h) {
		this.w=w; this.h=h;
	}
	abstract public T get(int x, int y);
	abstract public void set(int x, int y, T b);

	static public class Bool extends GameBoard<Boolean> {
		final boolean[] board;
		public Bool(int w, int h) {
			super(w,h);
			board = new boolean[w*h];
		}

		@Override
		public Boolean get(int x, int y) {
			if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException(String.format("%d,%d",x,y));
			return board[y*w+x];
		}
		@Override
		public void set(int x, int y, Boolean b) {
			if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException();
			board[y*w+x] = b;
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
		public T get(int x, int y) {
			if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException(String.format("%d,%d",x,y));
			return (T)board[y*w+x];
		}
		@Override
		public void set(int x, int y, T b) {
			if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException();
			board[y*w+x] = b;
		}
	}
}
