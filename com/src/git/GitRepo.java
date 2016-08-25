package git;

import java.io.File;

import sys.Env;

public class GitRepo {
	private static String git = "/usr/bin/git";
	private File path;

	public GitRepo(String p) {
		path = new File(Env.expandEnv(p));
	}

	public String log(String args) throws Exception {
		return Env.exec(String.format("%s %s %s", git, "log", args), path);
	}
}
