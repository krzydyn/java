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

import java.io.PrintStream;

public class SvgText extends SvgContainer {
	private int cx,cy;
	static class SvgRawText extends SvgObject {
		private String txt=null;
		SvgRawText(String t) {txt=t;}
		@Override
		public void write(PrintStream p) {
			p.print(SvgObject.escapeXmlEntity(txt));
		}
	}

	public SvgText(int x,int y) {
		super("text");
		setX(x); setY(y);
		cx=x;cy=y;
	}
	public SvgText addText(String t) {
		if (t != null && t.length() > 0) {
			objs.add(new SvgRawText(t));
			updateSize(cx+t.length()*7, cy+10);
			cx += t.length()*7;
		}
		return this;
	}
	public SvgTSpan tspan() {
		return new SvgTSpan(cx,cy);
	}
}
