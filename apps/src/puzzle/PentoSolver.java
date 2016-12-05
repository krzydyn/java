package puzzle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import puzzles.Pentomino;
import puzzles.Pentomino.ChangeListener;
import puzzles.Pentomino.FigPos;
import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class PentoSolver extends MainPanel implements ChangeListener {
	final List<FigPos> current = new ArrayList<Pentomino.FigPos>(12);

	final static Color[] colorTable = {
		Color.RED, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.BLUE
	};

	/* size  solutions time[sec]
	 * 10x6     2339      8.556
	 * 6x10     2339      8.787
	 * 12x5     1308      3.877
	 * 5x12     1308      3.889
	 * 15x4      402      0.947
	 * 4x15      402      0.987
	 * 20x3        4      0.185
	 * 3x20        4      0.188
	 */
	Pentomino pentomino = new Pentomino(10,6);
	int cnt=0;
	boolean done=false;
	long t0=0,elapsed=0;
	public PentoSolver() {}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	@Override
	public Dimension getPreferredSize() {
		int w=pentomino.getWidth(), h=pentomino.getHeight();
		Dimension d = new Dimension((w+1)*20, (h+2)*20);
		Log.debug("getPreferredSize %d x %d",d.width,d.height);
		return d;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w=pentomino.getWidth(), h=pentomino.getHeight();
		synchronized (current) {
			g2.setBackground(getBackground());
			g2.clearRect(0, 0, getWidth(),getHeight());
			g2.setColor(Color.BLACK);
			g2.drawRect(9, 9, w*20+1, h*20+1);
			g2.drawString(
					String.format("found: %d, time: %d ms", cnt, elapsed),
					10,20*(h+1)+10);
			if (done) g2.drawString("DONE",10,20*(h+2)+10);
			for (FigPos fp : current) {
				if (fp.fig.type < 6) g2.setColor(colorTable[fp.fig.type]);
				else g2.setColor(colorTable[fp.fig.type-6].darker());
				Point p=new Point();
				for (int i=0; fp.getPoint(i,p); ++i) {
					g2.fillRect(10+p.x*20, 10+p.y*20, 20, 20);
				}
			}
		}
	}

	@Override
	public void boardChanged(List<FigPos> list) {
		if (list.size()!=12) return ;
		synchronized (current) {
			current.clear();
			current.addAll(list);
		}
		elapsed=System.currentTimeMillis()-t0;
		repaint(100);
		if (list.size()==12) ++cnt;
		//if (list.size()==12) XThread.sleep(1000);
		//else XThread.sleep(10);
		//XThread.sleep(100);
	}

	@Override
	public void windowOpened() {
		new Thread("Solver") {
			@Override
			public void run() {
				pentomino.setListener(PentoSolver.this);
				t0=System.currentTimeMillis();
				pentomino.solve();
				done=true;
				repaint();
			}
		}.start();
	}

	@Override
	public void windowClosed() {
		pentomino.stop();
	}

	public static void main(String[] args) {
		start(PentoSolver.class);
		//Pentomino.printAllFigs();
	}
}
