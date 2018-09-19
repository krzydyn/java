package img;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import sys.Log;

public class Text2D {
	static public float pathLength(Shape shape) {
		float moveX = 0f, moveY = 0f;
		float lastX = 0f, lastY = 0f;
		float coords[] = new float[6];
		float length = 0f;

		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1f);
		for (; !it.isDone(); it.next()) {
			int type = it.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				moveX = lastX = coords[0];
				moveY = lastY = coords[1];
				break;
			case PathIterator.SEG_CLOSE:
				coords[0] = moveX;
				coords[1] = moveY;
				//fall...
			case PathIterator.SEG_LINETO:
				float dx = coords[0]-lastX;
				float dy = coords[1]-lastY;
				length += (float)Math.sqrt(dx*dx + dy*dy);
				lastX = coords[0];
				lastY = coords[1];
				break;
			default:
				break;
			}
		}
		return length;
	}

	static public float glyphLength(GlyphVector glyphVector) {
		float length = 0f;
		for (int i=0; i < glyphVector.getNumGlyphs(); ++i) {
			length += glyphVector.getGlyphMetrics(i).getAdvance();
		}
		return length;
	}

	// based on https://www.programcreek.com/java-api-examples/?code=Fivium/FOXopen/FOXopen-master/src/main/java/net/foxopen/fox/spatial/renderer/TextStroke.java#
	// http://www.jhlabs.com/java/java2d/strokes/
	static public void textOnPath(Graphics2D g, Shape shape, String text, Font font, float align) {
		if (text.length() == 0) return ;
		FontRenderContext frc = new FontRenderContext(null, true, true);
		GlyphVector glyphVector = font.createGlyphVector(frc, text);
		int nGlyphs = glyphVector.getNumGlyphs();
		if (nGlyphs == 0) return ;
		Log.debug("Draw text on Path, glyphs = %d", nGlyphs);

		RenderingHints rh = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHints(rh);

		float coords[] = new float[6];
		float moveX = 0f, moveY = 0f;
		float lastX = 0f, lastY = 0f;
		float nextCharOffs = 0f;

		if (align > 0f && align <= 1.0f) {
			float pl = pathLength(shape);
			float gl = glyphLength(glyphVector);
			nextCharOffs = align * (pl - gl);
		}

		g.draw(shape);
		g.setColor(Color.GREEN);
		AffineTransform st = g.getTransform();
		AffineTransform t = new AffineTransform();
		PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 1f);
		for (int curr = 0; curr < nGlyphs && !it.isDone(); it.next()) {
			int type = it.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				moveX = lastX = coords[0];
				moveY = lastY = coords[1];
				//nextCharOffs = 0;
				break;
			case PathIterator.SEG_CLOSE:
				coords[0] = moveX;
				coords[1] = moveY;
				//fall...
			case PathIterator.SEG_LINETO:
				float dx = coords[0]-lastX;
				float dy = coords[1]-lastY;
				float distance = (float)Math.sqrt(dx*dx + dy*dy);
				g.fill(new Ellipse2D.Double(coords[0]-1, coords[1]-1, 2, 2));
				Log.debug("Line to %.2f %.2f  (from %.3f %.2f)  dist = %.2f", coords[0], coords[1], lastX, lastY, distance);
				if (nextCharOffs < distance) {
					float r = 1.0f/distance;
					float angle = (float)Math.atan2(dy, dx);
					for (; curr < nGlyphs && nextCharOffs <= distance; ++curr) {
						Shape glyph = glyphVector.getGlyphOutline(curr);
						Point2D p = glyphVector.getGlyphPosition(curr);
						float advance = glyphVector.getGlyphMetrics(curr).getAdvance();
						Log.debug("   %d adv = %.2f", curr, advance);
						float x = lastX + nextCharOffs * dx * r;
						float y = lastY + nextCharOffs * dy * r;
						t.setToTranslation(x, y);
						t.rotate (angle);
						t.translate(-p.getX(), -p.getY());
						g.setTransform(t);
						g.fill(glyph);
						g.setTransform(st);
						nextCharOffs += advance;
					}
				}
				nextCharOffs -= distance;
				lastX = coords[0];
				lastY = coords[1];
				break;
			default:
				Log.debug("Wrong Segment type " + type);
				break;
			}
		}
		g.setTransform(st);
	}
}
