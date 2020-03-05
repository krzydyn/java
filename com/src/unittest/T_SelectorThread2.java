package unittest;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
	static void bind() {
		try {
			SelectorThread2 sel = new SelectorThread2();
			sel.start();

			sel.bind(null, 8080, new SelectorThread2.BindListener() {

				@Override
				public void accepted(SelSocket sock) {
					Log.debug("accepted connection");
				}

				@Override
				public void closed(SelSocket sock) {
					Log.debug("closed connection");
				}
			});

			Log.debug("bind ok");
			SelSocket sock = sel.connect("localhost", 80);
			sock.write("abc".getBytes());
			Thread.sleep(500);

			sel.stop();
		} catch (Exception e) {
			Log.error(e);
		}
	}
}
