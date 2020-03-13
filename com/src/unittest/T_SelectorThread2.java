package unittest;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import netio.SelSocket;
import netio.SelectorThread2;
import sys.Env;
import sys.Log;

public class T_SelectorThread2 extends UnitTest {

	static void _connect() {
		String HOST = "www.google.com";
		final byte[] CRLF = "\r\n".getBytes();
		final ByteBuffer data = ByteBuffer.allocate(100*1024);
		data.put("GET / HTTP/1.1".getBytes());
		data.put(CRLF);
		data.put(("Host: " + HOST).getBytes());
		data.put(CRLF); data.put(CRLF);
		data.flip();

		try {
			SelectorThread2 sel = new SelectorThread2();
			sel.start();

			SelSocket sock = sel.connect("www.google.com", 80);

			Log.debug("writing");
			//sock.write("GET / HTTP/1.1\r\n\r\n".getBytes());
			sock.write(data);

			OutputStream os = new FileOutputStream("/tmp/test-index.html");
			Log.debug("reading");
			byte[] buf = new byte[1000];
			int r;
			while ((r = sock.read(buf, 0, buf.length)) >= 0) {
				Log.debug("read %d: ", r);
				os.write(buf, 0, r);
				String s = new String(buf, 0, r, Env.UTF8_Charset);
				if (s.contains("</html>")) break;
			}
			os.close();
			sock.close();
			sel.stop();
		} catch (Exception e) {
			Log.error(e);
		}
	}

	static void connect_multi() {
		String HOST = "www.google.com";
		final byte[] CRLF = "\r\n".getBytes();

		SelectorThread2 sel = new SelectorThread2();
		sel.start();

		List<SelSocket> socks = new ArrayList<>();
		for (int i = 0; i < 100; ++i) {
			try {
				SelSocket sock = sel.connect("www.google.com", 80);
				socks.add(sock);
			} catch (Exception e) {
				Log.error(e);
			}
		}
		for (SelSocket sock : socks) {
			try {
				final ByteBuffer data = ByteBuffer.allocate(10*1024);
				data.put("GET / HTTP/1.1".getBytes());
				data.put(CRLF);
				data.put(("Host: " + HOST).getBytes());
				data.put(CRLF); data.put(CRLF);
				data.flip();
				sock.write(data);
			} catch (Exception e) {
				Log.error(e);
			}
		}

		ExecutorService pool = Executors.newFixedThreadPool(10);
		int i = 0;
		for (SelSocket sock : socks) {
			final int cnt = i++;
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
					String fn = String.format("/tmp/test-index-%d.html", cnt);
					OutputStream os = new FileOutputStream(fn);
					Log.debug("START reading");
					byte[] buf = new byte[1000];
					int r;
					while ((r = sock.read(buf, 0, buf.length)) >= 0) {
						os.write(buf, 0, r);
						String s = new String(buf, 0, r, Env.UTF8_Charset);
						if (s.contains("</html>")) {
							Log.debug("%s found </html>", sock.getName());
							break;
						}
						r = s.length();
						//Log.debug("%sread[%d] ...%s", sock.getName(), r, s.substring(r-10,r));
					}
					os.close();
					sock.close();
					Log.debug("%s END reading", sock.getName());
					}catch (Exception e) {
						Log.error(e);
					}
				}
			});
		}

		pool.shutdown();
		try {
			pool.awaitTermination(3, TimeUnit.SECONDS);
			for (SelSocket sock : socks) { sock.close(); }
			pool.shutdownNow();
		}catch (Exception e) {
			Log.error(e);
		}
		sel.stop();
	}

	static void _bind() {
		try {
			SelectorThread2 sel = new SelectorThread2();
			sel.start();

			SelSocket server = sel.bind(null, 8080);
			server.addListener(new SelSocket.SocketListener() {

				@Override
				public void closed(SelSocket sock) {
					Log.debug("closed connection");
				}

				@Override
				public void connected(SelSocket sock) {
					Log.debug("connected connection");

				}
			});
			Log.debug("bind ok");
			SelSocket sock = sel.connect("localhost", 8080);
			sock.write("abc".getBytes());
			Thread.sleep(500);

			sel.stop();
		} catch (Exception e) {
			Log.error(e);
		}
	}
}
