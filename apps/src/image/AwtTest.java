package image;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JFrame;

public class AwtTest {
	public static void main(String[] args) {
		Window w = new Window(null); //no decor
		w.setBounds(200,100,200,150);
		w.setBackground(Color.BLUE);
		w.setVisible(true);

		Frame f = new Frame("Test title"); //close button action = do nothing
		f.setBounds(300,200,300,150);
		f.setVisible(true);


		JFrame jf = new JFrame("Test title"); //close button action = close window/app
		jf.setBounds(300,200,300,150);
		jf.setVisible(true);
	}
}
