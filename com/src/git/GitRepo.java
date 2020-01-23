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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import sys.Env;

public class GitRepo {
	private final static String git = "git";
	private final File path;

	public GitRepo(String p) {
		path = new File(Env.expandEnv(p));
	}

	public String cmd(Collection<String> args) throws Exception {
		List<String> a = new ArrayList<>();
		a.add(git);
		a.addAll(args);
		return Env.exec(a, path);
	}

	public String log(List<String> args) throws Exception {
		List<String> a = new ArrayList<>();
		a.add(git);
		a.add("log");
		a.addAll(args);
		return Env.exec(a, path);
	}
	public String log(String ...args) throws Exception {
		return log(Arrays.asList(args));
	}

	public String lstree(List<String> args) throws Exception {
		List<String> a = new ArrayList<>();
		a.add(git);
		a.add("ls-tree");
		a.addAll(args);
		return Env.exec(a, path);
	}
	public String lstree(String ...args) throws Exception {
		return lstree(Arrays.asList(args));
	}

	public String diff(List<String> args) throws Exception {
		List<String> a = new ArrayList<>();
		a.add(git);
		a.add("diff");
		a.addAll(args);
		return Env.exec(a, path);
	}
	public String diff(String ...args) throws Exception {
		return diff(Arrays.asList(args));
	}
}
