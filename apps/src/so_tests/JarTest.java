package so_tests;

import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import sys.Env;

public class JarTest {
	public static void main(String[] args) throws IOException {
		JarFile f=new JarFile(Env.expandEnv("~/work/java/lib/jsoup-1.8.2.jar"));
		
		Manifest m = f.getManifest();
		printAttributes("Main",m.getMainAttributes());
		
		Map<String,Attributes> map=m.getEntries();
		for (String k : map.keySet()) {
			printAttributes(k, map.get(k));
		}
		f.close();
	}

	private static void printAttributes(String section, Attributes a) {
		System.out.printf("Section %s\n",section);
		for (Object k : a.keySet()) {
			System.out.printf("attr[%s] = %s\n", k, a.get(k));
		}
		System.out.println();
	}
	
}
