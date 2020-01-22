package netio;

import java.nio.ByteBuffer;

import text.Ansi;
import netio.SelectorThread.QueueChannel;
/**
 * Data Link Protocol
 * aka Binary Synchronous Communication (BSC or Bisync)
 * @author k.dynowski
 *
 */
public class DlpFilter implements ChannelHandler {

	ByteBuffer wbuf = ByteBuffer.allocate(1+2);

	@Override
	public ChannelHandler connected(QueueChannel qchn) {
		return null;
	}

	@Override
	public void closed(QueueChannel qchn, Throwable e) {
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
		wbuf.put((byte)calcBCC(buf));
		wbuf.flip();
		qchn.write(wbuf);
	}

	//block check character (LRC for USACSII, CRC for TC or EBDIC)
	private int calcBCC(ByteBuffer buf) {
		return 0;
	}
}
