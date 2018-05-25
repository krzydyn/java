package img;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class Text2D {
	void textOnPath(Graphics2D g, Shape shape, String text, Font font) {
		if (text.length() == 0) return ;
		FontRenderContext frc = new FontRenderContext(null, true, true);
		GlyphVector glyphVector = font.createGlyphVector(frc, text);
		int length = glyphVector.getNumGlyphs();
		if (length == 0) return ;

		AffineTransform t = new AffineTransform();
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1f);
		float lastX=0f;
		float lastY=0f;
		//PathIterator it = shape.getPathIterator(null);
		for (int curr=0; curr < length && !it.isDone(); ++curr) {
			Shape glyph = glyphVector.getGlyphOutline(curr);
			Point2D p = glyphVector.getGlyphPosition(curr);
			float x = lastX + 0f;
			float y = lastY + 0f;
			float dx = x - lastX;
			float dy = y - lastY;
			float angle = (float)Math.atan2( dy, dx );
			float advance = glyphVector.getGlyphMetrics(curr+1).getAdvance();
			t.setToTranslation(x, y);
			t.rotate (angle);
			t.translate(-p.getX() - advance, -p.getY() );
			g.fill(glyph);
			lastX=x; lastY=y;
		}
	}
}
