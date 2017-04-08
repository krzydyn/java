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

import io.IOText;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import text.Text;

public class Env {
	static public final String country() { return System.getProperty("user.country"); }
	static public final String language() { return System.getProperty("user.language"); }
	static public final String launcher() { return System.getProperty("sun.java.launcher"); }
	static public final String osName() { return System.getProperty("os.name"); }
	static public final String osArch() { return System.getProperty("os.arch"); }
	static public final String osVersion() { return System.getProperty("os.version"); }

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

	static public IOText startexec(File dir, List<String> args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectErrorStream(true);
		if (dir!=null) pb.directory(dir);
		//.environment(envp)
		Process child = pb.start();
		OutputStream out = child.getOutputStream();
		InputStream in = child.getInputStream();
		return new IOText(in, out);
	}

	static public String exec(File dir, List<String> args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(args);
		if (dir!=null) pb.directory(dir);
		//.environment(envp)
		Process child = pb.start();

		OutputStream out = child.getOutputStream();
		InputStream in = child.getInputStream();
		InputStream err = child.getErrorStream();

		InputStreamReader isr = new InputStreamReader(in, Text.UTF8_Charset);
		char[] buf = new char[1024];
		int r;

		StringBuilder str = new StringBuilder();
		while ((r=isr.read(buf)) >= 0) {
			str.append(buf, 0, r);
		}

		try {
			int ec = child.waitFor();
			Log.debug("exec %s exitcode=%d", Text.join(" ", args), ec);
			if (ec != 0) {
				str.setLength(0);
				isr = new InputStreamReader(err, Text.UTF8_Charset);
				while ((r=isr.read(buf)) >= 0) {
					str.append(buf, 0, r);
				}
				throw new IOException("Exit("+ec+") "+str.toString());
			}
		} catch (InterruptedException e) {
			Log.debug("exec %s interrupted", Text.join(" ", args));
			return null;
		} finally {
			out.close();
			in.close();
			err.close();
		}

		return str.toString();
	}

	static public String exec(File dir, String ...args) throws IOException {
		return exec(dir,new ArrayObj<String>(args));
	}

	static public String exec(String ...args) throws IOException {
		return exec(null,new ArrayObj<String>(args));
	}

	static private ClipboardOwner manClipboard = new ClipboardOwner() {
		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	};

	static public void setClipboardText(String data) throws Exception {

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), manClipboard);
	}
	static public String getClipboardText() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {}
		return null;
	}

	static boolean isAppJar() {
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

	static public List<File> getDirs(File parent, int level){
		List<File> dirs = new ArrayList<File>();
		File[] files = parent.listFiles();
		if (files == null) return dirs;
		for(File f: files){
			if(f.isDirectory()) {
				 if (level==0) dirs.add(f);
				 else if (level > 0) dirs.addAll(getDirs(f,level-1));
			}
		}
		return dirs;
	}

	static String memstat() {
		//System.gc(); // <=> Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();
		StringBuilder b = new StringBuilder();
		b.append("free: "+Runtime.getRuntime().freeMemory());
		b.append(", ");
		b.append("max: "+Runtime.getRuntime().maxMemory());
		b.append(", ");
		b.append("total: "+Runtime.getRuntime().totalMemory());
		return b.toString();
	}

	static public void setCause(Throwable e,Throwable c) {
		try {
			java.lang.reflect.Field cause = Throwable.class.getDeclaredField("cause");
			cause.setAccessible(true);
			cause.set(e, c);
		} catch (Throwable te) {}
	}
}
