package cutit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import puzzles.GameBoard.Sheet;
import sys.Env;
import ui.MainPanel;
import ui.RectsPanel;

@SuppressWarnings("serial")
public class CutIt extends MainPanel {
	static int TITLEBAR_HEIGHT = 30;
	Sheet sheet = new Sheet(500, 300);
	RectsPanel rectpanel = new RectsPanel(sheet);

	public CutIt() {
		add(rectpanel, BorderLayout.CENTER);
		Dimension d = Env.defaultScreenSize();
		d.height -= TITLEBAR_HEIGHT;
		if (sheet.w < d.width) {
			double scale = d.getWidth()/sheet.w;
			if (sheet.h*scale > d.height) scale = d.getHeight()/sheet.h;
			rectpanel.setPreferredSize(new Dimension((int)(sheet.w*scale), (int)(sheet.h*scale)));
		}
		else {
			rectpanel.setPreferredSize(new Dimension(sheet.w, sheet.h));
		}
	}

	public static void main(String[] args) {
		Object o;
		startGUI(CutIt.class, args);
	}

}
