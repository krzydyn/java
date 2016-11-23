package puzzles;

public abstract class GameBoard {

	int w,h;
	public GameBoard(int w, int h) {
		this.w=w; this.h=h;
	}
	abstract public boolean get(int x, int y);
	abstract public void set(int x, int y, boolean b);

	static public class Bool extends GameBoard {
		final boolean[] board;
		public Bool(int w, int h) {
			super(w,h);
			board = new boolean[w*h];
		}

		@Override
		public boolean get(int x, int y) {
			if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException(String.format("%d,%d",x,y));
			return board[y*w+x];
		}
		@Override
		public void set(int x, int y, boolean b) {
			if (x<0 || y<0 || x>=w || y>=h) throw new IndexOutOfBoundsException();
			board[y*w+x] = b;
		}
	}

}
