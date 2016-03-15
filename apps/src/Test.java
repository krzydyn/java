import java.util.HashMap;
import java.util.Map;


public class Test implements Runnable {
	
	private Map<String,Object> locks=new HashMap<String,Object>();
	
	public void getLock(String x) {
    	for (;;) {
    		Object lock;
    		synchronized (locks) {
    			lock=locks.get(x);
    			if (lock == null) {
    				locks.put(x, lock=new Object());
    				break;
    			}
    		}
			try {
				System.out.printf("Thread-%s locked\n",Thread.currentThread().getName());
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {}
    	}
	}
	public void unLock(String x) {
    	synchronized (locks) {
    		Object lock=locks.remove(x);
    		synchronized (lock) {
    			lock.notifyAll();
    		}
    	}		
	}
	
    public void myMethod(String x){
    	getLock(x);
    	
		System.out.printf("Thread-%s start\n",Thread.currentThread().getName());
		// do your staff
		for (int i=0; i<30; ++i) {
			System.out.printf("Thread-%s running %d\n", Thread.currentThread().getName(),i);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		System.out.printf("Thread-%s end\n",Thread.currentThread().getName());
    	
		unLock(x);
    }
    
    public void run() {
    	myMethod("abc");
    }

	public static void main(String[] args) {
		final Test t = new Test();
		Thread ths[] = new Thread[30];
		for (int i=0; i < ths.length; ++i)
			ths[i]=new Thread(t, String.valueOf(i));
		
		for (int i=0; i < ths.length; ++i)
			ths[i].start();
	}
}
