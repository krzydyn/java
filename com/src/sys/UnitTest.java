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

public class UnitTest {
	static private String classExt = ".class";
	static private Object[] empty = null;
	
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

	static public List<String> getTestUnits(String pkg) throws IOException {
		ArrayList<String> a = new ArrayList<String>();
		
		ClassLoader cl = getClassLoader();
		URL url = cl.getResource(pkg.replace(".", "/"));
		BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
		String line = null;
		while ((line = rd.readLine()) != null) {
			if(line.endsWith(classExt) && !line.contains("$")) {
				String unit = pkg + "." + line.substring(0, line.length() - classExt.length());
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
		System.out.println("* Processing unit: " + unit);
		Class<?> c;
		try {
			c = cl.loadClass(unit);
			Method[] mts = c.getDeclaredMethods();
			for (Method m : mts) {
				if (!Modifier.isStatic(m.getModifiers())) {
					continue;
				}
				if ("main".equals(m.getName())) continue;
				
				System.out.println("** Processing testcase: " + m.getName());
				// allow access to non public method
				m.setAccessible(true);
				try {
					m.invoke(null, empty);
				} catch (Throwable e) {
					System.err.println("Error in " + unit + "." + m.getName());
					e.printStackTrace();
				}
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Can't load unit " + unit);
		} catch (Throwable e) {
			System.err.println("Error in unit " + unit);
			e.printStackTrace();
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
	}
	static public void test(String[] units) {
		for (String u : units) test(u);
	}
	protected static void check(String t1, String t2) {
		if (!t1.equals(t2)) {
			Log.error("check failed: '%s'!='%s'", t1, t2);
		}
	}
}
