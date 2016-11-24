package puzzle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import puzzles.Pentomino;
import puzzles.Pentomino.ChangeListener;
import puzzles.Pentomino.FigPos;
import sys.XThread;
import ui.MainPanel;

@SuppressWarnings("serial")
public class PentoSolver extends MainPanel implements ChangeListener {
	final List<FigPos> current = new ArrayList<Pentomino.FigPos>(12);

	final static Color[] colorTable = {
		Color.GRAY, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.WHITE
	};

	public PentoSolver() {
		new Thread() {
			@Override
			public void run() {
				Pentomino p = new Pentomino(10, 6);
				p.setListener(PentoSolver.this);
				p.solve();
			}
		}.start();
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w=10, h=6;
		synchronized (current) {
			g2.clearRect(0, 0, (w+1)*20, (h+1)*20);
			g2.setColor(Color.BLACK);
			g2.drawRect(9, 9, w*20+1, h*20+1);
			for (FigPos fp : current) {
				if (fp.fig.type < 6) g2.setColor(colorTable[fp.fig.type]);
				else g2.setColor(colorTable[fp.fig.type-6].darker());
				Point p=new Point();
				for (int i =0; fp.getPoint(i,p); ++i) {
					g2.fillRect(10+p.x*20, 10+p.y*20, 20, 20);
				}
			}
		}
	}

	@Override
	public void boardChanged(List<FigPos> list) {
		synchronized (current) {
			current.clear();
			current.addAll(list);
		}
		repaint(10);
		XThread.sleep(100);
	}

	public static void main(String[] args) {
		start(PentoSolver.class);
		//Pentomino.printAllFigs();
	}
}
