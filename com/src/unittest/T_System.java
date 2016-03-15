package unittest;

import sys.UnitTest;

public class T_System extends UnitTest {
	public static void printStackTrace() {
		new Throwable().printStackTrace();
	}
	public static void listPackages() throws Exception {
        System.out.println(UnitTest.getClasses("unittest"));
    }
	public static void printAnsi() {
        System.out.println("Hello \u001b[1;31mred\u001b[0m world!");
    }
}
