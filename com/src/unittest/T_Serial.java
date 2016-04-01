package unittest;

import java.io.IOException;

import io.Serial;
//import io.Serial2;
import sys.Log;
import sys.UnitTest;
import sys.XThread;
import text.Text;

public class T_Serial extends UnitTest {
	static void serialListPorts() {
		Log.debug("ports %s", Text.join(Serial.listPorts(), ","));
		XThread.sleep(1000);
		Log.debug("ports %s", Text.join(Serial.listPorts(), ","));
	}
	static void serialReadWrite() throws IOException {
		byte[] tmp = new byte[10];

		Serial s = new Serial("/dev/ttyS0");
		s.open();
		s.read(tmp, 0, tmp.length);
		s.write(tmp, 0, tmp.length);
		s.close();
	}
}
