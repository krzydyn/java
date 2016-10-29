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
	static int limit = 800;

	private static void usage() {
		Log.debug("debug version");
		System.out.println("Usage is: ");
		System.out.println("java -jar gitsvg.jar [--line-height line-height] [--format fmt] [--svg <file>] [--log <file>] <path-to-repo> <branch-or-commit>");
		System.exit(1);
	}
	public static void main(String[] args) {
		int dy=20;
		String fmt="";
		String cmtfile=null;
		String svgfile="git.svg";

		int argi=0;
		for (; argi < args.length; ++argi) {
			if (!args[argi].startsWith("-")) break;
			if (args[argi].equals("--line-height")) {
				++argi;
				dy = Integer.parseInt(args[argi]);
			}
			else if (args[argi].equals("--format")) {
				++argi;
				fmt=args[argi];
			}
			else if (args[argi].equals("--svg")) {
				++argi;
				svgfile=args[argi];
			}
			else if (args[argi].equals("--log")) {
				++argi;
				cmtfile=args[argi];
			}
		}
		if (args.length < argi+2) {
			usage();
		}

		String repo_path = args[argi];
		String branch = args[argi+1];

		GitGraph graph = new GitGraph(new GitRepo(repo_path));
		graph.setUserFormat(fmt, cmtfile == null);
		Svg svg = graph.buildSvg(branch, dy, limit);

		if (cmtfile != null) {
			Log.notice("Writing commits to file '%s'",cmtfile);
			if (cmtfile.equals("-")) {
				try {
					graph.saveCommits(System.out);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try (OutputStream os=new FileOutputStream(cmtfile)) {
					graph.saveCommits(os);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.notice("Writing commits done");
		}

		if (svg != null) {
			Log.notice("Writing SVG to file");
			try (OutputStream os=new FileOutputStream(svgfile)) {
				svg.write(os);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.notice("Writing SVG done");
		}
	}
}
