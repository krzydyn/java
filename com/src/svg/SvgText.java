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

public class SvgText extends SvgObject {
	private final int x,y;
	private String txt="";
	public SvgText(int x,int y) {
		this.x=x; this.y=y;
	}
	public SvgText print(String t) {
		txt+=t;
		parent.updateSize(x+txt.length()*16, y+5);
		return this;
	}
	@Override
	public void write(PrintStream os) {
		os.printf("<text x=\"%d\" y=\"%d\" %s>%s</text>\n", x, y, props, txt);
	}

}
