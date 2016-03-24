package time;

public class LapTime {
	final private String unit;
	private long t0,t1,t;
	private double v0,v1,v;

	public LapTime(String unit) {
		this.unit=unit;
		reset(0.0);
	}
	public void reset(double v) {
		t0=t1=t=System.currentTimeMillis();
		v0=v1=this.v=v;
	}
	public void update(double v) { this.t=System.currentTimeMillis(); this.v+=v; }
	public void update(long t,double v) { this.t=t; this.v+=v; }
	public void nextLap() { t1=t; v1=v; this.t=System.currentTimeMillis(); }

	public long getTotalTime() { return t-t0; }
	public double getTotalSpeed() {
		long d=t-t0;
		return d==0 ? Double.NaN : (v-v0)/d;
	}

	public long getTime() { return t-t1; }
	public double getSpeed() {
		long d=t-t1;
		return d==0 ? Double.NaN : (v-v1)/d;
	}

	@Override
	public String toString() {
		return String.format("%d/%d %s/ms, %.3f k%s/s [%.3f k%s/s]", (long)v, getTotalTime(), unit, getSpeed(), unit, getTotalSpeed(), unit);

	}
}
