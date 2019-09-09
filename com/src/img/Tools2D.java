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

	private static long convolve(Raster2D r, MatrixI k, int x0, int y0) {
		Dimension dim = r.getSize();
		long a=0;
		x0 -= k.getWidth()/2;
		y0 -= k.getHeight()/2;
		for (int y=0; y < k.getHeight(); ++y) {
			int ry = y0+y;
			for (int x=0; x < k.getWidth(); ++x) {
				int p=0, rx=x0+x;

				//extend method (other: wrap, crop)
				if (rx < 0) rx=0;
				else if (rx >= dim.width) rx=dim.width-1;
				if (ry < 0) ry=0;
				else if (ry >= dim.height) ry=dim.height-1;

				p = r.getPixel(rx, ry)&0xff;
				int kxy = k.get(x, y);
				a += p*kxy;
			}
		}
		return a;
	}

	// y[n] = x[n] * h[n]
	public static void convolve(Raster2D dst, Raster2D src, MatrixI k) {
		long div_p=0, div_m=0;
		for (int y=0; y < k.getHeight(); ++y ) {
			for (int x=0; x < k.getWidth(); ++x ) {
				int a = k.get(x, y);
				if (a >= 0) div_p += a;
				else div_m -= a;
			}
		}
		long div = div_p + div_m;
		if (div <= 0) {
			Log.error("convolve out of range div=%d",div);
			throw new RuntimeException("convolve out of range");
		}
		int offs = div_m <= 0 ? 0 : 127;

		Dimension dim = src.getSize();
		int amin=Integer.MAX_VALUE,amax=Integer.MIN_VALUE;
		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int a = (int)(convolve(src, k, x, y)/div);
				a += offs;
				if (amin > a) amin=a;
				if (amax < a) amax=a;
				if (a < 0) a = 0; else if (a > 255) a = 255;
				dst.setPixel(x, y, (a<<16) + (a<<8) + a);
			}
		}
		Log.debug("convolve:  amin=%d   amax=%d  div=%d", amin, amax, div);
	}

	static MatrixI generateDiscreteGauss(float ro, int size) {
		MatrixI m = new MatrixI(size, size);
		int x0 = -size/2, y0 = -size/2;
		double c = 1.0/(2.0*Math.PI*ro*ro);
		double g = c*Math.exp(-(x0*x0)/(2.0*ro*ro));
		c = 0.5*c/g;
		for (int y = 0; y < size; ++y) {
			int ry = y0+y;
			for (int x = 0; x < size; ++x) {
				int rx = x0+x;
				g = c*Math.exp(-(rx*rx+ry*ry)/(2.0*ro*ro));
				m.set(x, y, (int)Math.round(g));
			}
		}
		return m;
	}

	static MatrixI gauss_ro_05 = generateDiscreteGauss(.5f, 5);
	static MatrixI gauss_ro_10 = generateDiscreteGauss(1f, 5);
	static MatrixI gauss_ro_50 = generateDiscreteGauss(5f, 5);

	public static void smoothGauss(Raster2D r) {
		MatrixI gauss = gauss_ro_05;
		Dimension dim = r.getSize();
		Raster2D rr = new ImageRaster2D(dim.width, dim.height);
		convolve(rr, r, gauss);

		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int a = rr.getPixel(x, y)&0xff;
				r.setPixel(x, y, (a<<16) + (a<<8) + a);
			}
		}
	}

	/*
	 * https://www.cs.auckland.ac.nz/compsci373s1c/PatricesLectures/Edge%20detection-Sobel_2up.pdf
	 */
	public static void edgeDetection(Raster2D r, MatrixI gx, MatrixI gy, Raster2D gradients) {
		Dimension dim = r.getSize();
		Raster2D rx = new ImageRaster2D(dim.width, dim.height);
		Raster2D ry = new ImageRaster2D(dim.width, dim.height);
		convolve(rx, r, gx);
		convolve(ry, r, gy);

		//TODO calc histogram
		int amin=Integer.MAX_VALUE,amax=Integer.MIN_VALUE;
		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int ax = (rx.getPixel(x, y)&0xff) - 127;
				int ay = (ry.getPixel(x, y)&0xff) - 127;
				//int a = (int)(Math.sqrt(ax*ax+ay*ay)*c+0.5);
				int a = Math.abs(ax)+Math.abs(ay);
				//int a = (ax+ay)/2 + 127;
				if (amin > a) amin=a;
				if (amax < a) amax=a;
			}
		}
		double c = 255.0/amax * 5.0;
		Log.debug("edgeDetection(1): amin=%d   amax=%d   c=%.3f", amin, amax, c);
		amin=Integer.MAX_VALUE; amax=Integer.MIN_VALUE;
		for (int y=0; y < dim.height; ++y ) {
			for (int x=0; x < dim.width; ++x) {
				int ax = (rx.getPixel(x, y)&0xff) - 127;
				int ay = (ry.getPixel(x, y)&0xff) - 127;
				//int a = (int)(Math.sqrt(ax*ax+ay*ay)*c+0.5);
				int a = (Math.abs(ax)+Math.abs(ay));
				a = (int)Math.round(a*c);
				if (amin > a) amin=a;
				if (amax < a) amax=a;
				if (a < 0) a = 0; else if (a > 255) a=255;
				r.setPixel(x, y, (a<<16) + (a<<8) + a);
			}
		}
		Log.debug("edgeDetection(2): amin=%d   amax=%d", amin, amax);
		if (gradients != null) {
			//save gradients
			amin=Integer.MAX_VALUE; amax=Integer.MIN_VALUE;
			for (int y=0; y < dim.height; ++y ) {
				for (int x=0; x < dim.width; ++x) {
					int ax = (rx.getPixel(x, y)&0xff)-127;
					int ay = (ry.getPixel(x, y)&0xff)-127;
					double phi = Math.atan2(ay, ax); // phi = (-pi .. pi)
					int a = (int)(Math.round(255*Math.abs(phi)/Math.PI));
					//int a = (int)(Math.round(127*phi/Math.PI)+127);
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

	public static void edgeSobel(Raster2D r) {
		edgeSobel(r, null);
	}
	public static void edgeSobel(Raster2D r, Raster2D gradients) {
		MatrixI gx = new MatrixI(3,
				-1, 0, 1,
				-2, 0, 2,
				-1, 0, 1);
		MatrixI gy = new MatrixI(3,
				-1, -2, -1,
				 0,  0,  0,
				 1,  2,  1);
		edgeDetection(r, gx, gy, gradients);
	}

	public static void edgePrewitt(Raster2D r, Raster2D gradients) {
		MatrixI gx = new MatrixI(3,
				-1, 0, 1,
				-1, 0, 1,
				-1, 0, 1);
		MatrixI gy = new MatrixI(3,
				-1, -1, -1,
				 0,  0,  0,
				 1,  1,  1);
		edgeDetection(r, gx, gy, gradients);
	}

	public static void edgeSobelFeldman(Raster2D r, Raster2D gradients) {
		MatrixI gx = new MatrixI(3,
				 -3, 0,  3,
				-10, 0, 10,
				 -3, 0,  3);
		MatrixI gy = new MatrixI(3,
				-3, -10, -3,
				 0,   0,  0,
				 3,  10,  3);
		edgeDetection(r, gx, gy, gradients);
	}

	public static void edgeScharr(Raster2D r, Raster2D gradients) {
		MatrixI gx = new MatrixI(3,
				 -47, 0,  47,
				-162, 0, 162,
				 -47, 0,  47);
		MatrixI gy = new MatrixI(3,
				-47, -162, -47,
				 0,     0,   0,
				 47,  162,  47);
		edgeDetection(r, gx, gy, gradients);
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
