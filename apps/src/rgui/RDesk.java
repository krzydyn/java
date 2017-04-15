package rgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
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
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import net.ChannelHandler;
import net.SelectorThread2;
import net.SelectorThread2.QueueChannel;
import sys.Env;
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
	private final List<ImageBox> imgq = new ArrayList<ImageBox>();
	private final List<ImageBox> rois = new ArrayList<ImageBox>();
	QueueChannel qchn;
	Point prevMouseLoc = new Point();
	String Host = null;

	static class ImageBox {
		Image i;
		int x,y,w,h;
		long tm;
		public ImageBox(Image i, int x, int y) {
			this.i=i;
			this.x=x; this.y=y;
			this.w=i.getWidth(null); this.h=i.getHeight(null);
			this.tm=System.currentTimeMillis();
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
			imgFull = null;
			rois.clear();
			inmsg.clear(); inlen=0;
			sendScreenInfoReq();
			sendScreenReq();
		}
		@Override
		public void disconnected(QueueChannel chn) {
			qchn = null;
			Log.debug("disconnected");
			rois.clear();
			repaint();
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
			if (chn==null) return ;
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

		setPreferredSize(new Dimension(1600,800));
		setFocusTraversalKeysEnabled(false);

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
		addMouseListener(mouseHnadler);
		addMouseMotionListener(mouseHnadler);
		addMouseWheelListener(mouseHnadler);
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
	protected void paintComponent(Graphics g) {
		Image ifu;
		synchronized (imgLock) {ifu=imgFull;}

		int maxx=3000, maxy=2000;
		if (ifu != null) {
			g.drawImage(ifu, 0, 0, null);
			maxx=ifu.getWidth(null);
			maxy=ifu.getHeight(null);
		}

		int mx=getWidth()/2;
		int my=getHeight()/2;
		synchronized (imgLock) {
			for (Iterator<ImageBox> i=rois.iterator(); i.hasNext(); ) {
				ImageBox r = i.next();
				if (r.tm + 1000 < System.currentTimeMillis()) i.remove();
			}
			g.setColor(Color.GREEN);
			g.fillRect(mx-2, my-10, 25,15);
			g.setColor(Color.BLACK);
			g.drawString(String.format("%d",rois.size()), mx, my);
			for (ImageBox r : rois) {
				if (r.x+r.w > maxx || r.y+r.h > maxy) {
					g.setColor(Color.RED);
					//Log.error("rect out of range: %s, (%d,%d)",r,maxx,maxy);
				}
				else
					g.setColor(Color.GREEN);
				g.drawRect(r.x, r.y, r.w, r.h);
				g.drawLine(mx, my, r.x+r.w/2, r.y+r.h/2);
			}
		}
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
		selector.stop();
	}

	private String readUTF(ByteBuffer b) {
		int l=b.getShort();
		if (l < 0) {
			throw new NegativeArraySizeException("s="+l);
		}
		byte[] a = new byte[l];
		b.get(a);
		return new String(a,Text.UTF8_Charset);
	}
	private void writeUTF(ByteBuffer b, String s) {
		byte[] a = s.getBytes(Text.UTF8_Charset);
		int l=a.length;
		b.putShort((short)l);
		b.put(a);
	}

	void updateRoi() {
		if (imgFull == null) {
			Log.error("updateRoi when ingFull is null");
			return ;
		}
		synchronized (imgLock) {
			int l=imgq.size();
			Graphics g = imgFull.getGraphics();
			for (int i=0; i < l; ++i) {
				ImageBox ib = imgq.get(i);
				g.drawImage(ib.i, ib.x, ib.y, null);
				ib.i=null;
				rois.add(ib);
			}
			g.dispose();
			while (l>0) {imgq.remove(0); --l;}
		}
		repaint(100);
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
		}
		else if (cmd == RCommand.SCREEN_IMG) {
			int x = inmsg.getInt();
			int y = inmsg.getInt();
			ByteArrayInputStream is = new ByteArrayInputStream(inmsg.array(),inmsg.position(),inmsg.remaining());
			Image i=null;
			try { i = ImageIO.read(is);}
			catch (IOException e) {Log.error(e);}
			if (i!=null) {
				synchronized (imgLock) {
					if (imgFull==null) {
						imgFull = i;
						Log.debug("recv fullscr %d,%d,%d,%d  bytes=%d",x,y,i.getWidth(null),i.getHeight(null),inmsg.remaining());
						sendRegister();
					}
					else {
						if (x==0 && y==0 && i.getWidth(null)==200) imgGray=i;
						else {
							//Log.debug("recv roi %d,%d,%d,%d  bytes=%d",x,y,i.getWidth(null),i.getHeight(null),inmsg.remaining());
							imgq.add(new ImageBox(i,x,y));
							if (imgq.size()>1) Log.debug("roiq len=%d",imgq.size());
						}
					}
				}
				//TODO update fullImg in separate thread
				updateRoi();
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
		while (selector.isRunning()) {
			if (qchn==null) {
				try {
					SelectionKey sk = selector.connect(Host, 3367, chnHandler);
					qchn=(QueueChannel)sk.attachment();
				} catch (IOException e) {
					Log.error(e);
					break;
				}
			}

			XThread.sleep(1000);
		}
		selector.stop();
	}

	public static void main(String[] args) {
		start(RDesk.class, args);
	}
}
