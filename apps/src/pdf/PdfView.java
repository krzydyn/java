package pdf;

import java.util.Set;
import java.util.TreeSet;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.ClippingPathInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;

import sys.Env;
import sys.Log;
import text.Text;

public class PdfView {

	static void extractText(String src) throws Exception {
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(src));

		int n = pdfDoc.getNumberOfPages();
		for (int i = 0; i< n; ++i) {
			PdfPage p = pdfDoc.getPage(i+1);
			Log.debug("Page[%d]: %s", i, p.toString());
			//Log.debug("content: %s", Text.vis(p.getContentBytes()));
			Log.debug("content: %s", PdfTextExtractor.getTextFromPage(p));
			if (i == 5) break;
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
				continue;
			}
			if (o.isArray()) {
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
				}
				continue;
			}
			Log.debug("obj[%d] = %s", o.getClass().getSimpleName());
		}
		pdfDoc.close();
	}

	static void processPdf(String src) throws Exception {
		PdfReader rd = new PdfReader(src);
		PdfDocument pdfDoc = new PdfDocument(rd);
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
				return supp;
			}

			@Override
			public void eventOccurred(IEventData ev, EventType type) {
				if (ev == null) {
					Log.debug("ev: null, type: %s", type!=null?type.toString():null);
				}
				else if (ev instanceof TextRenderInfo) {

				}
				else if (ev instanceof ClippingPathInfo) {

				}
				else if (ev instanceof PathRenderInfo) {

				}
				else {
					Log.debug("ev: %s, type: %s", ev!=null?ev.toString():null, type!=null?type.toString():null);
				}

			}
		};
		PdfDocumentContentParser p = new PdfDocumentContentParser(pdfDoc);
		int n = pdfDoc.getNumberOfPages();
		for (int i = 0; i< n; ++i) {
			p.processContent(n, evlistener);
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
