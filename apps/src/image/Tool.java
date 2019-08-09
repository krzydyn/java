package image;

import img.HoughLines;
import img.ImageRaster2D;
import img.Raster2D;
import img.Tools2D;

public abstract class Tool {
	abstract void transform(Raster2D src, Raster2D dst);

	static public class LumaTool extends Tool {
		@Override
		void transform(Raster2D src, Raster2D dst) {
			Tools2D.luminance(src);
		}
	}
	static public class EdgeTool extends Tool {
		@Override
		void transform(Raster2D src, Raster2D dst) {
			//Tools2D.edgeSobel(src);
			Tools2D.edgeScharr(src, null);
		}
	}
	static public class GaussTool extends Tool {
		@Override
		void transform(Raster2D src, Raster2D dst) {
			Tools2D.smoothGauss(src);
		}
	}

	static public class GradientTool extends Tool {
		@Override
		void transform(Raster2D src, Raster2D dst) {
			ImageRaster2D ir = new ImageRaster2D(src);
			Tools2D.edgeSobel(ir, src);
			ir.dispose();
		}
	}

	static public class HoughTool extends Tool {
		@Override
		void transform(Raster2D src, Raster2D dst) {
			HoughLines h = new HoughLines(src);
			h.transform();
			dst.assign(h.toImage());
		}
	}
}
