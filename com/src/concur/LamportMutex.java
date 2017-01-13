package concur;

import java.util.ArrayList;

public class LamportMutex {
	static final int threads=100;
	static ArrayList<Integer> number = new ArrayList<>(threads); // ticket for threads in line, n - number of threads
	static ArrayList<Boolean> entering = new ArrayList<>(threads); // True when thread entering in line

	public void lock(int pid) {
		entering.set(pid, true);
		int max = 0;
		for (int i = 0; i < threads; i++) {
			int current = number.get(i);
			if (current > max) {
				max = current;
			}
		}
		number.set(pid, 1 + max);
		entering.set(pid, false);
		for (int i = 0; i < number.size(); ++i) {
			if (i != pid) {
				while (entering.get(i)) { Thread.yield(); } // wait while other thread picks a ticket
				while (number.get(i) != 0 && ( number.get(pid) > number.get(i)  ||
						(number.get(pid) == number.get(i) && pid > i)))
				{ Thread.yield(); }
			}
		}
	}

	public void unlock(int pid) {
		number.set(pid, 0);
	}
}
