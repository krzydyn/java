package gthreads;

public abstract class GreenThread {
	public GreenThread() {
	}
	
	//input ready, generate output
	public abstract void process(Msg in, Msg out);
	
}

