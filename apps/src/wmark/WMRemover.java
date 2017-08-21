package wmark;

import img.Colors;
import img.ImageRaster2D;
import img.Tools2D.Segment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import sys.Log;
import ui.ImagePanel;
import ui.MainPanel;

@SuppressWarnings("serial")
public class WMRemover extends MainPanel {

	private final ImagePanel imgPanel = new ImagePanel();
	private Tool currTool = null;
	private final ArrayList<Segment> selection = new ArrayList<>();

	private static final ColorTool colorTool = new ColorTool();

	private float alpha = 1f;

	public WMRemover(String args[]) {
		setPreferredSize(new Dimension(800,600));
		add(createScrolledPanel(imgPanel), BorderLayout.CENTER);

		currTool = colorTool;

		if (args.length > 0) {
			try {
				openFile(args[0]);
			} catch (IOException e) {Log.error(e);}
		}

		imgPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				Point p = e.getPoint();
				mouseClickSelect(p.x, p.y);
			}
		});
	}

	Action file_open = new AbstractAction("Open") {
		@Override
		public void actionPerformed(ActionEvent ev) {
		}
	};
	Action file_quit = new AbstractAction("Quit") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			topFrame().dispose();
		}
	};
	Action sel_color = new AbstractAction("By Color") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			currTool = colorTool;
		}
	};
	Action func_calcalpha = new AbstractAction("Calc alpha") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			calc_alpha();
			repaint(20);
		}
	};
	Action func_unwatermark = new AbstractAction("Unwatermark") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			unwatermark();
			repaint(20);
		}
	};
	@Override
	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("File");
		m.add(new JMenuItem(file_open));
		m.add(new JMenuItem(file_quit));
		mb.add(m);

		m = new JMenu("Selection");
		m.add(new JMenuItem(sel_color));
		mb.add(m);

		m = new JMenu("Functions");
		m.add(new JMenuItem(func_calcalpha));
		m.add(new JMenuItem(func_unwatermark));
		mb.add(m);
		return mb;
	}

	private void openFile(String file) throws IOException {
		imgPanel.setImage(ImageIO.read(new File(file)));
	}

	private void mouseClickSelect(int x,int y) {
		if (currTool == null) {Log.debug("Tool not selected");return ;}
		if (currTool == colorTool) selectByColor(colorTool, x, y);
	}

	private void calc_alpha() {
		BufferedImage img = (BufferedImage)imgPanel.getImage();
		float[] fc={0,0,0};
		float av=0;
		int n=0;
		for (Segment s : selection) {
			for (int x=s.x0; x < s.x1; ++x) {
				Colors.rgbToFloat(img.getRGB(x, s.y), fc);
				av += (fc[0]+fc[1]+fc[2])/3;
			}
			n += s.x1-s.x0;
			if (n > 1000) break;
		}
		av /= n;

		Log.debug("float aver: %f",av);
		alpha = 1f/av-1f + (1f/av-1f)/7f;
	}
	/**
	 *
	 * @param img
	 * @param s
	 *
	 * watermark: dst = s1*A + s2*(1-A) = (s1-s2)*A + s2
	 * unwatermar: s1 = (dst - s2*(1-A))/A
	 * alpha:      A  = (dst - s2)/(s1-s2)
	 */
	private void unwatermark() {
		BufferedImage img = (BufferedImage)imgPanel.getImage();
		float[] fc={0,0,0};

		Log.debug("alpha: %f", alpha);
		for (Segment s : selection) {
			for (int x=s.x0; x < s.x1; ++x) {
				int c = img.getRGB(x, s.y);
				Colors.rgbToFloat(c, fc);
				for (int i=0; i < 3; ++i)
					fc[i] = (fc[i] - (1f-alpha))/alpha;
				c = Colors.rgb(fc);
				img.setRGB(x, s.y, c);
			}
		}
	}

	void selectByColor(ColorTool tool, int x0, int y0) {
		BufferedImage img = (BufferedImage)imgPanel.getImage();
		x0 = (int)(x0/imgPanel.getScale());
		y0 = (int)(y0/imgPanel.getScale());

		List<Segment> seq = tool.select(new ImageRaster2D(img), x0, y0);
		selection.clear();
		selection.addAll(seq);
		seq.clear();
		imgPanel.addSelection(selection);
	}

	public static void main(String[] args) {
		start(WMRemover.class, args);
	}
}
