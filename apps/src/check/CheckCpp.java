package check;

import java.io.IOException;

import text.FileUtils;
import time.LapTime;
import tokenize.CppParser;

public class CheckCpp {
	static String[] files={
			"libs/lxcpp/network-config.cpp",
			"libs/config/from-kvjson-visitor.hpp",
			"wrapper/wrapper-compatibility.cpp",
			"server/zones-manager.cpp",
			"libs/config/from-kvjson-visitor.hpp",
	};
	static String pfx="~/security-containers/";
	public static void main(String[] args) throws Exception {
		String p=FileUtils.expandEnv(pfx);
		for (String f : files) {
			f=FileUtils.expandEnv(f);
			if (f.startsWith("/"))
				dofile(f);
			else
				dofile(p+f);
		}
	}
	static void dofile(String f) throws Exception {
		CppParser p=new CppParser();
		LapTime tm=new LapTime("ln");
		try {
			CppParser.CppNode n=p.parse(f);
			tm.update(System.currentTimeMillis(), p.getLineNo());
			System.out.println(tm.toString());
			CppParser.printNode(n);
		}catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
