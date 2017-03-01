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
import java.util.ArrayList;
import java.util.List;

public abstract class SvgContainer extends SvgObject {
	protected int width,height;
	protected List<SvgObject> objs = new ArrayList<SvgObject>();
	final protected String name;

	protected SvgContainer(String nm) {
		name = nm;
	}

	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public void updateSize(int w,int h) {
		if (parent!=null) parent.updateSize(w, h);
		if (width < w) width = w;
		if (height < h) height = h;
	}

	public SvgGroup group() {
		SvgGroup o = new SvgGroup();
		objs.add(o); o.parent=this;
		return o;
	}
	public SvgText text(int x,int y) {
		SvgText o = new SvgText(x,y);
		objs.add(o); o.parent=this;
		return o;
	}
	public SvgCircle circle(int cx,int cy,int r) {
		SvgCircle o = new SvgCircle(cx,cy,r);
		objs.add(o); o.parent=this;
		updateSize(cx+r+1, cy+r+1);
		return o;
	}
	public SvgPath path() {
		SvgPath o = new SvgPath();
		objs.add(o); o.parent=this;
		return o;
	}

	@Override
	public void write(PrintStream p) {
		p.printf("<%s%s>\n", name, props);
		for (SvgObject o : objs) o.write(p);
		p.printf("</%s>\n", name);
	}
}
