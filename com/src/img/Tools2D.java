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
package img;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sys.Log;
import algebra.MatrixI;

public class Tools2D {
	public static void luminance(Raster2D r) {
		Dimension dim = r.getSize();
		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int a = r.getPixel(x, y);
				a = Colors.luminance2(a);
				r.setPixel(x, y, (a<<16) + (a<<8) + a);
			}
		}
	}

	public static class Segment {
		public Segment(int y, int x0, int x1) {
			this.y=y; this.x0=x0; this.x1=x1;
		}
		public int x0, x1, y;
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
		ArrayList<Segment> q = new ArrayList<>();
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
		ArrayList<Point> q = new ArrayList<>();
		q.add(new Point(x, y));

		while (!q.isEmpty()) {
			Point c=q.remove(q.size()-1);
			x=c.x;y=c.y;
			if (t.getPixel(x,y)==sval) continue;
			t.drawHline(x, x+1, y, dval);
			if (x>0 && t.getPixel(x-1,y)==sval) q.add(new Point(x-1, y));
			if (x+1<d.width && t.getPixel(x+1,y)==sval) q.add(new Point(x+1, y));
			if (y>0 && t.getPixel(x,y-1)==sval) q.add(new Point(x, y-1));
			if (y+1<d.height && t.getPixel(x,y+1)==sval) q.add(new Point(x, y+1));
		}
	}

	private static int convolve(Raster2D r, MatrixI k, int x0, int y0) {
		Dimension dim = r.getSize();
		int a=0;
		x0 -= k.getWidth()/2+1;
		y0 -= k.getHeight()/2+1;
		for (int y=0; y < k.getHeight(); ++y) {
			for (int x=0; x < k.getWidth(); ++x) {
				int p=0, rx=x0+x,ry=y0+y;

				//extend method (other: wrap, crop)
				if (rx < 0) rx=0;
				else if (rx >= dim.width) rx=dim.width-1;
				if (ry < 0) ry=0;
				else if (ry >= dim.height) ry=dim.height-1;

				p=r.getPixel(rx, ry)&0xff;
				a += p*k.get(x, y);
			}
		}
		return a;
	}

	// y[n] = x[n] * h[n]
	public static void convolve(Raster2D dst, Raster2D src, MatrixI k) {
		int div=0;
		for (int y=0; y < k.getHeight(); ++y ) {
			for (int x=0; x < k.getWidth(); ++x ) {
				div += Math.abs(k.get(x, y));
			}
		}
		if (div==0) div=1;
		Dimension dim = src.getSize();

		int amin=Integer.MAX_VALUE,amax=Integer.MIN_VALUE;
		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int a=convolve(src, k, x, y)/div;
				a += 127;
				if (amin > a) amin=a;
				if (amax < a) amax=a;
				if (a < 0) a=0; else if (a > 255) a=255;
				dst.setPixel(x, y, (a<<16) + (a<<8) + a);
			}
		}
		Log.debug("convol: amin=%d   amax=%d", amin, amax);
	}

	public static void smoothGauss(Raster2D r) {
		Dimension dim = r.getSize();
		MatrixI gx = new MatrixI(5,
				2, 4, 5, 4, 2,
				4, 9,12, 9, 4,
				5,12,15,12, 5,
				4, 9,12, 9, 4,
				2, 4, 5, 4, 2);
		Raster2D rr = new ImageRaster2D(dim.width, dim.height);
		convolve(rr, r, gx);

		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int a = rr.getPixel(x, y)&0xff;
				r.setPixel(x, y, (a<<16) + (a<<8) + a);
			}
		}
	}

	public static void edgeSobel(Raster2D r) {
		edgeSobel(r, null);
	}
	public static void edgeSobel(Raster2D r, Raster2D gradients) {
		Dimension dim = r.getSize();
		MatrixI gx = new MatrixI(3,
				-1, 0, 1,
				-2, 0, 2,
				-1, 0, 1);
		MatrixI gy = new MatrixI(3,
				 1,  2,  1,
				 0,  0,  0,
				-1, -2, -1);
		Raster2D rx = new ImageRaster2D(dim.width, dim.height);
		Raster2D ry = new ImageRaster2D(dim.width, dim.height);
		convolve(rx, r, gx);
		convolve(ry, r, gy);

		int amin=Integer.MAX_VALUE,amax=Integer.MIN_VALUE;
		double sqm = Math.sqrt(2)*127;
		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int ax = (rx.getPixel(x, y)&0xff)-127;
				int ay = (ry.getPixel(x, y)&0xff)-127;
				int a = (int)Math.round(255*Math.sqrt(ax*ax+ay*ay)/sqm);
				if (amin > a) amin=a;
				if (amax < a) amax=a;
				if (a < 0) a = 0; else if (a > 255) a=255;
				r.setPixel(x, y, (a<<16) + (a<<8) + a);
			}
		}
		Log.debug("sobel: amin=%d   amax=%d", amin, amax);
		if (gradients != null) {
			//save gradients
			amin=Integer.MAX_VALUE; amax=Integer.MIN_VALUE;
			for (int y=0; y < dim.height; ++y ) {
				for (int x=0; x < dim.width; ++x) {
					int ax = (rx.getPixel(x, y)&0xff)-127;
					int ay = (ry.getPixel(x, y)&0xff)-127;
					double phi = Math.atan2(ay, ax); // phi = (-pi .. pi)
					int a = (int)(Math.round(255*Math.abs(phi)/Math.PI));
					//double phi = (Math.atan2(ay, ax)+Math.PI)/2; // phi = (0 .. pi)
					//int a = (int)(Math.round(255*phi/Math.PI));
					if (amin > a) amin=a;
					if (amax < a) amax=a;
					if (a < 0) a = 0; else if (a > 255) a=255;
					gradients.setPixel(x, y, (a<<16) + (a<<8) + a);
				}
			}
			Log.debug("gradient: amin=%d   amax=%d", amin, amax);
		}
		rx.dispose();
		ry.dispose();
	}
	public static void edgeCanny(Raster2D r) {
		// 1. Apply Gaussian filter to smooth the image in order to remove the noise
		smoothGauss(r);
		// 2. Find the intensity gradients of the image
		edgeSobel(r);
		// 3. non-maximum suppression
		// 4. Apply double threshold to determine potential edges
		// 5. Track edge by hysteresis: Finalize the detection of edges by suppressing
		//    all the other edges that are weak and not connected to strong edges.
	}

	public static double cross(Point2D p1, Point2D p2, Point2D p3) {
		return (p2.getX()-p1.getX())*(p3.getY()-p1.getY())-(p2.getY()-p1.getY())*(p3.getX()-p1.getX());
	}

	public static double lineDistSq(Point2D p1, Point2D p2, Point2D p) {
		return Math.abs((p.getY() - p1.getY()) * (p2.getX() - p1.getX()) -
				(p2.getY() - p1.getY()) * (p.getX() - p1.getX()));
	}


	static private int compareResult(double r) {
		if (r < -1e-10) return -1;
		if (r > 1e-10) return 1;
		return 0;
	}
	static int orientation(Point2D p1, Point2D p2, Point2D p3) {
		return compareResult(cross(p1, p2, p3));
	}

	private static void hullQuick(List<Point2D> pnts, List<Point2D> h, Point2D p1, Point2D p2) {
		Point2D mp=null;
		double md = 0;
		for (Point2D p : pnts) {
			if (orientation(p1, p2, p) > 0) {
				double d = lineDistSq(p1,p2,p);
				if (md < d) {md=d; mp=p;}
			}
		}
		if (mp == null) {
			h.add(p1); h.add(p2);
			return ;
		}
		hullQuick(pnts,h, p1, mp);
		hullQuick(pnts,h, mp, p2);
	}

	private static List<Point2D> quadriInHull(List<Point2D> pnts) {
		List<Point2D> h = new ArrayList<>();
		if (pnts.size() == 0) return h;

		Point2D left,top,right,bot;
		left=right=top=bot=pnts.get(0);
		// find the leftmost, rightmost, topmost and bottommost points
		for (Point2D p : pnts) {
			if (p.getX() < left.getX()) left = p;
			if (p.getX() > right.getX()) right = p;
			if (p.getY() < top.getY()) top = p;
			if (p.getY() > bot.getY()) bot = p;
		}

		h.add(left); h.add(top); h.add(right); h.add(bot);
		return h;
	}

	private static List<Point2D> cutout(List<Point2D> pnts, List<Point2D> h) {
		List<Point2D> a = new ArrayList<>();
		if (h == null) h = quadriInHull(pnts);
		for (Point2D p : pnts) {
			if (!Lines.pointInPolygon(p, h)) a.add(p);
		}
		return a;
	}

	/**
	 * QuickHull finding the convex hull algorithm
	 * @param pnts
	 * @return
	 */
	public static List<Point2D> hullQuick(List<Point2D> pnts) {
		if (pnts.size() < 3) return new ArrayList<>(pnts);

		List<Point2D> h = quadriInHull(pnts);
		pnts = cutout(pnts, h);

		Point2D p1,p2;
		p1=p2=pnts.get(0);
		for (Point2D p : pnts) {
			if (p.getX() < p1.getX()) p1=p;
			else if (p.getX() > p2.getX()) p2=p;
		}
		hullQuick(pnts, h, p1, p2); //one side
		hullQuick(pnts, h, p2, p1); //other side
		return h;
	}
	/**
	 * Andrew's monotone chain finding the convex hull algorithm
	 * @param pnts
	 * @return
	 */
	public static List<Point2D> hullAndrew(List<Point2D> pnts) {
		if (pnts.size() < 3) return new ArrayList<>(pnts);
		List<Point2D> h = new ArrayList<>();
		pnts = cutout(pnts, null);

		Collections.sort(pnts, new Comparator<Point2D>() {
			@Override
			public int compare(Point2D p1, Point2D p2) {
				double r = p1.getY()!=p2.getY() ? p1.getY()-p2.getY() : p1.getX()-p2.getX();
				return r < 0 ? -1 : r > 0 ? 1 : 0;
			}
		});

		// lower hull
		for (int i=0; i < pnts.size(); ++i) {
			while (h.size() > 1 && cross(h.get(h.size()-2), h.get(h.size()-1), pnts.get(i)) <= 0)
				h.remove(h.size()-1);
			h.add(pnts.get(i));
		}

		int l = h.size();
		// upper hull
		for (int i=pnts.size()-2; i >= 0; --i) {
			while (h.size() > l && cross(h.get(h.size()-2), h.get(h.size()-1), pnts.get(i)) <= 0)
				h.remove(h.size()-1);
			if (i > 0) h.add(pnts.get(i));
		}
		//h.remove(h.size()-1);

		return h;
	}

	/**
	 * Graham's scan finding the convex hull algorithm
	 * @param pnts
	 * @return
	 */
	public static List<Point2D> hullGraham(List<Point2D> pnts) {
		if (pnts.size() < 3) return new ArrayList<>(pnts);
		final List<Point2D> h = new ArrayList<>();
		pnts = cutout(pnts, null);

		Point2D mP = pnts.get(0);
		//1. find point with lowest y then x
		for (int i=1; i < pnts.size(); ++i) {
			Point2D p = pnts.get(i);
			if (mP.getY() > p.getY()) mP = p;
			else if (mP.getY() == p.getY()) {
				if (mP.getX() > p.getX()) mP = p;
			}
		}

		final Point2D p0 = mP;
		//2. sort pnts by angle to x-axis
		Collections.sort(pnts, new Comparator<Point2D>() {
			@Override
			public int compare(Point2D p1, Point2D p2) {
				if (p0 == p1) return -1;
				if (p0 == p2) return 1;
				int r = orientation(p0, p2, p1);
				if (r == 0) {
					r = compareResult(p0.distanceSq(p1) - p0.distanceSq(p2));
				}
				return r;
			}
		});
		//Log.debug("sorted: %s", Text.join("\n", pnts));

		h.add(p0);
		//3. build hull
		for (int i=1; i < pnts.size(); ++i) {
			while (h.size() > 1 && cross(h.get(h.size()-2), h.get(h.size()-1), pnts.get(i)) <= 0) {
				h.remove(h.size()-1);
			}
			h.add(pnts.get(i));
		}
		return h;
	}

	/**
	 * TODO concave hull (alpha shape), need Delaunay triangulation, maybe not
	 * @param pnts
	 * @param a - alpha shape radius
	 * @return hull
	 */
	public static List<Point2D> alphaShape(List<Point2D> pnts, double a) {
		final List<Point2D> h = new ArrayList<>();
		return h;
	}
}
