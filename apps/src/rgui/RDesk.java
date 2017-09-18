package rgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import netio.ChannelHandler;
import netio.SelectorThread;
import netio.SelectorThread.QueueChannel;
import sys.Env;
import sys.Log;
import sys.XThread;
import ui.ImagePanel;
import ui.MainPanel;
import ui.ImagePanel.ImageUpdate;

@SuppressWarnings("serial")
public class RDesk extends MainPanel {
	final static String fileNameFormat = "screen-%03d.jpg";
	final private SelectorThread selector;

	ByteBuffer inmsg = ByteBuffer.allocate(1024*1024);
	int inlen;
	private final ImagePanel imgPanel = new ImagePanel();
	private final List<ImageUpdate> imgq = new ArrayList<ImageUpdate>();
	QueueChannel qchn;
	Point prevMouseLoc = new Point();
	String Host = null;
	int errCnt = 0;

	Action screen_shot = new AbstractAction("ScreenShot") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			OutputStream os=null;
			try {
				File f = null;
				for (int i=1; ; ++i) {
					f = new File(String.format(fileNameFormat, i));
					if (!f.exists()) break;
				}
				os = new FileOutputStream(f);
				RenderedImage i = (RenderedImage)imgPanel.getImage();
				ImageIO.write(i, "jpg", os);
			}
			catch (Exception e) {
				Log.error(e);
			}
			finally {
				Env.close(os);
			}
		}
	};

	ChannelHandler chnHandler = new ChannelHandler() {
		@Override
		public ChannelHandler createFilter() {
			return null;
		}
		@Override
		public void connected(QueueChannel chn) {
			Log.debug("connected");
			errCnt = 0;
			imgPanel.setImage(null);
			inmsg.clear(); inlen=0;
			sendScreenInfoReq();
			sendScreenReq();
			sendRegister();
		}
		@Override
		public void disconnected(QueueChannel chn, Throwable e) {
			Log.debug("disconnected %s", e==null?"":e.toString());
			qchn = null;
			if (e != null) ++errCnt;
		}

		//TODO use com.net.TcpFilter
		private int readTCP(int limit, ByteBuffer src) {
			if (inmsg.position() + src.remaining() < limit) {
				inmsg.put(src);
			}
			else {
				while (inmsg.position() < limit) inmsg.put(src.get());
			}
			return inmsg.position();
		}
		@Override
		public void received(QueueChannel chn, ByteBuffer buf) {
			int intbytes = 4;
			//must process all data from buf
			while (buf.hasRemaining()) {
				if (inlen==0) {
					if (readTCP(intbytes, buf) < intbytes)
						continue;
					inmsg.flip();
					inlen = inmsg.getInt();
					if (inmsg.capacity() < inlen)
						inmsg = ByteBuffer.allocate(inlen*2);
					inmsg.clear();
					if (inlen==0) {
						inmsg.put((byte)0);
						continue;
					}
					if (inlen < 0) throw new RuntimeException("Message outof sync");
					if (inlen > 10*1024*1024) throw new RuntimeException("Message too long "+inlen);
				}
				if (readTCP(inlen, buf) < inlen) {
					continue;
				}
				inmsg.flip();
				//Log.debug("received all of %d bytes", inlen);
				processMsg(chn);
				inmsg.clear();
				inlen=0;
			}
			if (inlen > 0) {
				//Log.debug("received %d of %d bytes", inmsg.position(),inlen);
			}
		}
		@Override
		public void write(QueueChannel chn, ByteBuffer buf) {
			if (chn==null || !chn.isConnected()) return ;
			ByteBuffer lenbuf = ByteBuffer.allocate(4);
			lenbuf.putInt(buf.remaining());
			lenbuf.flip();
			chn.write(lenbuf);
			chn.write(buf, true);
		}
	};

	public RDesk(String[] args) throws Exception{
		super(new BorderLayout());

		// auto wait can be set only when using robot from new Thread
		//robot.setAutoWaitForIdle(true);
		selector = new SelectorThread();
		selector.start();
		if (args.length > 0) Host = args[0];

		setFocusTraversalKeysEnabled(false);
		setPreferredSize(new Dimension(1000,800));
		add(createScrolledPanel(imgPanel), BorderLayout.CENTER);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				//Log.info("keyType %02x",(int)e.getKeyChar());
				//sendKeyType(e.getKeyChar());
			}
			@Override
			public void keyPressed(KeyEvent e) {
				//Log.info("keyPressed: %d(0x%x)",e.getKeyCode(),e.getKeyCode());
				sendKeyPressed(e.getKeyCode());
			}
			@Override
			public void keyReleased(KeyEvent e) {
				sendKeyReleased(e.getKeyCode());
			}
		});


		MouseAdapter mouseHnadler = new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				sendMouseMove(e.getX(), e.getY());
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				//Log.debug("mouseClick(%d,%d,%x) mod=%x  but=%d",e.getX(), e.getY(), e.getModifiersEx(), e.getModifiers(), e.getButton());
				//sendMouseClick(e.getX(), e.getY(), InputEvent.getMaskForButton(e.getButton()));
			}
			@Override
			public void mousePressed(MouseEvent e) {
				sendMousePressed(InputEvent.getMaskForButton(e.getButton()));
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				sendMouseReleased(InputEvent.getMaskForButton(e.getButton()));
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				sendMouseMove(e.getX(), e.getY());
			}
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				sendWheelMove(e.getWheelRotation());
			}
		};
		imgPanel.addMouseListener(mouseHnadler);
		imgPanel.addMouseMotionListener(mouseHnadler);
		imgPanel.addMouseWheelListener(mouseHnadler);
		imgPanel.setShowRoi(!Log.isRelease());
	}

	@Override
	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("File");
		m.add(new JMenuItem(screen_shot));
		mb.add(m);
		return mb;
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		sendSetClipboard(Env.getClipboardText());
	}
	@Override
	public void windowLostFocus(WindowEvent e) {
		sendGetClipboard();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		super.windowOpened(e);
	}
	@Override
	public void windowClosed(WindowEvent e) {
		super.windowClosed(e);
		Log.debug("stop selector");
		if (qchn!=null) qchn.close();
		selector.stop();
	}

	private String readUTF(ByteBuffer b) {
		int l=b.getShort();
		if (l < 0) {
			throw new NegativeArraySizeException("s="+l);
		}
		byte[] a = new byte[l];
		b.get(a);
		return new String(a,Env.UTF8_Charset);
	}
	private void writeUTF(ByteBuffer b, String s) {
		byte[] a = s.getBytes(Env.UTF8_Charset);
		int l=a.length;
		b.putShort((short)l);
		b.put(a);
	}

	private void processMsg(QueueChannel chn) {
		short cmd = inmsg.getShort();
		//Log.debug("recv cmd = %d, payload %d", cmd, inmsg.remaining());
		if (cmd == RCommand.SCREEN_INFO) {
			String id = readUTF(inmsg);
			int x = inmsg.getInt();
			int y = inmsg.getInt();
			int w = inmsg.getInt();
			int h = inmsg.getInt();
			Log.info("%s: %d %d %d %d",id,x,y,w,h);
			imgPanel.setImage(new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB));

			Rectangle scr = null;
			for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
				scr = gd.getDefaultConfiguration().getBounds();
				break;
			}
			if (scr != null) {
				if (scr.width > w) scr.width=w;
				if (scr.height > h) scr.height=h;
				Dimension size = new Dimension(scr.width, scr.height);
				topFrame().setSize(scr.width+20,scr.height+5);
				setPreferredSize(size);
				//topFrame().pack();
			}
		}
		else if (cmd == RCommand.SCREEN_IMG) {
			int x = inmsg.getInt();
			int y = inmsg.getInt();
			ByteArrayInputStream is = new ByteArrayInputStream(inmsg.array(),inmsg.position(),inmsg.remaining());
			Image i=null;
			try { i = ImageIO.read(is);}
			catch (IOException e) {Log.error(e);}
			if (i!=null) {
				imgq.add(new ImageUpdate(i,x,y));
				if (imgq.size()>1) Log.debug("roiq len=%d",imgq.size());
				//TODO update fullImg in separate thread
				imgPanel.update(imgq);
			}
		}
		else if (cmd == RCommand.CLIPBOARD_SET) {
			String s = readUTF(inmsg);
			try {
				Env.setClipboardText(s);
			} catch (Exception e) {
				Log.error(e);
			}
		}
		else {
			Log.error("unknown cmd:%d, payload %d", cmd, inmsg.remaining());
		}
	}

	private void sendScreenInfoReq() {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putShort(RCommand.SCREEN_INFO);
		b.flip();
		Log.debug("send SCREEN_INFO");
		chnHandler.write(qchn, b);
	}
	private void sendMouseMove(int x,int y) {
		if (qchn==null) return ;
		if (Math.abs(prevMouseLoc.x-x) + Math.abs(prevMouseLoc.y-y) < 5) return ;
		prevMouseLoc.setLocation(x, y);
		ByteBuffer b = ByteBuffer.allocate(14);
		b.putShort(RCommand.MOUSE_MOVE);
		b.putInt(x);
		b.putInt(y);
		b.flip();
		//Log.debug("send MOUSE_MOVE");
		chnHandler.write(qchn, b);
	}
	private void sendMouseClick(int x,int y,int button) {
		ByteBuffer b = ByteBuffer.allocate(14);
		b.putShort(RCommand.MOUSE_CLICK);
		b.putInt(x);
		b.putInt(y);
		b.putInt(button);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendKeyType(char c) {
		sendKeyType(new String(new char[]{c}));
	}
	private void sendKeyType(String s) {
		byte[] a = s.getBytes(Env.UTF8_Charset);
		ByteBuffer b = ByteBuffer.allocate(2+a.length);
		b.putShort(RCommand.TEXT_TYPE);//key type
		b.put(a, 0, a.length);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendScreenReq() {
		int w=getWidth(), h=getHeight();
		if (w<=0 || h <=0) return ;
		ByteBuffer b = ByteBuffer.allocate(10);
		b.putShort(RCommand.SCREEN_IMG);
		b.putShort((short)0);
		b.putShort((short)0);
		b.putFloat(1f);
		b.flip();
		Log.debug("send SCREEN_IMG");
		chnHandler.write(qchn, b);
	}
	private void sendRegister() {
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort(RCommand.CLIENT_REGISTER);
		b.flip();
		Log.debug("send CLIENT_REGISTER");
		chnHandler.write(qchn, b);
	}
	private void sendKeyPressed(int keycode) {
		//Log.debug("keypress %d(%x)",keycode,keycode);
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort(RCommand.KEY_PRESS);
		b.putInt(keycode);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendKeyReleased(int keycode) {
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort(RCommand.KEY_RELEASE);
		b.putInt(keycode);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendMousePressed(int buttons) {
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort(RCommand.MOUSE_PRESS);
		b.putInt(buttons);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendMouseReleased(int buttons) {
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort(RCommand.MOUSE_RELEASE);
		b.putInt(buttons);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendWheelMove(int rot) {
		if (qchn==null) return ;
		ByteBuffer b = ByteBuffer.allocate(14);
		b.putShort(RCommand.MOUSE_WHEEL);
		b.putInt(rot);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendSetClipboard(String t) {
		if (t==null) return ;
		ByteBuffer b = ByteBuffer.allocate(4+t.length()*4);
		b.putShort(RCommand.CLIPBOARD_SET);
		writeUTF(b, t);
		b.flip();
		Log.debug("send CLIPBOARD_SET");
		chnHandler.write(qchn, b);
	}

	private void sendGetClipboard() {
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort(RCommand.CLIPBOARD_GET);
		b.flip();
		//Log.debug("send CLIPBOARD_GET");
		chnHandler.write(qchn, b);
	}

	private void keep_connected() throws Exception {
		while (selector.isRunning()) {
			if (qchn==null) {
				SelectionKey sk = selector.connect(Host, 3367, chnHandler);
				qchn = (QueueChannel)sk.attachment();
			}
			if (errCnt >= 3) break;
			if (errCnt > 0) XThread.sleep(5000);
			else XThread.sleep(1000);
		}
		selector.stop();
	}

	public static void main(String[] args) {
		Log.setTestMode();
		//Log.setReleaseMode();
		RDesk desk = (RDesk)start(RDesk.class, args);
		try {
			desk.keep_connected();
		}
		catch (Throwable e) {
			Log.error(e);
		}
	}
}
