/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;

import sys.Env;

public class IOText extends AbstractSelectableChannel implements Readable,Appendable,Closeable {
	final Reader rd;
	final Writer wr;
	public IOText(InputStream i, OutputStream o) {
		super(null);
		InputStreamReader is = null;
		OutputStreamWriter os = null;
		if (i!=null) is = new InputStreamReader(i, Env.UTF8_Charset);
		if (o!=null) os = new OutputStreamWriter(o, Env.UTF8_Charset);
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
		wr.write(csq.toString(), start, end);
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
		return 0;
	}

	public static void save(File f, CharSequence data) throws IOException {
		IOText io = new IOText(null, new FileOutputStream(f));
		io.write(data);
		io.close();
	}
}
