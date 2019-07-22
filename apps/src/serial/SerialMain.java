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

package serial;

import io.Serial;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import sys.Env;
import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class SerialMain extends MainPanel {

	private final List<Serial> ports = new ArrayList<>();
	private final Map<Serial,AnsiTerminal> terminals = new HashMap<>();
	private boolean running = false;
	private JPanel tPanel;
	private AnsiTerminal opTrm;

	Action file_open = new AbstractAction("Open...") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			List<String> portn = Serial.listPorts();
			portn.add("bash");
			Log.debug("ports: %s", portn);
			String n = (String) JOptionPane.showInputDialog(topFrame(), "Open terminal", "Select port",
					JOptionPane.QUESTION_MESSAGE, null,
					portn.toArray(),
					portn.get(0)); // Initial choice
			if (n != null) openTerminal(n);
		}
	};
	Action file_cmd = new AbstractAction("Command...") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			String[] cmds = {"ls", "echo test" };
			String n = (String) JOptionPane.showInputDialog(topFrame(), "Send command", null,
					JOptionPane.QUESTION_MESSAGE, null,
					cmds,
					cmds[0]); // Initial choice
			if (n != null) sendCommand(n);
		}
	};
	Action file_quit = new AbstractAction("Quit") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			topFrame().dispose();
		}
	};

	public SerialMain() {this(null);}
	public SerialMain(String[] args) {
		setName("MultiConsole/"+Env.osName());
		setPreferredSize(new Dimension(1200,800));


		if (args != null) {
			List<String> portn = Serial.listPorts();
			for (String n : args) {
				if (n.length() == 0) continue;
				if (n.startsWith("-")) {
					continue;
				}

				if (n.startsWith("/")) ;
				else if (n.startsWith("tty")) n = "/dev/"+n;
				else n = "/dev/tty"+n;
				Log.debug("dev = '%s'",n);
				if (n.endsWith("*")) {
					n = n.substring(0, n.length()-1);
					for (String pn : portn) {
						if (pn.startsWith(n))
							ports.add(new Serial(pn));
					}
				}
				else {
					ports.add(new Serial(n));
				}
			}
		}

		JPanel p = null;
		if (ports.size()==0) {
			p = new JPanel(new BorderLayout());
		}
		else if (ports.size()==1) {
			p = new JPanel(new BorderLayout());
			Serial sp = ports.get(0);
			AnsiTerminal e = new AnsiTerminal(sp.getName());
			p.add(e);
			terminals.put(sp, e);
		}
		else if (ports.size()==2) {
			p = new JPanel(new BorderLayout());
			Serial sp1 = ports.get(0);
			Serial sp2 = ports.get(1);
			AnsiTerminal e1 = new AnsiTerminal(sp1.getName());
			AnsiTerminal e2 = new AnsiTerminal(sp2.getName());
			terminals.put(sp1, e1);
			terminals.put(sp2, e2);
			JSplitPane s = createSplitPanel(JSplitPane.HORIZONTAL_SPLIT,e1,e2);
			s.setDividerLocation(500);
			p.add(s);
		}
		else {
			int cols = 1;
			while (cols*cols < ports.size()) ++cols;
			p = new JPanel(new GridLayout(0,cols));
			for (Serial sp : ports) {
				AnsiTerminal e = new AnsiTerminal(sp.getName());
				p.add(e);
				terminals.put(sp, e);
				}
		}
		tPanel = p;
		add(p,BorderLayout.CENTER);

		if (ports.size() > 0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					running = true;
					Log.notice("Read loop started");
					try {
						readloop();
					}catch(Throwable e){
						Log.error(e);
					} finally {
						running = false;
						Log.notice("Read loop finished");
					}
				}
			}, "SerialLoop").start();
		}
	}

	@Override
	protected JMenuBar createMenuBar() {
		JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("Terminal");
		mb.add(m);
		m.add(new JMenuItem(file_open));
		m.add(new JMenuItem(file_cmd));
		m.add(new JMenuItem(file_quit));
		return mb;
	}

	@Override
	public void windowClosed(WindowEvent e) {
		running = false;
		if (opTrm != null) opTrm.stop();
	}

	private void openTerminal(String n) {
		Log.debug("open "+n);
		ProcessBuilder pb = new ProcessBuilder("bash", "-i");
		pb.directory(new File(Env.expandEnv("~")));
		pb.redirectErrorStream(true);
		try {
			Process child = pb.start();
			opTrm = new AnsiTerminal(n, child.getInputStream(), child.getOutputStream());
			tPanel.add(opTrm, BorderLayout.CENTER);
			validate();
		} catch (Exception e) {
			Log.error(e);
		}
	}
	private void sendCommand(String n) {
		opTrm.send(n + "\n");
	}

	public void readloop() {
		byte[] buffer = new byte[1024];
		long reopen = System.currentTimeMillis()-1;
		while (running) {
			if (reopen!=0 && reopen < System.currentTimeMillis()) {
				reopen = 0;
				for (Serial s : ports) {
					AnsiTerminal trm = terminals.get(s);
					try {
						if (!s.isOpen()) {
							s.open();
							s.setParams(115200, Serial.Param.DATA_8, Serial.Param.STOP_1, Serial.Param.FLOW_NONE);
							trm.write("Port opened\n");
							trm.setTitle(s.getName());
						}
					}
					catch (Throwable e) {
						Log.error(e);
						trm.write(String.format("Open failed: %s\n", e.getMessage()));
						trm.setTitle("Open failed");
					}
				}
			}

			for (Serial s : ports) {
				AnsiTerminal trm = terminals.get(s);
				if (!s.isOpen()) {
					trm.clearInput();
					continue;
				}
				int r = trm.read(buffer);
				if (r > 0) {
					try {
						s.write(buffer, 0, r);
					}catch(Throwable e) {
						Log.error(e);
					}
				}
			}

			for (Serial s : ports) {
				if (!s.isOpen()) {
					if (reopen == 0)
						reopen = System.currentTimeMillis()+3000;
					continue;
				}
				AnsiTerminal trm = terminals.get(s);
				try {
					int r;
					do {
						r = s.read(buffer, 0, buffer.length);
						trm.write(buffer, 0, r);
					}
					while (r > 0);
					trm.flushOutput();
				}catch(Throwable e) {
					Log.error(e);
					s.close();
					trm.setTitle("closed on error");
					trm.write("Port closed " + e.getMessage() + "\n");
				}
			}
		}

		if (Env.isMacos()) System.exit(0); //closing ports on Mac crashes
		Log.debug("closing ports");
		for (Serial s : ports) {
			s.close();
		}
	}

	public static void main(String[] args) {
		Log.setTestMode();

		Env.addLibraryPath("./jni/rxtx");

		Log.notice("Serial version %s", Serial.getVersion());
		//Log.setReleaseMode();
		startGUI(SerialMain.class, args);
	}
}
