package gftp;

import git.GitRepo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpDirEntry;
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
	static String user = host;
	static String passwd = "misio2";
	static FtpClient ftp = FtpClient.create();
	static int filesSent;

	public static void main(String[] args) {
		String srcDir=null, dstDir=null;
		if (args.length >= 2) {
			srcDir=args[0];
			dstDir=args[1];
		}
		else {
			srcDir = "~/www/cms/ckeditor";
			dstDir = "/www/cms/ckeditor";
		}

		filesSent=0;
		try {
			ftp.connect(new InetSocketAddress(host, 21));
			ftp.login(user, passwd.toCharArray());
			ftp.setBinaryType();
			//readStream(ftp.list("/"),System.out);

			synchronizeDirs(new File(Env.expandEnv(srcDir)), new File(dstDir));
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

	public static void gitSyncDirs(File src, File dst, String hash) throws Exception {
		if (!src.exists()) return ;
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

	public static void synchronizeDirs(File src, File dst) throws Exception {
		InputStream tmi = new ByteArrayInputStream("\n".getBytes(Env.UTF8_Charset));
		String tmfile = dst.getPath()+"/"+".time";
		sendFile(tmi, ftp.putFileStream(tmfile));
		long tmloc=System.currentTimeMillis();
		long tm = ftp.getLastModified(tmfile).getTime();
		Log.debug("dtm = %d", tmloc-tm);
		ftp.deleteFile(tmfile);
		synDirs(src, dst);
	}
	private static void synDirs(File src, File dst) throws Exception {
		if (!src.exists()) return ;
		GitRepo git = new GitRepo(src.getPath());
		List<File> localFiles = new ArrayList<>();
		if (src.isFile()) {
			++filesSent;
			Log.debug("send file '%s' -> '%s'", src.getPath(), dst.getName());
			sendFile(new FileInputStream(src), ftp.putFileStream(dst.getName()));
			return ;
		}
		else {
			String[] list = git.lstree("--name-only", "HEAD").split("\n");
			for (String fn : list) {
				File f = new File(src.getPath()+"/"+fn);
				localFiles.add(f);
			}
		}

		//Log.debug("local files: \n%s",Text.join(",", localFiles));
		Iterator<FtpDirEntry> it = ftp.listFiles(dst.getPath());
		List<File> filesToAdd = new ArrayList<>();
		List<File> dirsToGo = new ArrayList<>();
		while (it.hasNext()) {
			FtpDirEntry fde = it.next();
			if (fde.getName().equals(".") || fde.getName().equals("..")) continue;
			//Log.debug("file: %s, %s, %s", fde.getName(), fde.getType(), fde.getSize());

			int i = indexOf(localFiles, fde.getName());
			if (i < 0) {
				Log.debug("[NOT EXISTS] %s", fde.getName());
			}
			else {
				File f = localFiles.get(i);
				localFiles.remove(i);
				if (f.isDirectory()) {
					dirsToGo.add(f);
					continue;
				}
				if (f.length() != fde.getSize()) {
					filesToAdd.add(f);
					Log.debug("ADD (%d != %d) %s", f.length(), fde.getSize(), dst.getPath()+"/"+f.getName());
				}
				else if (f.lastModified() > fde.getLastModified().getTime()) {
					filesToAdd.add(f);
					Log.debug("ADD (dtm %d) %s", f.lastModified() - fde.getLastModified().getTime(),dst.getPath()+"/"+f.getName());
				}
				else {
					Log.info("SAME (%d == %d) %s", f.length(), fde.getSize(), dst.getPath()+"/"+f.getName());
				}
			}
		}
		for (File f : filesToAdd) {
			++filesSent;
			String d = dst.getPath()+"/"+f.getName();
			Log.info("send file '%s' -> '%s'", f.getPath(), d);
			//sendFile(new FileInputStream(f), ftp.putFileStream(d));
		}
		for (File f : dirsToGo) {
			if (f.isFile()) {continue;}
			synDirs(f, new File(dst+"/"+f.getName()));
		}
		for (File f : localFiles) {
			if (!f.isFile()) {continue;}
			Log.debug("NEW  %s", dst.getPath()+"/"+f.getName());
			++filesSent;
			//sendFile(new FileInputStream(f), ftp.putFileStream(d));
		}
	}

	private static int indexOf(List<File> list, String n) {
		for (int i=0; i < list.size(); ++i) {
			if (list.get(i).getName().equals(n)) return i;
		}
		return -1;
	}

	private static void sendFile(InputStream i, OutputStream o) throws IOException {
		byte[] buf=new byte[256];
		int r;
		while ((r=i.read(buf))>=0) {
			o.write(buf,0,r);
		}
		i.close();
		o.close();
	}
	private static void readStream(InputStream i, OutputStream o) throws IOException {
		byte[] buf=new byte[256];
		int r;
		while ((r=i.read(buf))>=0) {
			o.write(buf,0,r);
		}
		i.close();
		if (o != System.out) o.close();
	}
}
