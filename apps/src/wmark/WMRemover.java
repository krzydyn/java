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

	@Override
	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("File");
		m.add(new JMenuItem(open_file));
		mb.add(m);

		m = new JMenu("Selection");
		m.add(new JMenuItem(sel_color));
		mb.add(m);

		m = new JMenu("Functions");
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

	Action open_file = new AbstractAction("Open") {
		@Override
		public void actionPerformed(ActionEvent ev) {
		}
	};
	Action sel_color = new AbstractAction("By Color") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			currTool = colorTool;
		}
	};
	Action func_unwatermark = new AbstractAction("Unwotermark") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			BufferedImage img = (BufferedImage)imgPanel.getImage();
			for (Segment s : selection) {
				unwotermark(img,s);
			}
			repaint(20);
		}
	};

	/**
	 *
	 * @param img
	 * @param s
	 *
	 * watermark: x = a*q + b*(1-q)
	 * unwatermar: a = (x-b*(1-q))/q
	 */
	static void unwotermark(BufferedImage img, Segment s) {
		float q=0.4f;
		float[] fc={0,0,0};
		for (int x=s.x0; x < s.x1; ++x) {
			int c = img.getRGB(x, s.y);
			Colors.rgbGet(fc, c);
			for (int i=0; i < 3; ++i)
				fc[i] = (fc[i] - (1f-q))/q;
			c = Colors.rgb(fc);
			img.setRGB(x, s.y, c);
		}
	}

	void selectByColor(ColorTool tool, int x0, int y0) {
		BufferedImage img = (BufferedImage)imgPanel.getImage();
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
