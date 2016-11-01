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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

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

	protected void windowClosed(){}
	protected void windowOpened() {}

	static public void append(JTextPane t, String s) {
		try {
			Document doc = t.getDocument();
			doc.insertString(doc.getLength(), s, null);
		} catch (BadLocationException e) {
		}
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

	static public void start(final Class<? extends MainPanel> mainclass, String[] args) {
		try {
			intern_start(mainclass, args);
		}
		catch (Throwable e) {Log.error(e);}
	}
	static public void start(final Class<? extends MainPanel> mainclass) {
		start(mainclass,null);
	}
	static private MainPanel create(final Class<? extends MainPanel> mainclass, String[] args) throws Exception {
		try {
			return mainclass.getConstructor(String[].class).newInstance(new Object[]{args});
		}catch (NoSuchMethodException e) {}
		return mainclass.newInstance();
	}
	static private void intern_start(final Class<? extends MainPanel> mainclass, final String[] args) throws Exception {
		EventQueue.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					final MainPanel main = create(mainclass, args);
					final JFrame f = new JFrame();
					f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
					f.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent e) {
							main.windowClosed();
						}
						@Override
						public void windowOpened(WindowEvent e) {
							main.windowOpened();
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
