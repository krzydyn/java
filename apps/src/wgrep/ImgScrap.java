package wgrep;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sys.Log;
import time.LapTime;

/*
 * -sel a[class=fancybox-effects-a]
 * http://fotosa.pl/?pg=impreza&id=72&nr=%&od=5120
 */

public class ImgScrap {
	static class ImgInfo {
		public ImgInfo(String fn, Date dt) {
			this.fn=fn; this.dt=dt;
		}
		String fn;
		Date dt;
	}

	static Connection conn;
	static String sel;

	static String baseUrl = null;
	static LapTime lap=new LapTime("B");
	static long tmPrn;

	static SimpleDateFormat srcfmt = new SimpleDateFormat("HH:mm:ss  yyyy-MM-dd");
	static SimpleDateFormat dstfmt = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	static Date stop;

	static int imgCounter = 0;

    public static void main(String[] args) {
    	int i=0;
		for (; i < args.length; ++i) {
			String a = args[i];
			if (!a.startsWith("-")) break;
			if (a.equals("-sel")) sel=args[++i];
		}
		try {
			stop = srcfmt.parse("12:30:00  2017-09-23");
		} catch (ParseException e1) {}
		for (; i < args.length; ++i) {
			String u = args[i];
			baseUrl = u.substring(0, u.lastIndexOf('/')+1);
			conn=null;
			lap.reset(0.0);
			tmPrn=System.currentTimeMillis()+5000;
			try {
				while (u != null) {
					u = scrapUrl(u);
					System.out.printf("recv: %s, images=%d\n", lap, imgCounter);
					Log.debug("Next url: %s", u);
					//break;
				}
			}
			catch (IOException e) {
				Log.error(e, "%s", u);
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	static String scrapUrl(String url) throws IOException {
		File imgdir = new File("pics");
		if (!imgdir.exists())
			Files.createDirectories(imgdir.toPath());

		//System.err.println("grep URL "+url);
		if (conn==null) {
			conn = Jsoup.connect(url).timeout(5000);
			conn.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		}
		else {
			conn.url(url);
		}
		Log.notice("sel: '%s'",sel);

		File saved = new File("scrap.html");
		Document doc;
		int len=0;
		if (saved.exists()) {
			Log.debug("Loading saved copy from %s", saved.getName());
			doc = Jsoup.parse(saved, "UTF-8", url);
			len = (int)saved.length();
		}
		else {
			Log.debug("Connecting %s", url);
			Response resp=conn.execute();
			//IOText.save(saved, resp.body());
			doc = resp.parse();
			len=resp.body().length();
		}

		lap.update(len);
		List<ImgInfo> images = new ArrayList<>();
		long t = System.currentTimeMillis();
		if (tmPrn < t) {
			System.out.printf("recv: %s, images=%d\n", lap, images.size());
			tmPrn += 5000;
			lap.nextLap();
		}

		Elements elems = doc.select(sel);
		for (Element el : elems) {
		    //System.out.println(el.outerHtml());
		    String img = el.attr("href");
		    if (img.startsWith("pics/") && img.endsWith(".jpg")) {
			    String ti = el.attr("title");
			    ti = ti.substring(0, ti.indexOf(" -"));
			    //System.out.println(ti);
			    try {
					Date dt = srcfmt.parse(ti);
					if (dt.before(stop))
						images.add(new ImgInfo(img, dt));
				} catch (ParseException e) {Log.error(e);}
		    }
		}
		if (images.size() == 0) return null;
		for (ImgInfo info : images) {
			url = baseUrl + info.fn;
			File f = new File(imgdir, dstfmt.format(info.dt) + ".jpg");
			if (f.exists()) {
				System.out.println("already exist: " + dstfmt.format(info.dt));
				continue;
			}
			System.out.println("saving: " + info.fn);
			InputStream is = new URL(url).openStream();
			Files.copy(is, f.toPath());
			is.close();
		}
		imgCounter += images.size();
		images.clear();

		String nextUrl = null;
		elems = doc.select("div[class=col-full] > a"); // select A directly after DIV
		for (Element el : elems) {
			//if (!el.tagName().equals("div")) continue;
			if (el.hasAttr("href"))
				nextUrl = el.attr("href");
		}
		return baseUrl + nextUrl;
	}
}
