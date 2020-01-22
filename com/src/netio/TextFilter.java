package netio;

import java.nio.ByteBuffer;

import netio.SelectorThread.QueueChannel;

public class TextFilter extends AbstractFilter {
	StringBuilder requ = new StringBuilder();

	public TextFilter(ChannelHandler link) {
		super(link);
	}

	@Override
	public void received(QueueChannel qchn, ByteBuffer buf) {
		link.received(qchn, buf);
	}

	@Override
	public void write(QueueChannel qchn, ByteBuffer buf) {
		link.write(qchn, buf);
	}

}
