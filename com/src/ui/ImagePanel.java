package ui;

import img.ImageRaster2D;
import img.Raster2D;
import img.Tools2D.Segment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import sys.Log;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	public static class ImageUpdate extends Rectangle {
		Image i;
		public ImageUpdate(Image i, int x, int y) {
			this.i=i;
			this.x=x; this.y=y;
			this.width=i.getWidth(null);
			this.height=i.getHeight(null);
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
	private final Dimension vSize = new Dimension();
	private final List<Roi> rois = new ArrayList<Roi>();
	private final ArrayList<Segment> selection = new ArrayList<>();
	private float scale = 1f;

	private void updateSize() {
		vSize.setSize(imgSize.width*scale, imgSize.height*scale);
		setSize(vSize);
		setPreferredSize(vSize);
	}

	public ImagePanel(LayoutManager layout) {
		super(layout);
	}
	public ImagePanel() {
		super(new BorderLayout());
	}


	public float getScale() {return scale;}
	public void setScale(float s) {
		scale=s;
		updateSize();
	}

	public void setShowRoi(boolean show) {showRoi=show;}
	public Image getImage() { return img; }
	public Raster2D getRaster() { return new ImageRaster2D((BufferedImage)img); }
	public void setImage(Image i) {
		synchronized (imgLock) {
			rois.clear();
			img = i;
			if (img != null) {
				imgSize.width = img.getWidth(null) ;
				imgSize.height = img.getHeight(null) ;
			}
			else {
				Log.warn("Setting image to null");
				imgSize.width = imgSize.height = 0;
			}
		}
		updateSize();
		repaint();
	}
	public void clearRois() {
		rois.clear();
		repaint(20);
	}
	public void addRoi(Shape s) {
		rois.add(new Roi(s, 0));
		repaint(20);
	}
	public void setSelection(List<Segment> segs) {
		selection.clear();
		selection.addAll(segs);
		Log.debug("selection: %s segs", segs.size());
		repaint(20);
	}
	public void update(List<ImageUpdate> imgq) {
		int l=imgq.size();
		for (int i=l; i > 0; ) {
			--i;
			ImageUpdate ib = imgq.get(i);
			for (int j=0; j < i; ++j) {
				ImageUpdate jb = imgq.get(j);
				if (ib.contains(jb)) {
					jb.i=null;
					imgq.remove(j);
					--j; --i;
				}
			}
		}
		synchronized (imgLock) {
			Graphics g = img.getGraphics();
			for (int i=0; i < l; ++i) {
				ImageUpdate ib = imgq.get(i);
				g.drawImage(ib.i, ib.x, ib.y, null);
				if (showRoi) {
					Roi r = new Roi(ib,1000);
					rois.add(r);
				}
			}
			g.dispose();
		}
		while (l > 0) {
			ImageUpdate ib = imgq.remove(0); --l;
			ib.i=null;
		}
		repaint(20);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		Image img;
		synchronized (imgLock) {img=this.img;}
		if (img == null) return ;

		//long tm = System.currentTimeMillis();
		AffineTransform tr = g2.getTransform();
		g2.scale(scale, scale);
		g2.drawImage(img, 0, 0, this);

		if (selection.size() > 0) {
			g2.setColor(Color.GREEN);
			for (Segment s : selection) {
				g2.drawLine(s.x0, s.y, s.x0, s.y);
				g2.drawLine(s.x1, s.y, s.x1, s.y);
			}
		}
		if (!showRoi) {g2.setTransform(tr);return ;}
		int mx=getWidth()/2;
		int my=getHeight()/2;
		int roisSize = rois.size();
		synchronized (imgLock) {
			if (roisSize > 0) {
				for (Iterator<Roi> i=rois.iterator(); i.hasNext(); ) {
					Roi r = i.next();
					if (r.tm !=0 && r.tm + 1000 < System.currentTimeMillis()) i.remove();
				}
				g2.setColor(Color.GREEN);
				for (Roi r : rois) {
					g2.draw(r.shape);
				}
			}
		}
		if (roisSize > 0) {
			g2.setColor(Color.GREEN);
			g2.fillRect(mx-2, my-10, 25,15);
			g2.setColor(Color.BLACK);
			g2.drawString(String.format("%d",roisSize), mx, my);
		}
		g2.setTransform(tr);
	}
}
