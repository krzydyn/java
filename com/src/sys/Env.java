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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import text.Text;

public class Env {
	final public static Charset UTF8_Charset = Charset.forName("UTF-8");

	static public final String country() { return System.getProperty("user.country"); }
	static public final String language() { return System.getProperty("user.language"); }
	static public final String launcher() { return System.getProperty("sun.java.launcher"); }
	static public final String osName() { return System.getProperty("os.name"); }
	static public final String osArch() { return System.getProperty("os.arch"); }
	static public final String osVersion() { return System.getProperty("os.version"); }

	static public final boolean isMacos() {
		return osName().contains("Mac");
	}

	static public final boolean isHeadless() {
		return GraphicsEnvironment.isHeadless();
	}

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

	static public String exec(File dir, List<String> args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(args);
		if (dir!=null) pb.directory(dir);
		//pb.environment(envp)
		Process child = pb.start();

		OutputStream out = child.getOutputStream();
		InputStream in = child.getInputStream();
		InputStream err = child.getErrorStream();

		InputStreamReader isr = new InputStreamReader(in, UTF8_Charset);
		char[] buf = new char[1024];
		int r;

		StringBuilder str = new StringBuilder();
		while ((r=isr.read(buf)) >= 0) {
			str.append(buf, 0, r);
		}

		try {
			int ec = child.waitFor();
			if (ec != 0) {
				Log.error("exec(%s); exitcode=%d", Text.join(" ", args), ec);
				str.setLength(0);
				isr = new InputStreamReader(err, UTF8_Charset);
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
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			Log.info("lost content ownership");
		}
	};

	static public void setClipboardText(String data) throws Exception {
		setClipboardText(null, data);
	}
	static public void setClipboardText(Clipboard c, String data) throws Exception {
		if (c==null) c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(new StringSelection(data), manClipboard);
	}
	static public String getClipboardText() {
		return getClipboardText(null);
	}
	static public String getClipboardText(Clipboard c) {
		if (c==null) c = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			Transferable t = c.getContents(null);
			if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
				return (String)t.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {}
		return null;
	}

	public static String getFileContent(String fn) throws IOException {
		File file = new File(expandEnv(fn));
		byte b[] = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(b);
		fis.close();
		return new String(b, Env.UTF8_Charset);
	}
	public static void setFileContent(String fn, String c) throws IOException {
		File file = new File(fn);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(c.getBytes(Env.UTF8_Charset));
		fos.close();
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

	public String getRemoteContents(String url) throws IOException {
		URL urlObject = new URL(url);
		URLConnection conn = urlObject.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Env.UTF8_Charset));
		String inputLine, output = "";
		while ((inputLine = in.readLine()) != null) {
			 output += inputLine;
		}
		in.close();
		return output;
	}

	static public boolean isAppJar() {
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

	static public String memstat() {
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
	public static void remove(String fn) {
		new File(fn).delete();
	}

	public static Iterable<Path> getRoots() {
		java.nio.file.FileSystem fs = FileSystems.getDefault();
		return fs.getRootDirectories();
	}

	static public void sleep(long millis) {
		try {Thread.sleep(millis);}
		catch (InterruptedException e) { //InterruptedException clears interrupted flag of Thread
			Thread.currentThread().interrupt(); // set the flag again
		}
	}

	static public void close(Closeable s) {
		try {if (s != null) s.close();}catch(IOException e){}
	}
}
