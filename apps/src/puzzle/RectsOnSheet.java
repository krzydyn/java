package puzzle;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import puzzles.RectPack;
import sys.Log;
import ui.MainPanel;
//47683665
//14367241
@SuppressWarnings("serial")
public class RectsOnSheet extends MainPanel {
	private volatile boolean running;
	private volatile boolean ready=true;
	private final List<RectPack.Rect> rects=new ArrayList<RectPack.Rect>();
	private final List<RectPack.Rect> best=new ArrayList<RectPack.Rect>();
	RectPack.Dim sheet = new RectPack.Dim(17,15);
	RectPack.Dim rect = new RectPack.Dim(3,4);
	public RectsOnSheet() {
		setName("PackRect");

		new Thread(new Runnable() {
			@Override
			public void run() {
				running = true;
				Log.notice("loop started");
				try {
					solverLoop();
				}catch(Throwable e){
					Log.error(e);
				} finally {
					running = false;
					Log.notice("loop finished");
				}
			}
		}, "SolverLoop").start();
	}
	@Override
	public void paintComponent(Graphics g) {
		float s=Math.min(getWidth(), getHeight())-2;
		s/=Math.max(sheet.w, sheet.h);

		//Graphics2D g2 = (Graphics2D)g;
		g.clearRect(0, 0, getWidth(), getHeight());

		RectPack.Rect r;
		g.setColor(Color.BLACK);
		for (int i=0; i < rects.size(); ++i) {
			r=rects.get(i);
			g.drawRect((int)(r.x*s), (int)(r.y*s), (int)(r.s.w*s), (int)(r.s.h*s));
		}
		r = new RectPack.Rect(0,0,sheet);
		g.setColor(Color.BLUE);
		g.drawRect((int)(r.x*s), (int)(r.y*s), (int)(r.s.w*s), (int)(r.s.h*s));
		ready=true;
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
			if (best.size() == rp.getRects().size()) {
				++nmax;
				int q=quality(rp.getRects());
				if (bestq == q) ++nq;
				else if (bestq < q) {
					bestq=q;
					best.clear();
					for (RectPack.Rect r : rp.getRects()) {
						best.add(r);
					}
				}
				rects.clear();
				for (RectPack.Rect r : rp.getRects()) {
					rects.add(r);
				}
				repaint();
			}
			else if (best.size() < rp.getRects().size()) {
				bestq=quality(rp.getRects());
				best.clear(); nmax=1;
				for (RectPack.Rect r : rp.getRects()) {
					best.add(r);
				}
			}
			if (tm0 < System.currentTimeMillis()) {
				Log.debug("n=%d, rects=%d",n,rp.getRects().size());
				tm0+=2000;
			}
			/*if (ready) {
				rects.clear();
				for (RectPack.Rect r : rp.getRects()) {
					rects.add(r);
				}
				ready=false;
				repaint();
			}*/
		}
		int rs=rect.w*rect.h;
		Log.debug("n=%d, rects=%d(%d,%d) left:%d",n,best.size(),nmax,nq,sheet.w*sheet.h-best.size()*rs);
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
