package sys;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Log {
	private static final SimpleDateFormat tmfmt_rel = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final SimpleDateFormat tmfmt_tst = new SimpleDateFormat("HH:mm:ss.SSS");
	
	private static SimpleDateFormat tmfmt = tmfmt_tst;
	private static Object lock=new Object();
	private static void log(int level, String msg, Object ...args) {
		String file = null;
		int line = -1;
		if (tmfmt == tmfmt_tst) {
			StackTraceElement[] bt = new Throwable().getStackTrace();
			if (bt.length > 2) {
				file = bt[2].getFileName();
				line = bt[2].getLineNumber();
			}
		}
		
		@SuppressWarnings("resource")
		final PrintStream s = level == 0 ? System.err : System.out;
		synchronized (lock) {
			s.printf("%s: ", tmfmt.format(new Date()));
			if (file != null) s.printf("(%s:%d) ", file, line );
			s.printf((Locale)null, msg, args);
			s.println();
			s.flush();
		}		
	}
	
	public static void setReleaseMode() { tmfmt = tmfmt_rel; }
	public static void setTestMode() { tmfmt = tmfmt_tst; }
	public static void error(String msg) {log(0, msg);}
	public static void debug(String msg) {log(1, msg);}
	public static void error(String msg, Object ...args) {log(0, msg, args);}
	public static void debug(String msg, Object ...args) {log(1, msg, args);}
}
