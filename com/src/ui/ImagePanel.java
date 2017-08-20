package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	public static class ImageUpdate {
		Image i;
		int x,y,w,h;
		public ImageUpdate(Image i, int x, int y) {
			this.i=i;
			this.x=x; this.y=y;
			this.w=i.getWidth(null);
			this.h=i.getHeight(null);
		}
	}

	public static class Roi {
		public Roi(Shape s, long tmo) {
			shape = s;
			if (tmo==0) tm=0;
			else tm = System.currentTimeMillis()+tmo;
		}
		Shape shape;
		long tm;
	}

	private boolean showRoi=false;
	private final Object imgLock = new Object();
	private Image img;
	private final Dimension imgSize = new Dimension();
	private final List<Roi> rois = new ArrayList<Roi>();
	private final Runnable newImageNotifier = new Runnable() {
		@Override
		public void run() {
			setSize(imgSize);
			setPreferredSize(imgSize);
		}
	};
	public ImagePanel() {
		super(new BorderLayout());
	}
	public void setShowRoi(boolean show) {showRoi=show;}
	public Image getImage() { return img; }
	public void setImage(Image i) {
		synchronized (imgLock) {
			rois.clear();
			img = i;
			if (img != null) {
				imgSize.width = img.getWidth(null) ;
				imgSize.height = img.getHeight(null) ;
			}
			else {
				imgSize.width = imgSize.height = 0;
			}
		}
		EventQueue.invokeLater(newImageNotifier);
	}
	public void clearRois() {
		rois.clear();
		repaint(20);
	}
	public void addRois(Shape s) {
		rois.add(new Roi(s, 0));
		repaint(20);
	}
	public void update(List<ImageUpdate> imgq) {
		int l=imgq.size();
		synchronized (imgLock) {
			Graphics g = img.getGraphics();
			for (int i=0; i < l; ++i) {
				ImageUpdate ib = imgq.get(i);
				g.drawImage(ib.i, ib.x, ib.y, null);
				if (showRoi) {
					Roi r = new Roi(new Rectangle(ib.x, ib.y, ib.w, ib.h),1000);
					rois.add(r);
				}
				ib.i=null;
			}
			g.dispose();
		}
		while (l>0) {imgq.remove(0); --l;}
		repaint(20);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		Image img;
		synchronized (imgLock) {img=this.img;}
		if (img == null) return ;
		g2.drawImage(img, 0, 0, null);

		if (!showRoi) return ;
		int mx=getWidth()/2;
		int my=getHeight()/2;
		int roisSize = 0;
		synchronized (imgLock) {
			roisSize = rois.size();
			for (Iterator<Roi> i=rois.iterator(); i.hasNext(); ) {
				Roi r = i.next();
				if (r.tm !=0 && r.tm + 1000 < System.currentTimeMillis()) i.remove();
			}
			g2.setColor(Color.GREEN);
			for (Roi r : rois) {
				g2.draw(r.shape);
			}
			//Rectangle b = r.shape.getBounds();
			//Log.debug("roi: (%d,%d,%d,%d)",r.x,r.y,r.w,r.h);
			//g2.drawLine(mx, my, b.x+b.width/2, b.y+b.height/2);
		}
		g2.setColor(Color.GREEN);
		g2.fillRect(mx-2, my-10, 25,15);
		g2.setColor(Color.BLACK);
		g2.drawString(String.format("%d",roisSize), mx, my);
	}
}
