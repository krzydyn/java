package image;

import img.HoughLines;
import img.ImageRaster2D;
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
			Tools2D.edgeSobel(r);
		}
	}
	static public class GaussTool extends Tool {
		void filter(Raster2D r) {
			Tools2D.smoothGauss(r);
		}
	}

	static public class GradientTool extends Tool {
		void filter(Raster2D r) {
			ImageRaster2D ir = new ImageRaster2D(r);
			Tools2D.edgeSobel(ir, r);
			ir.dispose();
		}
	}

	static public class HoughTool extends Tool {
		Raster2D transform(Raster2D r) {
			HoughLines h = new HoughLines(r);
			h.transform();
			return h.toImage();
		}
	}
}
