/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package unittest;

import java.math.BigInteger;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.spec.IvParameterSpec;

import crypt.AES2;
import crypt.AES3;
import crypt.Base64;
import crypt.Prime;
import crypt.RSA;
import crypt.SuperHash;
import sys.Log;
import sys.UnitTest;
import text.Text;

public class T_Crypt extends UnitTest {
	static void testPrimes() throws Exception {
		int primes[] = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151};
		check(primes, Prime.PRIMES, primes.length);
	}
	static void encodeBase64() throws Exception {
		check(Base64.encode(new byte[] {'1'}), "MQ==");
		check(Base64.encode(new byte[] {'2','3'}), "MjM=");
		check(Base64.encode(new byte[] {'4','5','6'}), "NDU2");
		check(Base64.encode(new byte[] {'7','8','9','0'}), "Nzg5MA==");
		check(Base64.encode(new byte[] {'7','8','9','0', 'A'}), "Nzg5MEE=");
		check(Base64.encode(new byte[] {'7','8','9','0', 'A', 'B'}), "Nzg5MEFC");
	}
	static void decodeBase64() throws Exception {
		check(Base64.decode("MQ=AA"), new byte[] {'1'});
		check(Base64.decode("MjM"), new byte[] {'2','3'});
		check(Base64.decode("NDU2"), new byte[] {'4','5','6'});
		check(Base64.decode("Nzg5MA"), new byte[] {'7','8','9','0'});
		check(Base64.decode("Nzg5MEE"), new byte[] {'7','8','9','0', 'A'});
		check(Base64.decode("Nzg5MEFC"), new byte[] {'7','8','9','0', 'A', 'B'});
	}
	static void rsa() {
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {new RSA(1024);}
		});
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {new RSA(1024,BigInteger.valueOf(3));}
		});
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {new RSA(1024,BigInteger.valueOf(0x10001));}
		});
	}

	static class CryptoKey {
		CryptoKey(byte[] key, byte[] iv) {
			this.key = key;
			this.iv = iv;
		}
		CryptoKey(String key) {
			this.key = Text.bin(key);
			this.iv = null;
		}
		CryptoKey(String key, String iv) {
			this.key = Text.bin(key);
			this.iv = Text.bin(iv);
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
			Log.raw("max allowed keylen %d", javax.crypto.Cipher.getMaxAllowedKeyLength("AES"));
		} catch (NoSuchAlgorithmException e1) {
		}

		//AES.javax_listProviders();

		byte[] msg = message.getBytes();
		byte[] out = new byte[300];
		byte[] pad = new byte[16];

		int tc=0;
		for (CryptoTest t : tests) {
			try {
				Log.raw("Test[%d]: %s  keyBits=%d", tc, t.algo, t.key.key.length*8);
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
				else if (t.algo.indexOf("NoPadding")>=0) { //truncate (error?)
					ml -= mr;
				}
				else if (t.algo.indexOf("ISO9797_1")>=0) {
					for (int i=0; i<bs; ++i) pad[i]=0;
					pad[0]=(byte)0x80;
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
					Log.debug("final(%d) = %d",ml,l);
				}
				else {
					l=cipher.update(msg, 0, ml, out);
					Log.debug("update(%d) = %d",ml,l);
					l+=cipher.doFinal(pad, 0, pl, out, l);
				}

				Log.raw("\t\tbs=%d %s", cipher.getBlockSize(), Text.hex(out,0,l));
				Log.raw(" = %d, \"%s\"\n", l, Text.hexstr(out,l-8,8));

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
					Log.raw("Deci block = %s", new String(out,0,l));
				}

			}catch (Exception e) {
				Log.error(e);
			}
		}
	}

	static void des_aes() {
		byte[] key=new byte[24];
		byte[] msg = new byte[16];
		byte[] out = new byte[256];

		for (int i=0; i < 8; ++i) {
			byte x = (byte)(0x01+0x22*i);
			key[i] = key[16+i] = x;
			key[15-i] = (byte)(0x10+0x22*i);
		}
		for (int i=0; i < msg.length; ++i) msg[i]=0;

		//DES tests
		//Cipher.DES SupportedPaddings	NOPADDING|PKCS5PADDING|ISO10126PADDING
		try {
			Key ks = new javax.crypto.spec.SecretKeySpec(key,0,8,"DES");
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("DES/ECB/NoPadding");
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, ks);
			int l=cipher.doFinal(msg, 0, 8, out);
			Log.raw("des(k=%s,%s) = [%d] %s", Text.hex(key,0,8),Text.hex(msg), l, Text.hex(out,0,l));
			check(Text.hex(out,0,l), "D5D44FF720683D0D");
		}catch(Exception e) {
			Log.error(e);
		}

		//DES3 tests
		//Cipher.DESede SupportedPaddings	NOPADDING|PKCS5PADDING|ISO10126PADDING
		try {
			Key ks = new javax.crypto.spec.SecretKeySpec(key, "DESede");
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("DESede/ECB/NoPadding");
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, ks);
			int l=cipher.doFinal(msg, 0, 8, out);
			Log.raw("des3(k=%s,%s) = [%d] %s", Text.hex(key,0,16),Text.hex(msg), l, Text.hex(out,0,l));
			check(Text.hex(out,0,l), "08D7B4FB629D0885");
		}catch(Exception e) {
			Log.error(e);
		}

		//AES tests
		//Cipher.AES SupportedPaddings	NOPADDING|PKCS5PADDING|ISO10126PADDING
		try {
			Key ks = new javax.crypto.spec.SecretKeySpec(key,0,16, "AES");
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, ks);
			int l=cipher.doFinal(msg, 0, 16, out);
			Log.raw("aes(k=%s,%s) = [%d] %s", Text.hex(key,0,16),Text.hex(msg), l, Text.hex(out,0,l));
			check(Text.hex(out,0,l), "D5C825A21F04643B43E2DF3278A762F7");
		}catch(Exception e) {
			Log.error(e);
		}
	}

	static void aes2() {
		byte[] key={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		String msg="Test message    ";
		AES2 aes=new AES2();
		aes.setKey(key);
		byte [] orig=msg.getBytes();
		for(int i=0; i<1000; ++i) aes.encrypt(orig);
		byte[] result = aes.encrypt(orig);
		Log.info("encr(%s) = %s", msg, Text.hex(result));
		String rmsg = new String(aes.decrypt(result));
		check(msg, rmsg);
	}
	static void aes3() {
		byte[] key={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		String msg="Test message    ";
		AES3 aes=new AES3(key);

		byte [] orig=msg.getBytes();
		for(int i=0; i<1000; ++i) aes.encrypt(orig);
		byte[] result = aes.encrypt(orig);
		Log.info("encr(%s) = %s", msg, Text.hex(result));
		String rmsg = new String(aes.decrypt(result));
		check(msg, rmsg);
	}

	static void hash() {
		byte[] data=new byte[256];
		SuperHash h=new SuperHash();
		for (int i=0; i < data.length; ++i) {
			data[i] = (byte)(i*i*0x11b);
		}

		h.init(null);
		for (int i=0; i < 5000000; ++i) {
			h.update(data, 0, data.length);
		}
		//Log.raw("hash = %s", Text.join(h.finish(), ""));
		check(h.finish(), new byte[]{(byte)0xe5,(byte)0xf2,(byte)0xf9,(byte)0xfc});
	}
}
