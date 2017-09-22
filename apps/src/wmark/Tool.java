package wmark;

import img.Raster2D;
import img.Tools2D;

public class Tool {
	static public class LumaTool extends Tool {
		void filter(Raster2D r) {
			Tools2D.luminance(r);
		}
	}
	static public class EdgeTool extends Tool {
		void filter(Raster2D r) {
			Tools2D.sobel(r);
		}
	}
	static public class GaussTool extends Tool {
		void filter(Raster2D r) {
			Tools2D.gauss(r);
		}
	}

}
