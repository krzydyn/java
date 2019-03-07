package generator;

import java.io.File;
import java.io.FileFilter;
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

/*
 * Client API:
 * /home/k.dynowski/Secos/TEE-Initial-Configuration/TEE_Initial_Configuration-Test_Suite_v2_0_0_2-2017_06_09/packages/ClientAPI/xmlstable/TEE.xml
 *
 * Crypto API:
 * /home/k.dynowski/Secos/TEE-Initial-Configuration/TEE_Initial_Configuration-Test_Suite_v2_0_0_2-2017_06_09/packages/Crypto/xmlstable/TEE_Crypto_API.xml
 *
 * StorageData API:
 * /home/k.dynowski/Secos/TEE-Initial-Configuration/TEE_Initial_Configuration-Test_Suite_v2_0_0_2-2017_06_09/packages/DataStorage/xmlstable/TEE_DataStorage_API.xml
 */

public class GenFromXml {
	static String repoPath = "/home/k.dynowski/Secos/Trustware";
	static String xmlBasePath = repoPath + "/trustzone-application/test_suite_v2.0.0";
	static String xmlPackagesPath = xmlBasePath + "/packages";
	static String xmlValuesPath = xmlBasePath + "/Values";

	static String repoTestCodePath = repoPath + "/trustzone-application/test_usability";
	static String repoGPSuitePath = repoTestCodePath + "/ca/gp_suite";

	static String boilerPlate = "/*\n" +
					" *\n" +
					" * This source file is proprietary property of Samsung Electronics Co., Ltd.\n" +
					" *\n" +
					" * Copyright (C) 2018 Samsung Electronics Co., Ltd All Rights Reserved\n" +
					" *\n" +
					" * Contact: Krzysztof Dynowski <k.dynowski@samsung.com>\n" +
					" *\n" +
					" */\n";


	static int MAX_LINE_LENGHT = 120;

	static String INDENT = "    ";

	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder documentBuilder = null;

	static Set<String> tcPrefix = new TreeSet<>();
	static void makeTestCaseFilter(String name, String subsection) {
		tcPrefix.clear();
		if (subsection == null) {
			tcPrefix.add(""); // all tests will match
		}
		else if (name.equals("Crypto")) {
			tcPrefix.add("Invoke_Crypto_Cipher");
			tcPrefix.add("Invoke_Crypto_Copy");
			tcPrefix.add("Invoke_Crypto_DeriveKey");
			tcPrefix.add("Invoke_Crypto_Diges");
			tcPrefix.add("Invoke_Crypto_GenerateRandom");
			tcPrefix.add("Invoke_Crypto_GetOperationInfo");
			tcPrefix.add("Invoke_Crypto_MAC");
		}
		else if (name.equals("DataStorage")) {
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
		//opNameMap.put("SetUp_TEE", "");
		//opNameMap.put("SelectApp", "");
		//opNameMap.put("CloseSession", "TEEC_CloseSession");
		//opNameMap.put("FinalizeContext", "TEEC_FinalizeContext");
		//opNameMap.put("TearDown_TEE", "");

		//operation has the same name as one of test cases
		//opNameMap.put("Invoke_Crypto_GenerateRandom", "adaptation_Invoke_Crypto_GenerateRandom");
	}


	static Set<String> argPtr = new TreeSet<>();
	static {
		argPtr.add("ALL_THREADS");
		argPtr.add("ALL_CONTEXTS");
		argPtr.add("ALL_SESSIONS");
		argPtr.add("AttributeList");
		argPtr.add("ALL_OPERATIONS");
		argPtr.add("ALL_TEMPORARY_MEMORIES");
		argPtr.add("ALL_SHARED_MEMORIES");
	}

	static ArgInfo uint8buf_4k = new ArgInfo("4096", "uint8_t", "operationBuffer4k");
	//static ArgInfo uint8buf_32 = new ArgInfo("32", "uint8_t", "operationBuffer32");

	static Map<String,ArgInfo> opAppendArgs = new HashMap<>();
	static {
		opAppendArgs.put("Invoke_Crypto_GetOperationInfo", uint8buf_4k);
		opAppendArgs.put("Check_OperationInfo", uint8buf_4k);

		opAppendArgs.put("Invoke_Crypto_GetOperationInfoMultiple", uint8buf_4k);
		opAppendArgs.put("Check_0_OperationInfoMultiple", uint8buf_4k);
		opAppendArgs.put("Check_1_OperationInfoKey", uint8buf_4k);

		opAppendArgs.put("Invoke_GetObjectInfo1", uint8buf_4k);
		opAppendArgs.put("Invoke_GetObjectInfo", uint8buf_4k);
		opAppendArgs.put("Check_ObjectInfo", uint8buf_4k);

		opAppendArgs.put("Invoke_GetObjectBufferAttribute", uint8buf_4k);
		opAppendArgs.put("Check_ObjectBufferAttribute", uint8buf_4k);

		opAppendArgs.put("Invoke_GetObjectValueAttribute", uint8buf_4k);
		opAppendArgs.put("Check_ObjectValueAttribute", uint8buf_4k);

		opAppendArgs.put("Macro_GetRSAAttributes", uint8buf_4k);
		opAppendArgs.put("Macro_GetDHAttributes", uint8buf_4k);
		opAppendArgs.put("Check_GeneratedRSAAttributes", uint8buf_4k);

		opAppendArgs.put("Invoke_ReadObjectData", uint8buf_4k);
		opAppendArgs.put("Invoke_GetNextPersistentObject_All", uint8buf_4k);
		opAppendArgs.put("Check_ReadObjectData_DataRead", uint8buf_4k);
		opAppendArgs.put("Check_ReadObjectData_AfterWrite", uint8buf_4k);
		opAppendArgs.put("Check_ReadObjectData_AfterTruncate", uint8buf_4k);

		opAppendArgs.put("Check_GeneratedDHAttributes", uint8buf_4k);
		opAppendArgs.put("Check_EnumeratedPersistentObject", uint8buf_4k);
		opAppendArgs.put("Check_ObjectBufferAttribute_ValueIsTheFullKeySize", uint8buf_4k);
	}

	static Set<OperationInfo> operationTypes = new TreeSet<>();

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

		// unique variable
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof ArgInfo)) return false;
			ArgInfo o = (ArgInfo)obj;
			return value.equals(o.value);
		}
		@Override
		public int hashCode() {
			return value.hashCode();
		}

		@Override
		public String toString() {
			if (argPtr.contains(type) ) {
				if (value.equals("NULL")) return "NULL";
				if (value.equals("IGNORE")) return "NULL";
				return "&"+value;
			}
			else if (type.equals("ALL_TEEC_VALUES")) {
				if (value.equals("IGNORE")) return "TEEC_VALUE_IGNORE";
			}
			else if (type.equals("ALL_MEMORY_SIZES")) {
				if (value.equals("IGNORE")) return "MEMORY_SIZE_IGNORE";
			}
			else if (type.equals("ALL_KIND_MEMORIES")) {
				if (value.equals("TEMPORARY")) return "KIND_MEMORY_TEMPORARY";
				if (value.equals("SHARED")) return "KIND_MEMORY_SHARED";
			}
			else if (type.equals("ALL_RETURN_ORIGINS")) {
				if (value.equals("NULL")) return "RETURN_ORIGIN_NULL";
			}
			else if (type.equals("ALL_TEE_OBJECT_HANDLES")) {
				if (value.equals("NULL")) return "0";
			}
			else if (type.equals("ALL_TTA_STORED_OBJECT_ENUMERATORS")) {
				if (value.equals("NULL")) return "STORED_OBJECT_NULL";
			}
			else if (type.equals("ALL_STORED_VALUES_ROLES")) {
				if (value.equals("NULL")) return "STORED_VALUE_NULL";
			}
			else if (type.equals("ALL_ENUMERATORS")) {
				if (parameter.startsWith("OUT_")) return "&"+value;
			}
			if (parameter.endsWith("instance")) {
				return "&"+value;
			}
			return value;
		}

		public String prototype() {
			if (type.equals("uint8_t"))
				return String.format("%s* %s,", type, value);

			String p = parameter;
			if (p.startsWith("IN_")) p = p.substring(3);
			else if (p.startsWith("OUT_")) p = p.substring(4);
			if (p.equals("case")) p = "a_case";
			if (argPtr.contains(type) || parameter.endsWith("instance")) {
				return String.format("%s* %s,", type, p);
			}
			if (type.equals("ALL_ENUMERATORS") && parameter.startsWith("OUT_")) {
				return String.format("%s* %s,", type, p);
			}
			return String.format("%s %s,", type, p);
		}
	}
	static class OperationInfo implements Comparable<OperationInfo> {
		TestCaseInfo tc;
		String name;
		List<ArgInfo> args;
		public OperationInfo(TestCaseInfo tc) {
			this.tc = tc;
		}
		@Override
		public boolean equals(Object obj) {
			OperationInfo o = (OperationInfo)obj;
			return name.equals(o.name);
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		private boolean hasReturnCode() {
			if (name.startsWith("Check") || name.startsWith("check")) return true;
			if (name.equals("AllocateTempMemory") || name.equals("AllocateSharedMemory")) return true;
			for (ArgInfo a : args) {
				if (a.type.equals("ALL_TEE_RESULTS") || a.type.equals("ALL_RETURN_CODES")) return true;
			}
			return false;
		}
		public void implement(PrintStream pr) {
			String mapname = opNameMap.get(name);
			if (mapname == null) mapname = name;
			else if (mapname.isEmpty()) return ;

			String sep = ", ";
			if (hasReturnCode() && !tc.postamble) {
				String oneline = String.format(INDENT+"res = %s(%s);\n", mapname, Text.join(sep, args));
				if (oneline.length() > MAX_LINE_LENGHT) {
					String openIndent = String.format(INDENT+"res = %s(", mapname);
					sep = ",\n"+openIndent.replaceAll(".", " ");
				}

				pr.printf(INDENT+"res = %s(%s);\n", mapname, Text.join(sep, args));

				//if (name.equals("InitializeContext"))
				//	pr.printf(INDENT+"if (res != TEEC_SUCCESS) goto out;\n\n");
				//else
					pr.printf(INDENT+"if (res != TEEC_SUCCESS) goto postamble;\n\n");
			}
			else {
				String oneline = String.format(INDENT+"%s(%s);\n", mapname, Text.join(sep, args));
				if (oneline.length() > MAX_LINE_LENGHT) {
					String openIndent = String.format(INDENT+"%s(", mapname);
					sep = ",\n"+openIndent.replaceAll(".", " ");
				}
				pr.printf(INDENT+"%s(%s);\n", mapname, Text.join(", ", args));
			}
		}
		public void prototype(PrintStream pr) {
			StringBuilder b = new StringBuilder();
			if (hasReturnCode()) b.append("TEEC_Result ");
			else b.append("void ");
			b.append(name+"(");
			int indent = b.length();
			for (int i = 0; i < args.size(); ++i) {
				ArgInfo a = args.get(i);
				b.append(a.prototype());
				if (i < args.size()-1)
					b.append(" ");
			}
			if (b.length() > MAX_LINE_LENGHT) {
				b.setLength(indent);
				String openIndent = b.toString().replaceAll(".", " ");
				for (int i = 0; i < args.size(); ++i) {
					ArgInfo a = args.get(i);
					b.append(a.prototype());
					if (i < args.size()-1)
						b.append("\n" + openIndent);
				}
			}
			if (b.charAt(b.length()-1)==',') b.setLength(b.length()-1);
			b.append(");");
			pr.println(b.toString());
		}
		@Override
		public int compareTo(OperationInfo o) {
			return name.compareTo(o.name);
		}
	}
	static class StepInfo {
		List<OperationInfo> ops;
		public void implement(PrintStream pr) {
			for (OperationInfo op : ops)
				op.implement(pr);
		}
	}
	static class SeparationStep extends StepInfo {
		String label;
		public SeparationStep(String l) {
			label = l;
		}
		@Override
		public void implement(PrintStream pr) {
			pr.printf("%s:\n", label);
		}
	}
	static class TestCaseInfo {
		boolean postamble = false;
		String name;
		String id;
		List<String> localVars = new ArrayList<>();
		List<StepInfo> steps = new ArrayList<>();
		public void addLocalVar(String init) {
			if (!localVars.contains(init))
				localVars.add(init);
		}
		public void implement(PrintStream pr) {
			pr.printf("//Test: %s %s\n", name, id);
			pr.printf("static int tc_%s()\n", name);
			pr.printf("{\n");

			for (String init : localVars) {
				pr.printf(INDENT+"%s;\n", init);
			}
			pr.printf(INDENT+"TEEC_Result res = TEEC_SUCCESS;\n");
			pr.println();

			if (!steps.isEmpty()) { // check first and last step
				OperationInfo op;
				op = steps.get(0).ops.get(0);
				if (!op.name.equals("SetUp_TEE")) {
					throw new RuntimeException("no SetUp_TEE for " + name);
				}
				op = steps.get(steps.size()-1).ops.get(0);
				if (!op.name.equals("TearDown_TEE")) {
					throw new RuntimeException("no TearDown_TEE for " + name);
				}
			}

			for (StepInfo si : steps) {
				si.implement(pr);
				if (si == postambleStep) postamble = true;
			}

			//pr.printf(INDENT+"out:\n");
			pr.printf(INDENT+"return res == TEEC_SUCCESS ? TEST_PASS : TEST_FAIL;\n}\n");
		}
		public void addTestMacro(String sect, PrintStream pr) {
			String desc = name.replace("_", " ");
			//if (desc.length() > 39) desc = desc.substring(0,39-3)+"...";
			pr.printf("CREATE_TEST_CUSTOM(test_%s, \"%s\", \"%s%s\", adaptation_test_prepare, tc_%s, adaptation_test_fin);\n", name, sect, id, desc, name);
		}
		public void tableEntry(PrintStream pr) {
			pr.println(INDENT+"{");
			pr.printf(INDENT+INDENT+"test_%s,\n", name);
			pr.printf(INDENT+INDENT+"\"%s\",\n", name);
			pr.printf(INDENT+INDENT+"%s,\n", name);
			pr.printf(INDENT+INDENT+"false\n");
			pr.println(INDENT+"},");
		}
		public void enumEntry(PrintStream pr) {
			pr.printf(INDENT+"test_%s,\n", name);
		}
		public void serviceAddEntry(String sect, PrintStream pr) {
			String desc = name.replace("_", " ");
			if (desc.length() > 40) desc = desc.substring(0,40-3)+"...";
			pr.printf("ADD_TEST_GP_SUITE(gp_test_%s, \"TTA_%s\", \"%s%s\", test_%s);\n",
					name, sect, desc, id, name);
		}
		public void serviceTabEntry(PrintStream pr) {
			pr.printf(INDENT+"&test_gp_test_%s,\n",name);
		}
	}


	private static boolean filterPrefix(TestCaseInfo tc) {
		if (tcPrefix.isEmpty()) return true;
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

		OperationInfo op = new OperationInfo(tc);
		op.name = n.getAttributes().getNamedItem("name").getTextContent();
		if (op.name == null || op.name.isEmpty()) return null;

		//Log.info("    operation %s", op.name);
		op.args = new ArrayList<>(args.getLength());
		for (int i = 0; i < args.getLength(); ++i) {
			Node a = args.item(i);
			ArgInfo arg = parseArgument(a);
			if (arg == null) throw new RuntimeException("cannot parse Argument");

			if (arg.type.equals("ALL_THREADS")) {
				if (!arg.value.equals("THREAD01_DEFAULT"))
					tc.addLocalVar(String.format("%s %s = {}",arg.type,arg.value));
			}
			else if (arg.type.equals("ALL_CONTEXTS")) {
				if (!arg.value.equals("NULL"))
					tc.addLocalVar(String.format("%s %s = {}",arg.type,arg.value));
			}
			else if (arg.type.equals("ALL_SESSIONS")) {
				if (!arg.value.equals("NULL"))
					tc.addLocalVar(String.format("%s %s = {}",arg.type,arg.value));
			}
			else if (arg.type.equals("AttributeList")) {
				if (!arg.value.endsWith("Empty"))
					tc.addLocalVar(String.format("%s %s = {}",arg.type,arg.value));
			}
			if (arg.type.equals("ALL_OPERATIONS")) {
				if (!arg.value.equals("NULL"))
					tc.addLocalVar(String.format("%s %s = {}",arg.type,arg.value));
			}
			else if (arg.type.equals("ALL_TEMPORARY_MEMORIES")) {
				if (!arg.value.equals("NULL") && !arg.value.equals("IGNORE"))
					tc.addLocalVar(String.format("%s %s = {}",arg.type,arg.value));
			}
			else if (arg.type.equals("ALL_SHARED_MEMORIES")) {
				if (!arg.value.equals("NULL") && !arg.value.equals("IGNORE"))
					tc.addLocalVar(String.format("%s %s = {}",arg.type,arg.value));
			}
			else if (arg.type.equals("ALL_ENUMERATORS")) {
				if (!arg.value.equals("INVALID_ENUMERATOR"))
					tc.addLocalVar(String.format("%s %s = 0",arg.type,arg.value));
			}
			else if (arg.type.equals("HandleFlags") || arg.type.equals("DataFlags")) {
				if (!arg.value.endsWith("None"))
					tc.addLocalVar(String.format("%s %s = 0",arg.type,arg.value));
			}
			else if (arg.type.equals("ObjectUsage")) {
				if (!arg.value.endsWith("None") && !arg.value.endsWith("AllBitsOne") && !arg.value.endsWith("Unknown"))
					tc.addLocalVar(String.format("%s %s = 0",arg.type,arg.value));
			}
			op.args.add(arg);
		}
		if (opAppendArgs.containsKey(op.name)) {
			ArgInfo a = opAppendArgs.get(op.name);
			tc.addLocalVar(String.format("%s %s[%s]", a.type, a.value, a.parameter));
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
			OperationInfo op = parseOperation(tc, o);
			if (op == null) throw new RuntimeException("cannot parse Operation");;
			operationTypes.add(op);
			si.ops.add(op);
		}
		return si;
	}

	private static void parseSteps(TestCaseInfo tc, Node n) {
		Document d = documentBuilder.newDocument();
		d.appendChild(d.importNode(n, true));
		NodeList steps = d.getElementsByTagName("call");
		for (int i = 0; i < steps.getLength(); ++i) {
			Node s = steps.item(i);
			//Log.info("  step[%d]: %s", i, s.getAttributes().getNamedItem("stepNumber"));
			StepInfo si = parseStep(tc, s);
			if (si == null) throw new RuntimeException("cannot parse Step");
			tc.steps.add(si);
		}
	}

	static StepInfo postambleStep = new SeparationStep("postamble");

	private static TestCaseInfo parseTestCase(Node n) {
		TestCaseInfo tc = new TestCaseInfo();
		String nm = n.getAttributes().getNamedItem("name").getTextContent();
		int idx = nm.indexOf(' ');
		if (idx == -1) return null;
		tc.name = nm.substring(0, idx).replace('=', '_');
		tc.id = nm.substring(idx + 1);
		Log.info("node: %s", tc.name);

		NodeList scenario = n.getChildNodes();
		for (int i = 0; i < scenario.getLength(); ++i) {
			Node a = scenario.item(i);
			if (a.getNodeType() != Node.ELEMENT_NODE) continue;
			String tag = a.getNodeName();
			if (tag.equals("preamble")) parseSteps(tc, a);
			else if (tag.equals("body")) parseSteps(tc, a);
			else if (tag.equals("verification")) parseSteps(tc, a);
			else if (tag.equals("postamble")) {
				tc.steps.add(postambleStep);
				parseSteps(tc, a);
			}
		}

		return tc;
	}

	private static List<TestCaseInfo> parseTestCaseXML(String name) throws Exception {
		String fn = null;
		if (name.equals("ClientAPI"))
			fn = String.format("%s/%s/xmlstable/TEE.xml", xmlPackagesPath, name);
		else if (name.equals("Time_Arithmetical"))
			fn = String.format("%s/%s/xmlstable/TEE_TimeArithm_API.xml", xmlPackagesPath, name);
		else if (name.equals("TrustedCoreFw"))
			fn = String.format("%s/%s/xmlstable/TEE_Internal_API.xml", xmlPackagesPath, name);
		else
			fn = String.format("%s/%s/xmlstable/TEE_%s_API.xml", xmlPackagesPath, name, name);

		File f = new File(fn);
		if (!f.exists())
			throw new RuntimeException("File not exis: "+fn);

		makeTestCaseFilter(name, null);

		Document document = documentBuilder.parse(f);
		NodeList tclist = document.getElementsByTagName("scenario");
		Log.info("tclist %d", tclist.getLength());

		List<TestCaseInfo> tcs = new ArrayList<>();
		for (int i = 0; i < tclist.getLength(); ++i) {
			Node n = tclist.item(i);
			TestCaseInfo tc = parseTestCase(n);
			if (tc == null) throw new RuntimeException("cannot parse TestCase");
			if (filterPrefix(tc))
				tcs.add(tc);
		}
		Log.info("tcs %d", tcs.size());
		return tcs;
	}

	static class EnumEntry {
		String name;
		String value;
	}
	static class EnumInfo {
		String name;
		List<EnumEntry> entry = new ArrayList<>();
	}

	private static EnumEntry parseEnumEntry(Node n) {
		EnumEntry e = new EnumEntry();
		e.name = n.getAttributes().getNamedItem("name").getTextContent();
		NodeList attr = n.getChildNodes();
		for (int i = 0; i < attr.getLength(); ++i) {
			Node a = attr.item(i);
			if (a.getNodeType() != Node.ELEMENT_NODE) continue;
			String tag = a.getNodeName();
			if (tag.equals("description")) {
				e.value = a.getTextContent();
			}
		}
		return e;
	}
	private static EnumInfo parseValuesXML(File f) throws Exception {
		Document document = documentBuilder.parse(f);
		NodeList enlist = document.getElementsByTagName("Literal");
		String name = f.getName().substring(0, f.getName().length() - 4);
		Log.info("%s literals %d", name, enlist.getLength());

		EnumInfo enumdef = new EnumInfo();
		enumdef.name = name;
		for (int i = 0; i < enlist.getLength(); ++i) {
			Node n = enlist.item(i);
			EnumEntry e = parseEnumEntry(n);
			if (e == null) throw new RuntimeException("cannot parse enum entry");
			Log.info("entry %s, value=%s", e.name, e.value);
			enumdef.entry.add(e);
		}
		return null;
	}

	private static void genAdaptationHeader() {
		String name = "gp_adaptation_api";
		String fn = String.format("%s/include/%s.h", repoTestCodePath, name);

		Log.info("generating file %s", fn);
		PrintStream pr;
		try {
			pr = new PrintStream(fn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		pr.println(boilerPlate);
		String def = String.format("__%s_H__", name.toUpperCase());
		pr.println("#ifndef " + def);
		pr.println("#define " + def);
		pr.println();
		pr.println("#include \"gp_adaptation_types.h\"");
		pr.println();
		Log.info("  operations %d", operationTypes.size());
		for (OperationInfo op : operationTypes) {
			op.prototype(pr);
		}
		pr.println();
		pr.printf("#endif // %s\n", def);

	}
	private static void genTestCasesCode(String name, List<TestCaseInfo> tcs) {
		String fn = null;
		if (name.equals("ClientAPI"))
			fn = String.format("%s/tta_test_%s_auto.c", repoGPSuitePath, name);
		else
			fn = String.format("%s/tta_test_API_%s_auto.c", repoGPSuitePath, name);

		Log.info("generating file %s", fn);
		PrintStream pr;
		try {
			pr = new PrintStream(fn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		pr.println(boilerPlate);

		pr.println("#include \"gp_adaptation_api.h\"");
		pr.println("#include \"tf.h\"");
		pr.println();

		int n = 0;
		for (TestCaseInfo tc : tcs) {
			tc.implement(pr);
		}
		pr.println();

		pr.println("#include \"tf.h\"");
		for (TestCaseInfo tc : tcs) {
			tc.addTestMacro(name, pr);
		}

		pr.println("static struct test_case *test_array[] = {");
		for (TestCaseInfo tc : tcs) {
			pr.printf(INDENT+"&test_%s,\n", tc.name);
		}
		pr.println(INDENT + "NULL");
		pr.println("};");

		pr.println("INITIALIZE_TEST_MODULE(test_array);");
	}

	private static void genTestCasesService(String name, List<TestCaseInfo> tcs) {
		String fn = null;
		String header = null;
		if (name.equals("ClientAPI")) {
			fn = String.format("%s/test_service_tta_test_%s_auto.c", repoGPSuitePath, name);
			header = String.format("include/tta_test_%s_auto.h", name);
		}
		else {
			fn = String.format("%s/test_service_tta_test_API_%s_auto.c", repoGPSuitePath, name);
			header = String.format("include/tta_test_API_%s_auto.h", name);
		}

		Log.info("generating file %s", fn);
		PrintStream pr;
		try {
			pr = new PrintStream(fn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		pr.println(boilerPlate);

		pr.printf("#include \"%s\"\n", header);
		pr.println("#include \"tf_gp_suite.h\"");
		pr.println();
		pr.printf("static testStruct* pack = tests_API_%s;\n", name);
		for (TestCaseInfo tc : tcs) {
			tc.serviceAddEntry(name, pr);
		}
		pr.println();
		pr.println("static struct test_case *tests_table[] = {");
		for (TestCaseInfo tc : tcs) {
			tc.serviceTabEntry(pr);
		}
		pr.println(INDENT+"NULL\n};");
		pr.println();
		pr.println("INITIALIZE_TEST_MODULE(tests_table);");
	}

	static class Text {

		public static Object join(String sep, Object[] args) {
			if (args.length == 0) return "";
			StringBuilder b=new StringBuilder();
			b.append(args[0].toString());
			for (int i = 1; i < args.length; ++i) {
				b.append(sep);
				b.append(args[i].toString());
			}
			return b.toString();
		}
		public static Object join(String sep, List<?> args) {
			if (args.isEmpty()) return "";
			StringBuilder b=new StringBuilder();
			b.append(args.get(0).toString());
			for (int i = 1; i < args.size(); ++i) {
				b.append(sep);
				b.append(args.get(i).toString());
			}
			return b.toString();
		}

	}
	static class Log {
		public static void error(Exception e) {
			e.printStackTrace(System.err);
		}
		public static void error(String format, Object... args) {
			System.err.printf("ERROR: "+format, args);
			System.err.println();
		}
		public static void info(String format, Object... args) {
			System.err.printf(format, args);
			System.err.println();
		}
	}

	static void generateTC(String name) {
		try {
			List<TestCaseInfo> tcs = parseTestCaseXML(name);
			genTestCasesCode(name, tcs);
			//genTestCasesService(name, tcs);
		} catch (Exception e) {
			Log.error(e);
		}
	}

	static void generateValues(String name) {
	}
	static void generateValues() {

		File[] xmls = new File(xmlValuesPath).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".xml");
			}
		});
		Log.info("Files to scan: %d", xmls.length);
		for (File f : xmls) {
			try {
				parseValuesXML(f);
			} catch (Exception e) {
				Log.error(e);
			}
		}
	}

	static void usage() {
		Log.info("gentool [cmd] parts\n"
				+ "where parts is one or more from:\n"
				+ "  ClientAPI Crypto DataStorage");

	}

	private static final int CMD_X = 0;
	private static final int CMD_TC = 1;
	public static void main(String[] args) {
		try {
			//documentBuilderFactory.setNamespaceAware(true);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		}
		catch (Exception e) {
			Log.error(e);
			return ;
		}
		if (args.length < 1) usage();

		int cmd = CMD_TC;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("x")) {
				cmd = CMD_X;
			}
			else if (args[i].equals("tc")) {
				cmd = CMD_TC;
			}
			else if (args[i].equals("val")) {
				generateValues();
			}
			else {
				if (cmd == CMD_TC) generateTC(args[i]);
			}

		}
		genAdaptationHeader();
	}

}
