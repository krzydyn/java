package netio;

import netio.SelectorThread.QueueChannel;

public abstract class AbstractFilter implements ChannelHandler {
	protected final ChannelHandler link;

	public AbstractFilter(ChannelHandler link) {
		this.link=link;
	}

	@Override
	public ChannelHandler createFilter() {
		return null;
	}

	@Override
	public void connected(QueueChannel qchn) {
		link.connected(qchn);
	}

	@Override
	public void disconnected(QueueChannel qchn, Throwable e) {
		link.disconnected(qchn, e);
	}
}
