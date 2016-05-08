package unittest;

import java.util.ArrayList;
import java.util.List;

import algebra.Combinatory;
import sys.UnitTest;

public class T_Algebra extends UnitTest {
	static void permutation() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(2);
		l.add(4);
		l.add(7);
		int n=0;
		do {
			++n;
			//Log.debug("%d: %s", n, l.toString());
		} while (Combinatory.nextPermutation(l));
		check(n == 6, "should be 6 permutations");
	}
}
