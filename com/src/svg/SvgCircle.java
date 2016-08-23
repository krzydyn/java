package svg;

import java.io.PrintStream;

public class SvgCircle  extends SvgObject {
	final int cx,cy,r;
	public SvgCircle(int cx,int cy,int r) {
		this.cx=cx; this.cy=cy; this.r=r;
	}
	@Override
	public void write(PrintStream os) {
		os.printf("<circle cx=\"%d\" cy=\"%d\" r=\"%d\"%s/>\n",cx,cy,r,props);
	}
}
