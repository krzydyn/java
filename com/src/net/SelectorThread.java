package net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;

public class SelectorThread {
	private Selector selector;
	
	public SelectorThread() throws IOException {
		selector = Selector.open();
	}
	
	public void start() {
		new Thread(new Runnable() {
			public void run() {
				try { loop(); }
				catch (Throwable e) {}
			}
		}).start();
	}
	
	public void addChannel(SelectableChannel c, int ops, MsgDispatcher d) throws IOException {
		c.register(selector, ops, d);
	}
	public void bind(String addr, MsgDispatcher d) throws IOException {
		
	}
	public void connect(String addr, MsgDispatcher d) throws IOException {
	}
	
	private void idle() {}
	private void loop() throws Exception {
		for (;;) {
			int n=selector.select(1000);
			if (n==0) {idle();continue;}
			for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
				SelectionKey sk = i.next();
				i.remove();
				try {
					if (!sk.isValid()) ;
					else if (sk.isAcceptable()) accept(sk);
					else if (sk.isConnectable()) finishConnect(sk);
					else {
						if (sk.isWritable()) write(sk);
						if (sk.isReadable()) read(sk);
					}
				} catch (Throwable e) {
					sk.channel().close();
					sk.cancel();
				}
			}
		}
	}

	private void accept(SelectionKey sk) throws IOException {
		ServerSocketChannel server = (ServerSocketChannel)sk.channel();
		SocketChannel client = server.accept();
		//client.register(selector, SelectionKey.OP_READ);
		addChannel(client, SelectionKey.OP_READ, (MsgDispatcher)sk.attachment());
	}
	private void finishConnect(SelectionKey sk) throws IOException {
		SocketChannel client = (SocketChannel)sk.channel();
		client.register(selector, SelectionKey.OP_READ);
	}
	private void read(SelectionKey sk) throws IOException {
		ReadableByteChannel c=(ReadableByteChannel)sk.channel();
		ByteBuffer dst=ByteBuffer.allocate(1024);
		c.read(dst);
	}
	private void write(SelectionKey sk) throws IOException {
		WritableByteChannel c=(WritableByteChannel)sk.channel();
		ByteBuffer src=ByteBuffer.allocate(1024);
		c.write(src);
	}
}
