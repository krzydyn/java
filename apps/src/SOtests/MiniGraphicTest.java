package SOtests;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MiniGraphicTest extends JPanel{

	private static final long serialVersionUID = 1L;

	public int x,y;
	public MiniGraphicTest() {
	    super();
	    x = -1;
	    y = -1;
	    addMouseMotionListener(new MouseMotionAdapter() {
	        @Override
	        public void mouseMoved(MouseEvent m){
	            x = m.getX();
	            y = m.getY();
	            repaint();
	        }
	    });
	}
	@Override
	protected void paintComponent(Graphics g){
	    super.paintComponent(g);
	    //////////////////////
	    //DO NOT MODIFY HERE//
	    //////////////////////
	    //Draw gridline
	    int width = this.getWidth();
	    int height = this.getHeight();
	    g.setColor(Color.BLACK);
	    for(int i=100;i<width;i+=100){
	        g.drawLine(i, 0, i, height);
	    }
	    for(int i=100;i<height;i+=100){
	        g.drawLine(0, i, width, i);
	    }
	    //////////////////////
	    //put test code here//
	    //////////////////////
	    if(x == -1&&y==-1) return;//initially draw nothing
	    g.fillOval(x-25, y-25, 50, 50);
	}
	//main function is just showing the panel. nothing special
	public static void main(String[] args) {
	    JFrame jf = new JFrame("Test");
	    MiniGraphicTest test = new MiniGraphicTest();
	    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    test.setPreferredSize(new Dimension(300, 300));
	    jf.setResizable(false);
	    jf.setLocation(100,100);
	    jf.add(test);
	    jf.pack();
	    jf.setVisible(true);
	}
}