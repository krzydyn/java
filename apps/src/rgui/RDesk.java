package rgui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;

import javax.swing.JButton;

import net.ChannelStatusHandler;
import net.ChannelWriter;
import net.SelectorThread2;
import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class RDesk extends MainPanel implements ActionListener, ChannelStatusHandler{
	final SelectorThread2 selector;

	private boolean running=false;
	private Image img;
	private boolean paintDone=false;

	public RDesk() throws Exception{
		super(null);
		// auto wait can be set only whan using robot from new Thread
		//robot.setAutoWaitForIdle(true);
		selector = new SelectorThread2();
		selector.start();
		//selector.connect("192.168.1.110", 3367, this);
		selector.connect("127.0.0.1", 3367, this);

		setPreferredSize(new Dimension(2000,800));

		JButton b;
		add(b=new JButton("test"));
		b.setBounds(0, 0, 50, 20);
		b.addActionListener(this);
	}
	@Override
	protected void paintComponent(Graphics g) {
		//super.paintComponent(g);
		if (img!=null) g.drawImage(img, 0, 0, null);
		paintDone=true;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	}

	@Override
	protected void windowClosed() {
		running=false;
		selector.stop();
	}
	@Override
	public void connected(SelectorThread2 st, ChannelWriter w) {
		Log.debug("connected");
	}
	@Override
	public void received(SelectorThread2 st, ChannelWriter w, ByteBuffer buf) {
		Log.debug("received '%s'", new String(buf.array()));
	}

	public static void main(String[] args) {
		start(RDesk.class);
	}
}
