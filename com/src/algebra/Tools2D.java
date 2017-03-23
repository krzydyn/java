/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package algebra;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

public class Tools2D {
	private static class Segment {
		public Segment(int y, int x0, int x1) {
			this.y=y; this.x0=x0; this.x1=x1;
		}
		int x0, x1, y;
	}

	private static Segment makeSegment(Raster2D t,int x,int y,int sval, int xmax) {
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

	public static void floodFill(Raster2D t, int x,int y, int dval) {
		int sval=t.getPixel(x,y);
		if (sval==dval) return ;

		Dimension d = t.getSize();
		ArrayList<Segment> q = new ArrayList<Segment>();
		q.add(makeSegment(t, x, y, sval, d.width));

		while (!q.isEmpty()) {
			Segment s=q.remove(q.size()-1);
			t.drawHline(s.x0, s.x1, y, dval);

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

	public static void floodFill_simple(Raster2D t, int x,int y, int dval) {
		int sval=t.getPixel(x,y);
		if (sval==dval) return ;

		Dimension d = t.getSize();
		ArrayList<Point> q = new ArrayList<Point>();
		q.add(new Point(x, y));

		while (!q.isEmpty()) {
			Point c=q.remove(q.size()-1);
			x=c.x;y=c.y;
			t.drawHline(x, x+1, y, dval);
			if (x>0 && t.getPixel(x-1,y)==1) q.add(new Point(x-1, y));
			if (x+1<d.width && t.getPixel(x+1,y)==1) q.add(new Point(x+1, y));
			if (y>0 && t.getPixel(x,y-1)==1) q.add(new Point(x, y-1));
			if (y+1<d.height && t.getPixel(x,y+1)==1) q.add(new Point(x, y+1));
		}
	}

	private static int convolve(Raster2D r, MatrixI k, int x0, int y0) {
		Dimension dim = r.getSize();
		int a=0;
		x0 -= k.getWidth()/2;
		y0 -= k.getHeight()/2;
		for (int y=0; y < k.getHeight(); ++y) {
			for (int x=0; x < k.getWidth(); ++x) {
				int p=0, rx=x0+x,ry=y0+y;

				//extend method (other: wrap, crop)
				if (rx < 0) rx=0;
				else if (rx >= dim.width) rx=dim.width-1;
				if (ry < 0) ry=0;
				else if (ry >= dim.height) ry=dim.height-1;

				p=r.getPixel(rx, ry);
				a += p*k.get(x, y);
			}
		}
		return a;
	}

	// y[n] = x[n] * h[n]
	public static void convolve(Raster2D r, MatrixI k) {
		Dimension dim = r.getSize();
		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int a=convolve(r, k, x, y);
				r.setPixel(x, y, a);
			}
		}
	}
}
