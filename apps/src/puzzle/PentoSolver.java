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
import ui.MainPanel;

@SuppressWarnings("serial")
public class PentoSolver extends MainPanel implements ChangeListener {
	final List<FigPos> current = new ArrayList<Pentomino.FigPos>(12);

	final static Color[] colorTable = {
		Color.GRAY, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.WHITE
	};

	/* size  solutions time[sec]
	 * 10x6     2339    102.448
	 * 6x10     2339     10.597
	 * 12x5     1308    228.494
	 * 5x12     1308      4.945
	 */
	Pentomino pentomino = new Pentomino(6,10);
	int cnt=0;
	boolean done=false;
	long t0=0;
	public PentoSolver() {
		new Thread() {
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
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w=pentomino.getWidth(), h=pentomino.getHeight();
		synchronized (current) {
			g2.setBackground(Color.LIGHT_GRAY);
			g2.clearRect(0, 0, getWidth(),getHeight());
			g2.setColor(Color.BLACK);
			g2.drawRect(9, 9, w*20+1, h*20+1);
			g2.drawString(
					String.format("found: %d, time: %d ms", cnt, System.currentTimeMillis()-t0),
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
		repaint(100);
		if (list.size()==12) ++cnt;
		//if (list.size()==12) XThread.sleep(1000);
		//else XThread.sleep(10);
		//XThread.sleep(100);
	}

	public static void main(String[] args) {
		start(PentoSolver.class);
		//Pentomino.printAllFigs();
	}
}
