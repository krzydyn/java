/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.charset.Charset;

import sys.Env;

public class IOText extends AbstractSelectableChannel implements Readable,Appendable,Closeable {
	final Reader rd;
	final Writer wr;
	public IOText(InputStream i, OutputStream o) {
		super(null);
		InputStreamReader is = null;
		OutputStreamWriter os = null;
		if (i!=null) is = new InputStreamReader(i, Env.UTF8);
		if (o!=null) os = new OutputStreamWriter(o, Env.UTF8);
		this.rd=is;
		this.wr=os;

	}
	public void write(CharSequence csq) throws IOException {
		wr.write(csq.toString());
	}
	public void write(String str) throws IOException {
		wr.write(str);
	}
	public void write(String str, int off, int len) throws IOException {
		wr.write(str, 0, len);
	}
	public void write(char cbuf[], int off, int len) throws IOException {
		wr.write(cbuf, off, len);
	}
	@Override
	public Appendable append(CharSequence csq) throws IOException {
		wr.write(csq.toString());
		return this;
	}
	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		wr.write(csq.subSequence(start, end).toString());
		return this;
	}
	@Override
	public Appendable append(char c) throws IOException {
		wr.write(c);
		return this;
	}

	@Override
	public int read(CharBuffer cb) throws IOException {
		return rd.read(cb);
	}
	public int read(char cbuf[], int off, int len) throws IOException {
		return rd.read(cbuf, off, len);
	}
	public int read() throws IOException {
		return rd.read();
	}
	public int read(char cbuf[]) throws IOException {
		return rd.read(cbuf, 0, cbuf.length);
	}
	@Override
	protected void implCloseSelectableChannel() throws IOException {
		if (rd!=null) rd.close();
		if (wr!=null) wr.close();
	}
	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
	}
	@Override
	public int validOps() {
		return SelectionKey.OP_READ|SelectionKey.OP_WRITE;
	}

	public static void save(File f, CharSequence data) throws IOException {
		try (IOText io = new IOText(null, new FileOutputStream(f))) {
			io.write(data);
			io.close();
		}
	}
	public static CharSequence load(File f) throws IOException {
		StringBuilder s = new StringBuilder();
		int r;
		try (IOText io = new IOText(new FileInputStream(f), null)) {
			char data[] = new char[1024];
			while ((r = io.read(data)) > 0) {
				s.append(data, 0, r);
			}
			io.close();
		}
		return s;
	}

	public static long transfer(FileChannel src, WritableByteChannel target) throws IOException {
		return src.transferTo(0, src.size(), target);
	}
	public static long transfer(ReadableByteChannel src, FileChannel target) throws IOException {
		return target.transferFrom(src, 0, Long.MAX_VALUE);
	}

	//https://javapapers.com/java/java-nio-file-read-write-with-channels/
	public static CharSequence read(FileChannel chn, long offs, long size) throws IOException {
		MappedByteBuffer buf = chn.map(MapMode.READ_ONLY, offs, size);
		Charset cs = Env.UTF8;
		return cs.decode(buf);
		/*
		StringBuilder s = new StringBuilder();
		ByteBuffer bb = ByteBuffer.allocate(256);
		for (long i = 0; i < size; i += bb.limit()) {
			if (chn.read(bb) <= 0) break;
			bb.rewind();
			s.append(cs.decode(bb));
			bb.flip();
		}
		return s;
		 */
	}
	public static void write(FileChannel chn, long offs, CharSequence s) throws IOException {
		MappedByteBuffer buf = chn.map(MapMode.READ_WRITE, offs, s.length());
		Charset cs = Env.UTF8;
		buf.put(s.toString().getBytes(cs));
		buf.force();
	}
}
