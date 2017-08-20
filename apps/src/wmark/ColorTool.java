package wmark;

import img.BitRaster2D;
import img.Colors;
import img.Raster2D;
import img.Tools2D.Segment;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class ColorTool extends Tool {
	public float threshold = 5;

	public List<Segment> select(Raster2D r, int x, int y) {
		int c0=r.getPixel(x,y);
		Dimension d = r.getSize();
		BitRaster2D br = new BitRaster2D(d.width, d.height);
		ArrayList<Segment> seg = new ArrayList<Segment>();
		ArrayList<Segment> q = new ArrayList<Segment>();
		q.add(makeSegment(r,br,x,y,c0,threshold));
		while (!q.isEmpty()) {
			Segment s=q.remove(q.size()-1);
			br.drawHline(s.x0, s.x1, s.y, 1);
			seg.add(s);
			//segments above
			if (s.y > 0) {
				y=s.y-1;
				for (x=s.x0; x < s.x1;) {
					Segment ns = makeSegment(r, br, x, y, c0, threshold);
					if (ns!=null) {q.add(ns); x = ns.x1;}
					else ++x;
				}
			}
			//segments below
			if (s.y+1 < d.height) {
				y=s.y+1;
				for (x=s.x0; x < s.x1;) {
					Segment ns = makeSegment(r, br, x, y, c0, threshold);
					if (ns!=null) {q.add(ns); x = ns.x1;}
					else ++x;
				}
			}
		}
		return seg;
	}

	private static Segment makeSegment(Raster2D r,Raster2D rd,int x,int y,int c,float err) {
		if (Colors.rgbError(r.getPixel(x,y), c) >= err) return null;
		if (rd.getPixel(x, y)!=0) return null;
		Dimension d = r.getSize();
		int x0,x1;
		for (x0=x; x0>0 && Colors.rgbError(r.getPixel(x0-1,y), c) < err; --x0) ;
		for (x1=x+1; x1<d.width && Colors.rgbError(r.getPixel(x1,y),c) < err; ++x1) ;
		if (x0==x1) return null;
		return new Segment(y, x0, x1);
	}
}
