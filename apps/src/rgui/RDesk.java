package rgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.ChannelHandler;
import net.SelectorThread2;
import net.SelectorThread2.QueueChannel;
import sys.Log;
import sys.XThread;
import text.Text;
import ui.MainPanel;

@SuppressWarnings("serial")
public class RDesk extends MainPanel {
	final SelectorThread2 selector;

	ByteBuffer inmsg = ByteBuffer.allocate(1024*1024);
	int inlen;
	private final Object imgLock = new Object();
	private Image imgFull;
	private Image imgGray;
	private List<ImageBox> imgq = new ArrayList<ImageBox>();
	private List<Rectangle> activeBox = new ArrayList<Rectangle>();
	QueueChannel qchn;
	Point prevMouseLoc = new Point();
	String Host = null;

	static class ImageBox {
		Image i;
		int x,y,w,h;
		public ImageBox(Image i, int x, int y, int w, int h) {
			this.i=i;
			this.x=x; this.y=y;
			this.w=w; this.h=h;
		}
	}
	ChannelHandler chnHandler = new ChannelHandler() {
		@Override
		public ChannelHandler createFilter() {
			return null;
		}
		@Override
		public void connected(QueueChannel chn) {
			Log.debug("connected");
			inmsg.clear();
			inlen=0;
			sendScreenInfoReq();
		}
		@Override
		public void disconnected(QueueChannel chn) {
			Log.debug("disconnected");
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
			if (!chn.isConnected() && chn.queueSize() > 0) return ;
			ByteBuffer lenbuf = ByteBuffer.allocate(4);
			lenbuf.putInt(buf.remaining());
			lenbuf.flip();
			chn.write(lenbuf);
			chn.write(buf, true);
		}
	};

	public RDesk(String[] args) throws Exception{
		super(null);

		// auto wait can be set only when using robot from new Thread
		//robot.setAutoWaitForIdle(true);
		selector = new SelectorThread2();
		selector.start();
		if (args.length > 0) Host = args[0];
		//qchn = (QueueChannel)selector.connect("106.120.52.62", 3367, chnHandler).attachment();
		qchn = (QueueChannel)selector.connect(Host, 3367, chnHandler).attachment();

		setPreferredSize(new Dimension(1600,800));

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				//Log.info("keyType %02x",(int)e.getKeyChar());
				//sendKeyType(e.getKeyChar());
			}
			@Override
			public void keyPressed(KeyEvent e) {
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
		addMouseListener(mouseHnadler);
		addMouseMotionListener(mouseHnadler);
		addMouseWheelListener(mouseHnadler);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Image ifu=null, ig;
		int l = 0;
		synchronized (imgLock) {ifu=imgFull; ig=imgGray; l=imgq.size();}
		if (ifu != null) {
			if (l > 0) {
				activeBox.clear();
				Graphics gg = imgFull.getGraphics();
				for (int i=0; i < l; ++i) {
					ImageBox ib = imgq.get(i);
					gg.drawImage(ib.i, ib.x, ib.y, null);
					activeBox.add(new Rectangle(ib.x, ib.y, ib.w, ib.h));
				}
				gg.dispose();
				synchronized (imgLock) {
					while (l>0) {imgq.remove(0); --l;}
				}
			}
			g.drawImage(ifu, 0, 0, null);
			g.setColor(Color.RED);
			for (Rectangle r : activeBox) {
				g.drawRect(r.x, r.y, r.width, r.width);
			}
		}
		if (ig!=null) {
			g.drawImage(ig, 0, 0, null);
		}
	}


	@Override
	protected void windowOpened() {
		new Thread("PollScreen") {
			@Override
			public void run() {
				while (selector.isRunning()) {
					if (!qchn.isOpen()) qchn=null;
					if (qchn == null) {
						try {
							qchn = (QueueChannel)selector.connect(Host, 3367, chnHandler).attachment();
						} catch (IOException e) {
							Log.error(e);
							break;
						}
					}

					if (imgFull == null) {
						sendScreenReq();
					}
					XThread.sleep(1000);
				}
				selector.stop();
			}
		}.start();
	}
	@Override
	protected void windowClosed() {
		Log.debug("stop selector");
		selector.stop();
	}

	private String getUTF(ByteBuffer b) {
		int l=b.getShort();
		byte[] a = new byte[l];
		b.get(a);
		return new String(a,Text.UTF8_Charset);
	}

	private void processMsg(QueueChannel chn) {
		short cmd = inmsg.getShort();
		//Log.debug("msgtype = %d, payload %d", type, inmsg.remaining());
		if (cmd == RCommand.SCREEN_INFO) {
			String id = getUTF(inmsg);
			int x = inmsg.getInt();
			int y = inmsg.getInt();
			int w = inmsg.getInt();
			int h = inmsg.getInt();
			Log.info("%s: %d %d %d %d",id,x,y,w,h);
			sendRegister();
		}
		else if (cmd == RCommand.SCREEN_IMG) {
			int x = inmsg.getInt();
			int y = inmsg.getInt();
			ByteArrayInputStream is = new ByteArrayInputStream(inmsg.array(),inmsg.position(),inmsg.remaining());
			Image i=null;
			try { i = ImageIO.read(is);}
			catch (IOException e) {Log.error(e);}
			if (i!=null) {
				Log.debug("recv img %d,%d,%d,%d  bytes=%d",x,y,i.getWidth(null),i.getHeight(null),inmsg.remaining());
				synchronized (imgLock) {
					if (imgFull==null) {imgFull=i;}
					else {
						if (x==0 && y==0 && i.getWidth(null)==200) imgGray=i;
						else imgq.add(new ImageBox(i,x,y,i.getWidth(null),i.getHeight(null)));
					}
				}
				repaint(100);
				//if (paintDone) repaint();
				//else Log.error("prev repaint not finished");
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
		if (!qchn.isConnected()) return ;
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
		byte[] a = s.getBytes(Text.UTF8_Charset);
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
		b.putShort((short)w);
		b.putShort((short)h);
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
		if (!qchn.isOpen()) return ;
		ByteBuffer b = ByteBuffer.allocate(14);
		b.putShort(RCommand.MOUSE_WHEEL);
		b.putInt(rot);
		b.flip();
		chnHandler.write(qchn, b);
	}

	public static void main(String[] args) {
		start(RDesk.class, args);
	}
}
