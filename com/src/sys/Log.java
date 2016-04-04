package sys;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import text.Ansi;

public class Log {
	private static final SimpleDateFormat tmfmt_rel = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final SimpleDateFormat tmfmt_tst = new SimpleDateFormat("HH:mm:ss.SSS");
	private static final String[] LEVEL_ANSI_COLOR = {Ansi.SGR_RED, Ansi.SGR_YELLOW, Ansi.SGR_BLUE, Ansi.SGR_CYAN, "", Ansi.SGR_GREEN, Ansi.SGR_LIGHTMAGENTA };
	private static final String[] LEVEL_NAME = {"E", "W", "D", "T", "I", "N", ""};

	private static SimpleDateFormat tmfmt = tmfmt_tst;
	private static Object lock=new Object();

	private static void log(int level, int traceOffs, String fmt, Object[] args) {
		String file = null;
		int line = -1;
		Thread ct = Thread.currentThread();
		if (tmfmt == tmfmt_tst && traceOffs >= 0) {
			StackTraceElement[] bt = ct.getStackTrace();
			if (bt.length > 3+traceOffs) {
				file = bt[3+traceOffs].getFileName();
				line = bt[3+traceOffs].getLineNumber();
			}
		}
		if (level < 0) level=0;

		final String color = level < LEVEL_ANSI_COLOR.length ? LEVEL_ANSI_COLOR[level] : "";
		final String name = level < LEVEL_NAME.length ? LEVEL_NAME[level] : String.format("%d", level);
		final PrintStream s = System.out;
		synchronized (lock) {
			if (name.isEmpty()) s.printf("%s%s: ", color, tmfmt.format(new Date()));
			else s.printf("%s%s [%s] %s: ", color, tmfmt.format(new Date()), name, ct.getName());
			if (file != null) s.printf("(%s:%d) ", file, line );
			if (fmt != null) s.printf((Locale)null, fmt, args);
			if (!color.isEmpty()) s.printf(Ansi.SGR_RESET);
			s.println();
			s.flush();
		}
	}

	public static void setReleaseMode() { tmfmt = tmfmt_rel; }
	public static void setTestMode() { tmfmt = tmfmt_tst; }

	public static void error(Throwable e) {e.printStackTrace(System.out);}
	public static void error(String fmt,Object ...args) {log(0, 0, fmt, args);}
	public static void warn(String fmt,Object ...args) {log(1, 0, fmt, args);}
	public static void debug(String fmt,Object ...args) {log(2, 0, fmt, args);}
	public static void trace(String fmt,Object ...args) {log(3, 0, fmt, args);}
	public static void info(String fmt,Object ...args) {log(4, -1, fmt, args);}
	public static void notice(String fmt,Object ...args) {log(5, -1, fmt, args);}

	public static void error(int traceOffs,String fmt, Object ...args) {log(0, traceOffs, fmt, args);}
	public static void warn(int traceOffs,String fmt, Object ...args) {log(1, traceOffs, fmt, args);}
	public static void debug(int traceOffs,String fmt, Object ...args) {log(2, traceOffs, fmt, args);}
	public static void trace(int traceOffs,String fmt, Object ...args) {log(3, traceOffs, fmt, args);}
	public static void info(int traceOffs,String fmt, Object ...args) {log(4, traceOffs, fmt, args);}
	public static void notice(int traceOffs,String fmt, Object ...args) {log(5, traceOffs, fmt, args);}
}
