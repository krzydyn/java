package puzzle;

import java.util.Set;
import java.util.TreeSet;

public class CalcNumber {
	Set<Integer> avail = new TreeSet<>();
	int end;

	/*
	 * Go from start to end in lowest number of steps.
	 * Available operators: +,-,*
	 * Available numbers: numbers appeared up to current step
	 */
	public CalcNumber(int start, int end) {
		avail.add(start);
		this.end = end;
	}

	/*
	 * 1. each sep produces new number
	 * 2. 3 operators per step
	 * BrutForce complexity  = sum(a[[i])
	 *    a[0] = 1
	 *    a[i] = a[i-1]*i*3
	 */
	void start() {

	}


	public static void main(String[] args) {
		new CalcNumber(1, 123456).start();
	}
}
