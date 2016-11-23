package puzzles;

import java.awt.Point;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import sys.Log;

public class Pentomino {
	static final int STONES = 5;

	public static class FigPos {
		public Fig fig;
		public int x,y;
		public FigPos(Fig fig, int x, int y) {
			this.fig=fig;
			this.x=x; this.y=y;
		}
	}

	final private GameBoard board;
	final private Fig[] figUsed = new Fig[baseFig.length];

	public Pentomino(int w,int h) {
		board = new GameBoard.Bool(w, h);
	}
	private boolean findFree(Point cp) {
		for (;;) {
			if (cp.x >= board.w) {
				cp.x=0;
				++cp.y;
			}
			if (cp.y >= board.h) break;
			if (!board.get(cp.x, cp.y)) return true;
			++cp.x;
		}
		return false;
	}
	public void solve() {
		List<FigPos> list=new ArrayList<Pentomino.FigPos>();

		FigPos cfp = new FigPos(figs.get(0),1,0);

		while (cfp.y<board.h/2) {
			// put first fig on board
			Log.debug("start with %s at %d,%d", cfp.fig, cfp.x, cfp.y);
			put(cfp.fig, cfp.x, cfp.y);

			Point cp = new Point(0,0);
			int figfrom=1;
			while (findFree(cp)) {
				// try to cover cp with fig
				for (int i=figfrom; i < figs.size(); ++i) {
					Fig f = figs.get(i);
					if (figUsed[f.type] != null) continue;
					for (int x=0; x < f.w; ++x) {
						if (put(f,cp.x-x,cp.y)) {
							list.add(new FigPos(f, cp.x-x, cp.y));
							Log.debug("put fig %s at %d,%d",f,cp.x-x,cp.y);
							break;
						}
						else {
							Log.debug("can't put fig %s at %d,%d",f,cp.x-x,cp.y);
						}
					}
					if (figUsed[f.type]!=null) break;
				}

				if (!board.get(cp.x, cp.y)) {
					Log.debug("can't cover %d,%d", cp.x, cp.y);
					if (list.isEmpty()) break;

					FigPos fp=list.remove(list.size()-1);
					Log.debug("remove fig %s at %d,%d",fp.fig, fp.x, fp.y);
					remove(fp.fig, fp.x, fp.y);
					cp.x=fp.x; cp.y=fp.y;
					figfrom=figs.indexOf(fp.fig)+1;
				}


				if (list.isEmpty()) break;
			}

			// remove first fig from board
			remove(cfp.fig, cfp.x, cfp.y);
			++cfp.x;
			if (cfp.x >= board.w/2) {
				cfp.x=0;
				++cfp.y;
			}
			break;
		}
	}

	private boolean put(Fig f, int x0, int y0) {
		if (figUsed[f.type] != null) {
			throw new RuntimeException(String.format("Fig %s already put", f));
		}

		if (x0 < 0 || y0 < 0) return false;
		if (x0 + f.w > board.w || y0 + f.h > board.h) return false;

		for (int i=0; i < STONES; ++i) {
			if (board.get(x0+f.x[i], y0+f.y[i])) return false;
		}
		for (int i=0; i < STONES; ++i) {
			board.set(x0+f.x[i], y0+f.y[i], true);
		}
		figUsed[f.type] = f;
		return true;
	}
	private void remove(Fig f, int x0, int y0) {
		if (figUsed[f.type] == null) {
			throw new RuntimeException(String.format("Fig %s not on board", f));
		}
		for (int i=0; i < STONES; ++i) {
			board.set(x0+f.x[i], y0+f.y[i], false);
		}
		figUsed[f.type] = null;
	}

	final public static void printAllFigs() {
		for (Fig f : figs) {
			System.out.printf("Fig type %d:\n", f.type);
			f.print();
		}
		System.out.printf("There is %d figs\n", figs.size());
	}
	final public static void printFigs() {
		System.out.printf("Primary figs %d\n", baseFig.length);
		for (Fig f : baseFig) {
			f.print();
		}
	}
	final private static Fig[] baseFig = {
		/*   0 1 2
		 * 0   X
		 * 1 X X X
		 * 2   X
		 [0]*/
		new Fig(1,0,0,1,1,1,2,1,1,2),
		/*   0 1 2
		 * 0 X
		 * 1 X X X
		 * 2   X
		 [1]*/
		new Fig(0,0,0,1,1,1,2,1,1,2),
		/*   0 1 2
		 * 0 X
		 * 1 X X X
		 * 2 X
		 [2]*/
		new Fig(0,0,0,1,1,1,2,1,0,2),
		/*   0 1 2
		 * 0 X
		 * 1 X X X
		 * 2     X
		 [3]*/
		new Fig(0,0,0,1,1,1,2,1,2,2),
		/*   0 1 2
		 * 0 X
		 * 1 X X
		 * 2   X X
		 [4]*/
		new Fig(0,0,0,1,1,1,1,2,2,2),
		/*   0 1 2
		 * 0 X X X
		 * 1 X
		 * 2 X
		 [5]*/
		new Fig(0,0,1,0,2,0,0,1,0,2),
		/*   0 1 2
		 * 0 X X X
		 * 1 X X
		 [6]*/
		new Fig(0,0,1,0,2,0,0,1,1,1),
		/*   0 1 2
		 * 0 X X X
		 * 1 X   X
		 [7]*/
		new Fig(0,0,1,0,2,0,0,1,2,1),
		/*  0 1 2 3
		 *0 X X X X
		 *1 X
		 [8]*/
		new Fig(0,0,1,0,2,0,3,0,0,1),
		/*  0 1 2 3
		 *0 X X X X
		 *1   X
		 [9]*/
		new Fig(0,0,1,0,2,0,3,0,1,1),
		/*   0 1 2 3
		 * 0 X X X
		 * 1     X X
		 [10]*/
		new Fig(0,0,1,0,2,0,2,1,3,1),
		/*   0 1 2 3 4
		 * 0 X X X X X
		 [11]*/
		new Fig(0,0,1,0,2,0,3,0,4,0),
	};
	final private static List<Fig> figs = genAllFigs();
	final private static List<Fig> genAllFigs() {
		List<Fig> figs = new ArrayList<>();

		for (int fi=0; fi < baseFig.length; ++fi) {
			List<Fig> list = new ArrayList<Fig>();
			Fig f = baseFig[fi];
			list.add(f);
			//rotates
			for(;;) {
				f = new Fig(f);
				f.rotate();
				if (list.contains(f)) break;
				list.add(f);
			}
			//flips
			int n=list.size();
			for (int i=0; i < n; ++i) {
				f = new Fig(list.get(i));
				f.flipX();
				if (!list.contains(f))
					list.add(f);
			}
			figs.addAll(list);
		}
		return figs;
	}

	public static class Fig {
		static private String typeName = "XFTZWVPULYNI";
		static private short[] subtypeCnt = new short[12];
		static private int typeCounter=0;
		final private int type;
		private int subtype;
		final int[] x=new int[STONES];
		final int[] y=new int[STONES];
		private int w,h;
		Fig(int x0,int y0,int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4) {
			type=typeCounter++;
			subtype=subtypeCnt[type]=0;
			x[0]=x0;y[0]=y0;
			x[1]=x1;y[1]=y1;
			x[2]=x2;y[2]=y2;
			x[3]=x3;y[3]=y3;
			x[4]=x4;y[4]=y4;
			w=h=0;
			for (int i=0; i < STONES; ++i) {
				if (w < x[i]) w=x[i];
				if (h < y[i]) h=y[i];
			}
		}
		Fig(Fig o) {
			type=o.type;
			subtype=subtypeCnt[type]++;
			w=o.w; h=o.h;
			for (int i=0; i < STONES; ++i) {
				x[i]=o.x[i];
				y[i]=o.y[i];
			}
		}
		@Override
		public String toString() {
			return typeName.substring(type, type+1)+subtype;
		}
		void rotate() {
			int t=w; w=h; h=t;
			for (int i=0; i < STONES; ++i) {
				t=x[i]; x[i]=y[i]; y[i]=t;
			}
			flipX();
		}
		void flipX() {
			for (int i=0; i < STONES; ++i)
				x[i]=w-x[i];
		}
		void flipY() {
			for (int i=0; i < STONES; ++i)
				y[i]=h-y[i];
		}
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Fig)) return false;
			Fig o = (Fig)other;
			if (w!=o.w || h!=o.h) return false;
			BitSet s1=new BitSet(STONES*STONES);
			BitSet s2=new BitSet(STONES*STONES);
			for (int i=0; i < STONES; ++i) {
				s1.set(y[i]*STONES+x[i]);
				s2.set(o.y[i]*STONES+o.x[i]);
			}
			return s1.equals(s2);
		}
		void print() {
			byte[] m = new byte[STONES*STONES];
			for (int i=0; i < m.length; ++i) m[i]=' ';
			for (int i=0; i < STONES; ++i) {
				int x=this.x[i], y=this.y[i];
				m[y*STONES+x] = 'X';
			}
			for (int y=0; y <= this.h; ++y) {
				System.out.write(m, y*STONES, this.w+1);
				System.out.println();
			}
			System.out.printf("size %d x %d\n",this.w+1,this.h+1);
			System.out.println("--------------");
		}
	}
}
