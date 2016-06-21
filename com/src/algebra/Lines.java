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

import java.awt.Point;
import java.awt.geom.Point2D;

import sys.Log;

public class Lines {
	public static double EPS = 10e-10;
	public enum IntersectionType {
		OVERLAID,
		COLINEAR,
		PARALLEL,
		TOUCH,
		CROSS_IN,
		CROSS_OUT
	}

	/**
	 * <pre> x = x1 + ua (x2 - x1)
	 * y = y1 + ua (y2 - y1)</pre>
	 *
	 * @return IntersectionType
	 */
	public static IntersectionType intersetion(Point2D p1, Point2D p2, Point2D p3, Point2D p4, Point2D r) {
		if (r!=null) r.setLocation(0,0);

		double d = (p4.getY()-p3.getY())*(p2.getX()-p1.getX()) - (p4.getX()-p3.getX())*(p2.getY()-p1.getY());
		double a = (p4.getX()-p3.getX())*(p1.getY()-p3.getY()) - (p4.getY()-p3.getY())*(p1.getX()-p3.getX());
		double b = (p2.getX()-p1.getX())*(p1.getY()-p3.getY()) - (p2.getY()-p1.getY())*(p1.getX()-p3.getX());

		if (d == 0.0) {
			if (a == 0.0 && b == 0.0) {
				return IntersectionType.COLINEAR;
			}
			return IntersectionType.PARALLEL;
		}

		double ua = a/d;
		 //double ub = b/d;
		if (r!=null) {
			r.setLocation(
					p1.getX() + ua*(p2.getX() - p1.getX()),
					p1.getY() + ua*(p2.getY() - p1.getY()));
		}

		if (ua < -EPS || ua > 1.0+EPS) return IntersectionType.CROSS_OUT;
		if (Math.abs(ua) < EPS || Math.abs(ua-1.0) < EPS) return IntersectionType.TOUCH;

		//java.awt.geom.Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
		return IntersectionType.CROSS_IN;
	}

	public static IntersectionType intersection(Point p1, Point p2, Point p3, Point p4, Point2D r) {
		if (r!=null) r.setLocation(0,0);

		int d = (p4.y-p3.y)*(p2.x-p1.x) - (p4.x-p3.x)*(p2.y-p1.y);
		int a = (p4.x-p3.x)*(p1.y-p3.y) - (p4.y-p3.y)*(p1.x-p3.x);
		int b = (p2.x-p1.x)*(p1.y-p3.y) - (p2.y-p1.y)*(p1.x-p3.x);

		if (d == 0) {
			if (a == 0 && b == 0) {
				a = p2.x-p1.x;
				return IntersectionType.COLINEAR;
			}
			return IntersectionType.PARALLEL;
		}

		double ua = ((double)a)/d;
		double ub = ((double)b)/d;
		Log.debug("ua = %.3f,  ub = %.3f", ua, ub);

		if (r!=null) {
			r.setLocation(p1.x + ua*(p2.x - p1.x),p1.y + ua*(p2.y - p1.y));
		}

		if (ua < -EPS || ua > 1.0+EPS) return IntersectionType.CROSS_OUT;
		if (Math.abs(ua) < EPS || Math.abs(ua-1.0) < EPS) return IntersectionType.TOUCH;

		return IntersectionType.CROSS_IN;
	}
}
