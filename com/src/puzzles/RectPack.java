package puzzles;

import java.util.ArrayList;
import java.util.List;

import sys.Log;

public class RectPack {
	private int cx,cy;
	final private List<Rect> rects = new ArrayList<Rect>();
	final private Dim sheet;
	private final Dim rect = new Dim(0,0);
	private final Dim lay[] = new Dim[2];

	public static class Dim {
		public int w, h;
		public Dim(int w,int h) {this.w=w; this.h=h;}
		Dim(Dim d) {w=d.w; h=d.h;}
		void rot() { int t=w; w=h; h=t;}
		public Dim rotated() { Dim d=new Dim(this); d.rot(); return d;}
		public boolean contains(Rect r) {
			return 0<=r.x && 0<=r.y && r.x+r.s.w<=w && r.y+r.s.h<=h;
		}
	}
	public static class Rect {
		public int x, y;
		public Dim s;
		public Rect(int x,int y,Dim s) {
			set(x, y, s);
		}
		Rect(Rect r) {set(r);}
		void set(int x,int y,Dim s) {
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

	public RectPack(Dim sheet) {
		this.sheet=sheet;
	}
	public void setRect(Dim r) {
		rect.w=r.w;
		rect.h=r.h;
		rects.clear();
		lay[0] = rect;
		lay[1] = rect.rotated();
	}

	private Rect overlaps(Rect r) {
		for (int i=rects.size(); i>0; ) {
			--i;
			if (r.intersects(rects.get(i))) return rects.get(i);
		}
		return null;
	}
	private int solve_r() {
		rects.clear();
		int n=0,nbest=0,nmax=0;
		long tm0=System.currentTimeMillis()+1000;
		while (!next()) {
			++n;
			if (nbest == rects.size()) ++nmax;
			else if (nbest < rects.size()) {
				nmax=1;
				nbest=rects.size();
			}
		}

		if (tm0 < System.currentTimeMillis()) {
			Log.info("n=%d, rects=%d",n,rects.size());
			tm0+=2000;
		}
		Log.info("n=%d, best=%d(%d)",n,nbest,nmax);
		return nbest;
	}

	private int solve_sq(int s) {
		int nw = sheet.w/s;
		int nh = sheet.h/s;
		return nw*nh;
	}
	public int solve() {
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

		return solve_r();
	}

	public List<Rect> getRects() { return rects; }

	public boolean next() {
		if (rect.h<=0 || rect.w<=0) return false;
		Rect tr=new Rect(0,0,null);
		Rect r;

		if (rects.size() > 0) {
			do {
				r=rects.remove(rects.size()-1);
				int idx = -1;
				for (int i=0; i < lay.length; ++i)
					if (r.s == lay[i]) {idx=i;break;}
				if (idx < 0) throw new RuntimeException();

				tr.set(r); r=null;
				for (int i=idx+1; i < lay.length; ++i) {
					tr.s = lay[i];
					if (sheet.contains(tr) && overlaps(tr) == null) {
						r=tr; break;
					}
				}
				if (r == null) continue;
				rects.add(new Rect(r.x,r.y,r.s));
				cx = r.x+r.s.w; cy=r.y;
				break;
			} while (rects.size()>0);
			if (rects.size() == 0) return false;
		}
		else {
			cx=cy=0;
		}

		while (cy < sheet.h) {
			while (cx < sheet.w) {
				r=null;
				for (int i=0; i < lay.length; ++i) {
					tr.set(cx,cy,lay[i]);
					if (sheet.contains(tr)) { r=tr; break; }
				}
				if (r == null) break;
				Rect o=overlaps(tr);
				if (o == null) {
					rects.add(o = new Rect(r.x,r.y,r.s));
				}
				cx=o.x+o.s.w;
			}
			++cy; cx=0;
		}
		return true;
	}
}
