package puzzle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import puzzles.RectPack;
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

	private final List<RectPack.Rect> rects=new ArrayList<RectPack.Rect>();
	private final List<RectPack.Rect> best=new ArrayList<RectPack.Rect>();
	RectPack.Dim sheet = new RectPack.Dim(19,17);
	RectPack.Dim rect = new RectPack.Dim(5,3);

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
	public void windowClosed() {
		running=false;
	}

	private int quality(List<RectPack.Rect> list) {
		int q=0, n=list.size();
		for (int i=0; i < n; ++i) {
			if (list.get(i).y == list.get((i+1)%n).y) ++q;
			if (list.get(i).s == list.get((i+1)%n).s) ++q;
		}
		return q;
	}
	private void solverLoop() {
		RectPack rp = new RectPack(sheet);
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
					for (RectPack.Rect r : rp.getRects()) {
						best.add(r);
					}
				}
				draw=true;
			}
			else if (best.size() < rp.getRects().size()) {
				bestq=quality(rp.getRects());
				nq=1;
				best.clear(); nmax=1;
				for (RectPack.Rect r : rp.getRects()) {
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
					for (RectPack.Rect r : rp.getRects()) {
						rects.add(r);
					}
					int rs=rect.w*rect.h;
					int rest=sheet.w*sheet.h-rects.size()*rs;
					labInfo.setText(String.format("N = %d  Nr = %d/%d/%d  Pr = %d Waste=%d", n, rects.size(),nmax,nq, rs, rest));
					rectpanel.repaint();
				}
			}
		}
		int rs=rect.w*rect.h;
		Log.debug("n=%d, rects=%d(%d,%d) left:%d",n,best.size(),nmax,nq,sheet.w*sheet.h-best.size()*rs);
		rectpanel.waitRedy();
		rects.clear();
		for (RectPack.Rect r : best) {
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
	private final RectPack.Dim sheet;
	private final List<RectPack.Rect> list;
	public RectPanel(RectPack.Dim sheet, List<RectPack.Rect> list) {
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
		if (!ready) {
			synchronized (readyLock) {
				try { readyLock.wait();
				} catch (InterruptedException e) {}
			}
		}
		return ready;
	}

	@Override
	public void paintComponent(Graphics g) {
		float w=getWidth()-2, h=getHeight()-2;
		float s=Math.min(w/sheet.w, h/sheet.h);

		//Graphics2D g2 = (Graphics2D)g;
		g.clearRect(0, 0, getWidth(), getHeight());

		RectPack.Rect r;
		g.setColor(Color.BLACK);
		for (int i=0; i < list.size(); ++i) {
			r=list.get(i);
			g.drawRect((int)(r.x*s), (int)(r.y*s), (int)(r.s.w*s), (int)(r.s.h*s));
		}
		r = new RectPack.Rect(0,0,sheet);
		g.setColor(Color.BLUE);
		g.drawRect((int)(r.x*s), (int)(r.y*s), (int)(r.s.w*s), (int)(r.s.h*s));
		ready=true;
		synchronized (readyLock) {
			readyLock.notify();
		}
	}
}