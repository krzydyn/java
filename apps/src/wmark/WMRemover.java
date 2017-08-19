package wmark;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import rgui.ImagePanel;
import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class WMRemover extends MainPanel {

	private final ImagePanel imgPanel = new ImagePanel();
	private BufferedImage img;

	WMRemover(String args[]) {
		setPreferredSize(new Dimension(800,600));
		add(createScrolledPanel(imgPanel), BorderLayout.CENTER);

		if (args.length > 0) {
			try {
				openFile(args[0]);
			} catch (IOException e) {Log.error(e);}
		}
	}

	@Override
	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		mb.add(new JMenuItem(open_file));

		return mb;
	}

	private void openFile(String file) throws IOException {
		img = ImageIO.read(new File(file));
		imgPanel.setImage(img);
	}

	Action open_file = new AbstractAction("Open") {
		@Override
		public void actionPerformed(ActionEvent ev) {
		}
	};

	public static void main(String[] args) {
		start(WMRemover.class, args);
	}


}
