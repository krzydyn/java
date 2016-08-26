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

import java.awt.Point;
import java.awt.geom.Point2D;

import algebra.Lines;
import algebra.Lines.IntersectionType;
import algebra.Maths;
import sys.Log;
import sys.UnitTest;

public class T_Lines extends UnitTest {

	static void power() {
		Log.debug("pow=%d",Maths.power(3, 10));
		check(Maths.power(3, 10)==59049, "wrong power");
	}
	static void intersectInt() {
		Point[] p = {new Point(-3,0),new Point(3,0),new Point(0,2),new Point(-4,2)};
		Point2D r = new Point2D.Float();
		IntersectionType t;

		while (p[3].y>-4) {
			t = Lines.intersection(p[0], p[1], p[2], p[3], r);
			Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
			++p[3].x;
			p[3].y-=2;
		}
		while (p[3].x<4) {
			t = Lines.intersection(p[0], p[1], p[2], p[3], r);
			Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
			++p[3].x;
			++p[3].y;
		}

		p[2].x=p[0].x;
		p[2].y=p[0].y;
		p[3].x=p[1].x;
		p[3].y=p[1].y;
		t = Lines.intersection(p[0], p[1], p[2], p[3], r);
		Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
		p[3].x=p[1].x-1;
		t = Lines.intersection(p[0], p[1], p[2], p[3], r);
		Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
		p[3].x=p[1].x+5;
		t = Lines.intersection(p[0], p[1], p[2], p[3], r);
		Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
		p[2].x=p[1].x;
		t = Lines.intersection(p[0], p[1], p[2], p[3], r);
		Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
		p[2].x=p[1].x+1;
		t = Lines.intersection(p[0], p[1], p[2], p[3], r);
		Log.debug("{%s %s} {%s %s} : %s at %s", p[0], p[1], p[2], p[3], t.name(), r);
	}
}
