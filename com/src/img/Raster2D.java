package img;

import java.awt.Dimension;
import java.io.Closeable;
import java.io.IOException;

public abstract class Raster2D implements Closeable {
	@Override
	public void close() throws IOException {
		dispose();
	}
	abstract public void dispose();
	abstract public Dimension getSize();
	abstract public int getPixel(int x,int y);
	public void setPixel(int x,int y, int v) {
		drawHline(x, x+1, y, v);
	}
	abstract public void drawHline(int x1, int x2, int y, int v);
	abstract public void assign(Raster2D r);
}
