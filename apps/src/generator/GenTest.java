package generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sys.Env;
import sys.Log;

public class GenTest {
	final static String SECTION = "TTA_crypto";
	final static String TEST_PREFIX = "test_";
	final static String GP_PREFIX = "gp_";

	public static void main(String[] args) {
		for (String s : args) {
			try {
				generateFrom(Env.expandEnv(s));
			} catch (Exception e) {
				Log.error(e);
			}
		}

	}

	private static void generateFrom(String file) throws IOException {
		List<String> list = new ArrayList<>(50);

		BufferedReader bs = new BufferedReader(new FileReader(file));
		try {
			String ln;
			while ((ln = bs.readLine()) != null) {
				ln = ln.trim().replace(",", "");
				if (ln.isEmpty()) {
					list.add("");
					continue;
				}
				if (ln.indexOf(" (") < 0) continue;
				list.add(ln);
			}
		} finally {
			Env.close(bs);
		}
		generateEnums(list);
		generateDummyImpl(list);
		generateTestTable(list);
		generateService(list);
	}

	private static String getFunc(String ln) {
		return ln.substring(0, ln.indexOf(" ("));
	}
	private static String getCode(String ln) {
		return ln.substring(ln.indexOf(" (")+1);
	}
	static void generateEnums(List<String> list) {
		System.out.printf("typedef enum\n{\n");
		for (String ln : list) {
			if (ln.isEmpty()) {
				System.out.println();
				continue;
			}
			String func = getFunc(ln);
			String enu = TEST_PREFIX + func;
			System.out.printf("\t%s,\n", enu);
		}
		System.out.printf("\ttest_MaxNumTests\n");
		System.out.printf("} testIDType;\n");
	}
	static void generateDummyImpl(List<String> list) {
		for (String ln : list) {
			if (ln.isEmpty()) {
				System.out.println();
				continue;
			}
			String func = getFunc(ln);
			String code = getCode(ln);
			System.out.printf("// Test: %s %s\n", func, code);
			System.out.printf("static TEEC_Result %s()\n{\n", func);
			System.out.printf("\treturn TEEC_ERROR_GENERIC;\n");
			System.out.printf("}\n");
		}

	}
	static void generateTestTable(List<String> list) {
		System.out.printf("testStruct tests_%s [] =\n{\n", SECTION);
		for (String ln : list) {
			if (ln.isEmpty()) {
				System.out.println();
				continue;
			}
			String func = getFunc(ln);
			String enu = TEST_PREFIX + func;
			System.out.printf("\t{\n");
			System.out.printf("\t\t%s,\n", enu);
			System.out.printf("\t\t\"%s\",\n", func);
			System.out.printf("\t\t%s,\n", func);
			System.out.printf("\t\tfalse\n");
			System.out.printf("\t},\n");
		}

		System.out.printf("\t{\n"
			+ "\t\ttest_MaxNumTests,\n"
			+ "\t\t\"NONE\",\n"
			+ "\t\tNULL,\n"
			+ "\t\tfalse\n"
			+ "\t},\n");
		System.out.printf("};\n");
	}
	static void generateService(List<String> list) {
		for (String ln : list) {
			if (ln.isEmpty()) {
				System.out.println();
				continue;
			}
			String func = getFunc(ln);
			String code = getCode(ln);
			String enu = TEST_PREFIX + func;
			String desc = func.replace("_", " ");
			System.out.printf("ADD_TEST_GP_SUITE(%s, \"%s\", \"%s%s\", %s);\n",
				GP_PREFIX+func, SECTION, desc, code, enu);
		}
		System.out.println();
		System.out.println("static struct test_case *tests_table[] = {");
		for (String ln : list) {
			if (ln.isEmpty()) {
				System.out.println();
				continue;
			}
			String func = getFunc(ln);
			System.out.printf("\t&%s,\n", TEST_PREFIX+GP_PREFIX+func);
		}
		System.out.println("\tNULL");
		System.out.println("};\n\nINITIALIZE_TEST_MODULE(tests_table);");
	}

}
