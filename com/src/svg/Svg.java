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

public class Svg extends SvgContainer {

	public Svg() {
		super("svg");
	}

	@Override
	public void write(PrintStream p) {
		props += String.format(" width=\"%d\" height=\"%d\"",width,height);
		//props += " xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"";
		super.write(p);
	}
}
