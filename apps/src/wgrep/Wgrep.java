package wgrep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sys.Log;
import time.LapTime;

public class Wgrep {
	
	static Pattern regex = null;
	
	static String basePath;
	static Connection conn;
	static List<String> visitedDirs = new ArrayList<String>();
	static long tmPrn;
	
	static LapTime lap=new LapTime("B");
	
	public static void main(String[] args) {
		int i=0;
		
		if (i < args.length) regex=Pattern.compile(args[i++]);
		for (; i < args.length; ++i) {
			String u = args[i];
			conn=null;
			visitedDirs.clear();
			lap.reset(0.0);
			tmPrn=System.currentTimeMillis()+5000;
			try {
				grepUrl(u);
				System.out.printf("recv: %s, dirs=%d\n", lap, visitedDirs.size());
			}
			catch (IOException e) {
				System.err.println(u + e.toString());
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}


	private static void grepUrl(String url) throws IOException {
		//System.err.println("grep URL "+url);
		if (conn==null) {
			basePath = url;
			conn = Jsoup.connect(url);
			conn.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		}
		else {
			conn.url(url);
		}
		Log.debug("Connected %s", url);
		Response resp=conn.execute();
		long t=System.currentTimeMillis();
		lap.update(t, resp.body().length());
		if (tmPrn < t) {
			System.out.printf("recv: %s, dirs=%d\n", lap, visitedDirs.size());
			tmPrn += 5000;
			lap.nextLap();
		}
		
		Document doc = resp.parse();
		Elements links = doc.select("a[href]");
		List<String> dirs=new ArrayList<>();
		for (Element link : links) {
			String f=link.attr("abs:href");
			if (f.endsWith("/")) dirs.add(f);
			else {
				if (regex == null || regex.matcher(f).matches())
					System.out.println(f);
			}
	    }
		
		for (String u : dirs) {
			if (!u.startsWith(basePath)) continue;
			String ru=u.substring(basePath.length());
			if (visitedDirs.contains(ru)) {
				continue;
			}
			visitedDirs.add(ru);
			try {
				grepUrl(u);
			}catch (IOException e) {
				System.err.println(u + e.toString());
			}
		}
	}
}
