package net;

import java.nio.ByteBuffer;

import text.Ansi;
import net.SelectorThread.QueueChannel;
/**
 * Data Link Protocol
 * aka Binary Synchronous Communication (BSC or Bisync)
 * @author k.dynowski
 *
 */
public class DlpFilter implements ChannelHandler {

	ByteBuffer wbuf = ByteBuffer.allocate(1+2);

	@Override
	public ChannelHandler createFilter() {
		return null;
	}

	@Override
	public void connected(QueueChannel qchn) {
	}

	@Override
	public void disconnected(QueueChannel qchn) {
	}

	@Override
	public void received(QueueChannel qchn, ByteBuffer buf) {
	}

	@Override
	public void write(QueueChannel qchn, ByteBuffer buf) {
		wbuf.clear();
		wbuf.put((byte)Ansi.Code.STX); // or STH if header
		wbuf.flip();
		qchn.write(wbuf);
		qchn.write(buf);
		wbuf.clear();
		wbuf.put((byte)Ansi.Code.ETX); // or ETB if block
		byte bcc=0; //block check character (LRC for USACSII, CRC for TC or EBDIC)
		wbuf.put(bcc);
		wbuf.flip();
		qchn.write(wbuf);
	}
}
