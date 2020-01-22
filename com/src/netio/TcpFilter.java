package netio;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import netio.SelectorThread.QueueChannel;

public class TcpFilter extends AbstractFilter {
	static final int MAX_MSG_BUF = 16*1024;
	private final ByteBuffer inmsg = ByteBuffer.allocate(MAX_MSG_BUF);
	private int inlen;

	public TcpFilter(ChannelHandler link) {
		super(link);
	}
	@Override
	public ChannelHandler connected(QueueChannel qchn) {
		super.connected(qchn);
		((Buffer)inmsg).clear();
		return null;
	}

	@Override
	public void closed(QueueChannel qchn,Throwable e) {
		super.closed(qchn, e);
		((Buffer)inmsg).clear();
	}

	@Override
	public void received(QueueChannel qchn, ByteBuffer buf) {
		int intbytes = 4;
		while (buf.remaining() > 0) {
			if (inlen==0) {
				if (!readData(intbytes, buf)) return ;
				((Buffer)inmsg).flip();
				inlen = inmsg.getInt();
				((Buffer)inmsg).clear();
				if (inlen < 0) throw new RuntimeException("Message out of sync");
			}
			if (!readData(inlen, buf)) return ;
			((Buffer)inmsg).flip();
			link.received(qchn, inmsg);
			((Buffer)inmsg).clear();
			inlen=0;
		}
	}
	private boolean readData(int limit, ByteBuffer src) {
		if (inmsg.position() + src.remaining() < limit) {
			inmsg.put(src);
		}
		else {
			while (inmsg.position() < limit) inmsg.put(src.get());
		}
		return inmsg.position() == limit;
	}

	@Override
	public void write(QueueChannel qchn, ByteBuffer b) {
		ByteBuffer lenbuf = ByteBuffer.allocate(4);
		lenbuf.putInt(b.remaining());
		((Buffer)lenbuf).flip();
		//Log.debug("writeTCP(payload=%d)",b.remaining());
		qchn.write(lenbuf);
		qchn.write(b,true);
	}
}
