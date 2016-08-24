package svg;

import java.io.PrintStream;

public class Svg extends SvgContainer {

	public Svg() {
		super("svg");
	}

	@Override
	public void write(PrintStream p) {
		props += String.format(" width=\"%d\" height=\"%d\"",width,height);
		//props += " xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"";
		super.write(p);
	}
}
