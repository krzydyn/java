package image;

import img.HoughLines;
import img.ImageRaster2D;
import img.Raster2D;
import img.Tools2D;

public abstract class Tool {
	abstract void transform(Raster2D src, Raster2D dst);
	
	static public class LumaTool extends Tool {
		void transform(Raster2D src, Raster2D dst) {
			Tools2D.luminance(src);
		}
	}
	static public class EdgeTool extends Tool {
		void transform(Raster2D src, Raster2D dst) {
			Tools2D.edgeSobel(src);
		}
	}
	static public class GaussTool extends Tool {
		void transform(Raster2D src, Raster2D dst) {
			Tools2D.smoothGauss(src);
		}
	}

	static public class GradientTool extends Tool {
		void transform(Raster2D src, Raster2D dst) {
			ImageRaster2D ir = new ImageRaster2D(src);
			Tools2D.edgeSobel(ir, src);
			ir.dispose();
		}
	}

	static public class HoughTool extends Tool {
		void transform(Raster2D src, Raster2D dst) {
			HoughLines h = new HoughLines(src);
			h.transform();
			dst.assign(h.toImage());
		}
	}
}
