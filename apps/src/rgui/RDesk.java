package rgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import netio.ChannelHandler;
import netio.SelectorThread;
import netio.SelectorThread.QueueChannel;
import rgui.ImagePanel.ImageBox;
import sys.Env;
import sys.Log;
import sys.XThread;
import ui.MainPanel;

@SuppressWarnings("serial")
public class RDesk extends MainPanel {
	final SelectorThread selector;

	ByteBuffer inmsg = ByteBuffer.allocate(1024*1024);
	int inlen;
	private final ImagePanel imgPanel = new ImagePanel();
	private final List<ImageBox> imgq = new ArrayList<ImageBox>();
	QueueChannel qchn;
	Point prevMouseLoc = new Point();
	String Host = null;
	int errCnt = 0;

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
		public void disconnected(QueueChannel chn, Throwable thr) {
			Log.debug("disconnected");
			qchn = null;
			if (thr != null) ++errCnt;
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
		new Thread("ConnectKeeper") {
			@Override
			public void run() {
				keep_connected();
			}
		}.start();
	}
	@Override
	public void windowClosed(WindowEvent e) {
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
		//Log.debug("cmd = %d, payload %d", cmd, inmsg.remaining());
		if (cmd == RCommand.SCREEN_INFO) {
			String id = readUTF(inmsg);
			int x = inmsg.getInt();
			int y = inmsg.getInt();
			int w = inmsg.getInt();
			int h = inmsg.getInt();
			Log.info("%s: %d %d %d %d",id,x,y,w,h);

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
				if (imgPanel.getImage()==null) {
					imgPanel.setImage(i);
					Log.debug("recv fullscr %d,%d,%d,%d  bytes=%d",x,y,i.getWidth(null),i.getHeight(null),inmsg.remaining());
				}
				else {
					imgq.add(new ImageBox(i,x,y));
					if (imgq.size()>1) Log.debug("roiq len=%d",imgq.size());
				}
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
		b.putShort((short)3000);
		b.putShort((short)2000);
		b.putFloat(0.2f);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendRegister() {
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort(RCommand.CLIENT_REGISTER);
		b.flip();
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
		chnHandler.write(qchn, b);
	}

	private void sendGetClipboard() {
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort(RCommand.CLIPBOARD_GET);
		b.flip();
		chnHandler.write(qchn, b);
	}

	private void keep_connected() {
		int cnt=0;
		while (selector.isRunning()) {
			++cnt;
			if (qchn==null) {
				try {
					SelectionKey sk = selector.connect(Host, 3367, chnHandler);
					qchn = (QueueChannel)sk.attachment();
				} catch (IOException e) {
					Log.error(e);
					break;
				}
			}
			if (errCnt >= 3) break;
			if (errCnt > 0) XThread.sleep(5000);
			else XThread.sleep(1000);
			if (cnt>10) {
				if (qchn!=null) qchn.close();
				cnt=0;
			}
		}
		selector.stop();
	}

	public static void main(String[] args) {
		start(RDesk.class, args);
	}
}
