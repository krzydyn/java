package rgui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.JButton;

import net.ChannelHandler;
import net.SelectorThread2;
import net.SelectorThread2.QueueChannel;
import sys.Log;
import sys.XThread;
import ui.MainPanel;

@SuppressWarnings("serial")
public class RDesk extends MainPanel implements ActionListener, ChannelHandler{
	final SelectorThread2 selector;

	ByteBuffer inmsg = ByteBuffer.allocate(1024*1024);
	int inlen;
	private Image img;
	private boolean paintDone=false;
	QueueChannel chn;
	int pendingReq=0;

	public RDesk() throws Exception{
		super(null);
		// auto wait can be set only whan using robot from new Thread
		//robot.setAutoWaitForIdle(true);
		selector = new SelectorThread2();
		selector.start();
		//selector.connect("192.168.1.110", 3367, this);
		chn = (QueueChannel)selector.connect("localhost", 3367, this).attachment();

		setPreferredSize(new Dimension(1600,800));

		JButton b;
		add(b=new JButton("test"));
		b.setBounds(0, 0, 50, 20);
		b.addActionListener(this);
	}
	@Override
	protected void paintComponent(Graphics g) {
		Image i = null;
		synchronized (this) {
			i=img;
		}
		if (i!=null) g.drawImage(i, 0, 0, null);
		paintDone=true;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		getScreenReq();
	}

	@Override
	protected void windowOpened() {
		new Thread() {
			@Override
			public void run() {
				while (selector.isRunning()) {
					if (pendingReq < 2) {
						getScreenReq();
						XThread.sleep(200);
					}
					else XThread.sleep(100);
				}
			}
		}.start();
	}
	@Override
	protected void windowClosed() {
		Log.debug("stop selector");
		selector.stop();
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
	private void writeTCP(QueueChannel chn, ByteBuffer b) {
		ByteBuffer lenbuf = ByteBuffer.allocate(4);
		lenbuf.putInt(b.remaining());
		lenbuf.flip();
		chn.write(lenbuf);
		chn.write(b);
	}

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
	public void disconnected(QueueChannel chnst) {
		Log.debug("disconnected");
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
	public void write(QueueChannel qchn, ByteBuffer buf) {
		writeTCP(qchn, buf);
	}
	private void processMsg(QueueChannel chn) {
		short type = inmsg.getShort();
		//Log.debug("msgtype = %d, payload %d", type, inmsg.remaining());
		if (type == 0) {
		}
		else if (type == 4) {
			--pendingReq;
			ByteArrayInputStream is = new ByteArrayInputStream(inmsg.array(),inmsg.position(),inmsg.remaining());
			Image i=null;
			try { i = ImageIO.read(is);}
			catch (IOException e) {Log.error(e);}
			synchronized (this) {
				if (i!=null) img=i;
			}
			if (paintDone || pendingReq==0)
				repaint();
		}
	}

	private void getScreenReq() {
		++pendingReq;
		ByteBuffer b = ByteBuffer.allocate(10);
		b.putShort((short)4);//read screen
		b.putShort((short)getWidth());
		b.putShort((short)getHeight());
		b.putFloat(0.5f);
		b.flip();
		writeTCP(chn, b);
	}

	public static void main(String[] args) {
		start(RDesk.class);
	}
}
