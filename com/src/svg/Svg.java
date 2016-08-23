package svg;

import java.io.PrintStream;

public class Svg extends SvgContainer {

	@Override
	public void write(PrintStream p) {
		p.printf("<svg width=\"%d\" height=\"%d\"\n",width,height);
		if (!props.isEmpty()) p.printf("\t%s\n", props);
		p.printf("\txmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
		for (SvgObject o : objs) o.write(p);
		p.printf("</svg>");
	}
}
