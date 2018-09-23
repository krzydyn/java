package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import puzzles.GameBoard.Rect;
import puzzles.GameBoard.Sheet;

@SuppressWarnings("serial")
public class RectsPanel extends JPanel {
	private volatile boolean ready=true;
	private final Object readyLock = new Object();
	private final List<Rect> list = new ArrayList<Rect>();
	private Sheet sheet;
	public RectsPanel(Sheet sheet) {
		super(null);
		this.sheet=sheet;
	}
	public RectsPanel() { this(null); }

	public void setSheet(Sheet sheet) {
		this.sheet=sheet;
	}
	public boolean updateIfReady(List<Rect> l) {
		if (!ready) return false;
		update(l);
		return true;
	}
	public void update(List<Rect> l) {
		synchronized (readyLock) {
			if (!ready) {
				try { readyLock.wait();} catch (InterruptedException e) {}
			}
			list.clear();
			list.addAll(l);
		}
		repaint();
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		ready=false;
		super.repaint(tm, x, y, width, height);
	}

	@Override
	public void paintComponent(Graphics g) {
		float w=getWidth()-1, h=getHeight()-1;
		float s=Math.min(w/sheet.w, h/sheet.h);

		//Graphics2D g2 = (Graphics2D)g;
		g.clearRect(0, 0, getWidth(), getHeight());

		Rect r;
		g.setColor(Color.BLACK);
		for (int i=0; i < list.size(); ++i) {
			r=list.get(i);
			g.drawRect((int)(r.x*s), (int)(r.y*s), (int)(r.s.w*s), (int)(r.s.h*s));
		}
		r = new Rect(0,0,sheet);
		g.setColor(Color.BLUE);
		g.drawRect((int)(r.x*s), (int)(r.y*s), (int)(r.s.w*s), (int)(r.s.h*s));

		synchronized (readyLock) {
			ready=true;
			readyLock.notify();
		}
	}
}
