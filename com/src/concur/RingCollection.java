package concur;

public abstract class RingCollection {
	protected int idx,len;
	final synchronized public int size() { return len; }
	final synchronized public void clear() {
		idx=len=0;
	}
}
