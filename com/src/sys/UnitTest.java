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
import java.util.List;

import time.LapTime;

public class UnitTest {
	final static private String classExt = ".class";
	final static private Object[] empty = null;
	final static private LapTime time=new LapTime("s");
	final static private List<TestSummary> summary = new ArrayList<UnitTest.TestSummary>();

	private static class TestSummary {
		String testunit;
		String testcase;
		int checks;
		int errors;
	}

	static TestSummary current;

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
		BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
		String line = null;
		while ((line = rd.readLine()) != null) {
			if(line.endsWith(classExt) && !line.contains("$")) {
				String unit = pkg + "." + line.substring(0, line.length() - classExt.length());
				if (unit.startsWith(prefix))
				try {
					Class<?> c = cl.loadClass(unit);;
					if (UnitTest.class.isAssignableFrom(c)) {
						a.add(unit);
					}
				} catch (Throwable e) {}
			}
		}
		return a;
	}

	static public void test(String unit) {
		ClassLoader cl = getClassLoader();

		time.reset(0);
		Log.info("* TestUnit: %s start", unit);
		Class<?> c;
		try {
			c = cl.loadClass(unit);
			Method[] mts = c.getDeclaredMethods();
			for (Method m : mts) {
				if (!Modifier.isStatic(m.getModifiers())) {
					continue;
				}
				if ("main".equals(m.getName())) continue;


				current = new TestSummary();
				summary.add(current);
				current.testunit = unit;
				current.testcase = m.getName();

				time.nextLap();
				Log.info("  ** Testcase: %s start", m.getName());
				try {
					// allow access to non public method
					m.setAccessible(true);
					m.invoke(null, empty);
				} catch (Throwable e) {
					current.errors=-1;
					Log.error(1,"Error in %s.%s", unit, m.getName());
					e.printStackTrace();
				} finally {
					time.update(0);
					Log.info("  ** Testcase: %s end in %.3f sec", m.getName(), time.getTime()/1000.0);
					current=null;
				}
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

	static public void testAll(String pkg) {
		try {
			test(getTestUnits(pkg));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public void test(Iterable<String> units) {
		for (String u : units) test(u);
		Log.info("* *********** ");
		Log.info("* Tests: %d", summary.size());
		for (TestSummary s : summary) {
			if (s.errors!=0) Log.error(-1,"%s.%s:  %d / %d", s.testunit, s.testcase, s.checks-s.errors, s.checks);
			else Log.info("%s.%s:  %d / %d", s.testunit, s.testcase, s.checks-s.errors, s.checks);
		}
	}
	static public void test(String[] units) {
		for (String u : units) test(u);
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
	protected static void check(boolean r, String msg) {
		++current.checks;
		if (!r) {
			Log.error(1, "check failed: (false) %s", msg);
			++current.errors;
		}
	}
	protected static void check(String t1, String t2) {
		++current.checks;
		if (!t1.equals(t2)) {
			Log.error(1, "check failed: '%s'!='%s'", t1, t2);
			++current.errors;
		}
	}
	protected static void check(byte[] t1, byte[] t2) {
		++current.checks;
		if (t1.length != t2.length) {
			Log.error(1, "check failed: length %d!=%d", t1.length, t2.length);
			++current.errors;
			return ;
		}
		for (int i =0; i < t1.length; ++i) {
			if (t1[i] != t2[i]) {
				Log.error(1, "check failed: byte[%d] %d!=%d", i, t1[i], t2[i]);
				++current.errors;
				return ;
			}
		}
	}
}
