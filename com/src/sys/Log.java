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
	private static void log(int level, int traceOffs, Object ...args) {
		String file = null;
		int line = -1;
		if (tmfmt == tmfmt_tst && traceOffs >= 0) {
			StackTraceElement[] bt = new Throwable().getStackTrace();
			if (bt.length > 2+traceOffs) {
				file = bt[2+traceOffs].getFileName();
				line = bt[2+traceOffs].getLineNumber();
			}
		}
		String fmt = null;
		if (args.length > 0) {
			fmt = args[0] instanceof String ? (String)args[0] : args[0].toString();
			for (int i = 1; i < args.length; ++i) args[i-1]=args[i];
		}
		
		@SuppressWarnings("resource")
		final PrintStream s = level == 0 ? System.err : System.out;
		synchronized (lock) {
			s.printf("%s: ", tmfmt.format(new Date()));
			if (file != null) s.printf("(%s:%d) ", file, line );
			if (fmt != null) s.printf((Locale)null, fmt, args);
			s.println();
			s.flush();
		}		
	}
	
	public static void setReleaseMode() { tmfmt = tmfmt_rel; }
	public static void setTestMode() { tmfmt = tmfmt_tst; }

	public static void error(Object ...args) {log(0, 0, args);}
	public static void warn(Object ...args) {log(0, 0, args);}
	public static void debug(Object ...args) {log(1, 0, args);}
	public static void info(Object ...args) {log(3, -1, args);}

	public static void terror(int traceOffs, Object ...args) {log(0, traceOffs, args);}
	public static void twarn(int traceOffs, Object ...args) {log(0, traceOffs, args);}
	public static void tdebug(int traceOffs, Object ...args) {log(1, traceOffs, args);}
}
