package test;

public abstract class AbstractSingleton {
	private volatile static AbstractSingleton _INSTANCE=null;
	private static Object lock=new Object();

	abstract protected AbstractSingleton create();

	public AbstractSingleton getInstance() {
		if(_INSTANCE == null){
			synchronized(lock){
	        	 if(_INSTANCE == null) _INSTANCE = create();
			}
		}
		return _INSTANCE;
	}
	@Override
	public Object clone() {
		throw new RuntimeException("singleton can't be cloned");
	}
}
