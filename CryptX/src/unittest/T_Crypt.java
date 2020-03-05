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
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Key;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.security.Security;
import crypt.AES2;
import crypt.AES3;
import crypt.Base64;
import crypt.CryptX_AES;
import crypt.CryptX_Provider;
import crypt.Encoder64;
import crypt.Prime;
import crypt.RSA;
import crypt.SuperHash;
import sys.Log;
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

	static void encodeUU() throws Exception {
		Encoder64 uue = new Encoder64(Encoder64.Mode.UU64);
		check(uue.encode("Cats".getBytes()), "0V%T<P``");
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

		final byte[] msg = new byte[1024/8/2];
		for (int i=0; i < msg.length; ++i) msg[i]=(byte)(i+1);
		RSA r=new RSA(1024);
		byte[]x = r.encrypt(msg);
		byte[]m = r.decrypt(x);
		check(m, msg, m.length);

	}

	@SuppressWarnings("unchecked")
	private static URL getCodeBase(final Class<?> clazz) {
		URL url = (URL)AccessController.doPrivileged(new PrivilegedAction() {
		                @Override
						public Object run() {
		                    ProtectionDomain pd = clazz.getProtectionDomain();
		                    if (pd != null) {
		                        CodeSource cs = pd.getCodeSource();
		                        if (cs != null) {
		                            return cs.getLocation();
		                        }
		                    }
		                    return null;
		                }
		            });
		return url;
	}

	static void listProviders() {
		CryptX_Provider.register();
		/*for (Provider provider: Security.getProviders()) {
			System.out.println(provider.getName());
			for (String key: provider.stringPropertyNames())
				System.out.println("\t" + key + "\t" + provider.getProperty(key));
		}*/


		CryptX_AES.test();

		//test CryptXProvider
		try {
			Provider p = Security.getProvider("CryptX");
			URL providerURL = getCodeBase(p.getClass());
			Log.debug("providerURL '%s'", providerURL);

			javax.crypto.Cipher cipher;

			cipher = javax.crypto.Cipher.getInstance("AES/ECB/ISO9797_1PADDING",p);
			Log.debug("alg: %s",cipher.getAlgorithm());

			cipher = javax.crypto.Cipher.getInstance("AES/ECB/ISO9797_2PADDING",p);
			Log.debug("alg: %s",cipher.getAlgorithm());

		} catch (Exception e) {
			Log.error(e);
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
			Log.prn("des(k=%s,%s) = [%d] %s", Text.hex(key,0,8),Text.hex(msg), l, Text.hex(out,0,l));
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
			Log.prn("des3(k=%s,%s) = [%d] %s", Text.hex(key,0,16),Text.hex(msg), l, Text.hex(out,0,l));
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
			Log.prn("aes(k=%s,%s) = [%d] %s", Text.hex(key,0,16),Text.hex(msg), l, Text.hex(out,0,l));
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
		check(h.finish(), new byte[]{(byte)0xe5,(byte)0xf2,(byte)0xf9,(byte)0xfc},0);
	}
}
