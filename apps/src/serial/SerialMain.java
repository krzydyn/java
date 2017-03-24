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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class SerialMain extends MainPanel {

	private List<Serial> ports = new ArrayList<Serial>();
	private Map<Serial,AnsiTerminal> editors = new HashMap<Serial,AnsiTerminal>();
	private boolean running = false;

	public SerialMain() {this(null);}
	public SerialMain(String[] args) {
		setName("MultiConsole");
		setPreferredSize(new Dimension(800,600));

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
		if (ports.isEmpty()) {
			for (String f : Serial.listPorts()) {
				ports.add(new Serial(f));
			}
		}
		JPanel p = null;
		if (ports.size()==1) {
			p = new JPanel(new BorderLayout());
			Serial sp = ports.get(0);
			AnsiTerminal e = new AnsiTerminal(sp.getName());
			p.add(e);
			editors.put(sp, e);
		}
		else if (ports.size()==2) {
			p = new JPanel(new BorderLayout());
			Serial sp1 = ports.get(0);
			Serial sp2 = ports.get(1);
			AnsiTerminal e1 = new AnsiTerminal(sp1.getName());
			AnsiTerminal e2 = new AnsiTerminal(sp2.getName());
			editors.put(sp1, e1);
			editors.put(sp2, e2);
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
				editors.put(sp, e);
			}
		}
		add(p,BorderLayout.CENTER);

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

	@Override
	public void windowClosed() {
		running = false;
	}

	public void readloop() {
		byte[] buffer = new byte[1024];
		long reopen = System.currentTimeMillis()-1;
		while (running) {
			if (reopen!=0 && reopen < System.currentTimeMillis()) {
				reopen = 0;
				for (Serial s : ports) {
					AnsiTerminal trm = editors.get(s);
					try {
						if (!s.isOpen()) {
							s.open();
							s.setParams(115200, Serial.Param.DATA_8, Serial.Param.STOP_1, Serial.Param.FLOW_NONE);
							trm.write("Port opened\n");
							trm.setTitle(null);
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
				AnsiTerminal trm = editors.get(s);
				if (!s.isOpen()) {
					trm.clearInput();
					continue;
				}
				int r = trm.getInputBuffer(buffer);
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
				AnsiTerminal trm = editors.get(s);
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

		for (Serial s : ports) {
			s.close();
		}
	}

	public static void main(String[] args) {
		Log.notice("Serial version %s", Serial.getVersion());
		//Log.setReleaseMode();
		start(SerialMain.class, args);
	}
}
