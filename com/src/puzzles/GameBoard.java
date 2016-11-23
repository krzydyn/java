package puzzles;

public abstract class GameBoard {
	int w,h;
	public GameBoard(int w, int h) {
		this.w=w; this.h=h;
	}
	abstract public boolean get(int i, int j);
	abstract public void set(int i, int j, boolean b);

	static public class Bool extends GameBoard {
		final boolean[] board;
		public Bool(int w, int h) {
			super(w,h);
			board = new boolean[w*h];
		}

		@Override
		public boolean get(int i, int j) {
			if (i<0 || j<0 || i>=w || j>=h) throw new IndexOutOfBoundsException();
			return board[j*w+i];
		}
		@Override
		public void set(int i, int j, boolean b) {
			if (i<0 || j<0 || i>=w || j>=h) throw new IndexOutOfBoundsException();
			board[j*w+i] = b;
		}
	}

}
