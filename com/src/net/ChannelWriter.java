package net;

import java.nio.ByteBuffer;

public interface ChannelWriter {
	public void write(SelectorThread st, ByteBuffer b);
}
