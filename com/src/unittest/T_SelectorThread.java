package unittest;

import java.nio.ByteBuffer;

import sys.Log;
import sys.UnitTest;
import net.ChannelStatusHandler;
import net.ChannelWriter;
import net.SelectorThread;

public class T_SelectorThread extends UnitTest {
	//static String HOST = "stackoverflow.com";
	static String HOST = "www.google.com";
	//static String HOST = "104.16.33.249";
	
	static void startStop() throws Exception {
		SelectorThread t=new SelectorThread();
		t.start();
		Thread.sleep(1000);
		t.stop();
	}

	static void connectTCP() throws Exception {
		if (true) {
			return ;
		}
		final byte[] CRLF = "\r\n".getBytes();
		final ByteBuffer data = ByteBuffer.allocate(100*1024);
		data.put("GET /?gfe_rd=cr&ei=uUfgVvPuEeza8Afh9IbIDQ&gws_rd=cr HTTP/1.1".getBytes());
		data.put(CRLF);
		data.put(("Host: " + HOST).getBytes());
		data.put(CRLF); data.put(CRLF);
		//while (data.remaining() > CRLF.length) data.put(CRLF);
		data.flip();
		SelectorThread t=new SelectorThread();
		t.start();
		t.connect(HOST, 80, new ChannelStatusHandler(){
			public void connected(SelectorThread st, ChannelWriter w) {
				Log.debug("connected!");
				w.write(st, data);
			}			
			public void received(SelectorThread st, ChannelWriter w, ByteBuffer buf) {
				String rep = new String(buf.array(), buf.position(), buf.limit());
				Log.debug("data[%d]:  %s",rep.length(), rep);
			}
		});
		Thread.sleep(3000);
		t.stop();
	}
}
