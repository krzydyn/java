package gftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import git.GitRepo;
import net.ftp.FtpClient;
import net.ftp.FtpDirEntry;
import sys.Env;
import sys.Log;
import text.Text;

/*
 * .netrc
 * machine www.kysoft.pl
 * login www.kysoft.pl
 * password xxxxxxxxx
 */
public class GFTP {
	static String host = "www.kysoft.pl";
	static String user = "guest";
	static String passwd = "";
	static FtpClient ftp = FtpClient.create();
	static int filesSent;

	static class CopyJob {
		String src;
		String dst;
		public CopyJob(String s, String d) {
			src=s; dst=d;
		}
	}
	static List<String> exclude = new ArrayList<>();

	public static void main(String[] args) {
		List<CopyJob> jobs = new ArrayList<>();
		for (int i =0; i < args.length; ++i) {
			String a=args[i];
			if (a.startsWith("-")) {
				if (a.equals("-h")) host = args[++i];
				if (a.equals("-u")) user = args[++i];
				if (a.equals("-p")) passwd = args[++i];
			}
			else {
				jobs.add(new CopyJob(a, args[++i]));
			}
		}

		if (jobs.size() == 0) {
			exclude.add(Env.expandEnv("~/Work/www/cms/ckeditor"));
			//jobs.add(new CopyJob("~/Work/www", "/www"));
			//jobs.add(new CopyJob("~/Work/www/cms/lib", "/www/cms/lib"));
			//jobs.add(new CopyJob("~/Work/www/espdb", "/www/espdb"));
			//jobs.add(new CopyJob("~/Work/www/bridge", "/www/bridge"));
			jobs.add(new CopyJob("~/Work/www/kysoft", "/www/kysoft"));
			//jobs.add(new CopyJob("~/Work/www/templates", "/www/templates"));
			//jobs.add(new CopyJob("~/Work/www/przepisy", "/www/przepisy"));
			//jobs.add(new CopyJob("~/Work/www/ankieta.php", "/www/"));
		}

		filesSent=0;
		try {
			ftp.setConnectTimeout(3000);
			ftp.setReadTimeout(4000);
			Log.info("connecting ...");
			ftp.connect(new InetSocketAddress(host, FtpClient.defaultPort()));
			Log.info("user auth %s:[passwd]",user);
			ftp.login(user, passwd.toCharArray());
			ftp.enablePassiveMode(true);
			Log.info("set binary ...");
			ftp.setBinaryType();

			for (CopyJob cp : jobs) {
				Log.info("synchronize ... %s", cp.src);
				synchronizeDirs(new File(Env.expandEnv(cp.src)), new File(cp.dst));
			}
			//gitSyncDirs(new File(Env.expandEnv(srcDir)), new File(dstDir), "917145612cc54509a3dd52d20e0de3ca476365e1");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {ftp.close();}catch(Exception e){}
		}
		Log.info("Files sent %d\n", filesSent);
	}
	public static void synchronizeDirs(File src, File dst) throws Exception {
		syncDirs(src, dst);
	}

	private static String linuxPath(File f) {
		return f.getPath().replace('\\', '/');
	}

	private static void syncDirs(File src, File dst) throws Exception {
		if (!src.exists()) return ;
		Log.debug("gitsync dir %s", linuxPath(src));
		GitRepo git = new GitRepo(src.getPath());
		List<File> candidateFiles = new ArrayList<>();
		if (src.isFile()) {
			++filesSent;
			Log.debug("send file '%s' -> '%s'", src.getPath(), dst.getName());
			sendFile(new FileInputStream(src), ftp.putFileStream(dst.getName()));
			return ;
		}
		for (String fn : git.lstree("--name-only", "HEAD").split("\n")) {
			if (fn.equals(".gitignore")) continue;
			File f = new File(src.getPath()+"/"+fn);
			candidateFiles.add(f);
		}

		Iterator<FtpDirEntry> it = null;
		try {
			it = ftp.listFiles(linuxPath(dst));
		} catch (FileNotFoundException e) {
			Log.error(e);
			ftp.makeDirectory(linuxPath(dst));
			return ;
		}
		List<File> filesToAdd = new ArrayList<>();
		List<File> dirsToGo = new ArrayList<>();
		while (it.hasNext()) {
			FtpDirEntry fde = it.next();
			if (fde.getName().equals(".") || fde.getName().equals("..")) continue;
			//Log.debug("file: %s, %s, %s", fde.getName(), fde.getType(), fde.getSize());

			int i = indexOf(candidateFiles, fde.getName());
			if (i < 0) {
				Log.debug("[git:NOT EXISTS localy] %s", fde.getName());
			}
			else {
				File f = candidateFiles.get(i);
				String d = linuxPath(dst)+"/"+f.getName();
				candidateFiles.remove(i);
				if (exclude.contains(linuxPath(f))) {
					Log.debug("Ignored %s", linuxPath(f));
					continue;
				}
				if (f.isDirectory()) {
					dirsToGo.add(f);
					continue;
				}
				Date sTime = fde.getLastModified();
				if (f.length() != fde.getSize()) {
					filesToAdd.add(f);
					Log.debug("ADD (sz %d != %d) %s", f.length(), fde.getSize(), d);
				}
				else if (sTime == null || sTime.getTime() + 1000 < f.lastModified()) {
					filesToAdd.add(f);
					if (sTime == null) Log.debug("ADD (no server tm) %s", d);
					else Log.debug("ADD (dtm %d sec) %s", (f.lastModified() - sTime.getTime())/1000, d);
				}
				else {
					//Log.info("SAME (%d == %d) %s", f.length(), fde.getSize(), d);
				}
			}
		}

		Date time = new Date();
		for (File f : filesToAdd) {
			++filesSent;
			String d = linuxPath(dst)+"/"+f.getName();
			if (!f.exists()) {
				Log.debug("DEL file '%s'", f.getPath(), d);
				ftp.deleteFile(d);
			}
			else {
				Log.info("send file '%s' -> '%s'", f.getPath(), d);
				sendFile(new FileInputStream(f), ftp.putFileStream(d));
				time.setTime(f.lastModified());
				ftp.setLastModified(d, time);
			}
		}
		for (File f : dirsToGo) {
			if (f.isFile()) {continue;}
			syncDirs(f, new File(dst.getPath()+"/"+f.getName()));
		}
		for (File f : candidateFiles) {
			String d = linuxPath(dst)+"/"+f.getName();
			if (f.isDirectory()) {
				Log.info("NEW DIR %s", d);
				ftp.makeDirectory(d);
				syncDirs(f, new File(d));
				continue;
			}
			if (!f.isFile()) continue;
			++filesSent;
			Log.info("NEW FILE %s", d);
			sendFile(new FileInputStream(f), ftp.putFileStream(d));
			time.setTime(f.lastModified()+1000);
			ftp.setLastModified(d, time);
		}
	}

	public static void gitSyncDirs(File src, File dst, String hash) throws Exception {
		if (!src.exists()) return ;
		Log.debug("gitsync dir %s", src);
		GitRepo git = new GitRepo(src.getPath());
		List<File> localFiles = new ArrayList<>();
		if (src.isFile()) {
			Log.debug("send file '%s' -> '%s'", src.getPath(), dst.getName());
			//sendFile(new FileInputStream(src), ftp.putFileStream(dst.getName()));
			return ;
		}
		else {
			String[] list = git.diff("--name-only", hash).split("\n");
			for (String fn : list) {
				File f = new File(src.getPath()+"/"+fn);
				localFiles.add(f);
			}
		}
		Log.debug("local files: \n%s",Text.join("\n", localFiles));
		int srcPrefixLen = src.getPath().length()+1;
		for (File f : localFiles) {
			String d = dst.getPath()+"/"+f.getPath().substring(srcPrefixLen);
			if (!f.exists()) {
				Log.info("remove %s", d);
				continue;
			}
			if (f.isDirectory()) {
				Log.info("mkdir %s", d);
				continue;
			}
			++filesSent;
			Log.info("send file '%s' -> '%s'", f.getPath(), d);
			//sendFile(new FileInputStream(f), ftp.putFileStream(d));
		}
	}

	private static int indexOf(List<File> list, String n) {
		for (int i=0; i < list.size(); ++i) {
			if (list.get(i).getName().equals(n)) return i;
		}
		return -1;
	}

	static void sendFile(InputStream i, OutputStream o) throws IOException {
		byte[] buf=new byte[256];
		int r;
		while ((r=i.read(buf))>=0) {
			o.write(buf,0,r);
		}
		i.close();
		o.close();
	}
	static void readStream(InputStream i, OutputStream o) throws IOException {
		byte[] buf=new byte[256];
		int r;
		while ((r=i.read(buf))>=0) {
			o.write(buf,0,r);
		}
		i.close();
		if (o != System.out) o.close();
	}
}
