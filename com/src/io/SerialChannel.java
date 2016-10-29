package io;

import gnu.io.CommPort;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;

/**
 * Implements selectable SerialChannel that can be used with Selector
 * @author krzydyn
 *
 */
public class SerialChannel extends AbstractSelectableChannel implements ByteChannel {
	private CommPort commPort;

	protected SerialChannel() {
		super(null);
		//commPort =portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	}

	public static SerialChannel open() throws IOException {
        return SerialProvider.openSerialChannel();
    }

	// from AbstractSelectable
	@Override
	protected void implCloseSelectableChannel() throws IOException {
		commPort.close();
	}

	// from AbstractSelectable
	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int validOps() {
		return SelectionKey.OP_READ|SelectionKey.OP_WRITE;
	}

	static private class SerialProvider {
		public static SerialChannel openSerialChannel() {
			return new SerialChannel();
		}
	}
}
