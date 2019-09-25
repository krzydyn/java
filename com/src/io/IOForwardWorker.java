package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import sys.Env;
import sys.Log;

public class IOForwardWorker implements Runnable {
	private InputStream is;
	protected OutputStream os;
	private Reader rd;
	protected Writer wr;
	private boolean done = true;

	public IOForwardWorker(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}
	public IOForwardWorker(InputStream is, Writer wr) {
		this.is = is;
		this.wr = wr;
	}
	public IOForwardWorker(Reader rd, OutputStream os) {
		this.rd = rd;
		this.os = os;
	}
	public IOForwardWorker(Reader rd, Writer wr) {
		this.rd = rd;
		this.wr = wr;
	}

	protected boolean isDone() { return done; }

	private void run_is2os() throws IOException {
		byte[] buf = new byte[1024];
		int r;
		while ((r=is.read(buf)) >= 0) {
			os.write(buf, 0, r);
		}
	}
	private void run_is2wr() throws IOException {
		char[] buf = new char[1024];
		int r;
		InputStreamReader isr = new InputStreamReader(is, Env.UTF8_Charset);
		while ((r=isr.read(buf)) >= 0) {
			wr.write(buf, 0, r);
		}
	}
	private void run_rd2os() throws IOException {
		char[] buf = new char[1024];
		int r;
		OutputStreamWriter osw = new OutputStreamWriter(os);
		while ((r=rd.read(buf)) >= 0) {
			osw.write(buf, 0, r);
		}
	}
	private void run_rd2wr() throws IOException {
		char[] buf = new char[1024];
		int r;
		while ((r=rd.read(buf)) >= 0) {
			wr.write(buf, 0, r);
		}
	}

	@Override
	public void run() {
		done = false;
		try {
			if (is != null) {
				if (os != null) run_is2os();
				else if (wr != null) run_is2wr();
			}
			else if (rd != null) {
				if (os != null) run_rd2os();
				else if (wr != null) run_rd2wr();
			}
			synchronized (this) {
				done = true;
				notify();
			}
		} catch (IOException e) {
			Log.error(e);
		}
	}

	public void close() throws IOException {
		if (is != null) {
			is.close();
			is = null;
		}
		if (os != null) {
			os.close();
			os = null;
		}
		if (rd != null) {
			rd.close();
			rd = null;
		}
		if (wr != null) {
			wr.close();
			wr = null;
		}
	}
}
