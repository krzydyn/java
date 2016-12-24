package net;

import java.nio.ByteBuffer;

import net.SelectorThread2.QueueChannel;

public interface ChannelHandler {

	void connected(QueueChannel qchn);
	void received(QueueChannel chnst, ByteBuffer b);

}
