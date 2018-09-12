package generator;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GEnFromXml {
	static String repoPath = "/home/k.dynowski/Secos/Trustware/";
	static String section = "API_Data_Storage";

	static String TEEC_Result = "TEEC_Result";
	static String TEEC_Context = "TEEC_Context";
	static String TEEC_Session = "TEEC_Session";

	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder documentBuilder = null;

	static Set<String> tcPrefix = new TreeSet<>();
	static {
		if (section.equals("API_Crypto_InvokeCipher")) {
			tcPrefix.add("Invoke_Crypto_Cipher");
			tcPrefix.add("Invoke_Crypto_Copy");
			tcPrefix.add("Invoke_Crypto_DeriveKey");
			tcPrefix.add("Invoke_Crypto_Diges");
			tcPrefix.add("Invoke_Crypto_GenerateRandom");
			tcPrefix.add("Invoke_Crypto_GetOperationInfo");
			tcPrefix.add("Invoke_Crypto_MAC");
		}
		else if (section.equals("API_Data_Storage")) {
			tcPrefix.add("Invoke_AllocateTransientChain");
			tcPrefix.add("Invoke_CopyObjectAttributes");
			tcPrefix.add("Invoke_CreatePersistentObjec");
			tcPrefix.add("Invoke_EnumeratorOn");
			tcPrefix.add("Invoke_Free");
			tcPrefix.add("Invoke_GenerateKey");
			tcPrefix.add("Invoke_Read");
			tcPrefix.add("Invoke_Rename");
			tcPrefix.add("Invoke_Reset");
			tcPrefix.add("Invoke_Seek");
			tcPrefix.add("Invoke_Start");
			tcPrefix.add("Invoke_Truncate");
			tcPrefix.add("Invoke_Write");
		}
	}


	static Map<String, String> opNameMap = new HashMap<>();
	static {
		opNameMap.put("SetUp_TEE", "");
		opNameMap.put("SelectApp", "");
		opNameMap.put("CloseSession", "TEEC_CloseSession");
		opNameMap.put("FinalizeContext", "TEEC_FinalizeContext");
		opNameMap.put("TearDown_TEE", "");

		opNameMap.put("Invoke_Crypto_GenerateRandom", "intern_Invoke_Crypto_GenerateRandom");
		opNameMap.put("Check_ObjectInfo", "intern_Check_ObjectInfo");
	}


	static Set<String> opAddContext = new TreeSet<>();
	static {
		opAddContext.add("Invoke_Crypto_InitObjectWithKeys");
		opAddContext.add("Invoke_Crypto_InitObjectWithKeysExt");
		opAddContext.add("Macro_StoreRefAttribute");
		opAddContext.add("Invoke_Crypto_GetOperationInfoMultiple");
		opAddContext.add("Invoke_GetObjectBufferAttribute");
		opAddContext.add("Invoke_Crypto_GenerateRandom");

		opAddContext.add("Invoke_Crypto_AEInit");
		opAddContext.add("Invoke_Crypto_AEUpdateAAD");
		opAddContext.add("Invoke_Crypto_AEUpdate_for_encryption");
		opAddContext.add("Invoke_Crypto_AEEncryptFinal");
		opAddContext.add("Invoke_Crypto_AEDecryptFinal");

		opAddContext.add("Invoke_Crypto_CipherInit");
		opAddContext.add("Invoke_Crypto_CipherUpdate");
		opAddContext.add("Invoke_Crypto_CipherDoFinal");

		opAddContext.add("Invoke_Crypto_DigestInit");
		opAddContext.add("Invoke_Crypto_DigestUpdate");
		opAddContext.add("Invoke_Crypto_DigestDoFinal");

		opAddContext.add("Invoke_Crypto_MACInit");
		opAddContext.add("Invoke_Crypto_MACUpdate");
		opAddContext.add("Invoke_Crypto_MACCompareFinal");
		opAddContext.add("Invoke_Crypto_MACComputeFinal");

		opAddContext.add("Invoke_CreatePersistentObject");
		opAddContext.add("Invoke_OpenPersistentObject");
		opAddContext.add("Invoke_StoreBuffer");
	}

	static ArgInfo uint8buf_4k = new ArgInfo("4096", "uint8_t", "operationInfoOutput");
	static ArgInfo uint8buf_32 = new ArgInfo("32", "uint8_t", "operationInfoOutput");

	static Map<String,ArgInfo> opAppendArgs = new HashMap<>();
	static {
		opAppendArgs.put("Invoke_Crypto_GetOperationInfo", uint8buf_32);
		opAppendArgs.put("Invoke_Crypto_GetOperationInfoMultiple", uint8buf_4k);
		opAppendArgs.put("Check_OperationInfo", uint8buf_32);
		opAppendArgs.put("Check_0_OperationInfoMultiple", uint8buf_4k);
		opAppendArgs.put("Check_1_OperationInfoKey", uint8buf_4k);
	}

	static class ArgInfo {
		String parameter;
		String type;
		String value;
		public ArgInfo(){}
		public ArgInfo(String p, String t, String v){
			parameter = p;
			type = t;
			value = v;
		}

		@Override
		public String toString() {
			if (type.equals("ALL_TEEC_UUID")) return "&"+value;
			if (type.equals("ALL_RETURN_ORIGINS")) {
				if (value.equals("NULL")) return "ORIGIN_NULL";
			}
			if (type.equals("ALL_TEE_OBJECT_HANDLES")) {
				if (value.equals("NULL")) return "0";
			}
			if (type.equals("ALL_TEE_RESULTS")) {
				if (value.startsWith("TEE_"))
					return "TEEC" + value.substring(3);
			}
			if (type.equals("HandleFlags") && parameter.contains("instance")) {
				return "&"+value;
			}
			if (type.equals("DataFlags") && parameter.contains("instance")) {
				return "&"+value;
			}
			if (type.equals("AttributeList")) {
				return "&"+value;
			}
			return value;
		}
	}
	static class OperationInfo {
		String name;
		List<ArgInfo> args;
		private boolean containsExpectedReturn() {
			for (ArgInfo a : args) {
				if (a.type.equals("ALL_TEE_RESULTS") || a.type.equals("ALL_RETURN_CODES")) return true;
			}
			return false;
		}
		public void implement(PrintStream pr) {
			String mapname = opNameMap.get(name);
			if (mapname == null) mapname = name;
			else if (mapname.isEmpty()) return ;
			if (containsExpectedReturn()) {
				String sep = ", ";
				if (args.size() > 7) sep = ",\n\t\t\t\t";
				pr.printf("\tres = %s(%s);\n", mapname, Text.join(sep, args));
				pr.printf("\tif (res != TEEC_SUCCESS) goto out;\n\n");
			}
			else
				pr.printf("\t%s(%s);\n", mapname, Text.join(", ", args));
		}
	}
	static class StepInfo {
		List<OperationInfo> ops;
		public void implement(PrintStream pr) {
			for (OperationInfo op : ops)
				op.implement(pr);
		}
	}
	static class TestCaseInfo {
		String name;
		String id;
		List<String> initVar = new ArrayList<>();
		List<StepInfo> steps;
		public void addInitVar(String init) {
			if (!initVar.contains(init))
				initVar.add(init);
		}
		public void implement(PrintStream pr) {
			pr.printf("//Test: %s %s\n", name, id);
			pr.printf("%s %s()\n", TEEC_Result, name);
			pr.printf("{\n");
			pr.printf("\t%s context;\n", TEEC_Context);
			pr.printf("\t%s session;\n", TEEC_Session);
			pr.printf("\t%s res;\n", TEEC_Result);
			pr.println();

			if (!initVar.isEmpty()) {
				for (String init : initVar) {
					pr.printf("\t%s;\n", init);
				}
				pr.println();
			}

			for (StepInfo si : steps) {
				si.implement(pr);
			}

			pr.printf("out:\n");
			pr.printf("\treturn res;\n}\n");
		}
		public void tableEntry(PrintStream pr) {
			pr.println("\t{");
			pr.printf("\t\ttest_%s,\n", name);
			pr.printf("\t\t\"%s\",\n", name);
			pr.printf("\t\t%s,\n", name);
			pr.printf("\t\tfalse\n");
			pr.println("\t},");
		}
	}


	private static boolean filterPrefix(TestCaseInfo tc) {
		for (String p : tcPrefix) {
			if (tc.name.startsWith(p)) return true;
		}
		return false;
	}

	private static ArgInfo parseArgument(Node n) {
		ArgInfo arg = new ArgInfo();
		NodeList attr = n.getChildNodes();
		for (int i = 0; i < attr.getLength(); ++i) {
			Node a = attr.item(i);
			if (a.getNodeType() != Node.ELEMENT_NODE) continue;
			String tag = a.getNodeName();
			if (tag.equals("parameter"))
				arg.parameter = a.getAttributes().getNamedItem("name").getTextContent();
			else if (tag.equals("type"))
				arg.type = a.getAttributes().getNamedItem("name").getTextContent();
			else if (tag.equals("value"))
				arg.value = a.getAttributes().getNamedItem("name").getTextContent();
		}
		return arg;
	}
	private static OperationInfo parseOperation(TestCaseInfo tc, Node n) {
		Document d = documentBuilder.newDocument();
		d.appendChild(d.importNode(n, true));
		NodeList args = d.getElementsByTagName("argument");

		OperationInfo op = new OperationInfo();
		op.name = n.getAttributes().getNamedItem("name").getTextContent();
		Log.info("    operation %s", op.name);
		op.args = new ArrayList<>(args.getLength());
		if (opAddContext.contains(op.name)) {
			ArgInfo arg = new ArgInfo();
			arg.parameter = "IN_context";
			arg.type = "ALL_CONTEXTS";
			arg.value = "&context";
			op.args.add(arg);
		}
		for (int i = 0; i < args.getLength(); ++i) {
			Node a = args.item(i);
			ArgInfo arg = parseArgument(a);
			if (arg == null) break;
			if (arg.type.equals("ALL_COMMAND_ID_INTERNAL_API")) continue;
			if (arg.type.equals("AttributeList")) {
				if (!arg.value.contains("Empty"))
					tc.addInitVar(String.format("AttributeList %s={0,}",arg.value));
			}
			else if (arg.type.equals("HandleFlags")) {
				if (!arg.value.contains("None"))
					tc.addInitVar(String.format("uint32_t %s=0",arg.value));
			}
			else if (arg.type.equals("DataFlags")) {
				//if (!arg.value.contains("None"))
					tc.addInitVar(String.format("uint32_t %s=0",arg.value));
			}
			op.args.add(arg);
		}
		if (opAppendArgs.containsKey(op.name)) {
			ArgInfo a = opAppendArgs.get(op.name);
			tc.addInitVar(String.format("%s %s[%s]", a.type, a.value, a.parameter));
			op.args.add(a);
		}
		return op;
	}

	private static StepInfo parseStep(TestCaseInfo tc, Node n) {
		Document d = documentBuilder.newDocument();
		d.appendChild(d.importNode(n, true));
		NodeList ops = d.getElementsByTagName("operation");

		if (ops.getLength() == 0) return null;

		StepInfo si = new StepInfo();
		si.ops = new ArrayList<>(ops.getLength());
		for (int i = 0; i < ops.getLength(); ++i) {
			Node o = ops.item(i);
			if (o == null) break;
			si.ops.add(parseOperation(tc, o));

		}
		return si;
	}

	private static TestCaseInfo parseTestCase(Node n) {
		Document d = documentBuilder.newDocument();
		d.appendChild(d.importNode(n, true));
		NodeList steps = d.getElementsByTagName("call");
		Log.info("steps %d", steps.getLength());

		TestCaseInfo tc = new TestCaseInfo();
		String nm = n.getAttributes().getNamedItem("name").getTextContent();
		int idx = nm.indexOf(' ');
		if (idx == -1) return null;
		tc.name = nm.substring(0, idx);
		tc.id = nm.substring(idx + 1);
		Log.info("node: %s", tc.name);

		tc.steps = new ArrayList<>(steps.getLength());
		for (int i = 0; i < steps.getLength(); ++i) {
			Node s = steps.item(i);
			Log.info("  step[%d]: %s", i, s.getAttributes().getNamedItem("stepNumber"));
			StepInfo si = parseStep(tc, s);
			if (si == null) break;
			tc.steps.add(si);
		}
		return tc;
	}

	private static List<TestCaseInfo> genFrom(String fn) throws Exception {
		File f = new File(fn);
		if (!f.exists()) return null;

		Document document = documentBuilder.parse(f);
		NodeList tclist = document.getElementsByTagName("scenario");
		Log.info("tclist %d", tclist.getLength());

		List<TestCaseInfo> tcs = new ArrayList<>();
		for (int i = 0; i < tclist.getLength(); ++i) {
			Node n = tclist.item(i);
			TestCaseInfo tc = parseTestCase(n);
			if (tc == null) break;
			if (filterPrefix(tc))
				tcs.add(tc);
		}
		Log.info("tcs %d", tcs.size());
		return tcs;
	}

	private static void implement(List<TestCaseInfo> tcs) {
		String fn = String.format("%s/trustzone-application/test_usability/ca_tests/gp_suite/tta_test_%s_auto.c", repoPath, section);
		//PrintStream pr = System.out;
		PrintStream pr;
		try {
			pr = new PrintStream(fn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		pr.println("#include \"tf_gp_suite.h\"");
		//pr.println("#include \"include/tta_test_API_Crypto_Internal.h\"");
		pr.println("#include \"include/tta_test_API_Data_Storage.h\"");
		pr.println("#include \"include/tta_test_API_Data_Storage_Internal.h\"");
		pr.printf("#include \"include/tta_test_%s.h\"\n", section);
		pr.println();
		pr.println("#define CONTEXT01 (&context)");
		pr.println("#define SESSION01 (&session)");
		pr.println("#define CONTEXT02 (&context)");
		pr.println("#define SESSION02 (&session)");
		pr.println("#define false 0");

		pr.println("#define iHandleFlagsNone 0");
		pr.println("#define iObjectUsageUnknown 0");
		pr.println("static AttributeList iAttributeListEmpty={0,};");

		for (TestCaseInfo tc : tcs) {
			tc.implement(pr);
		}

		pr.println();
		pr.printf("testStruct tests_%s[] = {\n", section);
		for (TestCaseInfo tc : tcs) {
			tc.tableEntry(pr);
		}
		pr.println("\t{test_MaxNumTests,\"NONE\",NULL,false},");
		pr.println("};");
	}

	public static void main(String[] args) {
		try {
			//documentBuilderFactory.setNamespaceAware(true);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		}
		catch (Exception e) {
			Log.error(e);
			return ;
		}
		for (String fn : args) {
			try {
				List<TestCaseInfo> tcs = genFrom(fn);
				implement(tcs);
			} catch (Exception e) {
				Log.error(e);
			}
		}
	}

	static class Text {

		public static Object join(String sep, List<?> args) {
			if (args.isEmpty()) return "";
			StringBuilder b=new StringBuilder();
			b.append(args.get(0).toString());
			for (int i = 1; i < args.size(); ++i) {
				b.append(sep);
				b.append(args.get(0).toString());
			}
			return b.toString();
		}

	}
	static class Log {
		public static void error(Exception e) {
			e.printStackTrace(System.err);
		}
		public static void info(String format, Object... args) {
			System.err.printf(format, args);
			System.err.println();
		}

	}
}
