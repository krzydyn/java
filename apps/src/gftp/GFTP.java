package gftp;

import java.io.IOException;

import sys.Log;

/*
 * .netrc
 * machine www.kysoft.pl
 * login www.kysoft.pl
 * password xxxxxxxxx
 */
public class GFTP {
	private static String cat = "/bin/cat";
	private static String bash = "/bin/bash";
	private static String ftp = "/usr/bin/ftp";

	public static void main(String[] args) {
		String srcDir=null, dstDir=null;
		if (args.length >= 2) {
			srcDir=args[0];
			dstDir=args[1];
		}

		try {
			synchronizeDirs(srcDir, dstDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void synchronizeDirs(String srcDir, String dstDir) throws IOException {
		//ScriptShell shell = new ScriptShell(null, ftp, "-pv", "www.kysoft.pl");
		ScriptShell shell = new ScriptShell(null, bash);
		//ScriptShell shell = new ScriptShell(null, "/bin/ps");
		shell.start();
		shell.writeln("env");
		String r;
		int state=1;
		int idlecnt=0;
		while ((r=shell.readln()) != null) {
			if (r.equals("")) {
				Log.debug("nothing read");
				shell.writeln("exit");
				++idlecnt;
				if (idlecnt==2) break;
				continue;
			}
			idlecnt=0;
			System.out.print(r);
			//Log.debug("ftp-resp:\n%s", r);
			switch (state) {
			case 0:
				shell.writeln("open www.kysoft.pl");
				break;
			default:
				break;
			}
		}
		Log.debug("shell stop");
		shell.stop();
	}

}
