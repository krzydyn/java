/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import sys.Env;
import sys.Log;

/*
 * The Dock

The com.apple.eawt.Application class includes the following methods for managing your Java application’s Dock icon:

public java.awt.Image getDockIconImage()
public void setDockIconImage(java.awt.Image)
public java.awt.PopupMenu getDockMenu()
public void setDockMenu(java.awt.PopupMenu)
public void setDockIconBadge(String)

//http://www.oracle.com/technetwork/articles/javase/javatomac-140486.html
System.setProperty("com.apple.macos.useScreenMenuBar", "true");
System.setProperty("com.apple.mrj.application.apple.menu.about.name","xxx");//not works
System.setProperty("com.apple.mrj.application.growbox.intrudes","false");

 */

@SuppressWarnings("serial")
public class MainPanel extends JPanel implements WindowListener {

	private JFrame mainFame;

	public MainPanel(LayoutManager l) {
		super(l);
	}
	public MainPanel() {
		this(new BorderLayout());
		setEnabled(true);
		setFocusable(true);
		setRequestFocusEnabled(true);
	}

	public JFrame topFrame() { return mainFame; }

	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void windowClosing(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
	public void windowResized(ComponentEvent e) {}
	public void windowLostFocus(WindowEvent e) {}
	public void windowGainedFocus(WindowEvent e) {}

	static public void append(JTextPane t, String s) {
		try {
			Document doc = t.getDocument();
			doc.insertString(doc.getLength(), s, null);
		} catch (BadLocationException e) {}
	}

	static public JScrollPane createScrolledPanel(JComponent c) {
		JScrollPane sp;
		if (c instanceof JTextComponent) {
			//wrapping TextComponents with JPanel Border layout
			JPanel bpan = new JPanel(new BorderLayout());
			bpan.add(c);
			sp = new JScrollPane(bpan);
		}
		else sp = new JScrollPane(c);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JScrollBar sb;

		sb=sp.getVerticalScrollBar();
		sb.setUnitIncrement(32); // default is 1
		sb.setFocusable(false);

		sb=sp.getHorizontalScrollBar();
		sb.setFocusable(false);
		return sp;
	}

	static public JSplitPane createSplitPanel(int orientation,JComponent c1,JComponent c2) {
		JSplitPane split = new JSplitPane(orientation, c1, c2);
		split.setContinuousLayout(false);
		split.setOneTouchExpandable(true);
		//split.setDividerLocation(0.5);
		split.setResizeWeight(0.5);
		//split.setDividerLocation(500);
		return split;
	}

	static public MainPanel start(final Class<? extends MainPanel> mainclass, String[] args) {
		try { return internal_start(mainclass, args);}
		catch (Throwable e) {Log.error(e);}
		return null;
	}
	static public void start(final Class<? extends MainPanel> mainclass) {
		start(mainclass,null);
	}
	static private MainPanel create(final Class<? extends MainPanel> mainclass, String[] args) throws Exception {
		if (args != null) {
			try {return mainclass.getConstructor(String[].class).newInstance(new Object[]{args});}
			catch (NoSuchMethodException e) {}
			return mainclass.newInstance();
		}
		else {
			try {return mainclass.newInstance();}
			catch (InstantiationException e) {}
			return mainclass.getConstructor(String[].class).newInstance(new Object[]{args});
		}
	}
	static private MainPanel internal_start(final Class<? extends MainPanel> mainclass, final String[] args) throws Exception {
		final MainPanel[] mp = {null};
		EventQueue.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					final MainPanel main = create(mainclass, args);
					JFrame f = new JFrame();
					main.mainFame = f;
					f.setContentPane(main);
					f.setTitle(main.getName());
					f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
					f.addWindowListener(main);
					f.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							main.windowResized(e);
						}
					});
					f.addWindowFocusListener(new WindowFocusListener() {
						@Override
						public void windowGainedFocus(WindowEvent e) {
							main.windowGainedFocus(e);
						}
						@Override
						public void windowLostFocus(WindowEvent e) {
							main.windowLostFocus(e);
						}
					});
					Dimension d=main.getPreferredSize();
					if (d.width==0 || d.height==0) {
						d.width=800; d.height=600;
					}
					f.setPreferredSize(d);
					f.pack();
					d=main.getMinimumSize();
					if (d.width!=0 && d.height!=0) {
						Dimension d1=f.getSize();
						Dimension d2=main.getSize();
						Log.debug("frame size %d x %d", d1.width, d1.height);
						Log.debug("main size %d x %d", d2.width, d2.height);
						d.width += d1.width-d2.width;
						d.height += d1.height-d2.height;
						f.setMinimumSize(d);
					}
					f.setLocation(10,10);
					f.setVisible(true);
					main.requestFocus();
					mp[0]=main;
					Log.info("%s", Env.memstat());
				} catch (Throwable e) {
					Log.error(e);
				}
			}
		});
		return mp[0];
	}
}
