package test_SO;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class OP_41062672 extends JPanel {

	public OP_41062672() {
		//setBackground(new Color(255, 0, 0, 255));
		setBackground(new Color(0,true));
		setPreferredSize(new Dimension(400,400));
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		g.setColor(Color.RED);
		g.drawOval(0, 0, getWidth()-1, getHeight()-1);
	}

	public static void main(String[] args) {
		JFrame f=new JFrame();
		f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		f.setUndecorated(true);
		f.setBackground(new Color(0,true));
		//f.setOpacity(1f);
		f.setContentPane(new OP_41062672());
		f.pack();
		f.setVisible(true);
	}

}
