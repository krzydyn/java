package espdb;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import sys.Env;
import sys.Log;
import espdb.DataBase.Result;

/*
 * Language abbr from ISO 639‑1 (two letters)
 */
public class Immobiles {
	static private void printHead(ResultSet rs, PrintStream out) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		out.print("|");
		for (int i=1; i <= cols; ++i) {
			out.print(meta.getColumnLabel(i)+":"+meta.getColumnTypeName(i)+"|");
		}
		out.println();
	}
	static private void printRow(ResultSet rs, PrintStream out) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		out.print("|");
		for (int i=1; i <= cols; ++i) {
			out.print(rs.getString(i)+"|");
		}
		out.println();
	}

	static public void print(Result r, PrintStream out) throws SQLException {
		while(r.hasMore()) {
			if (r.hasResult()) {
				ResultSet rs = r.getResultSet();
				printHead(rs, out);
				while (rs.next())
					printRow(rs, out);
				rs.close();
			}
			else {
				out.println("updated rows " + r.getUpdateCount());
			}
			r.moreResults();
		}
		r.close();
	}

	//https://en.wikipedia.org/wiki/Grammatical_conjugation
	static String[] scipts = {
		"word.sql",
		//"conjugation.sql","person.sql","tense.sql",
		//"word_core.sql", "word_core_rel.sql"
		"word_lang_rel.sql",
		};

	/*
	 * http://www.codingpedia.org/ama/how-to-test-a-rest-api-from-command-line-with-curl/
	 */

	static void jsoupScrapp(String url, String classname) throws Exception {
		Log.debug("Loading  %s\n", url);
		Connection conn = Jsoup.connect(url).timeout(5000);
		conn.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		//conn.header("", "");
		Response resp = conn.execute();
		Document doc = resp.parse();
		//int len = resp.body().length();
		Elements elems = doc.getElementsByClass(classname);
		for (int i=0; i < elems.size(); ++i) {
			System.out.println(elems.get(i).text());
		}
	}
	
	static void otodom() throws Exception {
		//String filt = "search%5Bprivate_business%5D=private&search%5Border%5D=created_at_first%3Adesc";
		//String filt = "search%5Bprivate_business%5D=private&search%5Border%5D=created_at_first%3Adesc&nrAdsPerPage=100";
		String filt = "search%5Bfilter_float_price%3Ato%5D=200000&search%5Bprivate_business%5D=private&search%5Border%5D=filter_float_price_per_m%3Aasc&search%5Bregion_id%5D=7&nrAdsPerPage=100";
		jsoupScrapp("https://www.otodom.pl/sprzedaz/dzialka/mazowieckie/?" + filt, "offer-item");		
	}
	
	static void olx() throws Exception {
		//String filt = "search%5Bfilter_enum_type%5D%5B0%5D=dzialki-budowlane&search%5Bfilter_enum_type%5D%5B1%5D=dzialki-rolne&search%5Bfilter_enum_type%5D%5B2%5D=dzialki-inwestycyjne&search%5Bfilter_enum_type%5D%5B3%5D=dzialki-rolno-budowlane&search%5Border%5D=filter_float_price%3Aasc";
		String filt = "search%5Bfilter_enum_type%5D%5B0%5D=dzialki-budowlane&search%5Bfilter_enum_type%5D%5B1%5D=dzialki-rolne&search%5Bfilter_enum_type%5D%5B2%5D=dzialki-inwestycyjne&search%5Bfilter_enum_type%5D%5B3%5D=dzialki-rolno-budowlane&search%5Border%5D=filter_float_price%3Aasc&search%5Bdist%5D=30";
		jsoupScrapp("https://www.olx.pl/nieruchomosci/dzialki/sprzedaz/mazowieckie/?" + filt, "offer-wrapper");		
	}
	

	public static void main(String[] args) throws Exception {
		System.out.printf("cwd=%s\n", System.getProperty("user.dir"));
		olx();
	}

}
