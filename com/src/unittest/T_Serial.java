package unittest;

import java.io.IOException;

import io.Serial;
import sys.UnitTest;

public class T_Serial extends UnitTest {
	static void serialListPorts() {
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {Serial.listPorts();}
		});
	}
	static void serialReadWrite() throws IOException {
		final byte[] tmp = new byte[10];

		checkNoThrow(new RunThrowable() {
			@Override
			public void run() throws Exception {
				Serial s = new Serial("/dev/ttyS0");
				s.open();
				s.read(tmp, 0, tmp.length);
				s.write(tmp, 0, tmp.length);
				s.close();
			}
		});
	}
}
