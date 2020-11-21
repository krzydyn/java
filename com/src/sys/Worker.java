package sys;

public class Worker {
	private final Thread workThread;
	private boolean stopReq = false;

	public Worker(String name) {
		workThread = new Thread(name) {
			@Override
			public void run() {
				Log.notice("worker started");
				while (!stopReq) {
					try { Worker.this.run(); }
					catch (Throwable e) {
						Log.error(e, "Uncatched exception");
					}
					finally {
						if (!stopReq) {
							Log.warn("worker exit run(), restarting");
							Env.sleep(500); // do not restart too fast
						}
					}
				}
				Log.notice("worker died");
			}
		};
	}

	public boolean isRunning() {
		return workThread.isAlive();
	}

	public void start() {
		workThread.start();
	}
	public void stop() {
		stopReq = true;
		switch (workThread.getState()) {
		case NEW:
			break;
		case TERMINATED:
			break;
		case BLOCKED:
			break;
		case WAITING:
		case TIMED_WAITING:
			break;
		case RUNNABLE:
			break;
		}
	}

	public void run() {}
}
