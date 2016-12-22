package rgui;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import sys.Log;
import net.ChannelStatusHandler;
import net.ChannelWriter;
import net.SelectorThread2;

public class RServer implements ChannelStatusHandler {
	private int inlen;
	private ByteBuffer inmsg = ByteBuffer.allocate(10*1024);
	final SelectorThread2 selector;
	private final Robot robot;
	private final Point mouseLoc;
	private boolean useQualuty=true;

	SelectableChannel chn;
	private RenderedImage curScreen;

	private RServer() throws Exception {
		mouseLoc = MouseInfo.getPointerInfo().getLocation();
		robot = new Robot();
		robot.setAutoDelay(10);

		selector = new SelectorThread2();
		selector.start();
		selector.bind(null, 3367, this);
	}


	private int readTCP(int limit, ByteBuffer src) {
		if (inmsg.position() + src.remaining() < limit) {
			inmsg.put(src);
		}
		else {
			while (inmsg.position() < limit) inmsg.put(src.get());
		}
		return inmsg.position();
	}
	private void writeTCP(ChannelWriter w, ByteBuffer b) {
		ByteBuffer lenbuf = ByteBuffer.allocate(4);
		lenbuf.putInt(b.remaining());
		lenbuf.flip();
		//Log.debug("writeTCP(payload=%d)",b.remaining());
		w.write(lenbuf);
		w.write(b);
	}


	@Override
	public void connected(ChannelWriter w) {
		Log.debug("connected");
		inmsg.clear();
		inlen=0;

		ByteBuffer b = ByteBuffer.allocate(100);
		byte[] str = "hello\n".getBytes();
		b.putShort((short) 0); b.put(str, 0, str.length);
		writeTCP(w,b);
	}

	@Override
	public void received(ChannelWriter w, ByteBuffer buf) {
		int intbytes = 4;
		//must process all data from buf
		while (buf.hasRemaining()) {
			if (inlen==0) {
				if (readTCP(intbytes, buf) < intbytes)
					continue;
				inmsg.flip();
				inlen = inmsg.getInt();
				inmsg.clear();
			}
			if (readTCP(inlen, buf) < inlen) {
				continue;
			}
			inmsg.flip();
			processMsg(w);
			inmsg.clear();
			inlen=0;
		}
		if (inlen > 0) {
			//Log.debug("read %d of %d bytes", inmsg.position(),inlen);
		}
	}

	private void processMsg(ChannelWriter wr) {
		short type = inmsg.getShort();

		if (type == 0) {
		}
		else if (type == 1) {
			int x=inmsg.getInt();
			int y=inmsg.getInt();
			mounseMove(x, y);
		}
		else if (type == 2) {
			int buttons=inmsg.getInt();
			mounseClick(buttons);
		}
		else if (type == 3) {
			String s = new String(inmsg.array(),inmsg.position(),inmsg.remaining());
			keyType(s);
		}
		else if (type == 4) {
			int w = inmsg.getShort();
			int h = inmsg.getShort();
			float q = inmsg.getFloat();
			//Log.debug("sendImage(%d,%d,%.2f)",w,h,q);
			sendImage(wr,w, h, q);
		}
		else {
			Log.error("msgtype = %d, payload %d", type, inmsg.remaining());
		}
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
	private void sendImage(ChannelWriter wr,int w, int h, float q) {
		RenderedImage img = getScreen(w,h);
		ByteArrayOutputStream os = new ByteArrayOutputStream(512*1024);
		os.write(0);os.write(4);
		try {
			if (useQualuty) {
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
			writeTCP(wr,ByteBuffer.wrap(os.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean imageEqual(RenderedImage im1, RenderedImage im2) {
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
