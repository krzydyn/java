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

package unittest;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.List;

import io.Serial;
import io.SerialChannel;
import sys.Log;
import sys.UnitTest;
import text.Text;

public class T_Serial extends UnitTest {
	static void serialListPorts() {
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {
				List<String> l=Serial.listPorts();
				Log.debug("Ports: [%d] %s", l.size(), Text.join("\n", l));
			}
		});
	}
	static void serialReadWrite() throws IOException {
		final byte[] tmp = new byte[10];
		List<String> l=Serial.listPorts();
		if (l.size() == 0) {
			Log.warn("no serial ports available");
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
	static void channel() throws IOException {
		Selector sel = Selector.open();
		SerialChannel chn=SerialChannel.open();
		chn.configureBlocking(false);
		SelectionKey sk = chn.register(sel, SelectionKey.OP_READ, null);
	}
}
