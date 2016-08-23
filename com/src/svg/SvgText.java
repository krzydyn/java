package svg;

import java.io.PrintStream;

public class SvgText extends SvgObject {
	private final int x,y;
	private String txt="";
	public SvgText(int x,int y) {
		this.x=x; this.y=y;
	}
	public SvgText print(String t) {
		txt+=t;
		parent.updateSize(x+t.length()*16, y+5);
		return this;
	}
	@Override
	public void write(PrintStream os) {
		os.printf("<text x=\"%d\" y=\"%d\" %s>%s</text>\n", x, y, props, txt);
	}

}
