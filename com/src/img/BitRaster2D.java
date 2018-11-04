package img;

import java.awt.Dimension;

public class BitRaster2D extends Raster2D {
	private Dimension size;
	boolean[] data;
	public BitRaster2D(int w, int h) {
		size = new Dimension(w, h);
		data = new boolean[w*h];
	}
	public BitRaster2D(Dimension size) {
		this(size.width,size.height);
	}
	public BitRaster2D(Raster2D r) {
		this(r.getSize());
		for (int y=0; y < size.height; ++y) {
			for (int x=0; x < size.width; ++x) {
				setPixel(x, y, r.getPixel(x, y));
			}
		}
	}
	@Override
	public void dispose() {
		data=null;
	}
	@Override
	public Dimension getSize() {
		return new Dimension(size);
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
	@Override
	public void assign(Raster2D r) {
		if (r instanceof BitRaster2D) {
			BitRaster2D br = (BitRaster2D) r;
			data = br.data;
			size = br.size;
		}
		else {
			assign(new BitRaster2D(r));
		}
	}
}
