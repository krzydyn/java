package svg;

import svg.SvgText.SvgRawText;

public class SvgTSpan extends SvgContainer {
	private int cx,cy;
	public SvgTSpan(int x,int y) {super("tspan");cx=y;cy=y;}
	public SvgTSpan addText(String t) {
		if (t != null && t.length() > 0) {
			objs.add(new SvgRawText(t));
			updateSize(cx+t.length()*7, cy+10);
			cx += t.length()*7;
		}
		return this;
	}
}
