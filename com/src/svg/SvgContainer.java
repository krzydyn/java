package svg;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SvgContainer extends SvgObject {
	protected int width,height;
	protected List<SvgObject> objs = new ArrayList<SvgObject>();
	final protected String name;

	protected SvgContainer(String nm) {
		name = nm;
	}
	public void updateSize(int w,int h) {
		if (parent!=null) parent.updateSize(w, h);
		if (width < w) width = w;
		if (height < h) height = h;
	}

	public SvgGroup group() {
		SvgGroup o = new SvgGroup();
		objs.add(o); o.parent=this;
		return o;
	}
	public SvgText text(int x,int y) {
		SvgText o = new SvgText(x,y);
		objs.add(o); o.parent=this;
		return o;
	}
	public SvgCircle circle(int cx,int cy,int r) {
		SvgCircle o = new SvgCircle(cx,cy,r);
		objs.add(o); o.parent=this;
		updateSize(cx+r+1, cy+r+1);
		return o;
	}
	public SvgPath path() {
		SvgPath o = new SvgPath();
		objs.add(o); o.parent=this;
		return o;
	}

	@Override
	public void write(PrintStream p) {
		p.printf("<%s%s>\n", name, props);
		for (SvgObject o : objs) o.write(p);
		p.printf("</%s>\n", name);
	}
}
