package httpd;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import io.IOText;
import netio.ChannelHandler;
import netio.SelectorThread;
import netio.SelectorThread.QueueChannel;
import netio.TextFilter;
import sys.Env;
import sys.Log;

public class HttpServer implements ChannelHandler {
	private static final String CRLF = "\r\n";
	private static final int HTTP_PORT = 80;

	private final SelectorThread selector;
	private String serverRoot = Env.expandEnv("~/Work/www/kysoft");
	private List<String> reqFeatures;

	public HttpServer() throws Exception {
		selector = new SelectorThread();
	}

	@Override
	public ChannelHandler createFilter() {
		return new TextFilter(this);
	}

	@Override
	public void connected(QueueChannel qchn) {
		Log.debug("connected");
	}

	@Override
	public void disconnected(QueueChannel qchn, Throwable e) {
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
		String res = null;
		for (String s : msg.split("\n")) {
			s = s.trim();
			Log.debug("ln: %s", s);
			if (s.startsWith("GET ")) {
				int ei = s.indexOf(' ', 4);
				res = s.substring(4, ei);
				if (res.equals("/")) res = "/index.html";
			}
		}
		if (res != null) {
			Log.debug("Reading %s", res);
			CharSequence body;
			try {
				body = IOText.load(new File(serverRoot, res));
			} catch (IOException e) {
				String ln = "HTTP/1.1 404 File not found" + CRLF;
				write(qchn, ByteBuffer.wrap(ln.getBytes()));
				Log.error(e.getMessage());
				return ;
			}
			// status line
			//    * generic header | ... CRLF
			// CRLF
			String ln = "HTTP/1.1 200 OK" + CRLF;
			write(qchn, ByteBuffer.wrap(ln.getBytes()));
			ln = "Content-Type: text/html" +CRLF;
			write(qchn, ByteBuffer.wrap(ln.getBytes()));
			write(qchn, ByteBuffer.wrap(CRLF.getBytes()));

			// message body
			write(qchn, ByteBuffer.wrap(body.toString().getBytes()));
		}
		else {
			Log.debug("No resorce for the request");
			write(qchn, ByteBuffer.wrap("No resource\n\n".getBytes()));
		}
	}

	private void run() throws Exception {
		try {
		selector.start();
		selector.bind(null, HTTP_PORT, this);
		while (selector.isRunning()) {
			Thread.sleep(1000);
		}
		} finally {
			selector.stop();
		}
	}

	public static void main(String[] args) {
		try {
			new HttpServer().run();
		} catch (Throwable e) {
			Log.info("httpd failed");
			Log.error(e);
		}

	}

}
