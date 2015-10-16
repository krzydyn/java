package time;

public class LapTimer {
	private long t0,t1,t;
	private double v0,v1,v;
	final private String unit; 
	public LapTimer(String unit) {
		this.unit=unit;
		reset(0.0);
	}
	public void reset(double v) {
		t0=t1=t=System.currentTimeMillis();
		v0=v1=v;		
	}
	public void update(long t,double v) { this.t=t; this.v+=v; }
	public void updateSet(long t,double v) { this.t=t; this.v=v; }
	public void nextLap() { t1=t; v1=v; }
	
	public long getTotalTime() { return t-t0; }
	public long getLapTime() { return t-t1; }
	
	public double getTotalSpeed() {
		long d=t-t0;
		return d==0 ? Double.NaN : (v-v0)/d;
	}
	public double getPrevSpeed() {
		long d=t1-t0;
		return d==0 ? Double.NaN : (v1-v0)/d;
	}
	public double getLapSpeed() {
		long d=t-t1;
		return d==0 ? Double.NaN : (v-v1)/d;
	}
	public String toString() {
		return String.format("%d/%d %s/ms, %.3f k%s/s [%.3f k%s/s]", (long)v, getTotalTime(), unit, getLapSpeed(), unit, getTotalSpeed(), unit);
				
	}
}
