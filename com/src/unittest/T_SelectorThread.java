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

import java.nio.ByteBuffer;

import sys.Log;
import sys.UnitTest;
import text.Text;
import net.ChannelStatusHandler;
import net.ChannelWriter;
import net.SelectorThread2;

public class T_SelectorThread extends UnitTest {
	//static String HOST = "stackoverflow.com";
	static String HOST = "www.google.com";
	//static String HOST = "104.16.33.249";

	static void startStop() throws Exception {
		SelectorThread2 t=new SelectorThread2();
		t.start();
		Thread.sleep(1000);
		t.stop();
	}

	static void connectTCP() throws Exception {
		final byte[] CRLF = "\r\n".getBytes();
		final ByteBuffer data = ByteBuffer.allocate(100*1024);
		data.put("GET /?gfe_rd=cr&ei=uUfgVvPuEeza8Afh9IbIDQ&gws_rd=cr HTTP/1.1".getBytes());
		data.put(CRLF);
		data.put(("Host: " + HOST).getBytes());
		data.put(CRLF); data.put(CRLF);
		//while (data.remaining() > CRLF.length) data.put(CRLF);
		data.flip();
		SelectorThread2 t=new SelectorThread2();
		t.start();
		t.connect(HOST, 80, new ChannelStatusHandler(){
			@Override
			public void connected(ChannelWriter w) {
				Log.debug("connected!");
				w.write(data);
			}
			@Override
			public void received(ChannelWriter w, ByteBuffer buf) {
				String rep = new String(buf.array(), buf.position(), buf.limit(), Text.UTF8_Charset);
				Log.debug("data[%d]:  %s",rep.length(), rep.length()>20?rep.substring(0, 20)+"...":rep);
			}
		});
		Thread.sleep(3000);
		t.stop();
	}
}
