package unittest;

import sys.Log;
import sys.UnitTest;

public class T_System extends UnitTest {
	public static void printStackTrace() {
		new Throwable().printStackTrace(System.out);
	}

	public static void listPackages() throws Exception {
        System.out.println(UnitTest.getClasses("unittest"));
    }

	public static void colorLog() {
        Log.error("Error - red");
        Log.warn("Warning - yellow");
        Log.debug("Debug - default");
        Log.trace("Trace - cyan");
        Log.info("Info - blue");
        Log.notice("Notice - green");
    }
}
