package rgui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

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
	private Image img;
	private boolean paintDone=false;
	QueueChannel qchn;
	int pendingReq=0;

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
		}
		@Override
		public void disconnected(QueueChannel chn) {
			Log.debug("disconnected");
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
				}
				if (readTCP(inlen, buf) < inlen) {
					continue;
				}
				inmsg.flip();
				processMsg(chn);
				inmsg.clear();
				inlen=0;
			}
			if (inlen > 0) {
				//Log.debug("read %d of %d bytes", inmsg.position(),inlen);
			}
		}
		@Override
		public void write(QueueChannel chn, ByteBuffer buf) {
			ByteBuffer lenbuf = ByteBuffer.allocate(4);
			lenbuf.putInt(buf.remaining());
			lenbuf.flip();
			chn.write(lenbuf);
			chn.write(buf);
		}
	};

	public RDesk() throws Exception{
		super(null);
		// auto wait can be set only whan using robot from new Thread
		//robot.setAutoWaitForIdle(true);
		selector = new SelectorThread2();
		selector.start();
		//qchn = (QueueChannel)selector.connect("localhost", 3367, chnHandler).attachment();
		qchn = (QueueChannel)selector.connect("106.120.52.62", 3367, chnHandler).attachment();

		setPreferredSize(new Dimension(1600,800));

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				//Log.info("keyType %02x",(int)e.getKeyChar());
				//sendKeyType(e.getKeyChar());
			}
			@Override
			public void keyPressed(KeyEvent e) {
				Log.info("keyPressed #%02X",e.getKeyCode());
				sendKeyPressed(e.getKeyCode());
			}
			@Override
			public void keyReleased(KeyEvent e) {
				Log.info("keyReleased #%02X",e.getKeyCode());
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
		Image i = null;
		synchronized (imgLock) { i=img; }
		if (i!=null) g.drawImage(i, 0, 0, null);
		paintDone=true;
	}

	@Override
	protected void windowOpened() {
		new Thread("PollScreen") {
			@Override
			public void run() {
				while (selector.isRunning() && qchn.isOpen()) {
					if (pendingReq < 2) getScreenReq();
					else XThread.sleep(20);
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


	private void processMsg(QueueChannel chn) {
		short cmd = inmsg.getShort();
		//Log.debug("msgtype = %d, payload %d", type, inmsg.remaining());
		if (cmd == 0) {
		}
		else if (cmd == 4) {
			--pendingReq;
			ByteArrayInputStream is = new ByteArrayInputStream(inmsg.array(),inmsg.position(),inmsg.remaining());
			Image i=null;
			try { i = ImageIO.read(is);}
			catch (IOException e) {Log.error(e);}
			if (i!=null) {
				synchronized (imgLock) { img=i; }
				if (paintDone) {
					paintDone=false;
					repaint();
				}
			}
		}
	}

	private void sendMouseMove(int x,int y) {
		if (!qchn.isOpen()) return ;
		ByteBuffer b = ByteBuffer.allocate(14);
		b.putShort((short)1);//mouse move
		b.putInt(x);
		b.putInt(y);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendMouseClick(int x,int y,int button) {
		ByteBuffer b = ByteBuffer.allocate(14);
		b.putShort((short)2);//mouse click
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
		b.putShort((short)3);//key type
		b.put(a, 0, a.length);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void getScreenReq() {
		++pendingReq;
		ByteBuffer b = ByteBuffer.allocate(10);
		b.putShort((short)4);//read screen
		b.putShort((short)getWidth());
		b.putShort((short)getHeight());
		b.putFloat(0.5f);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendKeyPressed(int keycode) {
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort((short)6);//key pressed
		b.putInt(keycode);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendKeyReleased(int keycode) {
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort((short)7);//key released
		b.putInt(keycode);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendMousePressed(int buttons) {
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort((short)8);//key pressed
		b.putInt(buttons);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendMouseReleased(int buttons) {
		ByteBuffer b = ByteBuffer.allocate(2+4);
		b.putShort((short)9);//key released
		b.putInt(buttons);
		b.flip();
		chnHandler.write(qchn, b);
	}
	private void sendWheelMove(int rot) {
		if (!qchn.isOpen()) return ;
		ByteBuffer b = ByteBuffer.allocate(14);
		b.putShort((short)10);//mouse move
		b.putInt(rot);
		b.flip();
		chnHandler.write(qchn, b);
	}

	public static void main(String[] args) {
		start(RDesk.class);
	}
}
