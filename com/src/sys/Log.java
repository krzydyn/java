/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package sys;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import text.Ansi;

public abstract class Log {
	final private static SimpleDateFormat tmfmt_rel = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	final private static SimpleDateFormat tmfmt_tst = new SimpleDateFormat("HH:mm:ss.SSS");
	final private static String[] LEVEL_COLOR = {Ansi.SGR_RED, Ansi.SGR_YELLOW, Ansi.SGR_BLUE, Ansi.SGR_CYAN, "", Ansi.SGR_GREEN, Ansi.SGR_LIGHTMAGENTA };
	final private static String[] LEVEL_NAME = {"E", "W", "D", "T", "I", "N", ""};
	public static enum Level {
		FINEST
	}

	private static SimpleDateFormat tmfmt = tmfmt_tst;
	private static PrintStream prs = System.err;
	static {
		if (Env.isAppJar(Env.class))
			tmfmt = tmfmt_rel;
		else
			tmfmt = tmfmt_tst;
		try {
			prs = new PrintStream(new FileOutputStream("syslog.log"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	final private static void moveLeft(Object[] args, int pa) {
		if (pa>0) {
			for (int i=pa; i<args.length; ++i) args[i-pa]=args[i];
			for (int i = args.length-pa; i < args.length; ++i) args[i] = null;
		}
	}

	final private static void log(int level, int traceOffs, Object[] args) {
		if (tmfmt == tmfmt_rel && level==2 || level==3) //no debug/trace
			return ;

		Date tmstamp = new Date();
		String file = null;
		int line = -1;

		Throwable exc = null;
		String fmt=null;
		int pa=0;
		if (args.length > pa) {
			if (args[pa] instanceof Integer) traceOffs=(Integer)args[pa++];
		}
		if (args.length > pa) {
			if (args[pa] instanceof Throwable) exc = (Throwable)args[pa++];
		}
		if (args.length > pa) {
			if (args[pa] instanceof String) fmt = (String)args[pa++];
		}
		moveLeft(args,pa);

		Thread ct = Thread.currentThread();
		if ((level<=0 || tmfmt == tmfmt_tst) && traceOffs >= 0) {
			StackTraceElement[] bt = ct.getStackTrace();
			if (bt.length > 3+traceOffs) {
				if (bt[3+traceOffs].getLineNumber()==1) ++traceOffs;
				//String cl = bt[3+traceOffs].getClassName();
				//file = cl.replace('.',  '/') + ".java";
				file = bt[3+traceOffs].getFileName();
				line = bt[3+traceOffs].getLineNumber();
			}
		}
		if (level < 0) level=0;

		final String color = level < LEVEL_COLOR.length ? LEVEL_COLOR[level] : "";
		final String name = level < LEVEL_NAME.length ? LEVEL_NAME[level] : String.format("%d", level);
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		PrintStream pr = new PrintStream(bas);
		if (name.isEmpty())pr.printf("%s%s", color, tmfmt.format(tmstamp));
		else pr.printf("%s%s [%s] %s", color, tmfmt.format(tmstamp), name, ct.getName());
		if (file != null) pr.printf(" (%s:%d)", file, line );
		pr.print(": ");
		if (fmt != null) pr.printf((Locale)null, fmt, args);
		if (exc != null) {pr.println(); exc.printStackTrace(pr);}
		if (!color.isEmpty()) pr.printf(Ansi.SGR_RESET);
		pr.println();
		pr.close(); // flush data to underlying stream

		//PrintStream is synchronized
		try {
			if (prs != System.err)
				System.err.write(bas.toByteArray());
			prs.write(bas.toByteArray());
			prs.flush();
		}catch (Throwable e){}
	}

	final public static void setReleaseMode() { tmfmt = tmfmt_rel; }
	final public static void setTestMode() { tmfmt = tmfmt_tst; }
	final public static boolean isRelease() { return tmfmt == tmfmt_rel; }

	final public static void prn(String fmt,Object ...args) {
		if (tmfmt == tmfmt_rel) return ;
		prs.printf(fmt, args);
		prs.println();
	}
	final public static void error(Object ...args) {log(0, 0, args);}
	final public static void warn(Object ...args) {log(1, 0, args);}
	final public static void debug(Object ...args) {log(2, 0, args);}
	final public static void trace(Object ...args) {log(3, 0, args);}
	final public static void info(Object ...args) {log(4, -1, args);}
	final public static void notice(Object ...args) {log(5, -1, args);}

	final public static boolean isLoggable(Level finest) {
		return false;
	}

	final public static void finest(Object ...args) {
	}
}
