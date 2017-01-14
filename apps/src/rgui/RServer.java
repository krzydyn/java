package rgui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import sys.Log;
import net.ChannelHandler;
import net.SelectorThread2;
import net.SelectorThread2.QueueChannel;
import net.TcpFilter;

public class RServer implements ChannelHandler {
	final SelectorThread2 selector;
	private final Robot robot;
	private final boolean useQuality=true;

	private RenderedImage curScreen;

	private RServer() throws Exception {
		robot = new Robot();
		robot.setAutoDelay(10);

		selector = new SelectorThread2();
		selector.start();
		selector.bind(null, 3367, this);
	}

	@Override
	public ChannelHandler createFilter() {
		return new TcpFilter(this);
	}
	@Override
	public void connected(QueueChannel qchn) {
		Log.debug("connected");
		write(qchn,ByteBuffer.wrap("hello\n".getBytes()));
	}
	@Override
	public void disconnected(QueueChannel chnst) {
		Log.debug("disconnected");
	}
	@Override
	public void received(QueueChannel chn, ByteBuffer buf) {
		processMsg(chn, buf);
	}
	@Override
	public void write(QueueChannel qchn, ByteBuffer buf) {
		qchn.hnd.write(qchn,buf);
	}

	private void processMsg(QueueChannel chn, ByteBuffer msg) {
		short type = msg.getShort();

		if (type == 0) {
		}
		else if (type == 1) {
			int x=msg.getInt();
			int y=msg.getInt();
			mounseMove(x, y);
		}
		else if (type == 2) {
			int buttons=msg.getInt();
			mounseClick(buttons);
		}
		else if (type == 3) {
			String s = new String(msg.array(),msg.position(),msg.remaining());
			keyType(s);
		}
		else if (type == 4) {
			int w = msg.getShort();
			int h = msg.getShort();
			float q = msg.getFloat();
			//Log.debug("sendImage(%d,%d,%.2f)",w,h,q);
			sendImage(chn, w, h, q);
		}
		else if (type == 5) {
			int w = msg.getShort();
			int h = msg.getShort();
			registerMonitor(chn, w, h);
		}
		else {
			Log.error("msgtype = %d, payload %d", type, msg.remaining());
		}
	}

	private void registerMonitor(QueueChannel chn, int w ,int h) {

	}
	private RenderedImage getScreen(int w, int h) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (screenSize.width > w) screenSize.width=w;
		if (screenSize.height > h) screenSize.height=h;
		synchronized (this) {
			//if (curScreen == null) {
				curScreen = robot.createScreenCapture(new Rectangle(screenSize));
			//}
			return curScreen;
		}
	}

	private void mounseMove(int x,int y) {
		robot.mouseMove(x, y);
	}
	private void mounseClick(int buttons) {
		robot.mousePress(buttons);
		robot.mouseRelease(buttons);
	}
	static int FLAGS_BITS = 7;
	static int FLAGS_MASK = 0xf << FLAGS_BITS;
	static int FLAG_SHIFT = 1;
	static int FLAG_CTRL = 2;
	static int FLAG_ALT = 4;
	static int FLAG_META = 8;
	private void keyType(int keycode) {
		robot.keyPress(keycode);
		robot.keyRelease(keycode);
	}
	private int getkeycode(char c) {
		if (c == '\n' || c == '\t') return c;
		if (c >= ' ' && c <= 'Z') return c;
		if (c >= 'a' && c <= 'x') return c-32;
		return -1;
	}
	private void keyType(String s) {
		for (int i=0; i < s.length(); ++i) {
			int c = getkeycode(s.charAt(i));
			if (c >= 0) keyType(c);
		}
	}
	private void sendImage(QueueChannel chn,int w, int h, float q) {
		RenderedImage img = getScreen(w,h);
		ByteArrayOutputStream os = new ByteArrayOutputStream(512*1024);
		os.write(0);os.write(4);
		try {
			if (useQuality) {
				JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
				jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpegParams.setCompressionQuality(q);
				ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
				writer.setOutput(ImageIO.createImageOutputStream(os));
				writer.write(null, new IIOImage(img, null, null), jpegParams);
				writer.dispose();
			}
			else
				ImageIO.write(img, "jpg", os);
			//Log.debug("img size is %d", os.size());
			write(chn,ByteBuffer.wrap(os.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean imageEqual(RenderedImage im1, RenderedImage im2, Rectangle roi) {
		return false;
	}
	private void run() {
		/*Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		while (true) {
			RenderedImage im = robot.createScreenCapture(new Rectangle(screenSize));
			if (imageEqual(im, curScreen)) {
				curScreen = im;
				sendImage(wr,w,h,0.5f);
			}
			XThread.sleep(100);
		}*/
	}

	public static void main(String[] args) {
		try {
			new RServer().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
