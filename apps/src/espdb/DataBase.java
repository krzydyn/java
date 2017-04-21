package espdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBase {

	public static void main(String[] args) throws Exception {
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		connection = DriverManager.getConnection("jdbc:sqlite:res/espdb.db");
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("select * from person");
	}


}
