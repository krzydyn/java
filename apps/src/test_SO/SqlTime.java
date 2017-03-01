package test_SO;

import java.util.Date;

public class SqlTime {

	public static void main(String[] args) {
		Date now = new Date();
		java.sql.Date sqldt = new java.sql.Date(now.getTime());
		System.out.println(now.toString());
		System.out.println(sqldt.toString());
	}

}
