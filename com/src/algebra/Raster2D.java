package algebra;

import java.awt.Dimension;

public abstract class Raster2D {
	abstract public Dimension getSize();
	abstract public int getPixel(int x,int y);
	public void setPixel(int x,int y, int v) {
		drawHline(x, x+1, y, v);
	}
	abstract public void drawHline(int x1, int x2, int y, int v);
}
