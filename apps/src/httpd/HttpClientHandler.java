package httpd;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.IOText;
import netio.ChannelHandler;
import netio.SelectorThread.QueueChannel;
import sys.Env;
import sys.Log;

public class HttpClientHandler implements ChannelHandler {
	private static final String CRLF = "\r\n";

	private static Map<String,String> envp = new HashMap<>();
	static {
		envp.put("HTTP_USER_AGENT", "HttpdServer");
		envp.put("REQUEST_URI", "");
	}

	public HttpClientHandler() {
	}

	@Override
	public ChannelHandler connected(QueueChannel qchn) {
		//Log.debug("connected %s", qchn.getAddress());
		return null;
	}

	@Override
	public void closed(QueueChannel qchn, Throwable e) {
		//Log.debug("disconnected %s", qchn.getAddress());
	}

	@Override
	public void write(QueueChannel qchn, ByteBuffer msg) {
		//String s = new String(msg.array(), msg.position(), msg.remaining(), Env.UTF8_Charset);
		//if (s.length() > 300) s = s.substring(0,300);
		//Log.debug("Write: %s",  s);
		qchn.write(msg);
	}

	@Override
	public void received(QueueChannel qchn, ByteBuffer msg) {
		String s = new String(msg.array(), msg.position(), msg.remaining(), Env.UTF8);
		Log.debug("received: %s", s);
		processMsg(qchn, s);
		qchn.close();
	}

	private void processMsg(QueueChannel qchn, String msg) {
		List<String> requHeader = new ArrayList<>();

		requHeader.clear();
		for (String s : msg.split("\n")) {
			s = s.trim();
			if (s.isEmpty()) break;
			requHeader.add(s);
		}

		Status status = null;
		if (requHeader.isEmpty()) {
			Log.error("empty request");
			status = Status.BAD_REQUEST;
		}

		String method;
		String resource;
		String httpver;

		String[] n = requHeader.get(0).split(" ", 3);
		method = n[0];
		resource = n[1];
		httpver = n[2];

		envp.put("REQUEST_URI", resource);
		resource = "/index.php";

		CharSequence body = null;
		if (method == null) {
			Log.error("no method given");
			status = Status.BAD_REQUEST;
		}
		else if (method.equals("GET")) {
			envp.put("REDIRECT_STATUS", "200"); // mandatory
			envp.put("REQUEST_METHOD", method);
			envp.put("SCRIPT_FILENAME", HttpServer.serverRoot + resource);
			//envp.put("CONTENT_TYPE", "");
			envp.put("CONTENT_LENGTH", "0");
			if (resource.endsWith(".php")) {
				try {
					//https://stackoverflow.com/questions/3258634/php-how-to-send-http-response-code
					String[] args = {"php-cgi", "-f", HttpServer.serverRoot + resource};
					String result = Env.exec(Arrays.asList(args), new File(HttpServer.serverRoot), envp);

					if (result.startsWith("Status:")) {
						//read status from script statusline
						int idx = result.indexOf("\n");
						if (idx > 0) {
							String sln = result.substring(0, idx + 1);
							status = Status.getStatus(Integer.parseInt(sln.substring(8, 11)));
							if (status == Status.OK)
								body = result.substring(idx);
						}
					}
					else {
						String str = httpver + " " + Status.OK + CRLF + result;
						write(qchn, ByteBuffer.wrap(str.getBytes()));
						return ;
					}

				} catch (IOException e) {
					Log.error(e.getMessage());
					status = Status.FILE_NOT_FOUND;
					body = e.getMessage();
				}
			}
			else {
				Log.debug("Reading %s", resource);
				try {
					body = IOText.load(new File(HttpServer.serverRoot, resource));
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

		//Log.debug("body: '%s'", body);
		String str = httpver + " " + status + CRLF
				+ "Content-Type: text/html;charset=\"utf-8\"" + CRLF
				+ "Server: HttpServer" + CRLF
				+  CRLF
				// message body
				+ body;

		write(qchn, ByteBuffer.wrap(str.getBytes()));
	}
}
