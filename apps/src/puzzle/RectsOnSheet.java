package puzzle;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import ui.RectsPanel;
@SuppressWarnings("serial")
public class RectsOnSheet extends MainPanel {
	private volatile boolean running;
	/*
	 * (L,W,l,w) -> num
	 * (19,17,5,3) -> 21 boxes
	 * (49,28,8,3) -> 57 boxes
	 * (1600,1230,137,95) -> 147
	 */

	private final List<Rect> best=new ArrayList<Rect>();
	Sheet sheet = new Sheet(49,28);
	Sheet rect = new Sheet(8,3);

	private final RectsPanel rectpanel = new RectsPanel(sheet);
	private final JLabel labSt = new JLabel("STATUS");
	private final JLabel labInfo = new JLabel(".");

	public RectsOnSheet() {
		setName("PackRect");

		JPanel p = new JPanel();//new JPanel(new GridLayout(0, 1));
		p.add(labSt);
		p.add(labInfo);
		add(p, BorderLayout.SOUTH);
		add(rectpanel, BorderLayout.CENTER);
		if (sheet.w < 800) {
			int c = 800/sheet.w;
			rectpanel.setPreferredSize(new Dimension(sheet.w*c, sheet.h*c));
		}
		else {
			rectpanel.setPreferredSize(new Dimension(sheet.w, sheet.h));
		}

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

	//better if boxes have collinear edges
	private int quality(List<Rect> list) {
		int q=0, n=list.size();
		for (int i=0; i < n; ++i) {
			for (int j=i+1; j < n; ++j) {
				Rect ri = list.get(i);
				Rect rj = list.get(j);
				if (ri.x == rj.x) ++q;
				if (ri.y == rj.y) ++q;
				if (ri.x+ri.s.w == rj.x) ++q;
				if (ri.y+ri.s.h == rj.y) ++q;
				if (ri.x == rj.x+rj.s.w) ++q;
				if (ri.y == rj.y+rj.s.h) ++q;
			}

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
			int q=0;
			List<Rect> rects = rp.getRects();
			boolean draw=false;
			if (best.size() == rects.size()) {
				++nmax;
				q=quality(rects);
				if (bestq == q) ++nq;
				else if (bestq < q) {
					bestq=q; nq=1;
					best.clear();
					best.addAll(rects);
				}
				draw=true;
			}
			else if (best.size() < rp.getRects().size()) {
				q=bestq=quality(rp.getRects());
				nq=1; nmax=1;
				best.clear();
				best.addAll(rects);
				draw=true;
			}
			if (tm0 < System.currentTimeMillis()) {
				Log.debug("n=%d, rects=%d, best=%d",n,rp.getRects().size(), best.size());
				tm0+=2000;
			}
			if (draw) {
				rects = best;
				if (rectpanel.updateWhenReady(rects)) {
					int rs=rect.w*rect.h;
					int rest=sheet.w*sheet.h-rects.size()*rs;
					labInfo.setText(String.format("Nr = %d/%d/%d  Q = %d  Waste=%d/%d",rects.size(),nmax,nq,q,rest,rs));
				}
			}
		}

		{
		List<Rect> rects = best;
		int q = bestq;
		int rs=rect.w*rect.h;
		int rest=sheet.w*sheet.h-rects.size()*rs;
		Log.debug("n=%d, rects=%d/%d/%d Q=%d left:%d/%d",n,rects.size(),nmax,nq, q,rest,rs);
		rectpanel.update(rects);
		labInfo.setText(String.format("Nr = %d/%d/%d  Q = %d  Waste=%d/%d",rects.size(),nmax,nq, q,rest,rs));
		}
	}

	public static void main(String[] args) {
		startGUI(RectsOnSheet.class, args);
	}
}

