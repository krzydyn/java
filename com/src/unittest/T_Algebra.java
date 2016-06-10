package unittest;

import java.util.ArrayList;
import java.util.List;

import algebra.Combinatory;
import algebra.Maths;
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

	static void blackjack() {
		String[] test = {"", "AA", "XA", "9AA", "8AA", "8AAA"};
		int [] res    = {0, 12, 21, 21, 20, 21};
		for (int i=0; i < test.length; ++i) {
			int r=Maths.blackjackPoints(test[i]);
			check(r==res[i], String.format("%s=%d, should be %d", test[i], r, res[i]));
		}
	}
}
