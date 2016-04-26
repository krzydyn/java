package unittest;

import graph.Lines;
import graph.Lines.IntersectionType;

import java.awt.Point;
import java.awt.geom.Point2D;

import sys.Log;
import sys.UnitTest;

public class T_Lines extends UnitTest {

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
