package img;

import java.awt.Dimension;
import java.awt.geom.Point2D;

//https://www.uio.no/studier/emner/matnat/ifi/INF4300/h09/undervisningsmateriale/hough09.pdf

public class HoughLinear {
	static private final int NUM_PHI = 200;
	static private final int NUM_R = 200;

	private final Raster2D img;
	private float[] acc = new float[NUM_PHI*NUM_R];
	private int[] lacc = new int[NUM_PHI];

	public HoughLinear(Raster2D img) {
		this.img = img;
	}

	private boolean lineEvidence(int x0, int y0, Point2D rp) {
		for (double phi = 0; phi < Math.PI; phi += Math.PI/NUM_PHI) {

		}
		return false;
	}
	private void add(Point2D rp) {
		float phi = (float)rp.getX();
		float r = (float)rp.getY();
		if (r < 0) return ;
	}
	public void transform() {
		for (int i=0; i < acc.length; ++i) acc[i]=0f;
		Dimension size = img.getSize();
		Point2D rp = new Point2D.Double();
		for (int y = 0; y < size.height; ++y) {
			for (int x = 0; x < size.width; ++x) {
				if (lineEvidence(x,y,rp)) add(rp);
			}
		}
	}
}
