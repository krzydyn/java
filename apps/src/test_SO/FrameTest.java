package test_SO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class FrameTest {

	static public class Screen extends JPanel implements Runnable {

	Thread thread = new Thread(this);
	JFrame frame;

	private int fps = 0; 
	public int scene = 0;
	public boolean running = false;

	public Screen(JFrame frame){
	    this.frame = frame;
	    this.frame.addKeyListener(new KeyHandler(this));
	    setPreferredSize(new Dimension(200, 200));
	}
	public void start() {thread.start();}

	public void paintComponent(Graphics g){
	    g.clearRect(0, 0, this.frame.getWidth(),this.frame.getHeight());

	    if (scene == 0) {
	        g.setColor(Color.BLUE);
	    } else if (scene == 1) {
	        g.setColor(Color.GREEN);
	    } else {
	        g.setColor(Color.white);
	    }
	    g.fillRect(0, 0, getWidth(), getHeight());
	    
	    g.setColor(Color.WHITE);
	    g.drawString(fps + "", 10, 10);
	}


	public void run() {
	   System.out.println("[Success] Frame Created!");

	   long lastFrame = System.currentTimeMillis();
	   int frames = 0;

	   running = true;
	   scene = 0;

	   while(running){
	       repaint();
	       frames++;

	       if(System.currentTimeMillis() - 1000 >= lastFrame){
	           fps = frames;
	           frames = 0;
	           lastFrame = System.currentTimeMillis();
	       }

	       try {
	           Thread.sleep(2);
	       } catch (InterruptedException ex) {
	           ex.printStackTrace();
	       }
	   }
	   System.exit(0);
	}

	public class KeyTyped{
	    public void keySPACE() {
	        scene = 1;
	    }
	    public void keyESC(){
	        running = false;
	    }
	}
	}
	
	static class KeyHandler implements KeyListener {

		public Screen screen;
		public Screen.KeyTyped keyTyped;

		public KeyHandler(Screen screen){
		    this.screen = screen;
		    this.keyTyped = this.screen.new KeyTyped();

		}

		public void keyPressed(KeyEvent e) {
		    int keyCode = e.getKeyCode();

		    System.out.println(keyCode);

		    if(keyCode == 27){
		        this.keyTyped.keyESC();
		    }        
		    if(keyCode == 32){
		        this.keyTyped.keySPACE();
		    }
		}

		public void keyReleased(KeyEvent e) {}

		public void keyTyped(KeyEvent e) {}
	}

	public static void main(String[] args) {
		Screen s;
		JFrame f=new JFrame();
		f.add(s=new Screen(f));
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		s.start();
	}
}
