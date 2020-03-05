package concur;

import sys.Log;

// Thread safe ring queue similar to build-in ArrayBlockingQueue
public class RingArray<T> extends RingCollection {
	private final String name;
	private final Object canget = new Object();
	private final Object canadd = new Object();
	private T[] buf;
	private boolean intr;

	public RingArray(int capacity) {
		this("", capacity);
	}
	@SuppressWarnings("unchecked")
	public RingArray(String name, int capacity) {
		this.name = name;
		buf = (T[])new Object[capacity];
	}

	public void setInterrupted() {
		synchronized (canget) {
			intr = true;
			canget.notifyAll();
		}
	}

	public boolean isInterrupted() { return intr; }

	private void get_notify() {
		synchronized (canget) { canget.notify(); }
	}

	private boolean prv_add(T o) {
		synchronized (this) {
			if (len == buf.length) return false;
			buf[(idx+len)%buf.length]=o;
			++len;
			if (len == 1) get_notify();
		}
		return true;
	}

	// Add element (insert the element at the end)
	public boolean add(T o){
		if (!prv_add(o)) {
			Log.error("%s: queue overflow", name);
			return false;
		}
		return true;
	}

	// Push element (insert the element at the head)
	public boolean push(T o){
		synchronized (this) {
			if (len == buf.length) return false;
			idx = (idx+buf.length-1)%buf.length;
			buf[idx]=o;
			++len;
			if (len == 1) get_notify();
		}
		return true;
	}

	//Pop element (removes the element from the head)
	public T pop(){
		if (len == 0) throw new RuntimeException("queue is empty");
		return poll();
	}
	// Retrieve and remove the head of the queue
	public T poll() {
		T o = null;
		int l = len;
		synchronized (this) {
			if (len == 0) {
				Log.error("%s: queue underflow", name);
				return null;
			}
			o = buf[idx];
			buf[idx] = null;
			idx = (idx+buf.length+1)%buf.length;
			--len;
		}
		if (l <= buf.length)
			synchronized (canadd) { canadd.notify(); }
		return o;
	}
	// Retrieves, but does not remove, the head of the queue
	public T peek(){
		synchronized (this) {
			if (len == 0) return null;
			return buf[idx];
		}
	}

	//waiting version methods
	public T peekw() throws InterruptedException {
		if (len == 0) {
			synchronized (canget) {
				Log.debug(1, "%s canget.wait()  len == %d", name, len);
				if (!intr) canget.wait();
			}
		}
		return peek();
	}

	public void addw(T o) throws InterruptedException {
		while (!prv_add(o)) {
			synchronized (canadd) { canadd.wait(); }
		}
	}
	public boolean addw(T o, long tm) throws InterruptedException {
		if (prv_add(o)) return true;
		synchronized (canadd) { canadd.wait(tm); }
		return add(o);
	}
	public void pushw(T o) throws InterruptedException {
		while (!push(o)) {
			synchronized (canadd) { canadd.wait(); }
		}
	}
	public T popw(long t) throws InterruptedException {
		if (len == 0) synchronized (canadd) { canadd.wait(); }
		return poll();
	}
}
