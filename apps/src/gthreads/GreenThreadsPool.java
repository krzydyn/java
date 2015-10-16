package gthreads;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GreenThreadsPool {
	
	List<GreenThread> threads=new LinkedList<>();
	private final Object lock=new Object(); 
	
	void addThread(GreenThread t) {
		threads.add(t);
	}
	void tick() throws InterruptedException {
		if (threads.size()<2) return ;
		synchronized (lock) {
			lock.notify();
			lock.wait();
		}
	}
	
	static final Object vLock=new Object();
	static int value;
	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Runnable incr=new Runnable() {
			public void run() {
				//synchronized (vLock) {
					++value;					
				//}
			}
		};
		for (int i=0; i< 10000; ++i)
			executor.execute(incr);
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(value);
	}
}
