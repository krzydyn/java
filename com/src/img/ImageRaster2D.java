package img;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class ImageRaster2D extends Raster2D {
	private BufferedImage img;
	private final Dimension size;

	public ImageRaster2D(BufferedImage img) {
		size = new Dimension(img.getWidth(),img.getHeight());
		this.img = img;
	}
	public ImageRaster2D(int w, int h) {
		size = new Dimension(w, h);
		this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	}
	public ImageRaster2D(Raster2D r) {
		size = r.getSize();
		img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		for (int y=0; y < size.height; ++y) {
			for (int x=0; x < size.width; ++x) {
				img.setRGB(x, y, r.getPixel(x, y));
			}
		}
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
