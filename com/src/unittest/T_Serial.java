package unittest;

import java.io.IOException;
import java.util.List;

import io.Serial;
import sys.Log;
import sys.UnitTest;
import text.Text;

public class T_Serial extends UnitTest {
	static void serialListPorts() {
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {
				List<String> l=Serial.listPorts();
				Log.debug("Ports: [%d] %s", l.size(), Text.join(l, "\n"));
			}
		});
	}
	static void serialReadWrite() throws IOException {
		final byte[] tmp = new byte[10];
		List<String> l=Serial.listPorts();
		if (l.size() == 0) {
			Log.error("no serial ports available");
			return ;
		}
		final String port = l.get(0);
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() throws Exception {
				Serial s = new Serial(port);
				s.open();
				s.read(tmp, 0, tmp.length);
				s.write(tmp, 0, tmp.length);
				s.close();
			}
		});
	}
}
