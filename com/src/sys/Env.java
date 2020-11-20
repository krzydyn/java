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

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.IOCaptureWorker;
import io.IOText;
import text.Text;

public class Env {
	final public static Charset UTF8 = Charset.forName("UTF-8");

	final public static String PATH_SEPARATOR = ";";

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

	static public final Dimension defaultScreenSize() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		return new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
	}

	static private String linuxPath(String f) {
		return f.replace('\\', '/');
	}

	static public void addLibraryPath(String p) {
		p = Env.expandEnv(getAppPath(Env.class), p);
		Log.debug("Adding library path '%s'", p);
		Field usrPathsField;
		try {
			usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
			usrPathsField.setAccessible(true);
			final String[] paths = (String[])usrPathsField.get(null);
			for(String path : paths) {
				if(path.equals(p)) {
					Log.info("path %s already on list", p);
					return;
				}
			}

			final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
			newPaths[paths.length] = p;
			usrPathsField.set(null, newPaths);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void startApplication(final Class<?> main) throws Exception {
		Log.debug("starting applicatiobn %s", main.getName());
		RuntimeMXBean jvm = ManagementFactory.getRuntimeMXBean();
		ArrayList<String> args = new ArrayList<>();
		args.add(System.getProperty("java.home") + "/bin/java");
		args.addAll(jvm.getInputArguments());

		// ClassPath
		String cp = jvm.getClassPath();
		if (!cp.isEmpty()) {
			args.add("-cp"); args.add(jvm.getClassPath());
		}
		// Class to be executed
		args.add(main.getName());

		Log.debug("launch: %s", args.toString());
		ProcessBuilder pb = new ProcessBuilder(args).inheritIO();
		pb.start();
	}

	static public String getAppPath(Class<?> c) {
		try {
			File f = new File(c.getProtectionDomain().getCodeSource().getLocation().toURI());
			return f.getParentFile().getCanonicalPath();
		} catch (Exception e) {
			Log.error(e);
		}
		return null;
	}
	static public boolean isAppJar(Class<?> c) {
		try {
			URL u = c.getProtectionDomain().getCodeSource().getLocation();
			if (u.getPath().endsWith(".jar")) {
				return true;
			}
		} catch (Throwable e) {
			Log.error(e);
		}
		return false;
	}

	static public String expandEnv(String p) {
		return expandEnv(null, p);
	}
	static public String expandEnv(String wd, String p) {
		p = linuxPath(p);
		if (p.startsWith("/") || p.substring(1).startsWith(":/")) ;
		else if (p.startsWith("~/") || p.equals("~")) {
			p = System.getProperty("user.home") + p.substring(1);
		}
		else {
			if (wd == null) wd = "./";
			else if (!wd.endsWith("/")) wd += "/";
			p = wd + p;
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
		return linuxPath(p);
	}

	static public String exec(String[] args, String[] env, File dir) throws IOException {
		Process child = Runtime.getRuntime().exec(args, env, dir);
		OutputStream out = child.getOutputStream();
		Env.close(out);

		IOCaptureWorker in = new IOCaptureWorker(child.getInputStream());
		IOCaptureWorker err = new IOCaptureWorker(child.getErrorStream());
		new Thread(err).start();
		in.run();

		try {
			//int ec = child.waitFor(); // blocking
			boolean done = child.waitFor(3, TimeUnit.MINUTES); //TODO make timeout configurable
			if (!done) throw new IOException("Timeout exec");
			int ec = child.exitValue();
			if (ec != 0) {
				Log.error("exec(%s); exitcode=%d", Text.join(" ", args), ec);
				String msg = err.getOutput();
				if (msg.isEmpty()) msg = in.getOutput();
				throw new IOException("Exit("+ec+") "+ msg);
			}
		} catch (InterruptedException e) {
			Log.debug("exec %s interrupted", Text.join(" ", args));
			return null;
		} finally {
			child.destroy();
		}

		return in.getOutput();
	}

	static public String exec(List<String> args, Map<String,String> env, File dir) throws IOException {
		String[] envp = new String[env.size()];
		int ei = 0;
		for (Iterator<Map.Entry<String,String>> i = env.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String,String> e = i.next();
			//byte[] v = e.getValue().getBytes("Utf-8");
			envp[ei++] = String.format("%s=%s", e.getKey(), e.getValue());
		}
		Log.debug("ENV %s", Arrays.toString(envp));
		Log.debug("ARGS %s", args);
		return exec(args.toArray(new String[] {}), envp, dir);
	}

	static public String exec(List<String> args, Map<String,String> env, String dir) throws IOException {
		return exec(args, env, new File(dir));
	}

	static public String exec(List<String> args, File dir) throws IOException {
		return exec(args, null, dir);
	}

	static public String exec(String ...args) throws IOException {
		return exec(args, null, null);
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

	public static CharSequence getFileContent(String fn) throws IOException {
		return IOText.load(new File(expandEnv(fn)));
	}
	public static void setFileContent(String fn, CharSequence c) throws IOException {
		IOText.save(new File(expandEnv(fn)), c);
	}

	public static CharSequence getRemoteContent(String url, Map<String,String> props) throws IOException {
		URL urlObject = new URL(url);
		URLConnection conn = urlObject.openConnection();
		if (props != null) {
			for (Iterator<String> i = props.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				conn.setRequestProperty(key, props.get(key));
			}
		}
		conn.setConnectTimeout(2000);
		StringBuilder s = new StringBuilder();
		int r;
		try (IOText io = new IOText(conn.getInputStream(), null)) {
			char data[] = new char[1024];
			while ((r = io.read(data)) > 0) {
				s.append(data, 0, r);
			}
			io.close();
		}
		return s;
	}

	public static Iterable<Path> getRoots() {
		java.nio.file.FileSystem fs = FileSystems.getDefault();
		return fs.getRootDirectories();
	}

	static public List<File> getDirs(File parent, int level){
		List<File> dirs = new ArrayList<>();
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

	/**
	 * Split paths separated with ';' (; can be escaped as \; or put in single quotation)
	 * @param paths
	 * @return
	 */
	public static List<String> splitPaths(String paths) {
		final char escChar = '\\';
		final char pathSep = ';';
		final char quoteChar = '\'';

		List<String> l = new ArrayList<>();
		StringBuilder b = new StringBuilder();
		boolean esc = false;
		boolean quote = false;
		for (int i = 0; i < paths.length(); ++i) {
			char c = paths.charAt(i);
			if (esc) {
				esc = false;
				b.append(c);
			}
			else if (quote) {
				if (c == escChar && paths.charAt(i+1) == quoteChar) esc = true;
				else if (c == quoteChar) quote = false;
				else b.append(c);
			}
			else if (c == quoteChar) {
				quote = true;
			}
			else if (c == escChar) {
				esc = true;
			}
			else if (c == pathSep) {
				l.add(b.toString());
				b.setLength(0);
			}
			else b.append(c);
		}
		if (b.length() > 0) l.add(b.toString());

		return l;
	}

	static public void close(Closeable s) {
		try {if (s != null) s.close();}catch(IOException e){}
	}

	public static void remove(String fn) {
		new File(fn).delete();
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
	static public void sleep(long millis) {
		try {Thread.sleep(millis);}
		catch (InterruptedException e) { //InterruptedException clears interrupted flag of Thread
			Thread.currentThread().interrupt(); // set the flag again
		}
	}
}
