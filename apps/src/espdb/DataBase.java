package espdb;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
	Connection connection = null;

	public static class Result {
		private boolean hasResult;
		private Statement statement;
		private int updCnt=-1;

		public boolean hasMore() {
			return hasResult || updCnt >= 0;
		}
		public boolean moreResults() throws SQLException {
			if (statement == null) return false;
			hasResult = statement.getMoreResults();
			updCnt = statement.getUpdateCount();
			return hasMore();
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

	public static void main(String[] args) throws Exception {
		Class.forName("org.sqlite.JDBC");

		DataBase db = new DataBase("jdbc:sqlite:res/espdb.db");

		Result r;

		r = db.query("CREATE TABLE IF NOT EXISTS word ("
				+ " id INTEGER PRIMARY KEY AUTOINCREMENT"
				+ ",word VARCHAR"
				+ ",UNIQUE(word)"
				+ ")");
		print(r,System.out);
		r.close();

		r = db.query("insert into word (word)values (?)", "los;");
		print(r,System.out);
		r.close();

		r = db.query("select * from word");
		print(r,System.out);
		r.close();

		r = db.query("drop table word");
	}


}
