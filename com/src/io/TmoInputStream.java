package io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TmoInputStream extends InputStream {
	final private ByteBuffer b=ByteBuffer.allocate(1);
	final FileChannel fc;
	TmoInputStream(InputStream is) {
		if (!(is instanceof FileInputStream)) throw new RuntimeException();
		FileInputStream fi = (FileInputStream)is;
		fc = fi.getChannel();
	}

	@Override
	public int read() throws IOException {
		b.clear();
		if (fc.read(b) < 0) return -1;
		return b.get(0);
	}

}
