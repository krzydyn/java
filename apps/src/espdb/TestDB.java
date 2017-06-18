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
import espdb.DataBase.Result;

/*
 * Language abbr from ISO 639‑1 (two letters)
 */
public class TestDB {
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

	static void jsoupScrapp(String url) throws Exception {
		Connection conn = Jsoup.connect(url).timeout(5000);
		conn.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		//conn.header("", "");
		Response resp = conn.execute();
		Document doc = resp.parse();
		//int len = resp.body().length();
		Elements elems = doc.getElementsByClass("phr");
		for (int i=0; i < elems.size(); ++i) {
			System.out.println(elems.get(i).text());
		}
		elems = doc.getElementsByClass("defmetas");
		for (int i=0; i < elems.size(); ++i) {
			System.out.println(elems.get(i).text());
		}
	}
	public static void main(String[] args) throws Exception {
		Class.forName("org.sqlite.JDBC");

		Env.remove("res/espdb.db");
		DataBase db = new DataBase("jdbc:sqlite:res/espdb.db");
		db.query("");

		Result r;
		for (String s : scipts) {
			r = db.script(Env.getFileContent("~/www/espdb/sql/" + s));
			r.close();
		}

		r = db.query("INSERT INTO word_es (word) VALUES (?)", "");
		print(r,System.out);
		r.close();

		r = db.query("SELECT * FROM word_es");
		print(r,System.out);
		r.close();

		jsoupScrapp("https://glosbe.com/es/pl/ir");
	}

}
