package io;

import java.io.InputStream;
import java.io.StringWriter;

public class StreamCaptureWorker extends IOForwardWorker {
	private StringWriter wr;

	public StreamCaptureWorker(InputStream is) {
		super(is, new StringWriter());
		wr = (StringWriter) super.wr;
	}

	public String getOutput() {
		String s = null;
		synchronized (this) {
			while (!isDone()) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
			s = wr.getBuffer().toString().trim();
			wr.flush();
		}
		return s;
	}
}
