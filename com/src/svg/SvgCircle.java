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

public class SvgCircle  extends SvgObject {
	final int cx,cy,r;
	public SvgCircle(int cx,int cy,int r) {
		this.cx=cx; this.cy=cy; this.r=r;
	}
	@Override
	public void write(PrintStream os) {
		os.printf("<circle cx=\"%d\" cy=\"%d\" r=\"%d\"%s/>\n",cx,cy,r,props);
	}
}
