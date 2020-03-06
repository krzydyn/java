package concur;

import sys.Log;

// Thread safe ring queue similar to build-in ArrayBlockingQueue
public class RingArray<T> extends RingCollection {
	private final String name;
	private final Object lock = new Object();
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
		synchronized (lock) {
			intr = true;
			lock.notifyAll();
		}
	}

	public boolean isInterrupted() { return intr; }
	public boolean isFull() { return len == buf.length; }
	public boolean isEmpty() { return len == 0; }

	private boolean prv_add(T o) {
		if (len == buf.length) return false;
		buf[(idx+len)%buf.length]=o;
		++len;
		Log.debug("%s: packet added, len = %d", name, len);
		if (len == 1) lock.notifyAll();
		return true;
	}

	// Add element (insert the element at the end)
	public boolean add(T o){
		synchronized (lock) {
			if (!prv_add(o)) {
				Log.error("%s: queue overflow", name);
				return false;
			}
		}
		return true;
	}

	// Push element (insert the element at the head)
	public boolean push(T o){
		synchronized (lock) {
			if (len == buf.length) return false;
			idx = (idx+buf.length-1)%buf.length;
			buf[idx]=o;
			++len;
			if (len == 1) lock.notifyAll();
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
		synchronized (lock) {
			if (len == 0) {
				Log.error("%s: queue underflow", name);
				return null;
			}
			o = buf[idx];
			buf[idx] = null;
			idx = (idx+buf.length+1)%buf.length;
			--len;
			if (len == buf.length-1) lock.notifyAll();
		}
		return o;
	}
	// Retrieves, but does not remove, the head of the queue
	public T peek(){
		synchronized (lock) {
			if (len == 0) return null;
			return buf[idx];
		}
	}

	//waiting version methods
	public T peekw() throws InterruptedException {
		synchronized (lock) {
			while (len == 0 && !intr) {
				Log.debug(1, "%s wait() len == %d", name, len);
				lock.wait();
			}
			return peek();
		}
	}

	public void addw(T o) throws InterruptedException {
		Log.debug("%s addw()", name);
		synchronized (lock) {
			while (!prv_add(o)) {
				Log.debug(1, "%s wait() len == %d", name, len);
				lock.wait();
			}
		}
	}
	public boolean addw(T o, long tm) throws InterruptedException {
		synchronized (lock) {
			if (prv_add(o)) return true;
			Log.debug(1, "%s wait() len == %d", name, len);
			lock.wait(tm);
			return prv_add(o);
		}
	}
	public void pushw(T o) throws InterruptedException {
		synchronized (lock) {
			while (!push(o)) {
				Log.debug(1, "%s wait() len == %d", name, len);
				lock.wait();
			}
		}
	}
	public T popw(long t) throws InterruptedException {
		synchronized (lock) {
			while (len == 0)  {
				Log.debug(1, "%s wait() len == %d", name, len);
				lock.wait();
			}
			return poll();
		}
	}
}
