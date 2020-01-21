package pdf;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.itextpdf.kernel.geom.IShape;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Subpath;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.ClippingPathInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import sys.Env;
import sys.Log;
import text.Text;

public class PdfView {

	static void extractText(String src) throws Exception {
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(src));

		int n = pdfDoc.getNumberOfPages();
		for (int i = 0; i< n; ++i) {
			PdfPage p = pdfDoc.getPage(i+1);
			Log.debug("Page[%d]: %s", i + 1, p.toString());
			//Log.debug("content: %s", Text.vis(p.getContentBytes()));
			Log.debug("content: %s", PdfTextExtractor.getTextFromPage(p));
			if (i > 5) break;
		}

		pdfDoc.close();
	}
	static void extractImages(String src) throws Exception {
		PdfReader rd = new PdfReader(src);
		PdfDocument pdfDoc = new PdfDocument(rd);

		int n = pdfDoc.getNumberOfPdfObjects();
		for (int i = 0; i < n; ++i) {
			PdfObject o = pdfDoc.getPdfObject(i);
			if (o == null) { Log.debug("obj[%d] = null", i); continue; }
			if (o.isDictionary()) {
				Log.debug("pdf dictionary");
				continue;
			}
			if (o.isArray()) {
				Log.debug("pdf array");
				continue;
			}
			if (o.isStream()) {
				PdfStream st = (PdfStream)o;
				PdfObject pdfsub = st.get(PdfName.Subtype);
				if (pdfsub == null) continue;
				Log.debug("obj[%d] = %s, subtype = %s", i, o.getClass().getSimpleName(), pdfsub);
				if (pdfsub.toString().equals(PdfName.Image.toString())) {
					byte[] img;
					try { img = st.getBytes(true); } //true = DCTdecode
					catch (Exception e) { img = st.getBytes(false); }
					Log.debug("   Length = %d  %s", img.length, Text.hex(img, 0, 10));
					try (OutputStream os = new FileOutputStream(String.format("extracted_%d.img", i))) {
						os.write(img);
					}
				}
				else {
					Log.debug("stream obj: %s", pdfsub.toString());
				}
				continue;
			}
			Log.debug("obj[%d] = %s", o.getClass().getSimpleName());
		}
		pdfDoc.close();
	}

	static void processAnnotation(PdfPage p) {
		for (PdfAnnotation a : p.getAnnotations()) {
			Log.debug("annot: %s", a.getName());
		}
	}

	static HashMap<Integer, String> lines = new HashMap<>();
	static float PAGE_MARGIN_LEFT = 10;
	static float PAGE_WIDTH = 0;
	static float PAGE_HEIGHT = 0;
	static int PAGE_LINES = 100;
	static int PAGE_COLUMNS = 50;

	static int mapToColumn(float x) {
		return (int)(PAGE_COLUMNS*x/PAGE_WIDTH);
	}
	static int mapToColumn(float fw, float x) {
		float cols = PAGE_WIDTH / fw;
		return (int)(cols*x/PAGE_WIDTH);
	}
	static int mapToLine(float y) {
		return PAGE_LINES - (int)(PAGE_LINES*y/PAGE_HEIGHT);
	}

	static void processPdf(String src) throws Exception {
		PdfReader rd = new PdfReader(src);
		PdfDocument pdfDoc = new PdfDocument(rd);
		StringBuilder txt = new StringBuilder();

		IEventListener evlistener = new IEventListener() {
			Set<EventType> supp = new TreeSet<>();
			{
				for (EventType t : EventType.values())
					supp.add(t);
				//supp.add(EventType.RENDER_IMAGE);
				//supp.add(EventType.RENDER_TEXT);
			}

			@Override
			public Set<EventType> getSupportedEvents() {
				//return supp;
				return null; //all events
			}

			@Override
			public void eventOccurred(IEventData ev, EventType type) {
				if (ev == null) {
					//Log.debug("ev: %s, type: %s", ev, type!=null?type.toString():null);
				}
				else if (ev instanceof TextRenderInfo) {
					//Log.debug("ev: %s, type: %s", ev, type!=null?type.toString():null);
					TextRenderInfo info = (TextRenderInfo) ev;
					if (info.getText() != null) {
						Rectangle rect = info.getBaseline().getBoundingRectangle();
						String txt = info.getText();
						float x = rect.getLeft();
						float y = rect.getTop();
						int xpos = mapToColumn(x);
						int ypos = mapToLine(y);
						String s = lines.get(ypos);
						if (s == null) s = "";
						while (s.length() < xpos) s += " ";
						lines.put(ypos, s + txt);
					}
				}
				else if (ev instanceof ImageRenderInfo) {
					ImageRenderInfo info = (ImageRenderInfo)ev;
					//Log.debug("ev: %s, type: %s", ev, type!=null?type.toString():null);
					//PdfImageXObject img = info.getImage();
					Matrix m = info.getImageCtm();
					float x = m.get(Matrix.I31);
					float y = m.get(Matrix.I32);
					String txt = String.format("[IMAGE at (%.1f,%.1f)]", x, y);

					int xpos = mapToColumn(x);
					int ypos = mapToLine(y);
					String s = lines.get(ypos);
					if (s == null) s = "";
					while (s.length() < xpos) s += " ";
					lines.put(ypos, s + txt);
				}
				else if (ev instanceof ClippingPathInfo) {
					ClippingPathInfo info = (ClippingPathInfo) ev;
				}
				else if (ev instanceof PathRenderInfo) {
					PathRenderInfo info = (PathRenderInfo) ev;
					for (Subpath subpath : info.getPath().getSubpaths()) {
						for (IShape segment : subpath.getSegments()) {
							//Log.debug("    %s", segment.getBasePoints());
						}
					}
				}
				else {
					Log.debug("ev: %s, type: %s", ev, type!=null?type.toString():null);
				}

			}
		};

		PdfCanvasProcessor proc = new PdfCanvasProcessor(evlistener);

		//PdfDocumentContentParser parser = new PdfDocumentContentParser(pdfDoc);

		int n = pdfDoc.getNumberOfPages();
		for (int i = 0; i < n; ++i) {
			PdfPage p = pdfDoc.getPage(i+1);
			PAGE_WIDTH = p.getPageSize().getWidth();
			PAGE_HEIGHT = p.getPageSize().getHeight();
			lines.clear();

			//processAnnotation(p);
			proc.processPageContent(p);
			//parser.processContent(i+1, evlistener);

			txt.append(String.format("[Page %d]\n", i+1));
			for (int j = 0; j <= PAGE_LINES; ++j) {
				String ln = lines.get(j);
				if (ln != null) {
					txt.append(ln + "\n");
				}
			}
			if (i > 3) break;
			Log.debug("txt: %s", txt.toString());
			txt.setLength(0);
		}

		pdfDoc.close();
	}

	public static void main(String[] args) throws Exception {
		String src = Env.expandEnv("/home/k.dynowski/Documents/GlobalPlatform/GPD_TEE_Internal_API_Specification_v1.0.pdf");
		//extractText(src);
		//extractImages(src);
		processPdf(src);
	}

}
