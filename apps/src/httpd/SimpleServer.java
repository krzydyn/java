package httpd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sys.Env;
import sys.Log;

public class SimpleServer {
	public static final String serverRoot = Env.expandEnv("~/Work/java/com");
	private static final int HTTP_PORT = 8080;
	private static final ExecutorService pool = Executors.newFixedThreadPool(10);

	static class SocketHandler implements Runnable {
		final Socket s;
		SocketHandler(Socket s) {
			this.s = s;
		}

		@Override
		public void run() {
			Log.debug("Connection from %s", s.getRemoteSocketAddress());
			try {
				s.setReceiveBufferSize(1024);

				BufferedReader rd = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String ln;
				while ((ln = rd.readLine()) != null) {
					//Log.debug("%s: ", ln);
					if (ln.equals("")) break;
				}

				File f = new File(serverRoot, "res/unittest/html-index.txt");
				try (FileInputStream is = new FileInputStream(f)) {
					OutputStream os = s.getOutputStream();
					byte[] buf = new byte[1024];
					int r;
					while ((r = is.read(buf)) >= 0) {
						os.write(buf, 0, r);
					}
					os.flush();
				}
			} catch (IOException e) {
				Log.error(e);
			} finally {
				Env.close(s);
			}
		}
	}

	public static void main(String[] args) {
		Log.debug("ServerRoot: %s", serverRoot);
		try (ServerSocket server = new ServerSocket(HTTP_PORT)) {
			while (true) {
				Socket s = server.accept();
				pool.execute(new SocketHandler(s));
			}
		}
		catch (Exception e) {
			Log.error(e);
		}
	}
}
