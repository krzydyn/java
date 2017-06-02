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
import sys.Log;
import text.tokenize.BasicTokenizer;

public class DataBase {
	Connection connection = null;
	public String lastq=null;

	public static class Result {
		private boolean hasResult;
		private Statement statement;
		private int updCnt=-1;
		public Result next;

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
	public DataBase(String dbname, String user, String passwd) throws SQLException {
		connection = DriverManager.getConnection(dbname, user, passwd);
	}

	public Result query(String q) throws SQLException {
		lastq=q;
		Result r = new Result();
		r.statement = connection.createStatement();
		r.hasResult = r.statement.execute(q);
		r.updCnt = r.statement.getUpdateCount();
		return r;
	}

	public Result query(String q, Object... args) throws SQLException {
		lastq=q+"("+args+")";
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
		Result top=null, pr=null;
		try {
			while (tok.next(b)) {
				if (tok.isDelimiter(b.charAt(0))) continue;
				Log.debug("script: '%s'", b.toString());
				Result r = query(b.toString());
				if (top==null) pr=top=r;
				else {pr.next=r; pr=r;}
			}
		}catch(SQLException e) {
			if (lastq != null) Log.error(lastq);
			throw e;
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
			out.println("--------------------");
			r.moreResults();
		}
		r.close();
	}

	//https://en.wikipedia.org/wiki/Grammatical_conjugation

	public static void main(String[] args) throws Exception {
		String[] scipts = {"conjugation.sql","person.sql","tense.sql","word.sql"};
		Class.forName("org.sqlite.JDBC");

		Env.remove("res/espdb.db");
		DataBase db = new DataBase("jdbc:sqlite:res/espdb.db");
		Result r;
		for (String s : scipts) {
			r = db.script(Env.getFileContent("~/www/espdb/sql/" + s));
		}

		r = db.query("INSERT INTO word (word)VALUES (?)", "los;");
		print(r,System.out);
		r.close();

		r = db.query("SELECT * FROM word");
		print(r,System.out);
		r.close();

		r = db.query("DROP TABLE word");
	}
}
