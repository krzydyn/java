package crypt;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.IvParameterSpec;

public class GenSecosCrypt {
	static class Text {
		public static StringBuilder hex(StringBuilder b, byte[] s, int off, int len) {
			b.ensureCapacity(b.length()+2*len);
			for (int i=0; i<len; ++i) {
				b.append(String.format("%02X", s[off+i]&0xff));
			}
			return b;
		}
		public static String hex(byte[] s, int off, int len) {
			StringBuilder b=new StringBuilder(s.length);
			return hex(b, s, off, len).toString();
		}
		public static StringBuilder hexstr(StringBuilder b, byte[] s, int off, int len) {
			for (int i=0; i<len; ++i) {
				b.append(String.format("\\x%02X", s[off+i]&0xff));
			}
			return b;
		}
		public static String hexstr(byte[] s, int off, int len) {
			StringBuilder b=new StringBuilder(s.length);
			return hexstr(b, s, off, len).toString();
		}
	}
	static class Log {
		final public static void prn(String fmt,Object ...args) {
			System.err.printf(fmt+"\n", args);
		}
	}

	static class CryptoKey {
		CryptoKey(byte[] key, byte[] iv) {
			this.key = key;
			this.iv = iv;
		}
		final byte[] key;
		final byte[] iv;
	}
	static class CryptoTest {
		public CryptoTest(String algo, CryptoKey key) {
			this.algo=algo;
			this.key=key;
		}
		final String algo;
		final CryptoKey key;
	}

	static void secos_symmetric() {
		final CryptoKey[] keys = {
				new CryptoKey("12345678".getBytes(),"12345678".getBytes()),
				new CryptoKey("1234567812345678".getBytes(),"1234567812345678".getBytes()),
				new CryptoKey("123456781234567812345670".getBytes(),"12345678".getBytes()),
				new CryptoKey("12345678123456781234567812345678".getBytes(),"1234567812345678".getBytes()),
		};
		final CryptoTest[] tests = {
				new CryptoTest("AES/ECB/NoPadding", keys[1]),
				new CryptoTest("AES/ECB/PKCS5Padding", keys[1]),
				new CryptoTest("AES/ECB/PKCS7Padding", keys[1]), //PKCS7 == PKCS5
				new CryptoTest("AES/ECB/ISO9797_1Padding", keys[1]),
				new CryptoTest("AES/ECB/ISO9797_2Padding", keys[1]),

				new CryptoTest("AES/CBC/NoPadding", keys[1]),
				new CryptoTest("AES/CBC/PKCS5Padding", keys[1]),
				new CryptoTest("AES/CBC/PKCS7Padding", keys[1]), //PKCS7 == PKCS5
				new CryptoTest("AES/CBC/ISO9797_1Padding", keys[1]),
				new CryptoTest("AES/CBC/ISO9797_2Padding", keys[1]),

				new CryptoTest("AES/CTR/NoPadding", keys[1]),
				new CryptoTest("AES/CTR/PKCS5Padding", keys[1]),

				new CryptoTest("AES/ECB/NoPadding", keys[3]),
				new CryptoTest("AES/ECB/PKCS5Padding", keys[3]),
				new CryptoTest("AES/ECB/PKCS7Padding", keys[3]), //PKCS7 == PKCS5
				new CryptoTest("AES/ECB/ISO9797_1Padding", keys[3]),
				new CryptoTest("AES/ECB/ISO9797_2Padding", keys[3]),

				new CryptoTest("AES/CBC/NoPadding", keys[3]),
				new CryptoTest("AES/CBC/PKCS5Padding", keys[3]),
				new CryptoTest("AES/CBC/PKCS7Padding", keys[3]), //PKCS7 == PKCS5
				new CryptoTest("AES/CBC/ISO9797_1Padding", keys[3]),
				new CryptoTest("AES/CBC/ISO9797_2Padding", keys[3]),

				new CryptoTest("AES/CTR/NoPadding", keys[3]), // padding is ignored in CTR
				new CryptoTest("AES/CTR/PKCS5Padding", keys[3]),

				new CryptoTest("DES/ECB/NoPadding", keys[0]),
				new CryptoTest("DES/CBC/NoPadding", keys[0]),
				new CryptoTest("DESede/ECB/NoPadding", keys[2]),
				new CryptoTest("DESede/CBC/NoPadding", keys[2]),

				new CryptoTest("AES/CTS/PKCS5Padding", keys[1]),
				//new CryptoTest("AES/CTS/PKCS5Padding", keys[1]),
		};
		final String message = "111111112222222233333333444444445555555566666666777777778888888899999999"
				+ "0000000011111111222222223333333344444444555555556666666677777777888888880";

		try {
			Log.prn("JAVA max allowed keylen %d", javax.crypto.Cipher.getMaxAllowedKeyLength("AES"));
		} catch (NoSuchAlgorithmException e1) {}

		byte[] msg = message.getBytes();
		byte[] out = new byte[300];
		byte[] pad = new byte[16];

		int tc=0;
		for (CryptoTest t : tests) {
			try {
				Log.prn("Test[%d]: %s  keyBits=%d", tc, t.algo, t.key.key.length*8);
				++tc;
				String algo = t.algo;
				if (t.algo.indexOf("ISO9797")>=0) {
					algo = t.algo.substring(0, t.algo.lastIndexOf('/'))+"/NoPadding";
				}
				else if (t.algo.indexOf("PKCS7")>=0) {
					algo = t.algo.substring(0, t.algo.lastIndexOf('/'))+"/PKCS5Padding";
				}
				String enc = algo.substring(0, algo.indexOf('/'));
				Key ks = new javax.crypto.spec.SecretKeySpec(t.key.key,enc);
				javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(algo);
				if (algo.indexOf("ECB")>=0)
					cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, ks);
				else
					cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, ks, new IvParameterSpec(t.key.iv));

				int bs = t.algo.startsWith("AES") ? 16 : 8;
				int ml = msg.length;
				int mr = ml%bs;
				int pl = 0;
				if (t.algo.indexOf("CTR")>=0) {
					//for (int i=0; i<bs; ++i) pad[i]=0;
					//pl=bs-mr;
				}
				else if (t.algo.indexOf("NoPadding")>=0) { //truncate (should be error)
					if (mr!=0) {
						Log.prn("trunc msg from %d to %d", ml, ml-mr);
						ml -= mr;
					}
				}
				else if (t.algo.indexOf("ISO9797_1")>=0) {
					for (int i=0; i<bs; ++i) pad[i]=0;
					if (mr==0) pl=0;
					else pl=bs-mr;
				}
				else if (t.algo.indexOf("ISO9797_2")>=0) {
					for (int i=0; i<bs; ++i) pad[i]=0;
					pad[0]=(byte)0x80;
					pl=bs-mr;
				}

				int l=0;
				if (pl == 0) {
					l=cipher.doFinal(msg, 0, ml, out);
					Log.prn("final(%d) = %d",ml,l);
				}
				else {
					l=cipher.update(msg, 0, ml, out);
					Log.prn("update(%d) = %d",ml,l);
					l+=cipher.doFinal(pad, 0, pl, out, l);
				}

				Log.prn("bs=%d, %s", cipher.getBlockSize(), Text.hex(out,0,l));
				Log.prn(" = %d, \"%s\"\n", l, Text.hexstr(out,l-8,8));

				/*if (t.algo.equals("AES/ECB/ISO9797_1Padding")) {
					if (t.algo.indexOf("ECB")>=0)
						cipher.init(javax.crypto.Cipher.DECRYPT_MODE, ks);
					else
						cipher.init(javax.crypto.Cipher.DECRYPT_MODE, ks, new IvParameterSpec(t.key.iv));
					byte[] tst=Text.bin("2A6704F4A7585FD0CE673D31B9AC505C");
					l=cipher.doFinal(tst, 0, tst.length, out);
					Log.raw("Deci block = %s", Text.hex(out,0,l));
				}*/

				if (t.algo.equals("AES/CTR/NoPadding")) {
					if (t.algo.indexOf("ECB")>=0)
						cipher.init(javax.crypto.Cipher.DECRYPT_MODE, ks);
					else
						cipher.init(javax.crypto.Cipher.DECRYPT_MODE, ks, new IvParameterSpec(t.key.iv));
					byte[] tst=out;
					l=cipher.doFinal(tst, 0, l, out);
					Log.prn("Deci block = %s", new String(out,0,l));
				}

			}catch (Exception e) {
				Log.prn(e.getMessage());
			}
		}
	}

	public static void main(String[] args) {
		secos_symmetric();
	}
}
