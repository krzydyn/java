package netio;

import java.nio.ByteBuffer;

import netio.SelectorThread.QueueChannel;

public interface ChannelHandler extends ServerHandler {
	void received(QueueChannel qchn, ByteBuffer buf);
	void write(QueueChannel qchn, ByteBuffer buf);
}
