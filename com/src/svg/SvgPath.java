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

	private void checkOp(char type) {
		if (ops.size() > 0) {
			PathOp p1 = ops.get(ops.size()-1);
			if (p1.type=='Z' || p1.type=='z')
				throw new RuntimeException("invalid op");
			if (p1.type!='M' && p1.type!='L' && p1.type!='l')
				throw new RuntimeException("invalid op");
		}
		else if (type=='Z' || type=='z' || type=='m' || type=='L' || type=='l')
			throw new RuntimeException("invalid op");
	}

	public SvgPath moveTo(int x,int y) {
		checkOp('M');
		ops.add(new PathOp('M',x,y));
		return this;
	}
	public SvgPath moveRel(int dx,int dy) {
		checkOp('m');
		ops.add(new PathOp('m',dx,dy));
		return this;
	}
	public SvgPath lineTo(int x,int y) {
		checkOp('L');
		if (ops.size() > 1) {
			PathOp p1 = ops.get(ops.size()-1);
			PathOp p2 = ops.get(ops.size()-2);
			if (p1.type == 'L' && (p2.type=='L' || p2.type=='M')) {
				int dx1,dy1,dx2,dy2;
				dx1=x-p1.data[0];
				dy1=y-p1.data[1];
				dx2=x-p2.data[0];
				dy2=y-p2.data[1];
				if (dx1*dy2 == dx2*dy1) {
					p1.data[0]=x;
					p1.data[1]=y;
					return this;
				}
			}
		}
		ops.add(new PathOp('L',x,y));
		return this;
	}
	public SvgPath lineRel(int dx,int dy) {
		checkOp('l');
		ops.add(new PathOp('l',dx,dy));
		return this;
	}
	public SvgPath curveTo(int x,int y) {
		checkOp('T');
		ops.add(new PathOp('T',x,y));
		return this;
	}
	public SvgPath closePath() {
		checkOp('Z');
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
		os.printf("\"%s/>\n", props);
	}
}
