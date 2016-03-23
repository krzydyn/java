package unittest;

import java.io.PrintStream;

import sys.Log;
import sys.UnitTest;
import text.Ansi;

public class T_System extends UnitTest {

	public static void listPackages() throws Exception {
        System.out.println(UnitTest.getClasses("unittest"));
    }

	public static void ansiSequences() {
		PrintStream p = System.out;
		for (int i=0; i < 16; ++i)
			p.printf("Color %d: %s%d;%dmsample text%s\n", i, Ansi.CSI, i/8, 30+i%8, Ansi.SGR_RESET);
    }

	public static void loggerColors() {
		Log.error("Error");
        Log.warn("Warning");
        Log.debug("Debug");
        Log.trace("Trace");
        Log.info("Info");
        Log.notice("Notice");
	}
}
