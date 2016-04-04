package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import sys.Log;

@SuppressWarnings("serial")
public class MainPanel extends JPanel{

	public MainPanel(LayoutManager l) {
		super(l);
		setPreferredSize(new Dimension(1500,900));
	}
	public MainPanel() {
		this(new BorderLayout());
	}

	public void windowClosed(){}

	static public void append(JTextPane t, String s) {
		try {
			Document doc = t.getDocument();
			doc.insertString(doc.getLength(), s, null);
		} catch (BadLocationException e) {
		}
	}

	static public JScrollPane createScrolledPanel(JComponent scrollable) {
		JScrollPane sp = new JScrollPane(scrollable);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.getVerticalScrollBar().setFocusable(false);
		sp.getHorizontalScrollBar().setFocusable(false);
		return sp;
	}

	static public void start(final Class<? extends MainPanel> mainclass, String[] args) {
		try {
			intern_start(mainclass);
		}
		catch (Throwable e) {Log.error(e);}
	}
	static public void start(final Class<? extends MainPanel> mainclass) {
		start(mainclass,null);
	}

	static private void intern_start(final Class<? extends MainPanel> mainclass) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					final MainPanel main = mainclass.newInstance();
					final JFrame f = new JFrame();
					f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
					f.addWindowListener(new WindowListener() {
						@Override
						public void windowOpened(WindowEvent e) {
						}
						@Override
						public void windowIconified(WindowEvent e) {
						}
						@Override
						public void windowDeiconified(WindowEvent e) {
						}
						@Override
						public void windowDeactivated(WindowEvent e) {
						}
						@Override
						public void windowClosing(WindowEvent e) {
						}
						@Override
						public void windowClosed(WindowEvent e) {
							main.windowClosed();
						}
						@Override
						public void windowActivated(WindowEvent e) {
						}
					});
					f.setTitle(main.getName());
					f.setContentPane(main);
					f.pack();
					f.setLocation(10,10);
					f.setVisible(true);
				} catch (Exception e) {
					Log.error(e);
				}
			}
		});
	}
}
