package puzzles;

import java.util.ArrayList;
import java.util.List;

import sys.Log;

public class RectPack {
	private int cx,cy;
	final private List<Rect> rects = new ArrayList<Rect>();
	final private List<Rect> best = new ArrayList<Rect>();
	final private Dim sheet;

	public static class Dim {
		public int w, h;
		public Dim(int w,int h) {this.w=w; this.h=h;}
		Dim(Dim d) {w=d.w; h=d.h;}
		void rot() { int t=w; w=h; h=t;}
		public Dim rotated() { Dim d=new Dim(this); d.rot(); return d;}
	}
	public static class Rect {
		int x, y;
		Dim s;
		public Rect(int x,int y,Dim s) {
			this.x=x; this.y=y;
			this.s=s;
		}
		Rect(Rect r) {set(r);}
		void set(Rect r) {
			x=r.x; y=r.y;
			s=r.s;
		}
		@Override
		public String toString() {
			return String.format("(%d,%d,%d,%d)", x,y,s.w,s.h);
		}
		public boolean intersects(Rect r) {
			return x < r.x+r.s.w && r.x < x+s.w && y < r.y+r.s.h && r.y < y+s.h;
		}
	}

	public RectPack(Dim sheet) {
		this.sheet=sheet;
	}

	private Rect overlaps(Rect r) {
		for (int i=0; i < rects.size(); ++i)
			if (r.intersects(rects.get(i))) return rects.get(i);
		return null;
	}
	private int solve_r(Dim small_rect) {
		Dim d1 = small_rect;
		Dim d2 = small_rect.rotated();
		Rect r;

		long n=0,nmax=0,tm0=System.currentTimeMillis()+1000;
		cx=cy=0;
		for (;;) {

			while (cy < sheet.h) {
				while (cx < sheet.w) {
					if (cx+d1.w <= sheet.w && cy+d1.h <= sheet.h)
						r = new Rect(cx, cy, d1);
					else if (cx+d2.w <= sheet.w && cy+d2.h <= sheet.h)
						r = new Rect(cx, cy, d2);
					else break;
					Rect o=overlaps(r);
					if (o == null) {
						rects.add(o=r);
					}
					cx = o.x+o.s.w;
				}
				++cy; cx=0;
			}
			++n;

			if (best.size() == rects.size()) ++nmax;
			else if (best.size() < rects.size()) {
				nmax=1;
				for (int i=0; i < rects.size(); ++i) {
					if (best.size() <= i) best.add(new Rect(rects.get(i)));
					else best.get(i).set(rects.get(i));
				}
			}

			if (tm0 < System.currentTimeMillis()) {
				Log.debug("n=%d, nmax=%d, rects=%d",n,nmax,best.size());
				tm0+=2000;
			}

			do {
				r=rects.get(rects.size()-1);
				if (r.s==d1 && r.x+d2.w <= sheet.w && r.y+d2.h <= sheet.h) break;
				rects.remove(r);
			} while (rects.size()>0);

			if (r.s==d2) break;
			r.s = d2;
			//cx = r.x+d2.w; cy=r.y;
			cx = 0; cy = 0;
		}
		for (int i=0; i < best.size(); ++i) {
			Log.prn("rect[%d]: %s", i, best.get(i));
		}
		Log.debug("n=%d, nmax=%d, rects=%d",n,nmax,best.size());
		return best.size();
	}

	private int solve_sq(int s) {
		int nw = sheet.w/s;
		int nh = sheet.h/s;
		return nw*nh;
	}
	public int solve(Dim rect) {
		//trivial case
		if (rect.w > sheet.w && rect.w > sheet.h) return 0;
		if (rect.h > sheet.w && rect.h > sheet.h) return 0;
		if (rect.w*rect.h > sheet.w*sheet.h) return 0;

		//simple case
		if (rect.w==rect.h) return solve_sq(rect.w);
		if (sheet.w % rect.w == 0 && sheet.h % rect.h == 0)
			return (sheet.w/rect.w)*(sheet.h/rect.h);
		if (sheet.w % rect.h == 0 && sheet.h % rect.w == 0)
			return (sheet.w/rect.h)*(sheet.h/rect.w);

		return solve_r(rect);
	}
}
