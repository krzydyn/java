package img;

import java.awt.Dimension;
import sys.Log;

//Line equation Ax+By+C=0, M*(x-X0)-(y-y0) = 0
//Circle (x-X0)^2+(y-Y0)^2=R^2
//Ellipse equation M(x-X0)^2+(y-Y0)^2=R^2
//Parabola equation Ax^2+Bx+Cy+D=0

//https://www.uio.no/studier/emner/matnat/ifi/INF4300/h09/undervisningsmateriale/hough09.pdf
//http://www.cs.utah.edu/~vpegorar/courses/cs7966/Assignment4/
//http://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm
//TODO HoughTransform for variable number of params
//TODO Radon Transform
public class HoughLines {
	static private final int NUM_PHI = 200;
	static private final int NUM_R = 300;

	private final Raster2D img;
	private final float[] acc = new float[NUM_PHI*NUM_R]; //Hough transform accumulator
	float maxAcc=0f;
	float threshold = 0.01f;

	public HoughLines(Raster2D img) {
		this.img = img;
	}

	private void add(int nr, int nphi) {
		int d=3;
		for (int y=-d; y<=d; ++y) {
			if (nphi+y < 0 || nphi+y >= NUM_PHI) continue;
			for (int x=-d; x<=d; ++x) {
				if (nr+x < 0 || nr+y >= NUM_R) continue;
				acc[(nphi+y)*NUM_R+(nr+x)] += Math.exp(-(x*x+y*y));
			}
		}
		float a = acc[nphi*NUM_R+nr];
		if (maxAcc < a) maxAcc=a;
	}
	private void lineScan(int x, int y) {
		if (Colors.luminance(img.getPixel(x, y)) <= threshold) return ;

		int x0 = img.getSize().width/2;
		int y0 = img.getSize().height/2;
		double maxphi=Math.PI;
		double maxr = Math.hypot(img.getSize().width, img.getSize().height);
		for (int nphi = 0; nphi < NUM_PHI; ++nphi) {
			double phi = nphi*maxphi/NUM_PHI - maxphi/2.0;
			double r = (x-x0)*Math.cos(phi)+(y-y0)*Math.sin(phi)+maxr/2;
			//circle: r=sqrt((x-x0)^2+(y-y0)^2); for all a,b
			if (r >= 0 && r < maxr) {
				int nr = (int)Math.round(NUM_R*r/maxr);
				add(nr,nphi);
			}
		}
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

	public Raster2D toImage() {
		Log.debug("maxAcc = %.3f", maxAcc);
		Raster2D i = new ImageRaster2D(NUM_PHI, NUM_R);
		for (int nphi = 0; nphi < NUM_PHI; ++nphi) {
			for (int nr = 0; nr < NUM_R; ++nr) {
				float v = acc[nphi*NUM_R+nr];
				int a = Math.round(255*v/maxAcc);
				i.setPixel(nphi, nr, (a<<16) + (a<<8) + a);
			}
		}
		return i;
	}
}
