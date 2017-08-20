package img;

import java.awt.Dimension;

public class BitRaster2D extends Raster2D {
	private final Dimension size;
	boolean[] data;
	public BitRaster2D(int w, int h) {
		size = new Dimension(w, h);
		data = new boolean[w*h];
	}
	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public int getPixel(int x, int y) {
		return data[y*size.width+x] ? 1 : 0;
	}

	@Override
	public void drawHline(int x1, int x2, int y, int v) {
		boolean b = v!=0 ? true : false;
		for (int x=x1; x < x2; ++x) data[y*size.width+x]=b;
	}
}
