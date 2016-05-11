package algebra;

import java.awt.Dimension;
import java.util.ArrayList;

public class Planes {
	public static interface Raster {
		public int getPixel(int x,int y);
		public void setPixel(int x,int y, int v);
		public Dimension getSize();
	}

	private static class Segment {
		public Segment(int y, int x0, int x1) {
			this.y=y; this.x0=x0; this.x1=x1;
		}
		int x0, x1, y;
	}

	private static Segment makeSegment(Raster t,int x,int y,int sval, int xmax) {
		Dimension d = t.getSize();
		int x0,x1;
		if (t.getPixel(x,y)!=sval) {
			for (x0=x; x0<xmax && t.getPixel(x0,y)!=sval; ++x0) ;
			x=x0;
			if (x==xmax) return null;
		}
		else for (x0=x; x0>0 && t.getPixel(x0-1,y)==sval; --x0) ;
		for (x1=x+1; x1<d.width && t.getPixel(x1,y)==sval; ++x1) ;

		return new Segment(y, x0, x1);
	}

	public static void floodFill(Raster t, int x,int y, int dval) {
		int sval=t.getPixel(x,y);
		if (sval==dval) return ;

		Dimension d = t.getSize();
		ArrayList<Segment> q = new ArrayList<Segment>();
		q.add(makeSegment(t, x, y, sval, d.width));

		while (!q.isEmpty()) {
			Segment s=q.remove(q.size()-1);
			for (x=s.x0; x<s.x1; ++x) t.setPixel(x,s.y,dval);

			//segments above
			if (s.y > 0) {
				y=s.y-1;
				for (x=s.x0; x < s.x1;) {
					Segment ns = makeSegment(t, x, y, sval, s.x1);
					if (ns==null) break;
					q.add(ns); x = ns.x1;
				}
			}
			//segments below
			if (s.y+1 < d.height) {
				y=s.y+1;
				for (x=s.x0; x < s.x1;) {
					Segment ns = makeSegment(t, x, y, sval, s.x1);
					if (ns==null) break;
					q.add(ns); x = ns.x1;
				}
			}
		}
	}

	static class Coord {
		public Coord(int x,int y) {
			this.x=x; this.y=y;
		}
		int x,y;
	}
	static void floodFill_simple(Raster t, int x,int y, int dval) {
		int sval=t.getPixel(x,y);
		if (sval==dval) return ;

		Dimension d = t.getSize();
		ArrayList<Coord> q = new ArrayList<Coord>();
		q.add(new Coord(x, y));

		while (!q.isEmpty()) {
			Coord c=q.remove(q.size()-1);
			x=c.x;y=c.y;
			t.setPixel(x, y, dval);
			if (x>0 && t.getPixel(x-1,y)==1) q.add(new Coord(x-1, y));
			if (x+1<d.width && t.getPixel(x+1,y)==1) q.add(new Coord(x+1, y));
			if (y>0 && t.getPixel(x,y-1)==1) q.add(new Coord(x, y-1));
			if (y+1<d.height && t.getPixel(x,y+1)==1) q.add(new Coord(x, y+1));
		}
	}

}
