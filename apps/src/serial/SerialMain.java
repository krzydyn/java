package serial;

import io.Serial;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

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

		if (args != null) {
			for (String n : args)
				ports.add(new Serial(n));
		}
		if (ports.isEmpty()) {
			ports.add(new Serial("/dev/ttyUSB1"));
			ports.add(new Serial("/dev/ttyUSB2"));
		}
		int cols = (ports.size()+2)/3;
		JPanel p = new JPanel(new GridLayout(0,cols));

		for (Serial sp : ports) {
			AnsiTerminal e = new AnsiTerminal(sp.getName(), false);
			p.add(e);
			editors.put(sp, e);
		}

		add(p,BorderLayout.CENTER);

		new Thread(new Runnable() {
			@Override
			public void run() {
				running = true;
				try {
					readloop();
				}catch(Throwable e){
					Log.error(e);
				} finally {
					running = false;
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
						//Log.error("%s", e);
						trm.setTitle("Open failed");
					}
				}
			}

			for (Serial s : ports) {
				AnsiTerminal trm = editors.get(s);
				if (!s.isOpen()) {
					trm.flushInput();
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
					int r, n=5;
					while ((r = s.read(buffer, 0, buffer.length)) > 0) {
						trm.write(buffer, 0, r);
						if (r < buffer.length) break;
						if (--n == 0) break;
					}
				}catch(Throwable e) {
					Log.error(e);
					s.close();
					trm.setTitle("closed on error");
				}
			}
		}

		Log.notice("Read loop finished");
		for (Serial s : ports) {
			s.close();
		}
	}

	public static void main(String[] args) {
		start(SerialMain.class, args);
	}
}
