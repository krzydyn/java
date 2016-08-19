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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Env {
	static public final String expandEnv(String p) {
		if (p.startsWith("~/") || p.equals("~")) {
			p=System.getProperty("user.home")+p.substring(1);
		}
		int s=0,i,e;
		while ((s=p.indexOf('$',s))>=0) {
			i=s+1;
			char c=p.charAt(i);
			if (c=='(') {++i; e=p.indexOf(')',i);}
			else if (c=='{') {++i; e=p.indexOf('}',i);}
			else {c=' ';e=p.indexOf(' ',i);if (e<0) e=p.length(); }
			if (e<0) {
				p=p.substring(0,s);
				break;
			}
			String env=System.getenv(p.substring(i, e));
			if (c!=' ') ++e;
			if (env==null) p=p.substring(0,s)+p.substring(e);
			else p=p.substring(0,s)+env+p.substring(e);
		}
		return p;
	}

	static public String exec(String cmd, File dir) throws IOException {
		Log.debug("exec %s", cmd);

		StringBuilder str = new StringBuilder();
		Process child = Runtime.getRuntime().exec(cmd, null, dir);

		// Get output stream to write from it
		OutputStream out = child.getOutputStream();
		InputStream in = child.getInputStream();
		InputStream err = child.getErrorStream();

		byte[] buf = new byte[1024];
		int r;
		while ((r=in.read(buf)) >= 0) {
			for (int i=0; i < r; ++i)
				str.append((char)buf[i]);
		}

		try {
			int ec = child.waitFor();
			if (ec != 0) throw new IOException("Process exit code "+ec);
		} catch (InterruptedException e) {
			return null;
		} finally {
			out.close();
			in.close();
			err.close();
		}

		return str.toString();
	}
}
