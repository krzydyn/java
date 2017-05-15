package espdb;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import sys.Env;
import text.tokenize.BasicTokenizer;

public class DataBase {
	Connection connection = null;

	public static class Result {
		private boolean hasResult;
		private Statement statement;
		private int updCnt=-1;
		private Result next;

		public boolean hasMore() {
			return hasResult || updCnt >= 0;
		}
		public boolean moreResults() throws SQLException {
			if (statement == null) return false;
			hasResult = statement.getMoreResults();
			updCnt = statement.getUpdateCount();
			return hasMore();
		}

		public ResultSet getResultSet() throws SQLException {
			return statement.getResultSet();
		}

		public void close() {
			if (statement!=null) {
				try {statement.close();}
				catch (SQLException e) {}
				statement = null;
			}
			hasResult = false;
			updCnt = -1;
		}

		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}
	}

	public DataBase(String dbname) throws SQLException {
		connection = DriverManager.getConnection(dbname);
	}

	public Result query(String q) throws SQLException {
		Result r = new Result();
		r.statement = connection.createStatement();
		r.hasResult = r.statement.execute(q);
		r.updCnt = r.statement.getUpdateCount();
		return r;
	}

	public Result query(String q, Object... args) throws SQLException {
		Result r = new Result();
		PreparedStatement pstmt = connection.prepareStatement(q);
		r.statement = pstmt;
		int i = 1;
		for(Object a : args) {
			pstmt.setObject(i++, a);
		}
		r.hasResult = pstmt.execute();
		return r;
	}

	public Result script(String script) throws SQLException,IOException {
		BasicTokenizer tok = new BasicTokenizer(script);
		tok.setDelimiter(";");
		StringBuilder b = new StringBuilder();
		Result top=null;
		Result pr=null;
		while (tok.next(b)) {
			Result r = query(script);
			if (top==null) pr=top=r;
			else {pr.next=r; pr=r;}
		}
		return top;
	}

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
			if (r.hasResult) {
				ResultSet rs = r.statement.getResultSet();
				printHead(rs, out);
				while (rs.next())
					printRow(rs, out);
				rs.close();
			}
			else {
				out.println("updated rows " + r.updCnt);
			}
			r.moreResults();
		}
		r.close();
	}

	//https://en.wikipedia.org/wiki/Grammatical_conjugation
	static void createTables(DataBase db) throws SQLException {
		Result r;

		r = db.query("CREATE TABLE IF NOT EXISTS word ("
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT"
				+ ",word VARCHAR"
				+ ",UNIQUE(word)"
				+ ")");
		r.close();

		r = db.query("CREATE TABLE IF NOT EXISTS tense ("
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT"
				+ ",name VARCHAR"
				+ ",UNIQUE(name)"
				+ ")");
		r.close();
		r = db.query("CREATE TABLE IF NOT EXISTS person ("
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT"
				+ ",name VARCHAR"
				+ ",gender ENUM('masculino','femenino')"
				+ ",UNIQUE(name)"
				+ ")");
		r.close();
		r = db.query("CREATE TABLE IF NOT EXISTS conjugation ("
				+ " id_word_infinitive INTEGER" //bezokolicznik
				+ ",id_tense INTEGER"  //present,past,future...
				+ ",id_person INTEGER"
				+ ",id_word"
				+ ")");
		r.close();


		r = db.query("CREATE TABLE IF NOT EXISTS sentence ("
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT"
				+ ",sentence TEXT"
				+ ",UNIQUE(sentence)"
				+ ")");
		r.close();
		r = db.query("CREATE TABLE IF NOT EXISTS wordsentence ("
				+ " id_word INTEGER"
				+ ",id_sentence INTEGER"
				+ ",UNIQUE(id_word,id_sentence)"
				+ ")");
		r.close();

	}

	public static void main(String[] args) throws Exception {
		Class.forName("org.sqlite.JDBC");

		Env.remove("res/espdb.db");

		DataBase db = new DataBase("jdbc:sqlite:res/espdb.db");
		createTables(db);

		Result r;

		r = db.query("INSERT INTO word (word)VALUES (?)", "los;");
		print(r,System.out);
		r.close();

		r = db.query("SELECT * FROM word");
		print(r,System.out);
		r.close();

		r = db.query("DROP TABLE word");
	}
}
