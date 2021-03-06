package image;

import image.Tool.EdgeTool;
import image.Tool.GaussTool;
import image.Tool.GradientTool;
import image.Tool.HoughTool;
import image.Tool.LumaTool;
import img.Colors;
import img.ImageRaster2D;
import img.Tools2D.Segment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import sys.Log;
import ui.Dialogs;
import ui.ImagePanel;
import ui.MainPanel;

/*
 * SAD (Sum of absolute differences)
 *
 * crosscoleration
 * http://werner.yellowcouch.org/Papers/subimg/index.html
 * KMP
 * https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm
 *
 * Where is Waldo
 * https://stackoverflow.com/questions/8479058/how-do-i-find-waldo-with-mathematica?rq=1
 */
@SuppressWarnings("serial")
public class ImageEditor extends MainPanel {

	private final JFileChooser chooser = Dialogs.createFileChooser(false);
	private final ImagePanel imgPanel = new ImagePanel();
	private Tool currTool = null;
	private final ArrayList<Segment> selection = new ArrayList<>();

	private static final ColorTool colorTool = new ColorTool();
	private static final GaussTool gaussTool = new GaussTool();
	private static final LumaTool lumaTool = new LumaTool();
	private static final EdgeTool edgeTool = new EdgeTool();
	private static final GradientTool gradTool = new GradientTool();
	private static final HoughTool houghTool = new HoughTool();

	private float alpha = 1f;

	public ImageEditor(String args[]) {
		setPreferredSize(new Dimension(800,600));
		add(createScrolledPanel(imgPanel), BorderLayout.CENTER);

		currTool = colorTool;
		Dialogs.addFilter(chooser, new FileFilter() {
			@Override
			public boolean accept(File path) {
				return path.isDirectory()
						|| path.getName().endsWith(".jpg")
						|| path.getName().endsWith(".gif")
						|| path.getName().endsWith(".png");
			}
		}, "picture");

		if (args.length > 0) {
			try {
				File f = new File(args[0]);
				if (f.exists()) openFile(f);
			} catch (IOException e) {Log.error(e);}
		}

		imgPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				Point p = e.getPoint();
				mouseClickSelect(p.x, p.y, e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK);
			}
		});
		imgPanel.setScale(1f);
	}

	Action file_open = new AbstractAction("Open") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			int r = chooser.showOpenDialog(topFrame());
			if (r == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
				try {
					openFile(f);
				} catch (IOException e) {
					Log.error(e);
				}
			}
		}
	};
	Action file_create = new AbstractAction("Create") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			BufferedImage i = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) i.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(10, 10, 50, 50);
			g.dispose();
			imgPanel.setImage(i);
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
		}
	};
	Action func_unwatermark = new AbstractAction("Unwatermark") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			unwatermark();
			repaint(20);
		}
	};

	Action filter_luma = new AbstractAction("GrayScale") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			lumaTool.transform(imgPanel.getRaster(), null);
			imgPanel.repaint();
		}
	};
	Action filter_gauss = new AbstractAction("Gauss") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			gaussTool.transform(imgPanel.getRaster(), null);
			imgPanel.repaint();
		}
	};
	Action filter_edge = new AbstractAction("Edge detect") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			edgeTool.transform(imgPanel.getRaster(), null);
			imgPanel.repaint();
		}
	};
	Action filter_grad = new AbstractAction("Gradient") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			gradTool.transform(imgPanel.getRaster(), null);
			imgPanel.repaint();
		}
	};
	Action show_hough = new AbstractAction("Hough space") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			JFrame f = new JFrame((String)getValue(Action.NAME));
			f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			ImagePanel i = new ImagePanel();
			i.setScale(2);
			ImageRaster2D r = new ImageRaster2D(1,1);
			houghTool.transform(imgPanel.getRaster(), r);
			i.setImage(r.getImage());
			f.setContentPane(createScrolledPanel(i));
			f.setSize(i.getPreferredSize());
			f.setVisible(true);
		}
	};
	@Override
	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("File");
		mb.add(m);
		m.add(new JMenuItem(file_open));
		m.add(new JMenuItem(file_create));
		m.add(new JMenuItem(file_quit));

		m = new JMenu("Selection");
		mb.add(m);
		m.add(new JMenuItem(sel_color));

		m = new JMenu("Functions");
		mb.add(m);
		m.add(new JMenuItem(func_calcalpha));
		m.add(new JMenuItem(func_unwatermark));

		m = new JMenu("Filters");
		mb.add(m);
		m.add(new JMenuItem(filter_luma));
		m.add(new JMenuItem(filter_gauss));
		m.add(new JMenuItem(filter_edge));
		m.add(new JMenuItem(filter_grad));

		m.add(new JMenuItem(show_hough));

		return mb;
	}

	Font font = new Font("Arial", Font.PLAIN, 34);

	@Override
	public void postChildren(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		//Shape s = new Arc2D.Double(20,120,400,200, -180, 360, Arc2D.OPEN);
		//Text2D.textOnPath(g2, s, "Test string hohoho", font, 0.5f);
	}

	private void openFile(File file) throws IOException {
		imgPanel.setImage(ImageIO.read(file));
	}

	private void mouseClickSelect(int x,int y, boolean append) {
		if (currTool == null) {Log.debug("Tool not selected");return ;}
		if (currTool == colorTool) selectByColor(colorTool, x, y, append);
	}

	private void calc_alpha() {
		BufferedImage img = (BufferedImage)imgPanel.getImage();
		float[] fc={0,0,0};
		float av=0;
		int n=0;
		for (Segment s : selection) {
			for (int x=s.x0; x < s.x1; ++x) {
				Colors.rgb2float(img.getRGB(x, s.y), fc);
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
				Colors.rgb2float(c, fc);
				for (int i=0; i < 3; ++i)
					fc[i] = (fc[i] - (1f-alpha))/alpha;
				c = Colors.rgb(fc);
				img.setRGB(x, s.y, c);
			}
		}
	}

	void selectByColor(ColorTool tool, int x0, int y0, boolean append) {
		Dimension s = imgPanel.getRaster().getSize();
		x0 = (int)(x0/imgPanel.getScale());
		y0 = (int)(y0/imgPanel.getScale());

		if (x0 < 0 || y0 < 0 || x0 >= s.width || y0 >= s.height) {
			if (append == false) {
				selection.clear();
				imgPanel.setSelection(selection);
			}
			return ;
		}

		List<Segment> seq = tool.select(imgPanel.getRaster(), x0, y0);
		if (append == false) selection.clear();
		selection.addAll(seq);
		seq.clear();
		imgPanel.setSelection(selection);
	}

	public static void main(String[] args) {
		startGUI(ImageEditor.class, args);
	}
}
