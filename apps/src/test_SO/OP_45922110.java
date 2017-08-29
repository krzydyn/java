package test_SO;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import crypt.Base64;

public class OP_45922110 {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		String value = "byndyusoft2014";
		Charset utf16 = Charset.forName("UTF-16");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		System.out.println(Base64.encode(md5.digest(value.getBytes(utf16))));
	}

}
