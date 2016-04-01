package serial;

import io.Serial;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import sys.Log;
import ui.MainPanel;

@SuppressWarnings("serial")
public class SerialMain extends MainPanel implements FocusListener,KeyListener {

	private List<Serial> ports = new ArrayList<Serial>();
	private Map<Serial,EditorUI> editors = new HashMap<Serial,EditorUI>();
	private boolean running = false;

	private EditorUI focused;
	private StringBuilder keysToSend = new StringBuilder(50);

	public SerialMain() {
		JPanel p;

		setName("MultiConsole");

		/*p = new JPanel();
		p.add(new JButton("Start"));
		p.add(new JButton("Stop"));
		add(p,BorderLayout.NORTH);*/

		p = new JPanel(new GridLayout(2,0));

		p.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		p.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

		List<String> nm = Serial.listPorts();
		for (String n : nm) {
			if (n.contains("USB"))
				ports.add(new Serial(n));
		}

		for (Serial sp : ports) {
			EditorUI e = new EditorUI(sp.getName(), false);
			p.add(e);
			e.addFocusListener(this);
			e.addKeyListener(this);
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
					EditorUI ed = editors.get(s);
					try {
						if (!s.isOpen()) {
							s.open();
							s.setParams(115200, Serial.Param.DATA_8, Serial.Param.STOP_0, Serial.Param.FLOW_NONE);
							ed.append("Port opened\n");
						}
					}
					catch (Throwable e) {
						Log.error("%s", e);
						ed.append("Open failed\n");
					}
				}
			}

			if (keysToSend.length() > 0) {
				for (Serial s : ports) {
					EditorUI ed = editors.get(s);
					if (ed == focused) {
						byte[] b = keysToSend.toString().getBytes();
						try {
							s.write(b, 0, b.length);
						}catch(Exception e) {
							Log.error(e);
							s.close();
							ed.append("Port closed on error\n");
						}
					}
				}
				keysToSend.setLength(0);
			}

			for (Serial s : ports) {
				if (!s.isOpen()) {
					if (reopen == 0)
						reopen = System.currentTimeMillis()+1000;
					continue;
				}
				EditorUI ed = editors.get(s);
				try {
					int r = s.read(buffer, 0, buffer.length);
					if (r > 0) {
						ed.append(buffer, 0, r);
					}
				}catch(Throwable e) {
					Log.error(e);
					s.close();
					ed.append("Port closed on error\n");
				}
			}
		}

		Log.notice("Read loop finished");
		for (Serial s : ports) {
			s.close();
		}
	}


	public static void main(String[] args) {
		start(SerialMain.class);
	}

	@Override
	public void focusGained(FocusEvent e) {
		focused = (EditorUI)e.getSource();
		Log.debug("Focused %s", focused.getName());
	}
	@Override
	public void focusLost(FocusEvent e) {
		//focused = null;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		Log.debug("key pressed %d", (int)e.getKeyChar());
		keysToSend.append(e.getKeyChar());
		e.consume();
	}
	@Override
	public void keyPressed(KeyEvent e) {
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}
}
