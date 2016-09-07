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
	//static GitRepo repo = new GitRepo("~/tmp/nuclear-js");
	//static String branch = "origin/master";

	//static GitRepo repo = new GitRepo("~/tmp/linux");
	//static String branch = "2cde51fbd0f3"; //linux octopus commit
	//static String branch ="7c4c62a";

	//static GitRepo repo = new GitRepo("~/sec-os/secos");
	//static String branch = "--all";
	//static String branch = "origin/devel/k.debski/openssl-20160801";
	//static String branch = "origin/devel/anchit/gatekeeper";
	static int limit = 8000;

	private static void usage() {
		Log.debug("debug version");
		System.out.println("Usage is: ");
		System.out.println("java -jar gitsvg [-l line-height] <path-to-repo> <branch-or-commit>");
		System.exit(1);
	}
	public static void main(String[] args) {
		int dy=43;
		String fmt="";
		String ofile="git.svg";

		int argi=0;
		for (; argi < args.length; ++argi) {
			if (!args[argi].startsWith("-")) break;
			if (args[argi].equals("-l")) {
				++argi;
				dy = Integer.parseInt(args[argi]);
			}
			else if (args[argi].equals("-f")) {
				++argi;
				fmt=args[argi];
			}
			else if (args[argi].equals("-o")) {
				++argi;
				ofile=args[argi];
			}
		}
		if (args.length < argi+2) {
			usage();
		}

		String repo_path = args[argi];
		String branch = args[argi+1];

		GitGraph graph = new GitGraph(new GitRepo(repo_path));
		graph.setUserFormat(fmt);
		Svg svg = graph.buildSvg(branch, dy, limit);

		Log.notice("Writing SVG to file");
		try (OutputStream os=new FileOutputStream(ofile)) {
			svg.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.notice("Writing SVG done");
	}

}
