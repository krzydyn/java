package img;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class ImageRaster2D extends Raster2D {
	private BufferedImage img;
	private final Dimension size;

	public ImageRaster2D(BufferedImage img) {
		this.img = img;
		size = new Dimension(img.getWidth(),img.getHeight());
	}
	@Override
	public void dispose() {
		img=null;
	}

	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public int getPixel(int x, int y) {
		return img.getRGB(x, y);
	}

	@Override
	public void drawHline(int x1, int x2, int y, int v) {
		for (int x=x1; x < x2; ++x) img.setRGB(x, y, v);
	}

}
