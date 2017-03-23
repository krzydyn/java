package algebra;

import java.awt.Dimension;

public interface Raster2D {
	public int getPixel(int x,int y);
	public void drawHline(int x1, int x2,int y, int v);
	public Dimension getSize();
}
