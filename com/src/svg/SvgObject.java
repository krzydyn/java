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

package svg;

import java.io.OutputStream;
import java.io.PrintStream;

public abstract class SvgObject {
	protected SvgContainer parent;
	protected String props="";

	public static String escapeXmlEntity(String t) {
		return t.replace("\"", "&quot;")
				.replace("&", "&amp;")
				.replace("'", "&apos;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	public SvgObject fill(String c) {
		props+= String.format(" fill=\"%s\"",c);
		return this;
	}
	public SvgObject stroke(String c) {
		props+= String.format(" stroke=\"%s\"",c);
		return this;
	}
	public SvgObject strokeWidth(int w) {
		props+= String.format(" stroke-width=\"%d\"",w);
		return this;
	}
	//public abstract Rectangle getBounds();
	public abstract void write(PrintStream p);

	final public void write(OutputStream os){
		write(new PrintStream(os));
	}
}
