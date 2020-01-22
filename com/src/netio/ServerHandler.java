package netio;

import netio.SelectorThread.QueueChannel;

public interface ServerHandler {
	ChannelHandler connected(QueueChannel qchn);
	void closed(QueueChannel qchn, Throwable e);
}
