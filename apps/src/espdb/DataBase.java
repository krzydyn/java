package espdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import sys.Log;
import text.Text;
import text.tokenize.BasicTokenizer;

public class DataBase {
	private final Object[] emptyArray = {};
	private Connection connection = null;
	private String lastq=null;

	public static class Result {
		private Statement statement;
		private boolean hasResult;
		private int updCnt=-1;
		private int genKey=-1;

		public boolean hasMore() {
			return hasResult || updCnt >= 0;
		}
		public int getUpdateCount() { return updCnt; }
		public int getGenKey() { return genKey; }
		public boolean hasResult() { return hasResult; }
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
			//super.finalize();
		}
	}

	public DataBase(String dbname) throws SQLException {
		connection = DriverManager.getConnection(dbname);
	}
	public DataBase(String dbname, String user, String passwd) throws SQLException {
		connection = DriverManager.getConnection(dbname, user, passwd);
	}

	public String getlastQuery() { return lastq; }

	public Result query(String q) throws SQLException {
		if (q.isEmpty()) return null;
		return query(q,emptyArray);
	}

	public Result query(String q, Object... args) throws SQLException {
		if (q.isEmpty()) return null;
		lastq=args.length>0? q+"("+Text.join(",",args)+")" : q;
		Result r = new Result();
		int opt = Statement.NO_GENERATED_KEYS;
		if (q.toUpperCase().contains("INSERT"))
			opt = Statement.RETURN_GENERATED_KEYS;

		PreparedStatement pstmt = connection.prepareStatement(q, opt);
		r.statement = pstmt;
		int i = 1;
		for(Object a : args) {
			pstmt.setObject(i++, a);
		}
		r.hasResult = pstmt.execute();
		r.updCnt = r.statement.getUpdateCount();

		if (r.updCnt > 0) {
			ResultSet k = r.statement.getGeneratedKeys();
			if (k.next()) {
				r.genKey = k.getInt(1);
			}
		}
		return r;
	}

	public Result script(CharSequence script) throws SQLException,IOException {
		//TODO SQL tokenizer
		BasicTokenizer tok = new BasicTokenizer(script.toString());
		tok.setDelimiter(";");
		StringBuilder b = new StringBuilder();
		Result r = null;
		try {
			while (tok.next(b)) {
				String q = b.toString().trim();
				q = q.replaceAll("--.*\n", "");
				if (q.equals(";")) continue;
				Log.debug("script: '%s'", q);
				if (r != null) r.close();
				r = query(q);
			}
		}catch(SQLException e) {
			if (lastq != null) Log.error(lastq);
			throw e;
		}
		return r;
	}
}
