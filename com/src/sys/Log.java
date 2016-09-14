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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import text.Ansi;

public class Log {
	final private static Object lock=new Object();

	final private static SimpleDateFormat tmfmt_rel = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	final private static SimpleDateFormat tmfmt_tst = new SimpleDateFormat("HH:mm:ss.SSS");
	final private static String[] LEVEL_ANSI_COLOR = {Ansi.SGR_RED, Ansi.SGR_YELLOW, Ansi.SGR_BLUE, Ansi.SGR_CYAN, "", Ansi.SGR_GREEN, Ansi.SGR_LIGHTMAGENTA };
	final private static String[] LEVEL_NAME = {"E", "W", "D", "T", "I", "N", ""};

	private static SimpleDateFormat tmfmt = tmfmt_tst;
	static {
		if (Env.checkApp())
			tmfmt = tmfmt_rel;
		else
			tmfmt = tmfmt_tst;
	}

	final private static void moveLeft(Object[] args, int pa) {
		if (pa>0)
			for (int i=pa; i<args.length; ++i) args[i-pa]=args[i];
	}

	final private static void log(int level, int traceOffs, Object[] args) {
		if (tmfmt == tmfmt_rel && level==2 || level==3) //no debug/trace
			return ;

		String file = null;
		int line = -1;

		Throwable e = null;
		String fmt=null;
		int pa=0;
		if (args.length > pa) {
			if (args[pa] instanceof Integer) {
				traceOffs=(Integer)args[pa++];
			}
		}
		if (args.length > pa) {
			if (args[pa] instanceof Throwable) {
				e=(Throwable)args[pa++];
			}
		}
		if (args.length > pa) {
			if (args[pa] instanceof String) {
				fmt=(String)args[pa++];
			}
		}
		moveLeft(args,pa);


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
		final PrintStream s = System.err;
		synchronized (lock) {
			if (name.isEmpty()) s.printf("%s%s: ", color, tmfmt.format(new Date()));
			else s.printf("%s%s [%s] %s: ", color, tmfmt.format(new Date()), name, ct.getName());
			if (file != null) s.printf("(%s:%d) ", file, line );
			if (fmt != null) s.printf((Locale)null, fmt, args);
			if (e != null) {s.println();e.printStackTrace(s);}
			if (!color.isEmpty()) s.printf(Ansi.SGR_RESET);
			s.println();
		}
	}

	final public static void setReleaseMode() { tmfmt = tmfmt_rel; }
	final public static void setTestMode() { tmfmt = tmfmt_tst; }

	final public static void raw(String fmt,Object ...args) {
		System.err.printf(fmt+"\n", args);
	}
	final public static void error(Object ...args) {log(0, 0, args);}
	final public static void warn(Object ...args) {log(1, 0, args);}
	final public static void debug(Object ...args) {log(2, 0, args);}
	final public static void trace(Object ...args) {log(3, 0, args);}
	final public static void info(Object ...args) {log(4, -1, args);}
	final public static void notice(Object ...args) {log(5, -1, args);}
}
