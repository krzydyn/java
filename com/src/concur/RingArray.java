package concur;

// Similar to build-in java.utils.ArrayDeque
public class RingArray<T> extends RingCollection {
	private final T[] buf;
	@SuppressWarnings("unchecked")
	public RingArray(int capacity) {
		buf = (T[])new Object[capacity];
	}

	boolean add(T o){
		if (len == buf.length) return false;
		buf[(idx+len)%buf.length]=o;
		++len;
		return true;
	}
	//Pushes an element onto the stack (inserts the element at the front)
	boolean push(T o){
		if (len == buf.length) return false;
		idx=(idx+buf.length-1)%buf.length;
		buf[idx]=o;
		++len;
		return true;
	}
	//Pops an element from the stack (removes the element at the front)
	T pop(){
		if (len == 0) throw new RuntimeException("queue is empty");
		return poll();
	}
	// Retrieves and removes the head of the queue
	T poll(){
		if (len == 0) return null;
		T o = buf[idx];
		buf[idx]=null;
		++idx; --len;
		return o;
	}
	// Retrieves, but does not remove, the head of the queue
	T peek(){
		if (len == 0) return null;
		return buf[idx];
	}
}
