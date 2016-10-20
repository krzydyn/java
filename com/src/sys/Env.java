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

import io.IOChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;

import text.Text;

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

	static public IOChannel startexec(File dir, String ...args) throws IOException {
		Process child = new ProcessBuilder(args).directory(dir)
				.redirectErrorStream(true)
				.start();
		OutputStream out = child.getOutputStream();
		InputStream in = child.getInputStream();
		return new IOChannel(in, out);
	}

	static public String exec(File dir, String ...args) throws IOException {
		Log.debug("exec %s", Text.join(" ", args));

		StringBuilder str = new StringBuilder();
		//Process child = Runtime.getRuntime().exec(cmd_args, null, dir);
		Process child = new ProcessBuilder(args).directory(dir)
				.start();
		//.environment(envp)

		// Get output stream to write from it
		OutputStream out = child.getOutputStream();
		InputStream in = child.getInputStream();
		InputStream err = child.getErrorStream();

		InputStreamReader isr = new InputStreamReader(in, Text.UTF8_Charset);
		char[] buf = new char[1024];
		int r;

		while ((r=isr.read(buf)) >= 0) {
			str.append(buf, 0, r);
		}

		try {
			int ec = child.waitFor();
			if (ec != 0) {
				str.setLength(0);
				isr = new InputStreamReader(err, Text.UTF8_Charset);
				while ((r=isr.read(buf)) >= 0) {
					str.append(buf, 0, r);
				}
				throw new IOException("Exit("+ec+") "+str.toString());
			}
		} catch (InterruptedException e) {
			return null;
		} finally {
			out.close();
			in.close();
			err.close();
		}

		return str.toString();
	}

	final static private String[] stringArray={};
	static public String exec(File dir, Collection<String> args) throws IOException {
		return exec(dir,args.toArray(stringArray));
	}

	static public String exec(Collection<String> args) throws IOException {
		Process child = Runtime.getRuntime().exec(args.toArray(stringArray));
		return null;
	}

	static boolean checkApp() {
		try {
			URL u=Env.class.getProtectionDomain().getCodeSource().getLocation();
			if (u.getFile().endsWith(".jar")) {
				return true;
			}
		} catch (Throwable e) {
			Log.error(e);
		}
		return false;
	}
}
