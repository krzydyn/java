package rgui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import sys.XThread;
import text.Text;
import net.ChannelHandler;
import net.SelectorThread2;
import net.SelectorThread2.QueueChannel;
import net.TcpFilter;

public class RServer implements ChannelHandler {
	final int MAXSCREEN_BUF = 16*1024;
	private final SelectorThread2 selector;
	private final Robot robot;
	private final boolean useQuality=true;
	private final int mouseButtonMask;

	private RenderedImage curScreen;
	private Rectangle screenRect;

	private RServer() throws Exception {
		robot = new Robot();
		robot.setAutoDelay(10); // delay before generating even

		mouseButtonMask = InputEvent.BUTTON1_MASK|InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK |
					InputEvent.BUTTON1_DOWN_MASK|InputEvent.BUTTON2_DOWN_MASK|InputEvent.BUTTON3_DOWN_MASK;

		selector = new SelectorThread2();
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
		try {
			processMsg(chn, buf);
		}catch (Throwable e) {
			Log.error(e);
		}
	}
	@Override
	public void write(QueueChannel qchn, ByteBuffer buf) {
		qchn.hnd.write(qchn,buf);
	}

	private void processMsg(QueueChannel chn, ByteBuffer msg) {
		short cmd = msg.getShort();

		if (cmd == 0) {
		}
		else if (cmd == 1) {
			int x=msg.getInt();
			int y=msg.getInt();
			mounseMove(x, y);
		}
		else if (cmd == 2) {
			int x=msg.getInt();
			int y=msg.getInt();
			int buttons=msg.getInt();
			mounseClick(x, y, buttons);
		}
		else if (cmd == 3) {
			String s = new String(msg.array(),msg.position(),msg.remaining(),Text.UTF8_Charset);
			keyType(s);
		}
		else if (cmd == 4) {
			int w = msg.getShort();
			int h = msg.getShort();
			float q = msg.getFloat();
			//Log.debug("sendImage(%d,%d,%.2f)",w,h,q);
			sendImage(chn, w, h, q);
		}
		else if (cmd == 5) {
			int w = msg.getShort();
			int h = msg.getShort();
			registerMonitor(chn, w, h);
		}
		else if (cmd == 6) {
			int keycode=msg.getInt();
			keyPressed(keycode);
		}
		else if (cmd == 7) {
			int keycode=msg.getInt();
			keyReleased(keycode);
		}
		else if (cmd == 8) {
			int buttons=msg.getInt();
			mousePressed(buttons);
		}
		else if (cmd == 9) {
			int buttons=msg.getInt();
			mouseReleased(buttons);
		}
		else if (cmd == 10) {
			int rot=msg.getInt();
			mouseWheel(rot);
		}
		else {
			Log.error("wrong cmd:%d, payload %d", cmd, msg.remaining());

		}
	}

	private void registerMonitor(QueueChannel chn, int w ,int h) {

	}
	private RenderedImage getScreen(int w, int h) {
		Rectangle r = new Rectangle();
		r.x=0; r.y=0;
		if (w > screenRect.x+screenRect.width) w = screenRect.x+screenRect.width;
		if (h > screenRect.y+screenRect.height) h = screenRect.y+screenRect.height;
		r.width=w;
		r.height=h;
		synchronized (this) {
			//if (curScreen == null) {
				curScreen = robot.createScreenCapture(r);
			//}
			return curScreen;
		}
	}

	private void mounseMove(int x,int y) {
		robot.mouseMove(x, y);
	}
	private void mounseClick(int x,int y,int buttons) {
		Log.info("mouseClick(%d,%d,%x) / %x",x,y,buttons,mouseButtonMask);
		buttons &= mouseButtonMask;
		robot.mouseMove(x, y);
		robot.mousePress(buttons);
		robot.mouseRelease(buttons);
	}
	private void keyType(int keycode) {
		int key = keycode&0xffff;
		int mod = (keycode>>16)&0xffff;
		if (mod!=0) {
			if (mod==KeyEvent.VK_ALT_GRAPH) {
				robot.keyPress(KeyEvent.VK_ALT);
				robot.keyPress(KeyEvent.VK_CONTROL);
			}
			else
				robot.keyPress(mod);
			robot.keyPress(key);
			robot.keyRelease(key);
			if (mod==KeyEvent.VK_ALT_GRAPH) {
				robot.keyRelease(KeyEvent.VK_ALT);
				robot.keyRelease(KeyEvent.VK_CONTROL);
			}
			else
				robot.keyRelease(mod);
		}
		else {
			robot.keyPress(key);
			robot.keyRelease(key);
		}
	}
	private int getkeycode(char c) {
		if (c == KeyEvent.VK_ENTER || c == KeyEvent.VK_TAB || c == KeyEvent.VK_BACK_SPACE ||
			c == KeyEvent.VK_SPACE || c == KeyEvent.VK_ESCAPE)
			return c;
		if (c > 0 && c <= 26) return KeyEvent.VK_A+(c-1) | (KeyEvent.VK_CONTROL<<16);

		if (c >= ',' && c <= '/') return KeyEvent.VK_COMMA+(c-'.');
		if (c >= '0' && c <= '0') return KeyEvent.VK_0+(c-'0');
		if (c >= 'A' && c <= 'Z') return KeyEvent.VK_A+(c-'A') | (KeyEvent.VK_SHIFT<<16);
		if (c >= 'a' && c <= 'z') return KeyEvent.VK_A+(c-'a');
		if (c == 0x7F) return KeyEvent.VK_DELETE;
		if (c == 0x105) return KeyEvent.VK_A | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0x107) return KeyEvent.VK_C | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0x119) return KeyEvent.VK_E | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0x142) return KeyEvent.VK_L | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0x144) return KeyEvent.VK_N | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0xF3) return KeyEvent.VK_O | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0x15B) return KeyEvent.VK_S | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0x17A) return KeyEvent.VK_X | (KeyEvent.VK_ALT_GRAPH<<16);
		if (c == 0x17C) return KeyEvent.VK_Z | (KeyEvent.VK_ALT_GRAPH<<16);
		return -1;
	}
	private void keyType(String s) {
		for (int i=0; i < s.length(); ++i) {
			int c = getkeycode(s.charAt(i));
			if (c != 0) keyType(c);
		}
	}
	private boolean altPressed=false;
	private void keyPressed(int keycode) {
		if(keycode == KeyEvent.VK_ALT) altPressed=true;
		if (keycode==KeyEvent.VK_ALT_GRAPH) {
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_CONTROL);
		}
		else
			robot.keyPress(keycode);
	}
	private void keyReleased(int keycode) {
		if(keycode == KeyEvent.VK_ALT) {
			if (altPressed) altPressed=false;
		}
		if (keycode==KeyEvent.VK_ALT_GRAPH) {
			robot.keyRelease(KeyEvent.VK_ALT);
			robot.keyRelease(KeyEvent.VK_CONTROL);
		}
		else
			robot.keyRelease(keycode);
		if (altPressed) {
			robot.keyRelease(KeyEvent.VK_ALT);
			altPressed=false;
		}
	}
	private void mousePressed(int buttons) {
		buttons &= mouseButtonMask;
		robot.mousePress(buttons);
	}
	private void mouseReleased(int buttons) {
		buttons &= mouseButtonMask;
		robot.mouseRelease(buttons);
	}
	private void mouseWheel(int rot) {
		robot.mouseWheel(rot);
	}
	private void sendImage(QueueChannel chn, int w, int h, float q) {
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
			write(chn,ByteBuffer.wrap(os.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean imageEqual(RenderedImage im1, RenderedImage im2, Rectangle roi) {
		return false;
	}
	private void run() throws Exception {
		screenRect = null;
		int shiftX=0,shiftY=0;
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			Rectangle r = gd.getDefaultConfiguration().getBounds();
			Log.debug("device %s: %s",gd.getIDstring(),r);
			if (screenRect == null) screenRect=r;
			else {
				if (r.x == 0) shiftX = screenRect.x;
				if (r.y == 0) shiftY = screenRect.y;
				screenRect = screenRect.union(r);
			}
		}
		screenRect.x -= shiftX;
		screenRect.y -= shiftY;
		Log.info("screen bounds (%d,%d %dx%d)",screenRect.x,screenRect.y,screenRect.width,screenRect.height);

		selector.start();
		selector.bind(null, 3367, this);
		while (selector.isRunning()) {
			XThread.sleep(1000);
		}
		Log.error("selctor stopped running");
	}

	public static void main(String[] args) {
		try {
			new RServer().run();
		} catch (Throwable e) {
			Log.error(e);
		}
	}

}
