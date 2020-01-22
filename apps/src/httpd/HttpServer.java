package httpd;

import java.util.List;

import netio.ChannelHandler;
import netio.SelectorThread;
import netio.SelectorThread.QueueChannel;
import netio.ServerHandler;
import sys.Env;
import sys.Log;

public class HttpServer implements ServerHandler {
	public static final String serverRoot = Env.expandEnv("~/krzydyn/www");

	private static final int HTTP_PORT = 8080;


	private final SelectorThread selector;
	private List<String> reqFeatures;

	public HttpServer() throws Exception {
		selector = new SelectorThread();
	}

	@Override
	public ChannelHandler connected(QueueChannel qchn) {
		return new HttpClientHandler();
	}

	@Override
	public void closed(QueueChannel qchn, Throwable e) {
		Log.debug("server socket closed");
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
