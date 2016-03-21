package sys;

final public class XThread extends Thread {
	private boolean interrupted = false;
	public XThread(Runnable r) {
		super(r);
	}
	public boolean isInterrupted() {
		if (!interrupted && super.isInterrupted()) interrupted = true;
		return interrupted;
	}
	public void interrupt() { interrupted=true; super.interrupt(); }

	static public void sleep(long millis) {
		try {Thread.sleep(millis);}
		catch (InterruptedException e) { //InterruptedException clears interrupted flag of Thread
			Thread t = Thread.currentThread();
			t.interrupt(); // set the flag again
			if (t instanceof XThread) ((XThread)t).interrupted = true;
		}
	}
}
