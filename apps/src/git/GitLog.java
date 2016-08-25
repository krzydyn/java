package git;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import svg.Svg;
import sys.Log;

public class GitLog {
	static final String[] ArrayOfString0 = new String[0];
	//static GitRepo repo = new GitRepo("~/sec-os/secos");
	//static GitRepo repo = new GitRepo("~/tmp/linux");
	static String repo = "~/tmp/nuclear-js";
	static String branch = "origin/master";
	//static String branch = "2cde51fbd0f3"; //linux octopus commit
	//static String branch ="7c4c62a";
	static int limit = 3000;
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
