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

package wgrep;

import io.IOText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import sys.Log;
import time.LapTime;

public class Wgrep {

	static Pattern regex = null;
	static String sel = null;

	static String basePath;
	static Connection conn;
	static List<String> visitedDirs = new ArrayList<String>();

	static LapTime lap=new LapTime("B");
	static long tmPrn;

	public static void main(String[] args) {
		int i=0;

		for (; i < args.length; ++i) {
			String a = args[i];
			if (!a.startsWith("-")) break;
			if (a.equals("-sel")) sel=args[++i];
			if (a.equals("-re")) regex=Pattern.compile(args[++i]);
		}
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
				Log.error("%s: %s", u, e.toString());
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	//<div class="feed-actual-price"> 35 zł<span class="currency-subscript">PLN</span> </div>
	private static void grepUrl(String url) throws IOException {
		//System.err.println("grep URL "+url);
		if (conn==null) {
			basePath = url;
			conn = Jsoup.connect(url).timeout(5000);
			conn.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		}
		else {
			conn.url(url);
		}
		Log.notice("sel: '%s'",sel);

		File saved = new File("wgrep.html");
		Document doc;
		int len=0;
		if (saved.exists()) {
			doc = Jsoup.parse(saved, "UTF-8", url);
			len = (int)saved.length();
		}
		else {
			Log.debug("Connecting %s", url);
			Response resp=conn.execute();
			IOText io = new IOText(null, new FileOutputStream(saved));
			io.write(resp.body());
			io.close();
			doc = resp.parse();
			len=resp.body().length();
		}

		long t=lap.update(len);
		if (tmPrn < t) {
			System.out.printf("recv: %s, dirs=%d\n", lap, visitedDirs.size());
			tmPrn += 5000;
			lap.nextLap();
		}


		Elements elems = doc.select(sel);
		System.out.println(elems.text());
		/*for (org.jsoup.nodes.Element el : elems) {
			System.out.println(el.ownText());
		}*/

		/*
		List<String> dirs=new ArrayList<String>();
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
	    */
	}
}
