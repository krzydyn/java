package puzzle;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import sys.Sound;
import ui.MainPanel;

/*
 * https://en.wikipedia.org/wiki/M,n,k-game
 */
@SuppressWarnings("serial")
public class BigTacToe extends MainPanel{
	static final int NETSIZE=25;
	static final Stroke thinLine=new BasicStroke(0f);
	static final Stroke normLine=new BasicStroke(2f);

	float scale=2f;
	float offsX=0,offsY=0;
	static class PlayerMove {
		int x,y;

		@Override
		public boolean equals(Object o) {
			PlayerMove op = (PlayerMove)o;
			return op.x==x && op.y==y;
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			//Log.debug("finalize PlayMove");
		}
	}
	char startSym = 'X';
	char oponent(char p) {
		return p == 'X' ? 'O' : 'X';
	}

	float disp2realX(float x) {return x*scale+offsX;}
	float disp2realY(float y) {return y*scale+offsY;}
	float real2dispX(float x) {return (x-offsX)/scale;}
	float real2dispY(float y) {return (y-offsY)/scale;}

	List<PlayerMove> moves=new ArrayList<PlayerMove>();
	public BigTacToe(){
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PlayerMove pm = new PlayerMove();
				pm.x = (int)(real2dispX(e.getPoint().x)/NETSIZE);
				pm.y = (int)(real2dispY(e.getPoint().y)/NETSIZE);
				if (moves.indexOf(pm)<0) {
					moves.add(pm);
					repaint(100);
				}
				else Sound.dong();
			}
		});
	}

	private void drawGrid(Graphics2D g2) {
		int xmax=(int)real2dispX(getWidth());
		int ymax=(int)real2dispX(getHeight());
		g2.setStroke(thinLine);
		for (int y=0; y<=ymax; y+=NETSIZE) {
			g2.drawLine(0, y, xmax, y);
		}
		for (int x=0; x<=xmax; x+=NETSIZE) {
			g2.drawLine(x, 0, x, ymax);
		}
	}
	private void drawO(Graphics2D g2,int x,int y) {
		g2.setStroke(normLine);
		g2.drawOval(x*NETSIZE+2, y*NETSIZE+2, NETSIZE-5, NETSIZE-5);
	}
	private void drawX(Graphics2D g2,int x,int y) {
		g2.setStroke(normLine);
		g2.drawLine(x*NETSIZE+3, y*NETSIZE+3, (x+1)*NETSIZE-4, (y+1)*NETSIZE-4);
		g2.drawLine(x*NETSIZE+3, (y+1)*NETSIZE-4, (x+1)*NETSIZE-4, y*NETSIZE+3);
	}
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		AffineTransform tr = g2.getTransform();
		g2.scale(scale, scale);
		drawGrid(g2);
		boolean sym = startSym=='O';
		for (PlayerMove pm : moves) {
			if (sym) drawO(g2,pm.x,pm.y);
			else drawX(g2,pm.x,pm.y);
			sym = !sym;
		}

		g2.setTransform(tr);
	}
	@Override
	public void windowClosed(WindowEvent e) {
		super.windowClosed(e);
		moves.clear();
		System.gc();
	}

	public static void main(String[] args) {
		startGUI(BigTacToe.class);
	}

}
