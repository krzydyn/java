package test;

import java.awt.Rectangle;
import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.jnativehook.mouse.NativeMouseMotionListener;


public class RobotTest implements NativeMouseListener, NativeMouseMotionListener {
	final private Robot robot;
	boolean running=false;

	public RobotTest() {
		Robot r=null;
		//Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
		try {
			//reset log
			LogManager.getLogManager().reset();
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.OFF);
			
			r=new Robot();
			GlobalScreen.registerNativeHook();
		} catch (Exception e) {
			e.printStackTrace();
		}
		robot=r;
	}
	
	public void loop() {
		Rectangle scr=new Rectangle(0,0,300,300);
		running = robot!=null;
		
		GlobalScreen.getInstance().addNativeMouseListener(this);
		GlobalScreen.getInstance().addNativeMouseMotionListener(this);
		while (running) {
			robot.createScreenCapture(scr);
			try {
				Thread.sleep(500);
				System.out.printf("loop\n");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		running = false;
		GlobalScreen.getInstance().removeNativeMouseListener(this);
		GlobalScreen.getInstance().removeNativeMouseMotionListener(this);
		//cleanup
		GlobalScreen.unregisterNativeHook();		
		System.out.printf("cleanup\n");
	}
	
	@Override
	public void nativeMouseClicked(NativeMouseEvent ev) {
		System.out.printf("mouse cli %s\n", ev);
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent ev) {
		System.out.printf("mouse pre %s\n", ev);
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent ev) {
		System.out.printf("mouse rel %s\n", ev);
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent arg0) {
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent ev) {
		System.out.printf("mouse move %d,%d\n", ev.getX(),ev.getY());
		if (ev.getX()<2 && ev.getY()<2) {
			System.out.printf("set run false\n");
			running = false;
		}
	}

	public static void main(String args[]){
		new RobotTest().loop();
		System.out.printf("main done\n");
	}

}
