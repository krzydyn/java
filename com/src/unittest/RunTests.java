package unittest;

import sys.UnitTest;

public class RunTests {

	public static void main(String[] args) {
		if (args.length == 0) {
			UnitTest.testAll("unittest.");
		}
		else {
			for (String p : args)
				UnitTest.testAll(p);
		}
	}
}
