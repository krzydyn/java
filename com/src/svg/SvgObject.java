package svg;

import java.io.OutputStream;
import java.io.PrintStream;

public abstract class SvgObject {
	protected SvgContainer parent;
	protected String props="";

	public SvgObject fill(String c) {
		props+= String.format(" fill=\"%s\"",c);
		return this;
	}
	public SvgObject stroke(String c) {
		props+= String.format(" stroke=\"%s\"",c);
		return this;
	}
	public SvgObject strokeWidth(int w) {
		props+= String.format(" stroke-width=\"%d\"",w);
		return this;
	}
	//public abstract Rectangle getBounds();
	public abstract void write(PrintStream p);

	final public void write(OutputStream os){
		write(new PrintStream(os));
	}
}
