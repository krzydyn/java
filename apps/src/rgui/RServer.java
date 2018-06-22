package rgui;

import img.Colors;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import sys.Env;
import sys.Log;
import sys.XThread;
import netio.ChannelHandler;
import netio.SelectorThread;
import netio.TcpFilter;
import netio.SelectorThread.QueueChannel;

public class RServer implements ChannelHandler {
	private static final int RSERVER_PORT = 9085; // IANA IBM remote system console
	private static final int FORCE_ACTION_TIME = 60*1000;
	private static boolean keepScreenOn;
	private final SelectorThread selector;
	private final Robot robot;
	private final int mouseButtonMask;

	private long forceActionTm=0;
	private BufferedImage screenImg;
	private Rectangle screenRect;
	private final List<QueueChannel> clients=new ArrayList<>();

	private RServer() throws Exception{
		robot = new Robot();
		robot.setAutoDelay(10); // delay before generating event

		mouseButtonMask = /*InputEvent.BUTTON1_MASK|InputEvent.BUTTON2_MASK|InputEvent.BUTTON3_MASK |*/
					InputEvent.BUTTON1_DOWN_MASK|InputEvent.BUTTON2_DOWN_MASK|InputEvent.BUTTON3_DOWN_MASK;

		selector = new SelectorThread();
	}

	@Override
	public ChannelHandler createFilter() {
		return new TcpFilter(this);
	}
	@Override
	public void connected(QueueChannel chn) {
		Log.debug("connected");
	}
	@Override
	public void disconnected(QueueChannel chn, Throwable e) {
		Log.debug("disconnected %s", e==null?"":e.toString());
		clients.remove(chn);
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
		int xcode=-1;

		try {
		if (cmd == RCommand.SCREEN_INFO) {
			getScreenInfo(chn);
		}
		else if (cmd == RCommand.MOUSE_MOVE) {
			int x=msg.getInt();
			int y=msg.getInt();
			mounseMove(x, y);
		}
		else if (cmd == RCommand.MOUSE_CLICK) {
			int x=msg.getInt();
			int y=msg.getInt();
			int buttons=msg.getInt();
			xcode=buttons;
			mounseClick(x, y, buttons);
		}
		else if (cmd == RCommand.TEXT_TYPE) {
			String s = new String(msg.array(),msg.position(),msg.remaining(),Env.UTF8_Charset);
			xcode = s.charAt(0);
			keyType(s);
		}
		else if (cmd == RCommand.SCREEN_IMG) {
			int w = msg.getShort();
			int h = msg.getShort();
			float q = msg.getFloat();
			if (w <= 0) w =  (int)screenRect.getMaxX();
			if (h <= 0) h =  (int)screenRect.getMaxY();
			sendImage(chn, 0, 0, w, h, q);
		}
		else if (cmd == RCommand.CLIENT_REGISTER) {
			Log.debug("processing CLIENT_REGISTER");
			registerClient(chn);
		}
		else if (cmd == RCommand.KEY_PRESS) {
			int keycode=msg.getInt();
			xcode=keycode;
			keyPressed(keycode);
		}
		else if (cmd == RCommand.KEY_RELEASE) {
			int keycode=msg.getInt();
			xcode=keycode;
			keyReleased(keycode);
		}
		else if (cmd == RCommand.MOUSE_PRESS) {
			int buttons=msg.getInt();
			xcode=buttons;
			mousePressed(buttons);
		}
		else if (cmd == RCommand.MOUSE_RELEASE) {
			int buttons=msg.getInt();
			xcode=buttons;
			mouseReleased(buttons);
		}
		else if (cmd == RCommand.MOUSE_WHEEL) {
			int rot=msg.getInt();
			xcode=rot;
			mouseWheel(rot);
		}
		else if (cmd == RCommand.CLIPBOARD_GET) {
			getClipboard(chn);
		}
		else if (cmd == RCommand.CLIPBOARD_SET) {
			setClipboard(msg);
		}
		else {
			Log.error("unknown cmd:%d, payload %d", cmd, msg.remaining());

		}
		}
		catch (Exception e) {
			Log.error(e, "cmd=%d, xcode = 0x%X (%d)",cmd,xcode,xcode);
		}
	}

	private void getScreenInfo(QueueChannel chn) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(512*1024);
		DataOutputStream dos = new DataOutputStream(os);
		try {
			dos.writeShort(RCommand.SCREEN_INFO);
			for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
				Rectangle r = gd.getDefaultConfiguration().getBounds();
				Log.debug("device %s: %s",gd.getIDstring(),r);
				dos.writeUTF(gd.getIDstring());
				dos.writeInt(r.x);
				dos.writeInt(r.y);
				dos.writeInt(r.width);
				dos.writeInt(r.height);
			}
			dos.close();
			write(chn,ByteBuffer.wrap(os.toByteArray()));
		} catch (IOException e) {
			Log.error(e);
		}
	}

	private BufferedImage getScreen(int x, int y, int w, int h) {
		BufferedImage i = null;
		synchronized (this) {
			if (screenImg==null) return null;
			i=screenImg;
		}
		return i.getSubimage(x, y, w, h);
	}

	private void mounseMove(int x,int y) {
		synchronized (robot) {
			robot.mouseMove(x, y);
		}
		forceActionTm = System.currentTimeMillis()+FORCE_ACTION_TIME;
	}
	private void mounseClick(int x,int y,int buttons) {
		Log.info("mouseClick(%d,%d,%x) / %x",x,y,buttons,mouseButtonMask);
		buttons &= mouseButtonMask;
		robot.mouseMove(x, y);
		robot.mousePress(buttons);
		robot.mouseRelease(buttons);
		forceActionTm = System.currentTimeMillis()+FORCE_ACTION_TIME;
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
	private void registerClient(QueueChannel chn) {
		if (!clients.contains(chn))
			clients.add(chn);
	}
	private boolean altPressed=false;
	private boolean winPressed=false;
	private boolean metaPressed=false;
	private void keyPressed(int keycode) {
		//on Win10 VK_META cause Exception
		if(keycode == KeyEvent.VK_META) keycode = KeyEvent.VK_WINDOWS;

		if(keycode == KeyEvent.VK_ALT) altPressed=true;
		else if(keycode == KeyEvent.VK_WINDOWS) winPressed=true;
		else if(keycode == KeyEvent.VK_META) metaPressed=true;

		if (keycode==KeyEvent.VK_ALT_GRAPH) {
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_CONTROL);
		}
		else robot.keyPress(keycode);
	}
	private void keyReleased(int keycode) {
		if(keycode == KeyEvent.VK_META) keycode = KeyEvent.VK_WINDOWS;

		if(keycode == KeyEvent.VK_ALT) altPressed=false;
		else if(keycode == KeyEvent.VK_WINDOWS) winPressed=false;
		else if(keycode == KeyEvent.VK_META) metaPressed=false;
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
		if (winPressed) {
			robot.keyRelease(KeyEvent.VK_WINDOWS);
			winPressed=false;
		}
		if (metaPressed) {
			robot.keyRelease(KeyEvent.VK_META);
			metaPressed=false;
		}
	}
	private void mousePressed(int buttons) {
		buttons &= mouseButtonMask;
		synchronized (robot) {
			robot.mousePress(buttons);
		}
	}
	private void mouseReleased(int buttons) {
		buttons &= mouseButtonMask;
		synchronized (robot) {
			robot.mouseRelease(buttons);
		}
		forceActionTm = System.currentTimeMillis()+FORCE_ACTION_TIME;
	}
	private void mouseWheel(int rot) {
		robot.mouseWheel(rot);
		forceActionTm = System.currentTimeMillis()+FORCE_ACTION_TIME;
	}
	private String getUTF(ByteBuffer b) {
		int l=b.getShort();
		if (l < 0) {
			throw new NegativeArraySizeException("s="+l);
		}
		byte[] a = new byte[l];
		b.get(a);
		return new String(a,Env.UTF8_Charset);
	}
	private void setClipboard(ByteBuffer msg) {
		String s = getUTF(msg);
		try {
			Env.setClipboardText(s);
		} catch (Exception e) {
			Log.error(e);
		}
	}
	private void getClipboard(QueueChannel chn) {
		String s = Env.getClipboardText();
		if (s == null) return ;
		ByteArrayOutputStream os = new ByteArrayOutputStream(s.length()*2+2);
		DataOutputStream dos = new DataOutputStream(os);
		try {
			dos.writeShort(RCommand.CLIPBOARD_SET);
			dos.writeUTF(s);
			dos.close();
			write(chn,ByteBuffer.wrap(os.toByteArray()));
		} catch (IOException e) {
			Log.error(e);
		}

	}

	private byte[] buildImageMsg(RenderedImage img, int x, int y, float q) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(img.getWidth()*img.getHeight());
		DataOutputStream dos = new DataOutputStream(os);

		dos.writeShort(RCommand.SCREEN_IMG);
		dos.writeInt(x);
		dos.writeInt(y);

		if (q > 0f && q < 1f) {
			JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
			jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpegParams.setCompressionQuality(q);
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
			writer.setOutput(ImageIO.createImageOutputStream(dos));
			writer.write(null, new IIOImage(img, null, null), jpegParams);
			writer.dispose();
		}
		else {
			ImageIO.write(img, "png", dos);
		}
		dos.close();
		return os.toByteArray();
	}

	private void sendImage(QueueChannel chn, int x, int y, int w, int h, float q) {
		if (w<=0 || h<=0) return ;
		if (x >= screenRect.x+screenRect.width) x=screenRect.x+screenRect.width;
		else if (x < screenRect.x) { w -= x; x = screenRect.x; }
		if (y >= screenRect.y+screenRect.height) y=screenRect.y+screenRect.height;
		else if (y < screenRect.y) { h -= y; y = screenRect.y; }
		if (x+w > screenRect.x+screenRect.width) w = screenRect.x+screenRect.width-x;
		if (y+h > screenRect.y+screenRect.height) h = screenRect.y+screenRect.height-y;

		RenderedImage img = getScreen(x,y,w,h);
		try {
			byte[] ba = buildImageMsg(img, x, y, q);
			write(chn,ByteBuffer.wrap(ba));
		} catch (IOException e) {
			Log.error(e);
		}
	}

	private void sendImageAll(BufferedImage img,int x, int y, float q) {
		if (clients.size() == 0) return ;
		//Log.debug("sendImage to %d",clients.size());

		try {
			byte[] ba=buildImageMsg(img, x, y, q);

			for (Iterator<QueueChannel> i=clients.iterator(); i.hasNext(); ) {
				QueueChannel chn = i.next();
				if (!chn.isOpen()) {
					i.remove();
					continue;
				}
				try {
					write(chn,ByteBuffer.wrap(ba));
				}catch (Throwable e) {
					i.remove();
					Log.error(e);
				}
			}

		} catch (IOException e) {
			Log.error(e);
		}
	}

	static List<Point> pntcache = new ArrayList<>();
	static private Point newPoint(int x, int y) {
		if (pntcache.size() == 0) return new Point(x, y);
		Point p = pntcache.remove(pntcache.size()-1);
		p.setLocation(x, y);
		return p;
	}
	Rectangle box_bfs(BufferedImage t, int x, int y, Rectangle r, int dv) {
		r.setBounds(x,y,0,0);
		List<Point> q=new ArrayList<>();
		t.setRGB(x, y, 0);
		q.add(newPoint(x,y));
		Dimension d=new Dimension(t.getWidth(), t.getHeight());
		while (q.size()>0) {
			Point p=q.remove(0);
			x=p.x; y=p.y;
			pntcache.add(p);
			r.add(x,y);
			if (x>=dv && (t.getRGB(x-dv,y)&0xff)!=0) {t.setRGB(x-dv, y, 0);q.add(newPoint(x-dv, y));}
			if (x+dv<d.width && (t.getRGB(x+dv,y)&0xff)!=0) {t.setRGB(x+dv, y, 0);q.add(newPoint(x+dv, y));}
			if (y>=dv && (t.getRGB(x,y-dv)&0xff)!=0) {t.setRGB(x, y-dv, 0);q.add(newPoint(x, y-dv));}
			if (y+dv<d.height && (t.getRGB(x,y+dv)&0xff)!=0) {t.setRGB(x, y+dv, 0);q.add(newPoint(x, y+dv));}
		}
		r.grow(dv, dv);
		return r;
	}

	void addRoi(List<Rectangle> rois, Rectangle r, int maxw, int maxh) {
		if (r.x < 0) {r.width+=r.x;r.x=0;}
		if (r.y < 0) {r.height+=r.y;r.y=0;}
		if (r.x+r.width > maxw) r.width = maxw-r.x;
		if (r.y+r.height > maxh) r.height = maxh-r.y;
		if (r.width<=0 || r.height<=0) return;

		int g=25;
		r.grow(g, g);
		boolean added=false;
		for (Rectangle rr : rois) {
			if (rr.intersects(r)) {
				r.grow(-g, -g);
				rr.add(r);
				added=true;
			}
		}
		if (!added) {
			r.grow(-g, -g);
			rois.add(new Rectangle(r));
		}
	}
	void detectChanges(BufferedImage p,BufferedImage i) {
		List<Rectangle> rois=new ArrayList<>();

		for (int y=0; y < p.getHeight(); y+=5) {
			for (int x=0; x < p.getWidth(); x+=5) {
				int r=Math.abs(Colors.quick_luminance(p.getRGB(x, y)) - Colors.quick_luminance(i.getRGB(x, y)));
				//int r=Colors.errorSum(p.getRGB(x, y),i.getRGB(x, y));
				if (r<1) r=0;
				else if (r > 255) r=255;
				p.setRGB(x, y, (r<<16)|(r<<8)|r);
			}
		}

		Rectangle radd=new Rectangle(0,0,1,1);
		final int dv=5;
		for (int y=0; y < p.getHeight(); y+=dv) {
			for (int x=0; x < p.getWidth(); x+=dv) {
				if ((p.getRGB(x, y)&0xff)==0) continue;
				box_bfs(p,x,y,radd, dv);
				addRoi(rois,radd,i.getWidth(),i.getHeight());
			}
		}
		for (Rectangle r : rois) {
			if (r.width < 1 || r.height < 1) continue;
			sendImageAll(i.getSubimage(r.x, r.y, r.width, r.height),r.x, r.y, 0.2f);
		}
		rois.clear();
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

		Rectangle rect = new Rectangle(0,0,(int)screenRect.getMaxX(),(int)screenRect.getMaxY());
		screenImg = robot.createScreenCapture(rect);
		Log.info("update rect (%d,%d %dx%d)",rect.x,rect.y,rect.width,rect.height);

		selector.start();
		selector.bind(null, RSERVER_PORT, this);
		Point lastMouseLoc = null;
		while (selector.isRunning()) {
			if (clients.size()>0) {
				BufferedImage i = robot.createScreenCapture(rect);
				BufferedImage p;
				synchronized (this) { p=screenImg; screenImg=i; }
				detectChanges(p,i);
				i=null; p=null;
			}

			if (keepScreenOn && forceActionTm < System.currentTimeMillis()) {
				Point m = MouseInfo.getPointerInfo().getLocation();
				if (lastMouseLoc != null && lastMouseLoc.equals(m)) {
					synchronized (robot) {
						robot.mouseMove(m.x>0?m.x-1:m.x+1, m.y);
						robot.mouseMove(m.x, m.y);
					}
				}
				else lastMouseLoc = m;
				forceActionTm = System.currentTimeMillis()+FORCE_ACTION_TIME;
			}
			XThread.sleep(1000/50);
		}
	}

	public static void main(String[] args) throws Exception {
		Log.setTestMode();
		for (int i=0; i <args.length; ++i) {
			if (args[i].equalsIgnoreCase("keepon")) keepScreenOn=true;
		}
		try {
			new RServer().run();
		} catch (Throwable e) {
			Log.info("rserver finished");
			Log.error(e);
		}
	}
}

