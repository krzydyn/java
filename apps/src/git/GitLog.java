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

package git;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import svg.Svg;
import sys.Log;

public class GitLog {
	static final String[] ArrayOfString0 = new String[0];
	//static GitRepo repo = new GitRepo("~/sec-os/secos");
	static GitRepo repo = new GitRepo("~/tmp/linux");
	//static GitRepo repo = new GitRepo("~/tmp/nuclear-js");
	//static String branch = "origin/master";
	static String branch = "2cde51fbd0f3"; //linux octopus commit
	//static String branch ="7c4c62a";
	static int limit = 8000;
	//static String branch = "origin/devel/k.debski/openssl-20160801";
	//static String branch = "origin/devel/anchit/gatekeeper";

	public static void main(String[] args) {
		GitGraph graph = new GitGraph(repo);
		Svg svg = graph.buildSvg(branch, limit);

		Log.notice("Writing SVG to file");
		try (OutputStream os=new FileOutputStream("git.html")) {
			svg.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.notice("Writing SVG done");
	}

}
