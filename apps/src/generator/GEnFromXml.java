package generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sys.Log;
import text.Text;

public class GEnFromXml {
	static String TEEC_Rezult = "TEEC_Rezult";

	static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder documentBuilder = null;

	static class OperationInfo {
		String name;
		List<String> args;
		public void implement() {
			System.out.printf("\tres = %s(%s);\n", name, Text.join(", ", args));
		}
	}
	static class StepInfo {
		OperationInfo op;
		public void implement() {
			op.implement();
		}
	}
	static class TestCaseInfo {
		String name;
		String id;
		List<StepInfo> steps;
		public void implement() {
			System.out.printf("//Test: %s %s\n", name, id);
			System.out.printf("%s %s()\n", TEEC_Rezult, name);
			System.out.printf("{\n");
			System.out.printf("\t%s res;\n", TEEC_Rezult);

			for (StepInfo si : steps) {
				si.implement();
			}

			System.out.printf("\treturn res;\n}\n");
		}
	}

	private static OperationInfo parseOperation(Node n) {
		Document d = documentBuilder.newDocument();
		d.appendChild(d.importNode(n, true));
		NodeList args = d.getElementsByTagName("value");

		OperationInfo op = new OperationInfo();
		op.name = n.getAttributes().getNamedItem("name").getTextContent();
		Log.info("    operation %s", op.name);
		op.args = new ArrayList<>(args.getLength());
		for (int i = 0; i < args.getLength(); ++i) {
			Node a = args.item(i);
			String arg = a.getAttributes().getNamedItem("name").getTextContent();
			Log.info("       arg %s", arg);
			op.args.add(arg);
		}
		return op;
	}

	private static StepInfo parseStep(Node n) {
		Document d = documentBuilder.newDocument();
		d.appendChild(d.importNode(n, true));
		NodeList ops = d.getElementsByTagName("operation");

		if (ops.getLength() == 0) return null;
		if (ops.getLength() > 1) throw new RuntimeException("too many ops");

		StepInfo si = new StepInfo();
		si.op = parseOperation(ops.item(0));
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
			StepInfo si = parseStep(s);
			if (si == null) break;
			tc.steps.add(si);
		}
		return tc;
	}

	private static void genFrom(String fn) throws Exception {
		File f = new File(fn);
		if (!f.exists()) return;

		Document document = documentBuilder.parse(f);
		NodeList tclist = document.getElementsByTagName("scenario");
		Log.info("tclist %d", tclist.getLength());

		for (int i = 0; i < tclist.getLength(); ++i) {
			Node n = tclist.item(i);
			TestCaseInfo tc = parseTestCase(n);
			tc.implement();
			break;
		}
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
				genFrom(fn);
			} catch (Exception e) {
				Log.error(e);
			}
		}
	}
}
