package sys;

public class Time {
	static public void sleep(long millis) {
		try {Thread.sleep(millis);} catch (InterruptedException e) {}
	}
}
