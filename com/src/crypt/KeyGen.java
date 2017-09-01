package crypt;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class KeyGen {
	private static final Random random = new Random();
	private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String charset =
			"0123456789" +
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
			"abcdefghijklmnopqrstuvwxyz" +
			"!@#$%&*()_+-=[]|,./?><";

	public static byte[] pbkdf2(String password, byte[] salt, int iterations, int bytes) throws GeneralSecurityException {
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, bytes * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
		return skf.generateSecret(spec).getEncoded();
	}

	public static SecretKey srongKey(String algo, int bytes) throws NoSuchAlgorithmException {
		KeyGenerator kg = KeyGenerator.getInstance(algo);
		kg.init(bytes*8);
		return kg.generateKey();
	}

	public static String password(int size) {
		char[] password = new char[size];
		for (int i = 0; i < size; i++) {
			int position = random.nextInt(charset.length());
			password[i] = charset.charAt(position);
		}
		return new String(password);
	}
}
