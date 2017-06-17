package gftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import sys.ArrayObj;
import sys.Env;
import sys.Log;
import text.Text;

public class ScriptShell {
	private final File dir;
	private final List<String> args;
	private OutputStreamWriter childwriter;
	private final Object readready = new Object();
	private String lastrd;

	public ScriptShell(File dir, List<String> args) {
		this.dir=dir;
		this.args=args;
		lastrd=null;
	}
	public ScriptShell(File dir, String ...args) {
		this(dir, new ArrayObj<String>(args));
	}

	public void start() {
		if (lastrd==null) lastrd="";
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ScriptShell.this.run();
				}
				catch (Throwable e) {
					Log.error(e, "script execution failed");
				}
				finally {
					args.clear();
					Log.debug("script done");
				}
			}
		}, args.get(0)).start();
	}

	public void stop() {
		try {
			if (childwriter!=null) {
				//childwriter.write(Ansi.Code.EOT);
				childwriter.close();
			}
		} catch (Exception e) {}
	}

	public void writeln(String s) throws IOException {
		if (childwriter==null) {
			lastrd=s+"\n";
			return ;
		}
		Log.debug("send '%s'", s);
		childwriter.write(s+"\n");
		childwriter.flush();
	}
	public String readln() throws IOException {
		synchronized (readready) {
			for (;;) {
				try {
					Log.debug("waiting lastrd to be ready");
					readready.wait(1000);
					break;
				} catch (InterruptedException e) {
					Log.error(e.getMessage());
				}
			}
			String r=lastrd;
			lastrd="";
			return r;
		}
	}

	private void run() throws Exception {
		//java using ptys?
		ProcessBuilder pb = new ProcessBuilder(args);
		//pb.environment().put("TERM", "vt100");
		pb.redirectErrorStream(true); //redirect stderr to stdout
		//pb.redirectInput(Redirect.PIPE); pipe is default
		if (dir!=null) pb.directory(dir);
		Process child = pb.start();
		InputStreamReader is = new InputStreamReader(child.getInputStream(), Env.UTF8_Charset);
		OutputStreamWriter os = new OutputStreamWriter(child.getOutputStream(), Env.UTF8_Charset);
		childwriter = os;
		Log.debug("child started: %s", Text.join(" ", args));
		if (lastrd!=null) {
			Log.debug("send %s", Text.vis(lastrd));
			childwriter.write(lastrd);
			childwriter.flush();
		}

		lastrd="";
		Log.debug("waiting for child output...");
		char[] buf = new char[1024];
		int r;
		while ((r=is.read(buf, 0, buf.length)) >= 0) {
			String rd = new String(buf, 0, r);
			Log.debug("read: %s", Text.vis(rd));
			synchronized (readready) {
				lastrd+=rd;
				readready.notify();
			}
		}
		Log.debug("for child output done (%d)",r);
		try {
			Log.debug("waiting for child exit...");
			int ec = child.waitFor();
			Log.debug("exec %s exitcode=%d", Text.join(" ", args), ec);
			if (ec != 0) throw new IOException("Exit("+ec+")");
		}
		finally {
			childwriter=null;
			is.close();
			os.close();
			synchronized (readready) {
				lastrd=null;
				readready.notify();
			}
		}
	}

}
