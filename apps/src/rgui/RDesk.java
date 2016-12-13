package rgui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JButton;

import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class RDesk extends MainPanel implements ActionListener{
	private final Robot robot;
	private boolean running=false;
	private Image img;
	private boolean paintDone=false;
	public RDesk() {
		super(null);
		Robot r = null;
		try {
			r = new Robot();
		} catch (Exception e) {
			Log.error("can't create java.awt.Robot");
		}
		robot = r;
		if (robot!=null) {
			robot.setAutoDelay(10);
			// auto wait can be set only whan using robot from new Thread
			//robot.setAutoWaitForIdle(true);
		}

		setPreferredSize(new Dimension(2000,800));

		JButton b;
		add(b=new JButton("test"));
		b.setBounds(0, 0, 50, 20);
		b.addActionListener(this);

		new Thread(){
			@Override
			public void run() {
				try {
					running=true;
					Log.info("loop started");
					robotloop();
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					running=false;
					Log.info("loop finished");
				}
			}
		}.start();
	}
	@Override
	protected void paintComponent(Graphics g) {
		//super.paintComponent(g);
		if (img!=null) g.drawImage(img, 0, 0, null);
		paintDone=true;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		testrobot();
		robot.mouseMove(getTopLevelAncestor().getX()+10, getTopLevelAncestor().getY()+10);
	}
	private void robotloop() throws Exception {
		Rectangle scr = new Rectangle(0,0,3000,800);
		while (running) {
			if (paintDone) {
				img = robot.createScreenCapture(scr);
				repaint();
			}
			Thread.sleep(100);
		}
	}
	private void testrobot() {
		robot.mouseMove(2540, 260);
		mouseClick(InputEvent.BUTTON1_MASK);

		keyType("test\n");
	}

	static int FLAGS_BITS = 7;
	static int FLAGS_MASK = 0xf << FLAGS_BITS;
	static int FLAG_SHIFT = 1;
	static int FLAG_CTRL = 2;
	static int FLAG_ALT = 4;
	static int FLAG_META = 8;
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
	private void mouseClick(int buttons) {
		robot.mousePress(buttons);
		robot.mouseRelease(buttons);
	}
	@Override
	protected void windowClosed() {
		running=false;
	}
	public static void main(String[] args) {
		start(RDesk.class);
	}
}
