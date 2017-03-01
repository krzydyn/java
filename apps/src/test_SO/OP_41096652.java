package test_SO;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OP_41096652 {
	@SuppressWarnings("serial")
	static public class IconPanel extends JPanel {
		private ImageIcon characterIntro;
		private ImageIcon characterIdle;
		private ImageIcon characterAttack;
		private ImageIcon characterJump;
		private ImageIcon[] ico;

		private JLabel lbl = new JLabel();
		private boolean running=true;

		public IconPanel() {
			super(new GridLayout(1,0));
			try { loadIcons(this); } catch (IOException e) {e.printStackTrace();}
			lbl.setPreferredSize(new Dimension(50, 50));
			add(lbl);

			new Thread(){
				@Override
				public void run() {
					int[] idxrep = {0,1,2,1,3};
					int[] dlyrep = {5,3,6,5,3};

					int idx=0;
					while (running) {
						lbl.setIcon(ico[idxrep[idx]]);
						try {Thread.sleep(dlyrep[idx]*100);}catch(Exception e){}

						++idx;
						if (idx == idxrep.length) idx=1;
					}
				}
			}.start();
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		final IconPanel p = new IconPanel();
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				p.running=false;
			}
		});
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
	}

	private static ImageIcon readIcon(String file) throws IOException {
		Image i = ImageIO.read(new File(file));
		return new ImageIcon(i.getScaledInstance(50, 50, BufferedImage.TYPE_INT_RGB));
	}
	private static void loadIcons(IconPanel p) throws IOException {
		p.characterIntro = readIcon("res/skul1.png");
		p.characterIdle = readIcon("res/skul2.png");
		p.characterAttack = readIcon("res/skul3.png");
		p.characterJump = readIcon("res/skul4.png");
		p.ico = new ImageIcon[]{p.characterIntro, p.characterIdle, p.characterAttack, p.characterJump};
	}
}
