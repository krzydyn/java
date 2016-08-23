package svg;

import java.awt.Rectangle;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SvgPath extends SvgObject {
	static class PathOp {
		public PathOp(char t,int ...data) {this.type=t;this.data=data;}
		final char type;
		final int[] data;
	}

	private List<PathOp> ops = new ArrayList<PathOp>();

	public SvgPath moveTo(int x,int y) {
		ops.add(new PathOp('M',x,y));
		return this;
	}
	public SvgPath moveRel(int dx,int dy) {
		ops.add(new PathOp('m',dx,dy));
		return this;
	}
	public SvgPath lineTo(int x,int y) {
		ops.add(new PathOp('L',x,y));
		return this;
	}
	public SvgPath lineRel(int dx,int dy) {
		ops.add(new PathOp('l',dx,dy));
		return this;
	}
	public SvgPath curveTo(int x,int y) {
		ops.add(new PathOp('T',x,y));
		return this;
	}
	public SvgPath closePath() {
		ops.add(new PathOp('Z')); //same as 'z'
		return this;
	}

	public Rectangle getBounds() {
		Rectangle r = new Rectangle();
		for (PathOp op : ops) {
			for (int i=0; i < op.data.length; i+=2) {
				int x=op.data[i], y=op.data[i+1];
				if (r.width==0) r.setBounds(x, y, 1, 1);
				else r.add(x, y);
			}
		}
		return r;
	}
	@Override
	public void write(PrintStream os) {
		os.printf("<path d=\"");
		for (PathOp op : ops) {
			os.print(op.type);
			for (int v : op.data) os.printf("%d ",v);
		}
		os.printf("\"%s\"/>\n", props);
	}
}
