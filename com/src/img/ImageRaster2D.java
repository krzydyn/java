package img;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageRaster2D extends Raster2D {
	private BufferedImage img;
	private final Dimension size;
	private Graphics2D g2;

	public ImageRaster2D(BufferedImage img) {
		size = new Dimension(img.getWidth(),img.getHeight());
		this.img = img;
		g2=null;
	}
	public ImageRaster2D(int w, int h) {
		this(new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB));
	}
	public ImageRaster2D(Dimension size) {
		this(size.width,size.height);
	}
	public ImageRaster2D(Raster2D r) {
		this(r.getSize());
		for (int y=0; y < size.height; ++y) {
			for (int x=0; x < size.width; ++x) {
				img.setRGB(x, y, r.getPixel(x, y));
			}
		}
	}

	@Override
	public void dispose() {
		if (g2 != null) {
			g2.dispose();
			g2 = null;
		}
		img = null;
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
		if (g2 == null) g2 = img.createGraphics();
		g2.setColor(new Color(v));g2.drawLine(x1, y, x2-1, y);
		//for (int x=x1; x < x2; ++x) img.setRGB(x, y, v);
	}
	public Image getImage() {
		return img;
	}

}
