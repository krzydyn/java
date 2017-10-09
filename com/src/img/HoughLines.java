package img;

import java.awt.Dimension;

//https://www.uio.no/studier/emner/matnat/ifi/INF4300/h09/undervisningsmateriale/hough09.pdf
//TODO HoughTransfor for variable number of params
//TODO Radon Transform
public class HoughLines {
	static private final int NUM_PHI = 100;
	static private final int NUM_R = 100;

	private final Raster2D img;
	private float[] acc = new float[NUM_PHI*NUM_R]; //Hough transform accumulator
	private int[] lacc = new int[NUM_PHI];

	public HoughLines(Raster2D img) {
		this.img = img;
	}

	private void lineScan(int x0, int y0) {
		for (double phi = 0; phi < 2*Math.PI; phi += Math.PI/NUM_PHI) {
			for (float r = 0; r < 10; r+=0.1) {
				double x = x0 + r*Math.cos(phi);
				double y = y0 + r*Math.sin(phi);
			}
		}
	}
	private void add(float r, float phi) {
		if (r < 0) return ;

	}
	public void transform() {
		for (int i=0; i < acc.length; ++i) acc[i]=0f;
		Dimension size = img.getSize();
		for (int y = 0; y < size.height; ++y) {
			for (int x = 0; x < size.width; ++x) {
				lineScan(x,y);
			}
		}
	}
}
