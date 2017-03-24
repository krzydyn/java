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

package checker;

import java.io.IOException;
import java.io.OutputStreamWriter;

import sys.Env;
import text.tokenize.CppBuilder;
import text.tokenize.CppParser;
import time.LapTime;

public class CheckCpp {
	static String[] files={
			"~/sec-os/key-manager/src/manager/crypto/tz-backend/internals.cpp",
	};
	static String pfx="~/security-containers/";
	public static void main(String[] args) throws Exception {
		String p=Env.expandEnv(pfx);
		for (String f : files) {
			f=Env.expandEnv(f);
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
			CppBuilder.write(n, new OutputStreamWriter(System.out));
		}catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
