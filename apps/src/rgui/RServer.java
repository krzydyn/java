package rgui;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import net.ChannelStatusHandler;
import net.ChannelWriter;
import net.SelectorThread2;

public class RServer implements ChannelStatusHandler {
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

	@Override
	public void connected(SelectorThread2 st, ChannelWriter w) {
		w.write(st, ByteBuffer.wrap("hello\n".getBytes()));
	}

	@Override
	public void received(SelectorThread2 st, ChannelWriter w, ByteBuffer buf) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		BufferedImage img = robot.createScreenCapture(new Rectangle(screenSize));
		ByteOutputStream os = new ByteOutputStream();
		try {
			ImageIO.write(img, "jpg", os);
			w.write(st, ByteBuffer.wrap(os.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void mounseMove(int x,int y) {
		robot.mouseMove(2540, 260);
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
			if (c >= 0) {
				robot.keyPress(c);
				robot.keyRelease(c);
			}
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
