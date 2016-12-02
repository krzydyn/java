package puzzles;

import java.awt.Point;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import sys.Log;

public class Pentomino {
	static final int STONES = 5;

	public static interface ChangeListener {
		public void boardChanged(List<FigPos> list);
	}

	public static class FigPos {
		public Fig fig;
		public int x,y;
		public FigPos(Fig fig, int x, int y) {
			this.fig=fig;
			this.x=x; this.y=y;
		}
		public boolean getPoint(int i, Point p) {
			if (i >= STONES) return false;
			p.setLocation(x+fig.x[i],y+fig.y[i]);
			return true;
		}
	}

	private ChangeListener listener;
	final private GameBoard<Boolean> board;
	final private Fig[] figUsed = new Fig[baseFig.length];
	private boolean running=false;

	public Pentomino(int w,int h) {
		board = new GameBoard.Bool(w, h);
	}
	public int getWidth() {return board.w;}
	public int getHeight() {return board.h;}
	public void setListener(ChangeListener l) {listener=l;}
	private boolean nextFree(Point cp) {
		cp.x=cp.y=0;
		if (board.w>board.h) {
			for (;;) {
				if (cp.y >= board.h) {
					cp.y=0;
					++cp.x;
				}
				if (cp.x >= board.w) break;
				if (!board.get(cp.x, cp.y)) return true;
				++cp.y;
			}
		}
		else {
			for (;;) {
				if (cp.x >= board.w) {
					cp.x=0;
					++cp.y;
				}
				if (cp.y >= board.h) break;
				if (!board.get(cp.x, cp.y)) return true;
				++cp.x;
			}
		}
		return false;
	}
	public void stop() {
		running=false;
	}
	private void sortX() {
		for (Fig f : figs) {
			int xmin=f.w;
			int jm=-1;
			for (int j=0; j < STONES; ++j) {
				if (f.y[j]>0) continue;
				if (xmin>f.x[j]) {xmin=f.x[j];jm=j;}
			}
			if (jm>0) {
				int t=f.x[0];
				f.x[0]=f.x[jm];
				f.x[jm]=t;
				t=f.y[0];
				f.y[0]=f.y[jm];
				f.y[jm]=t;
			}
		}
	}
	private void sortY() {
		for (Fig f : figs) {
			int ymin=f.h;
			int jm=-1;
			for (int j=0; j < STONES; ++j) {
				if (f.x[j]>0) continue;
				if (ymin>f.y[j]) {ymin=f.y[j];jm=j;}
			}
			if (jm>0) {
				int t=f.x[0];
				f.x[0]=f.x[jm];
				f.x[jm]=t;
				t=f.y[0];
				f.y[0]=f.y[jm];
				f.y[jm]=t;
			}
		}
	}
	public void solve() {
		running=true;
		List<FigPos> list=new ArrayList<Pentomino.FigPos>();

		if (board.w>board.h) sortY();
		else sortX();

		FigPos cfp = new FigPos(figs.get(0),1,0);
		while (2*cfp.y <= board.h-cfp.fig.h) {
			// put first fig on board
			list.clear();
			Log.debug("start with %s at %d,%d", cfp.fig, cfp.x, cfp.y);
			if (!put(cfp.fig, cfp.x, cfp.y)) {
				Log.debug("can't put cfp here");
				cfp.x=0;
				++cfp.y;
				continue;
			}
			list.add(new FigPos(cfp.fig, cfp.x, cfp.y));

			Point cp = new Point(0,0);
			int findFrom=1;
			while (nextFree(cp)) {
				if (!running) return ;
				//Log.debug("trying to cover %d,%d", cp.x,cp.y);
				// try to cover cp with fig
				for (int i=findFrom; i < figs.size(); ++i) {
					Fig f = figs.get(i);
					if (figUsed[f.type] != null) continue;
					int x=cp.x-f.x[0], y=cp.y-f.y[0];
					if (put(f,x,y)) {
						list.add(new FigPos(f, x, y));
						findFrom=1;
						//Log.debug("put fig %s at %d,%d",f,cp.x-xmin,cp.y);
						if (listener!=null) listener.boardChanged(list);
						break;
					}
				}

				if (board.get(cp.x, cp.y)) {
					if (list.size() == 12) {
						FigPos fp=list.remove(list.size()-1);
						remove(fp.fig, fp.x, fp.y);
						cp.x=fp.x; cp.y=fp.y;
						findFrom = figs.indexOf(fp.fig)+1;
					}
				}
				else {
					if (list.size()==1) break;
					FigPos fp=list.remove(list.size()-1);
					remove(fp.fig, fp.x, fp.y);
					cp.x=fp.x; cp.y=fp.y;
					findFrom = figs.indexOf(fp.fig)+1;
				}
			}

			// remove first fig from board
			remove(cfp.fig, cfp.x, cfp.y);
			++cfp.x;
			if (2*cfp.x > board.w-cfp.fig.w) {
				cfp.x=0;
				++cfp.y;
			}
		}
		running=false;
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
		public final int type;
		private final int subtype;
		private final int[] x=new int[STONES];
		private final int[] y=new int[STONES];
		private int w,h;

		Fig(int x0,int y0,int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4) {
			type=typeCounter++;
			subtype=subtypeCnt[type]=0;
			x[0]=x0;y[0]=y0;
			x[1]=x1;y[1]=y1;
			x[2]=x2;y[2]=y2;
			x[3]=x3;y[3]=y3;
			x[4]=x4;y[4]=y4;
			w=h=-1;
			for (int i=0; i < STONES; ++i) {
				if (w < x[i]) w=x[i];
				if (h < y[i]) h=y[i];
			}
			++w; ++h;
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
				x[i]=w-x[i]-1;
		}
		void flipY() {
			for (int i=0; i < STONES; ++i)
				y[i]=h-y[i]-1;
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
	}
}
