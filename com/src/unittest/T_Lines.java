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

package unittest;

import img.Lines;
import img.Lines.IntersectionType;

import java.awt.Point;
import java.awt.geom.Point2D;

import algebra.Maths;
import sys.Colors;
import sys.Log;
import sys.UnitTest;

public class T_Lines extends UnitTest {

	static void colors() {
		int a = Colors.quick_luminance(0);
		int b = Colors.quick_luminance(0xffffff);
		check("black",0,a);
		check("while",223,b);
	}
	static void power() {
		Log.debug("pow=%d",Maths.power(3, 10));
		check("wrong power", Maths.power(3, 10)==59049);
	}
	static void intersectInt() {
		Point[] p = {new Point(-3,0),new Point(3,0),new Point(0,2),new Point(-4,2)};
		Point2D r = new Point2D.Float();
		IntersectionType[] expectedType = {
				IntersectionType.PARALLEL,
				IntersectionType.TOUCH,
				IntersectionType.CROSS_IN,
				IntersectionType.CROSS_IN,
				IntersectionType.CROSS_IN,
				IntersectionType.CROSS_IN,
				IntersectionType.CROSS_IN,
				IntersectionType.TOUCH,
				IntersectionType.COLINEAR,
		};

		IntersectionType t;
		int e=0;
		while (p[3].y>-4) {
			t = Lines.intersection(p[0], p[1], p[2], p[3], r);
			//Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
			check("Wrong type " + t.name() + "!=" + expectedType[e].name(), t == expectedType[e]); ++e;
			++p[3].x;
			p[3].y-=2;
		}
		while (p[3].x<4) {
			t = Lines.intersection(p[0], p[1], p[2], p[3], r);
			check("Wrong type " + t.name() + "!=" + expectedType[e].name(), t == expectedType[e]); ++e;
			//Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
			++p[3].x;
			++p[3].y;
		}

	}
}
