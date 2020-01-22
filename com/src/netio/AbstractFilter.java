package netio;

import netio.SelectorThread.QueueChannel;

public abstract class AbstractFilter implements ChannelHandler {
	protected final ChannelHandler link;

	public AbstractFilter(ChannelHandler link) {
		this.link=link;
	}

	@Override
	public ChannelHandler connected(QueueChannel qchn) {
		return link.connected(qchn);
	}

	@Override
	public void closed(QueueChannel qchn, Throwable e) {
		link.closed(qchn, e);
	}
}
