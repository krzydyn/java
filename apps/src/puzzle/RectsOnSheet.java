package puzzle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import puzzles.GameBoard.Rect;
import puzzles.GameBoard.Sheet;
import puzzles.RectPackBruteForce;
import sys.Log;
import ui.MainPanel;
@SuppressWarnings("serial")
public class RectsOnSheet extends MainPanel {
	private volatile boolean running;
	/*
	 * (L,W,l,w) -> num
	 * (49,28,8,3) -> 57 boxes
	 * (1600,1230,137,95) -> 147
	 */

	private final List<Rect> rects=new ArrayList<Rect>();
	private final List<Rect> best=new ArrayList<Rect>();
	Sheet sheet = new Sheet(19,17);
	Sheet rect = new Sheet(5,3);

	private final RectPanel rectpanel = new RectPanel(sheet, rects);
	private final JLabel labSt = new JLabel("STATUS");
	private final JLabel labInfo = new JLabel(".");

	public RectsOnSheet() {
		setName("PackRect");

		JPanel p = new JPanel();//new JPanel(new GridLayout(0, 1));
		p.add(labSt);
		p.add(labInfo);
		add(p, BorderLayout.SOUTH);
		add(rectpanel, BorderLayout.CENTER);

		new Thread(new Runnable() {
			@Override
			public void run() {
				running = true;
				labSt.setText("WORKING");
				Log.notice("loop started");
				try {
					solverLoop();
					labSt.setText("DONE");
				}catch(Throwable e){
					labSt.setText("ERROR");
					Log.error(e);
				} finally {
					running = false;
					Log.notice("loop finished");
				}
			}
		}, "SolverLoop").start();
	}
	@Override
	public void windowClosed(WindowEvent e) {
		running=false;
	}

	private int quality(List<Rect> list) {
		int q=0, n=list.size();
		for (int i=0; i < n; ++i) {
			if (list.get(i).y == list.get((i+1)%n).y) ++q;
			if (list.get(i).s == list.get((i+1)%n).s) ++q;
		}
		return q;
	}
	private void solverLoop() {
		RectPackBruteForce rp = new RectPackBruteForce(sheet);
		rp.setRect(rect);
		long tm0=System.currentTimeMillis()+1000;
		int n=0,nmax=0,nq=0,bestq=0;
		while (running) {
			if (!rp.next()) break;

			++n;
			boolean draw=false;
			if (best.size() == rp.getRects().size()) {
				++nmax;
				int q=quality(rp.getRects());
				if (bestq == q) ++nq;
				else if (bestq < q) {
					bestq=q; nq=1;
					best.clear();
					for (Rect r : rp.getRects()) {
						best.add(r);
					}
				}
				draw=true;
			}
			else if (best.size() < rp.getRects().size()) {
				bestq=quality(rp.getRects());
				nq=1;
				best.clear(); nmax=1;
				for (Rect r : rp.getRects()) {
					best.add(r);
				}
				draw=true;
			}
			if (tm0 < System.currentTimeMillis()) {
				Log.debug("n=%d, rects=%d",n,rp.getRects().size());
				tm0+=2000;
			}
			if (draw) {
				if (rectpanel.waitRedy()) {
					rects.clear();
					for (Rect r : rp.getRects()) {
						rects.add(r);
					}
					rectpanel.repaint();
					int rs=rect.w*rect.h;
					int rest=sheet.w*sheet.h-rects.size()*rs;
					labInfo.setText(String.format("N = %d  Nr = %d/%d/%d  Pr = %d Waste=%d", n, rects.size(),nmax,nq, rs, rest));
				}
			}
		}
		int rs=rect.w*rect.h;
		Log.debug("n=%d, rects=%d(%d,%d) left:%d",n,best.size(),nmax,nq,sheet.w*sheet.h-best.size()*rs);
		rectpanel.waitRedy();
		rects.clear();
		for (Rect r : best) {
			rects.add(r);
		}
		repaint();
	}
	public static void main(String[] args) {
		start(RectsOnSheet.class, args);
	}
}

@SuppressWarnings("serial")
class RectPanel extends JPanel {
	private volatile boolean ready=true;
	private final Object readyLock = new Object();
	private final Sheet sheet;
	private final List<Rect> list;
	public RectPanel(Sheet sheet, List<Rect> list) {
		super(null);
		this.sheet=sheet;
		this.list=list;
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		ready=false;
		super.repaint(tm, x, y, width, height);
	}

	public boolean waitRedy() {
		synchronized (readyLock) {
			if (!ready) {
				try { readyLock.wait();} catch (InterruptedException e) {}
			}
			return ready;
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		float w=getWidth()-2, h=getHeight()-2;
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