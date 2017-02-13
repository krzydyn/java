package concur;

import java.util.Map;
import java.util.TreeMap;

public class LamportMutex {
	Map<Integer,Boolean> entering = new TreeMap<Integer, Boolean>();
	Map<Integer,Integer> number = new TreeMap<Integer, Integer>();

	public void lock(int pid) {
		entering.put(pid, true);
		int max = 0;
		for (int i : number.values()) {
			if (max < i) max = i;
		}
		number.put(pid, 1 + max);
		entering.put(pid, false);
		for (int i : number.values()) {
			if (i != pid) {
				while (entering.get(i)) { Thread.yield(); } // wait while other thread picks a ticket
				while (number.get(i) != 0 && ( number.get(pid) > number.get(i)  ||
						(number.get(pid) == number.get(i) && pid > i)))
				{ Thread.yield(); }
			}
		}
	}

	public void unlock(int pid) {
		number.put(pid, 0);
	}
}
