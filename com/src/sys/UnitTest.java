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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import text.Text;
import time.LapTime;

public class UnitTest {
	final static private String classExt = ".class";
	final static private Object[] empty = null;
	final static private LapTime time=new LapTime("s");
	final static private List<TestSummary> summary = new ArrayList<UnitTest.TestSummary>();

	private static class TestSummary {
		String testunit;
		String testcase;
		long elapsed;
		int checks;
		int errors;
	}

	static TestSummary current = new TestSummary();

	static ClassLoader getClassLoader() {
		//return ClassLoader.getSystemClassLoader();
		return Thread.currentThread().getContextClassLoader();
	}

	static public List<String> getClasses(String pkg) throws IOException {
		ArrayList<String> a = new ArrayList<String>();

		ClassLoader cl = getClassLoader();
		URL url = cl.getResource(pkg.replace(".", "/"));
		BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
		String line = null;
		while ((line = rd.readLine()) != null) {
			if(line.endsWith(classExt) && !line.contains("$")) {
				String unit = pkg + "." + line.substring(0, line.length() - classExt.length());
				a.add(unit);
			}
		}
		return a;
	}

	static public List<String> getTestUnits(String prefix) throws IOException {
		ArrayList<String> a = new ArrayList<String>();

		String pkg = "";
		if (prefix.lastIndexOf('.')>0) pkg=prefix.substring(0, prefix.lastIndexOf('.'));
		ClassLoader cl = getClassLoader();
		URL url = cl.getResource(pkg.replace(".", "/"));
		if (url == null) {
			prefix = pkg;
			pkg=prefix.substring(0, pkg.lastIndexOf('.'));
			url = cl.getResource(pkg.replace(".", "/"));
		}
		Log.debug("reading from: %s", url.getPath());
		BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
		String line = null;
		while ((line = rd.readLine()) != null) {
			if(line.endsWith(classExt) && !line.contains("$")) {
				String unit = pkg + "." + line.substring(0, line.length() - classExt.length());
				Log.debug("adding test unit: %s", unit);
				if (unit.startsWith(prefix))
				try {
					Class<?> c = cl.loadClass(unit);;
					if (UnitTest.class.isAssignableFrom(c)) {
						a.add(unit);
					}
				} catch (Throwable e) {}
			}
		}
		Collections.sort(a);
		return a;
	}

	static public void test(String prefix, String unit) {
		ClassLoader cl = getClassLoader();

		Class<?> c;
		try {
			c = cl.loadClass(unit);
			Method[] mts = c.getDeclaredMethods();
			Arrays.sort(mts, new Comparator<Method>() {
				@Override
				public int compare(Method o1, Method o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			Log.info("* TestUnit: %s start", unit);
			time.reset(0);
			for (Method m : mts) {
				if (!Modifier.isStatic(m.getModifiers()) || Modifier.isPrivate(m.getModifiers())) {
					continue;
				}
				if ("main".equals(m.getName()) || m.getName().contains("$")) continue;
				if (m.getName().startsWith("no_") || m.getName().startsWith("_")) continue;
				if (!(unit + "." + m.getName()).startsWith(prefix)) continue;

				try {
					m.setAccessible(true); // allow access to non public method
				} catch (Throwable e) {
					Log.error(e,"method not accessible in %s.%s", unit, m.getName());
					continue;
				}

				current = new TestSummary();
				summary.add(current);
				current.testunit = unit;
				current.testcase = m.getName();
				current.elapsed = -1;

				time.update(0);
				Log.info("  ** Testcase: %s start", m.getName());
				try {
					++current.checks;
					m.invoke(null, empty);
				} catch (Throwable e) {
					++current.errors;
					if (e.getCause() != null) e=e.getCause();
					Log.error(-1,e,"Exception in %s.%s", unit, m.getName());
				} finally {
					time.nextLap();
					current.elapsed = time.getTime();
					Log.info("  ** Testcase: %s end in %.3f sec", m.getName(), current.elapsed/1000.0);
					current=null;
				}
				System.gc();
			}
		} catch (ClassNotFoundException e) {
			Log.error(1,"Can't load unit %s", unit);
		} catch (Throwable e) {
			Log.error(1,"Exception in unit %s", unit);
			e.printStackTrace();
		} finally {
			Log.info("* TestUnit: %s end in %.3f sec", unit, time.getTotalTime()/1000.0);
		}
	}

	static public void testAll(String prefix) {
		try {
			test(prefix, getTestUnits(prefix));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public void test(String prefix, Iterable<String> units) {
		for (String u : units) test(prefix,u);
		Log.notice("* ===================================== *");
		Log.notice("           Tests summary: %d", summary.size());
		Log.notice("* ===================================== *");
		for (TestSummary s : summary) {
			if (s.errors!=0) Log.error(-1, "%s.%s:  %d / %d    %.3f",s.testunit,s.testcase,s.checks-s.errors,s.checks,s.elapsed/1000.0);
			else Log.notice("%s.%s:  %d / %d    %.3f",s.testunit,s.testcase,s.checks-s.errors,s.checks,s.elapsed/1000.0);
		}
	}
	static public void test(String prefix, String[] units) {
		for (String u : units) test(prefix, u);
	}

	static public interface RunThrowable {
		public void run() throws Throwable;
	}

	protected static void checkNoThrow(RunThrowable r) {
		++current.checks;
		try {
			r.run();
		} catch (Throwable e) {
			Log.error(e);
			++current.errors;
		}
	}
	protected static void checkThrow(RunThrowable r, Class<? extends Throwable> c) {
		++current.checks;
		try {
			r.run();
			++current.errors;
			Log.error("Exception expected");
		} catch (Throwable e) {
			if (!e.getClass().isAssignableFrom(c)) {
				Log.error(e);
				++current.errors;
			}
		}
	}
	protected static void check(String msg, boolean r) {
		++current.checks;
		if (!r) {
			Log.error(1, "check failed: %s", msg);
			++current.errors;
		}
	}
	protected static void check(String msg, int e, int r) {
		++current.checks;
		if (e != r) {
			Log.error(1, "check failed: %s %d != %d", msg, r, e);
			++current.errors;
		}
	}
	protected static void check(String msg, long e, long r) {
		++current.checks;
		if (e != r) {
			Log.error(1, "check failed: %s %d != %d", msg, r, e);
			++current.errors;
		}
	}
	protected static void check(String t1, String t2) {
		++current.checks;
		if (!t1.equals(t2)) {
			Log.error(1,"check failed: '%s'!='%s'", t1, t2);
			++current.errors;
		}
	}
	protected static void check(byte[] t1, byte[] t2) {
		check_prv(t1, t2, 0);
	}
	protected static void check(byte[] t1, byte[] t2, int n) {
		check_prv(t1, t2, n);
	}
	private static void check_prv(byte[] t1, byte[] t2, int n) {
		++current.checks;
		if (n > 0) {
			if (t1.length < n || t2.length < n) {
				Log.error(2, "check failed: length %d!=%d", t1.length, t2.length);
				++current.errors;
				return ;
			}
		}
		else {
			if (t1.length != t2.length) {
				Log.error(2,"check failed: length %d!=%d  (%s != %s)", t1.length, t2.length, Text.hex(t1), Text.hex(t2));
				++current.errors;
				return ;
			}
			n=t1.length;
		}
		for (int i=0; i < n; ++i) {
			if (t1[i] != t2[i]) {
				Log.error(2, "check failed: byte[%d] #%x!=#%x", i, t1[i]&0xff, t2[i]&0xff);
				++current.errors;
				return ;
			}
		}
	}
	protected static void check(int[] t1, int[] t2, int n) {
		++current.checks;
		if (n > 0) {
			if (t1.length < n || t2.length < n) {
				Log.error(1, "check failed: length %d!=%d", t1.length, t2.length);
				++current.errors;
				return ;
			}
		}
		else {
			if (t1.length != t2.length) {
				Log.error(1, "check failed: length %d!=%d", t1.length, t2.length);
				++current.errors;
				return ;
			}
			n=t1.length;
		}

		for (int i=0; i < n; ++i) {
			if (t1[i] != t2[i]) {
				Log.error(1, "check failed: int[%d] %d!=%d", i, t1[i], t2[i]);
				Log.error(1, Text.join(",", t1) + " <> " + Text.join(",", t2));
				++current.errors;
				return ;
			}
		}
	}
}
