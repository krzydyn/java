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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sys.Env;

public class GitRepo {
	private static String git = "/usr/bin/git";
	private File path;

	public GitRepo(String p) {
		path = new File(Env.expandEnv(p));
	}

	public String log(String ...args) throws Exception {
		List<String> a = new ArrayList<String>();
		a.add(git);
		a.add("log");
		for (int i=0; i < args.length; ++i) a.add(args[i]);
		return Env.exec(path, a.toArray(new String[]{}));
	}
}
