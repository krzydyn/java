package puzzles;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Pentomino {
	static final int ELEMS=5;
	static class Fig {
		int[] x=new int[ELEMS];
		int[] y=new int[ELEMS];
		int w,h;
		Fig(int x0,int y0,int x1,int y1,int x2,int y2,int x3,int y3,int x4,int y4) {
			x[0]=x0;y[0]=y0;
			x[1]=x1;y[1]=y1;
			x[2]=x2;y[2]=y2;
			x[3]=x3;y[3]=y3;
			x[4]=x4;y[4]=y4;
			w=h=0;
			for (int i=0; i < ELEMS; ++i) {
				if (w < x[i]) w=x[i];
				if (h < y[i]) h=y[i];
			}
		}
		Fig(Fig o) {
			w=o.w; h=o.h;
			for (int i=0; i < ELEMS; ++i) {
				x[i]=o.x[i];
				y[i]=o.y[i];
			}
		}
		void rotate() {
			int t=w; w=h; h=t;
			for (int i=0; i < ELEMS; ++i) {
				t=x[i]; x[i]=y[i]; y[i]=t;
			}
			flipX();
		}
		void flipX() {
			for (int i=0; i < ELEMS; ++i)
				x[i]=w-x[i];
		}
		void flipY() {
			for (int i=0; i < ELEMS; ++i)
				y[i]=h-y[i];
		}
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Fig)) return false;
			Fig o = (Fig)other;
			if (w!=o.w || h!=o.h) return false;
			BitSet s1=new BitSet(ELEMS*ELEMS);
			BitSet s2=new BitSet(ELEMS*ELEMS);
			for (int i=0; i < ELEMS; ++i) {
				s1.set(y[i]*ELEMS+x[i]);
				s2.set(o.y[i]*ELEMS+o.x[i]);
			}
			return s1.equals(s2);
		}
		void print() {
			byte[] m = new byte[ELEMS*ELEMS];
			for (int i=0; i < m.length; ++i) m[i]=' ';
			for (int i=0; i < ELEMS; ++i) {
				int x=this.x[i], y=this.y[i];
				m[y*ELEMS+x] = 'X';
			}
			for (int y=0; y <= this.h; ++y) {
				System.out.write(m, y*ELEMS, this.w+1);
				System.out.println();
			}
			System.out.printf("size %d x %d\n",this.w+1,this.h+1);
			System.out.println("--------------");
		}
	}
	final public static void printAllFigs() {
		System.out.printf("There is %d figs\n", figs.size());
		for (Fig f : figs) {
			f.print();
		}
	}
	final public static void printFigs() {
		System.out.printf("There is %d figs\n", baseFig.length);
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
	final static List<Fig> genAllFigs() {
		List<Fig> figs = new ArrayList<Fig>();
		List<Fig> list = new ArrayList<Fig>();
		for (Fig f : baseFig) {
			list.add(f);
			Fig o = f;
			//rotates
			for(;;) {
				o = new Fig(o);
				o.rotate();
				if (list.contains(o)) break;
				list.add(o);
			}
			//flips
			int n=list.size();
			for (int i=0; i < n; ++i) {
				o = new Fig(list.get(i));
				o.flipX();
				if (!list.contains(o))
					list.add(o);
			}
			figs.addAll(list);
			list.clear();
		}
		return figs;
	}
}
