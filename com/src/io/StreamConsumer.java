package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import sys.Env;

public class StreamConsumer implements Runnable {
	private InputStream in;
	private StringBuilder str = new StringBuilder();

	public StreamConsumer(InputStream is) {
		this.in = is;
	}

	public String getOutput() { return str.toString().trim(); }

	@Override
	public void run() {
		BufferedReader x;
		char[] buf = new char[1024];
		InputStreamReader isr = new InputStreamReader(in, Env.UTF8_Charset);
		int r;
		try {
			while ((r=isr.read(buf)) >= 0) {
				str.append(buf, 0, r);
			}
		} catch (IOException e) {
		}
		Env.close(isr);
	}

	public void close() throws IOException {
		if (in == null) return ;
		in.close();
		in = null;
	}
}
