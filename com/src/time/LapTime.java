/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

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
	public long update(double v) { t=System.currentTimeMillis(); this.v+=v; return t;}
	public long updateAbs(double v) { t=System.currentTimeMillis(); this.v=v; return t;}
	public void nextLap() { t1=t; v1=v; }

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
