package generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import sys.Env;

public class GenTestFormat {

	static final String RESULTS_PATH = Env.expandEnv("~/Secos/Test-Results");

	class TestResult {

	}

	public static void main(String[] args) {
		String fn = RESULTS_PATH + "/test-results_devel_r.tyminski_TIZEN5.0_2019_master";
		try {
			processFile(fn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processFile(String fn) throws Exception {
		PrintStream pr;
		try {
			pr = new PrintStream(fn+"-gen.csv");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		processFileTF(fn+"-TF.csv", pr);
		processFileASLR(fn+"-ASLR.csv", pr);
		processFileCATA(fn+"-CATA.csv", pr);
		pr.close();
	}

	private static void processFileCATA(String fn, PrintStream pr) throws Exception {
		BufferedReader rd = new BufferedReader(new FileReader(fn));
		String precon = "load tzdev; exec tzdaemon";
		String expected = "TEST SUCCEED";
		String tstep = "./test_ca_ta_paring";
		String ln;
		int n=1;
		while ((ln = rd.readLine()) != null) {
			String[] fld = ln.split(",");
			if (fld.length != 4) continue;
			String tgrp = "CATAPAIR", descr = fld[1]+" "+fld[2], run = fld[3].trim();
			pr.printf("%s_%02d,,%s,%s,%s,,%s,%s\n", tgrp.toUpperCase(), n, descr, precon, tstep, expected, run);
			++n;
		}
		rd.close();
	}

	private static void processFileASLR(String fn, PrintStream pr) throws Exception {
		BufferedReader rd = new BufferedReader(new FileReader(fn));
		String precon = "";
		String expected = "ASLR=1;SPP=1";
		String tstep = "./exe-checker";
		String ln;
		int n=1;
		while ((ln = rd.readLine()) != null) {
			String[] fld = ln.split(",");
			if (fld.length != 8) continue;
			String tgrp = "ASLR_CANARY", descr = fld[3], run="ASLR="+fld[5]+";SPP="+fld[7];
			File f = new File(descr);
			descr = "<path_to_check>/" + f.getName();
			pr.printf("%s_%02d,,%s,%s,%s,,%s,%s\n", tgrp.toUpperCase(), n, descr, precon, tstep, expected, run);
			++n;
		}
		rd.close();
	}

	private static void processFileTF(String fn, PrintStream pr) throws Exception {
		BufferedReader rd = new BufferedReader(new FileReader(fn));
		String precon = "load tzdev; exec tzdaemon";
		String expected = "TEST_PASS";
		String ln;
		while ((ln = rd.readLine()) != null) {
			String[] fld = ln.split(",");
			if (fld.length != 6) continue;
			if (fld[0].startsWith("TEST_")) continue;
			String tgrp=fld[0].trim(), num=fld[1].trim(), descr=fld[2].trim(),
					prep=fld[3].trim(), run=fld[4].trim(), fin=fld[5].trim();
			int n = Integer.parseInt(num);
			String tstep = "./tf " + tgrp + "/" +num;
			pr.printf("%s_%02d,,%s,%s,%s,,%s,%s\n", tgrp.toUpperCase(), n, descr, precon, tstep, expected, run);
		}
		rd.close();
	}
}
