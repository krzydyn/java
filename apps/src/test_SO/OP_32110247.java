package test_SO;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class OP_32110247 extends JPanel {
	private final JLabel l = new JLabel();
	final BufferedImage image;
	public OP_32110247(String imgfile, String txt) throws IOException {
		image = ImageIO.read(new URL(imgfile));
		l.setText(txt);
		l.setFont(getFont().deriveFont(Font.BOLD,30f));
		l.setSize(l.getPreferredSize());
		l.setForeground(Color.GREEN);
		Dimension d = new Dimension(image.getWidth(), image.getHeight());
		setPreferredSize(d);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension d = getSize();
		g.drawImage(image, 0, 0, null);
		//place text in center of image
		g.translate((d.width-l.getWidth())/2, (d.height-l.getHeight())/2);
		l.paint(g);
	}

	public static void main(String[] args) throws IOException {
		String txt = "<html>line1<br>line2";
		String image = "http://kysoft.pl/proj/java/j+c.png";
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		f.setContentPane(new OP_32110247(image,txt));
		f.pack();
		f.setVisible(true);
	}
}
