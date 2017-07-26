package rgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	static class ImageBox {
		Image i;
		int x,y,w,h;
		long tm;
		public ImageBox(Image i, int x, int y) {
			this.i=i;
			this.x=x; this.y=y;
			this.w=i.getWidth(null); this.h=i.getHeight(null);
			this.tm=System.currentTimeMillis();
		}
	}

	private final Object imgLock = new Object();
	private Image img;
	private final Dimension imgSize = new Dimension();
	private final List<ImageBox> rois = new ArrayList<ImageBox>();
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
	public void update(List<ImageBox> imgq) {
		int l=imgq.size();
		synchronized (imgLock) {
			Graphics g = img.getGraphics();
			for (int i=0; i < l; ++i) {
				ImageBox ib = imgq.get(i);
				g.drawImage(ib.i, ib.x, ib.y, null);
				ib.i=null;
				rois.add(ib);
			}
			g.dispose();
		}
		while (l>0) {imgq.remove(0); --l;}
		repaint(20);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Image img;
		synchronized (imgLock) {img=this.img;}
		if (img == null) return ;
		g.drawImage(img, 0, 0, null);

		int mx=getWidth()/2;
		int my=getHeight()/2;
		int roisSize = 0;
		synchronized (imgLock) {
			roisSize = rois.size();
			for (Iterator<ImageBox> i=rois.iterator(); i.hasNext(); ) {
				ImageBox r = i.next();
				if (r.tm + 1000 < System.currentTimeMillis()) i.remove();
			}
			for (ImageBox r : rois) {
				if (r.x+r.w > imgSize.width || r.y+r.h > imgSize.height) {
					g.setColor(Color.RED);
					//Log.error("rect out of range: %s, (%d,%d)",r,maxx,maxy);
				}
				else
					g.setColor(Color.GREEN);
				//Log.debug("roi: (%d,%d,%d,%d)",r.x,r.y,r.w,r.h);
				g.drawRect(r.x, r.y, r.w, r.h);
				g.drawLine(mx, my, r.x+r.w/2, r.y+r.h/2);
			}
		}
		g.setColor(Color.GREEN);
		g.fillRect(mx-2, my-10, 25,15);
		g.setColor(Color.BLACK);
		g.drawString(String.format("%d",roisSize), mx, my);
	}
}
