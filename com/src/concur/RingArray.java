package concur;

// Thread safe ring queue similar to build-in ArrayDeque
public class RingArray<T> extends RingCollection {
	private T[] buf;
	private Object canpop=new Object();
	private Object canpush=new Object();

	@SuppressWarnings("unchecked")
	public RingArray(int capacity) {
		buf = (T[])new Object[capacity];
	}

	synchronized public boolean add(T o){
		if (len == buf.length) return false;
		buf[(idx+len)%buf.length]=o;
		++len;
		if (len==1) canpop.notify();
		return true;
	}
	//Pushes an element onto the stack (inserts the element at the front)
	synchronized public boolean push(T o){
		if (len == buf.length) return false;
		idx=(idx+buf.length-1)%buf.length;
		buf[idx]=o;
		++len;
		if (len==1) canpop.notify();
		return true;
	}
	//Pops an element from the stack (removes the element at the front)
	synchronized public T pop(){
		if (len == 0) throw new RuntimeException("queue is empty");
		return poll();
	}
	// Retrieves and removes the head of the queue
	synchronized public T poll(){
		if (len == 0) return null;
		T o = buf[idx];
		buf[idx]=null;
		++idx; --len;
		return o;
	}
	// Retrieves, but does not remove, the head of the queue
	synchronized public T peek(){
		if (len == 0) return null;
		return buf[idx];
	}

	//waiting version methods
	synchronized public void addw(T o) throws InterruptedException {
		while (len == buf.length) {
			canpush.wait();
		}
		add(o);
	}
	synchronized public void pushw(T o) throws InterruptedException {
		while (len == buf.length) {
			canpush.wait();
		}
		push(o);
	}
	synchronized public T popw(long t) throws InterruptedException {
		if (len == 0) canpop.wait(t);
		return poll();
	}
}
