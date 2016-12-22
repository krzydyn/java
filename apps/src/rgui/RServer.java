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

import javax.imageio.ImageIO;

import sys.Log;
import net.ChannelStatusHandler;
import net.ChannelWriter;
import net.SelectorThread2;

public class RServer implements ChannelStatusHandler {
	int inlen;
	ByteBuffer inmsg = ByteBuffer.allocate(10*1024);
	final SelectorThread2 selector;
	private final Robot robot;
	private final Point mouseLoc;
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

	private void processMsg(ChannelWriter w) {
		short type = inmsg.getShort();
		Log.debug("msgtype = %d, payload %d", type, inmsg.remaining());
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
			RenderedImage img = getScreen();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(0);os.write(4);
			try {
				ImageIO.write(img, "jpg", os);
				writeTCP(w,ByteBuffer.wrap(os.toByteArray()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private RenderedImage getScreen() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return robot.createScreenCapture(new Rectangle(screenSize));
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
	private void run() {
		robot.mouseMove(mouseLoc.x, mouseLoc.y);
	}

	public static void main(String[] args) {
		try {
			new RServer().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
