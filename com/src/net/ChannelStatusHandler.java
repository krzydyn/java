package net;

import java.nio.ByteBuffer;

public interface ChannelStatusHandler {
	public void connected(SelectorThread st, ChannelWriter w);
	public void received(SelectorThread st, ChannelWriter w, ByteBuffer buf);
}
