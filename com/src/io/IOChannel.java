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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

import text.Text;

public class IOChannel implements Readable,Appendable,Closeable {
	final Reader rd;
	final Writer wr;
	public IOChannel(InputStream i, OutputStream o) {
		this.rd=new InputStreamReader(i, Text.UTF8_Charset);
		this.wr=new OutputStreamWriter(o, Text.UTF8_Charset);
	}
	@Override
	public void close() throws IOException {
		rd.close();
		wr.close();
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
		if (csq == null)
            wr.write("null");
        else
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
}
