package netio;

import java.nio.ByteBuffer;

import netio.SelectorThread.QueueChannel;

public interface ChannelHandler {
	ChannelHandler createFilter();
	void connected(QueueChannel qchn);
	void disconnected(QueueChannel qchn);
	void received(QueueChannel qchn, ByteBuffer buf);
	void write(QueueChannel qchn, ByteBuffer buf);
}
