package httpd;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.IOText;
import netio.ChannelHandler;
import netio.SelectorThread.QueueChannel;
import sys.Env;
import sys.Log;

public class HttpClientHandler implements ChannelHandler {
	private static final String CRLF = "\r\n";

	public HttpClientHandler() {
	}

	@Override
	public ChannelHandler connected(QueueChannel qchn) {
		Log.debug("connected");
		return null;
	}

	@Override
	public void closed(QueueChannel qchn, Throwable e) {
		Log.debug("disconnected");
	}

	@Override
	public void received(QueueChannel qchn, ByteBuffer msg) {
		String s = new String(msg.array(),msg.position(),msg.remaining(),Env.UTF8_Charset);
		Log.debug("received: %s", s);
		processMsg(qchn, s);
		qchn.close();
	}

	@Override
	public void write(QueueChannel qchn, ByteBuffer msg) {
		Log.debug("Write: %s",  new String(msg.array(),msg.position(),msg.remaining(),Env.UTF8_Charset));
		qchn.write(msg);
	}

	private void processMsg(QueueChannel qchn, String msg) {
		String method = null;
		String req = null;
		for (String s : msg.split("\n")) {
			s = s.trim();
			if (s.startsWith("GET ")) {
				method = "GET";
				int ei = s.indexOf(' ', 4);
				req = s.substring(4, ei);
				if (req.equals("/")) req = "/index.html";
			}
		}

		Status status = null;
		CharSequence body = null;
		if (method == null) {
			Log.error("no method given");
			status = Status.BAD_REQUEST;
		}
		else if (method.equals("GET")) {
			if (req == null) {
				Log.debug("no method given");
				status = Status.BAD_REQUEST;
			}
			else if (req.endsWith(".php")) {
				try {
					body = Env.exec(new File(HttpServer.serverRoot), "php", HttpServer.serverRoot + req);
					status = Status.OK;
				} catch (IOException e) {
					Log.error(e.getMessage());
					status = Status.FILE_NOT_FOUND;
				}
			}
			else {
				Log.debug("Reading %s", req);
				try {
					body = IOText.load(new File(HttpServer.serverRoot, req));
					status = Status.OK;
				} catch (IOException e) {
					Log.error(e.getMessage());
					status = Status.FILE_NOT_FOUND;
				}
			}
		}

		if (status == null) {
			Log.debug("no status created");
			status = Status.BAD_REQUEST;
		}

		if (body == null) {
			StringBuilder s = new StringBuilder(200);
			s.append("<!DOCTYPE html>\n");
			s.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n");
			s.append("<head>\n");
			s.append(String.format("<title>%s</title>\n", status));
			s.append("</head>\n<body>\n");
			s.append(String.format("<h1>%s</h1>\n", status));
			s.append("</body>\n</html>");
			body = s;
		}
		// status line
		//    * generic header | ... CRLF
		// CRLF

		String str = "HTTP/1.1 " + status + CRLF
				+ "Content-Type: text/html;charset=\"utf-8\"" + CRLF
				+ "Server: HttpServer" + CRLF
				+  CRLF
				// message body
				+ body.toString();
		write(qchn, ByteBuffer.wrap(str.getBytes()));
	}
}
