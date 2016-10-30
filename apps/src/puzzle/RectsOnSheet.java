package puzzle;

import java.util.ArrayList;
import java.util.List;

import sys.Log;

public class RectsOnSheet {
	static class Dim {
		Dim(int w,int h) {this.w=w; this.h=h;}
		Dim(Dim d) {w=d.w; h=d.h;}
		int w, h;
		void rot() { int t=w; w=h; h=t;}
		Dim rotated() { Dim d=new Dim(this); d.rot(); return d;}
	}
	static class Rect {
		Rect(int x,int y,Dim s) {
			this.x=x; this.y=y;
			this.s=s;
		}
		Rect(Rect r) {set(r);}
		void set(Rect r) {
			x=r.x; y=r.y;
			s=r.s;
		}
		int x, y;
		Dim s;
		@Override
		public String toString() {
			return String.format("(%d,%d,%d,%d)", x,y,s.w,s.h);
		}
		public boolean intersects(Rect r) {
			return x < r.x+r.s.w && r.x < x+s.w && y < r.y+r.s.h && r.y < y+s.h;
		}
	}
	private static Rect overlaps(List<Rect> rects, Rect r) {
		for (int i=0; i < rects.size(); ++i)
			if (r.intersects(rects.get(i))) return rects.get(i);
		return null;
	}
	static int square(Dim sheet, int s) {
		int nw = sheet.w/s;
		int nh = sheet.h/s;
		return nw*nh;
	}
	static List<Rect> rects(Dim sheet, Dim rect) {
		int maxlev = 0;
		List<Rect> rectsmax = new ArrayList<RectsOnSheet.Rect>();
		List<Rect> rects = new ArrayList<RectsOnSheet.Rect>();
		List<Rect> r0 = new ArrayList<RectsOnSheet.Rect>();
		Dim d1 = rect;
		Dim d2 = rect.rotated();
		int cx,cy;
		cy = 0;cx = 0;
		Rect r = null;

		long n=0,nmax=0,tm0=System.currentTimeMillis();
		while (true) {
			while (cy + d1.h <= sheet.h) {
				while (cx < sheet.w) {
					if (cx+d1.w > sheet.w) break;
					r = new Rect(cx, cy, d1);
					Rect o=overlaps(rects,r);
					if (o !=null) {
						Log.prn("r=%s overlaps with %s",r,o);
						break;
					}
					rects.add(r);
					if (r.x == 0) r0.add(r);
					cx+=r.s.w;
				}
				cx=0; cy+=r0.get(r0.size()-1).s.h;
			}
			++n;
			if (tm0 < System.currentTimeMillis()) {
				Log.debug("n=%d  nmax=%d",n,nmax);
				tm0+=2000;
			}
			if (maxlev == rects.size()) ++nmax;
			if (maxlev < rects.size()) {
				maxlev = rects.size();
				for (int i=0; i < rects.size(); ++i) {
					if (rectsmax.size() <= i) rectsmax.add(new Rect(rects.get(i)));
					else rectsmax.get(i).set(rects.get(i));
				}
				Log.debug("new max = %d", rectsmax.size());
				nmax=1;
			}

			do {
				r=rects.get(rects.size()-1);
				if (r.s==d1 && r.x+d2.h <= sheet.h) break;
				rects.remove(r);
				if (r.x==0) r0.remove(r);
			} while (rects.size()>0);

			if (r.s==d2) break;
			r.s=d2;
			cx=r.x+r.s.w; cy=r.y;
		}
		Log.debug("n=%d  nmax=%d",n,nmax);
		System.out.println(rectsmax.toString());
		return rectsmax;
	}

	static int solve(Dim sheet, Dim rect) {
		//trivial case
		if (rect.w > sheet.w && rect.w > sheet.h) return 0;
		if (rect.h > sheet.w && rect.h > sheet.h) return 0;
		if (rect.w*rect.h > sheet.w*sheet.h) return 0;

		//simple case
		if (rect.w==rect.h) return square(sheet, rect.w);
		if (sheet.w % rect.w == 0 && sheet.h % rect.h == 0)
			return (sheet.w/rect.w)*(sheet.h/rect.h);
		if (sheet.w % rect.h == 0 && sheet.h % rect.w == 0)
			return (sheet.w/rect.h)*(sheet.h/rect.w);
		List<Rect> r = rects(sheet, rect);
		draw(sheet,r);
		return r.size();
	}

	static void draw(Dim d,char[] sc,Rect r) {
		int y1=r.y*d.w+r.x;
		int y2=(r.y+r.s.h)*d.w+r.x;
		for (int x=0; x < r.s.w; ++x) {
			sc[y1+x] = '-';
			sc[y2+x] = '-';
		}
		for (int y=0; y < r.s.h; ++y) {
			sc[(r.y+y)*d.w+r.x] = '|';
			sc[(r.y+y)*d.w+r.x+r.s.w] = '|';
		}
	}
	static void draw(Dim sheet,List<Rect> rects) {
		Dim d=new Dim(sheet.w, sheet.h);
		for (int i=0; i < rects.size(); ++i) {
			Rect r=rects.get(i);
			if (d.w < r.x+r.s.w) d.w = r.x+r.s.w;
			if (d.h < r.y+r.s.h) d.h = r.y+r.s.h;
		}
		++d.w; ++d.h;
		char[] sc = new char[d.w*d.h];
		for (int i=0; i < rects.size(); ++i)
			draw(d,sc,rects.get(i));
		StringBuilder b=new StringBuilder(d.w);
		for (int y=0; y < d.h; ++y) {
			b.setLength(0);
			int y1=y*d.w;
			for (int x=0; x < d.w; ++x) {
				if (sc[y1+x]==0) b.append(' ');
				else b.append(sc[y1+x]);
			}
			Log.prn("%s", b.toString());
		}
	}

	public static void main(String[] args) {
		Dim sheet = new Dim(15,15);
		Dim rect = new Dim(4,3);

		int ssq = sheet.w*sheet.h;
		int rsq = rect.w*rect.h;

		int n = solve(sheet,rect);
		System.out.printf("Sheet: %d mm^2, %d rects = %d mm^2, lost: %d mm^2\n", ssq, n, n*rsq, ssq-n*rsq);
	}
}
