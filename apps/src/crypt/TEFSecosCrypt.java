package crypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import crypt.tef.TEF;
import crypt.tef.TEF_Types;
import sys.Log;
import sys.UnitTest;
import text.Text;

public class TEFSecosCrypt extends UnitTest implements TEF_Types {

	static void listProviders() {
		for (Provider provider: Security.getProviders()) {
			  System.out.println(provider.getName());
			  for (String key: provider.stringPropertyNames())
			    System.out.println("\t" + key + "\t" + provider.getProperty(key));
			}
	}

	static class EncryptKey {
		EncryptKey(tef_key_type_e kt, byte[] key) {
			this.kt=kt; this.key=key;
		}
		tef_key_type_e kt;
		byte[] key;
	}

	static class DigestTC {
		DigestTC(TEF.tef_digest_e digest, byte[] datain, byte[] dataout) {
			this.digest = digest;
			this.datain=datain; this.dataout=dataout;
		}
		TEF.tef_digest_e digest;
		byte[] datain;
		byte[] dataout;
	}

	static class KeyGenerateTC {
		KeyGenerateTC(tef_key_type_e kt, int bits) {this.kt=kt; this.bits=bits;}
		tef_key_type_e kt;
		int bits;
	}

	static class EncryptTC {
		EncryptTC(EncryptKey key, TEF.tef_algorithm algo, byte[] datain, byte[] dataout) {
			this.key=key; this.algo=algo;
			this.datain=datain; this.dataout=dataout;
		}
		EncryptKey key;
		TEF.tef_algorithm algo;
		byte[] datain;
		byte[] dataout;
	}

	static byte[] digestMsg = Text.bin("");

	static DigestTC digestTC[] = {
		new DigestTC(tef_digest_e.TEF_CRC16,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_CRC32,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_MD2,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_MD4,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_MD5,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA0,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA1,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA224,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA256,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA384,digestMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA512,digestMsg,null),
	};

	static KeyGenerateTC generateTC[] = {
		new KeyGenerateTC(tef_key_type_e.TEF_DES, 56),
		new KeyGenerateTC(tef_key_type_e.TEF_DES, 112),
		new KeyGenerateTC(tef_key_type_e.TEF_DES, 168),
		new KeyGenerateTC(tef_key_type_e.TEF_DES, 64),
		new KeyGenerateTC(tef_key_type_e.TEF_DES, 128),
		new KeyGenerateTC(tef_key_type_e.TEF_DES, 192),
		new KeyGenerateTC(tef_key_type_e.TEF_AES, 128),
		new KeyGenerateTC(tef_key_type_e.TEF_AES, 192),
		new KeyGenerateTC(tef_key_type_e.TEF_AES, 256),
	};
	static KeyGenerateTC generateTC_exc[] = {
		new KeyGenerateTC(tef_key_type_e.TEF_DES, 80),
		new KeyGenerateTC(tef_key_type_e.TEF_AES, 320),
	};


	final static byte[] ZERO8 = new byte[8];
	final static byte[] ZERO16 = new byte[16];
	final static byte[] ZERO24 = new byte[24];
	final static byte[] ZERO32 = new byte[32];

	static byte[] AES_PLAINTEXT = Text.bin(
			  "6bc1bee22e409f96e93d7e117393172a ae2d8a571e03ac9c9eb76fac45af8e51"
			+ "30c81c46a35ce411e5fbc1191a0a52ef f69f2445df4f9b17ad2b417be66c3710");


	static EncryptKey[] keys_simp = {
		new EncryptKey(tef_key_type_e.TEF_DES, Text.bin("0123456789ABCDEF")),
		new EncryptKey(tef_key_type_e.TEF_DES, Text.bin("0123456789ABCDEF FEDCBA9876543210")),
		new EncryptKey(tef_key_type_e.TEF_DES, Text.bin("0123456789ABCDEF FEDCBA9876543210 1032547698badcfe")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("2b7e151628aed2a6abf7158809cf4f3c")),
	};
	static TEF.tef_algorithm[] algos_simp = {
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
		//.set(tef_algorithm_param_e.TEF_IV, Text.bin("0000000000000000"))
		,
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("00000000000000000000000000000000")),
	};

	//AES key vectors from http://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38a.pdf
	static EncryptKey[] nist_keys = {
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("2b7e151628aed2a6abf7158809cf4f3c")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("603deb1015ca71be2b73aef0857d7781 1f352c073b6108d72d9810a30914dff4")),

		//GCM-AES  from http://csrc.nist.gov/groups/STM/cavp/documents/mac/gcmtestvectors.zip
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("11754cd72aec309bf52f7687212e8957")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("272f16edb81a7abbea887357a58c1917")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("77be63708971c4e240d1cb79e8d77feb")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("2301a2bba4f569826ca3cee802f53a7c")),
	};
	static TEF.tef_algorithm[] nist_algos = {
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("000102030405060708090a0b0c0d0e0f")),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CFB, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("000102030405060708090a0b0c0d0e0f")),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_OFB, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("000102030405060708090a0b0c0d0e0f")),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CTR, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff")),

		//GCM-AES  from http://csrc.nist.gov/groups/STM/cavp/documents/mac/gcmtestvectors.zip
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("3c819d9a9bed087615030b65"))
				.set(tef_algorithm_param_e.TEF_TAGLEN, 128),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("794ec588176c703d3d2a7a07"))
				.set(tef_algorithm_param_e.TEF_TAGLEN, 120),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("e0e00f19fed7ba0136a797f3"))
				.set(tef_algorithm_param_e.TEF_AAD, Text.bin("7a43ec1d9c0a5a78a0b16533a6213cab"))
				.set(tef_algorithm_param_e.TEF_TAGLEN, 128),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV,
						Text.bin("bed48d86e1ff4bff37286a5c428c719130200dce04011edb967f5aaff6a9fb4ad0fcf0dd474e12dcfbcca7fa1ff9bb66b2624aaf1a90f33ed2bab0ee5b465174a722eaa3353bcb354165a1a852468ece974a31429c6e1de7a34e6392f24225d539eaa6b8c1183bfb37627eb16dcd81bba9d65051ff84bd63ee814bea0e1c34d2"))
				.set(tef_algorithm_param_e.TEF_AAD,
						Text.bin("a481e81c70e65eeb94cdf4e25b0a225a4f48b58b12cde148a3a9aa4db0d2988da27591d65827eed39ad6933f267e486c31dc586c36ebaa0c349b9c12ed33221a463737695743cebb456f0705a9895a5aac720f8a53981a231fde"))
				.set(tef_algorithm_param_e.TEF_TAGLEN, 96),

		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_PCBC, tef_padding_mode_e.TEF_PADDING_NONE),
	};

	static EncryptTC encryptTC[] = {
		new EncryptTC(keys_simp[0],algos_simp[0],ZERO8,Text.bin("D5D44FF720683D0D")),
		new EncryptTC(keys_simp[0],algos_simp[1],ZERO8,Text.bin("D5D44FF720683D0D")),
		new EncryptTC(keys_simp[1],algos_simp[0],ZERO8,Text.bin("08D7B4FB629D0885")),
		new EncryptTC(keys_simp[1],algos_simp[1],ZERO8,Text.bin("08D7B4FB629D0885")),
		new EncryptTC(keys_simp[2],algos_simp[0],ZERO8,Text.bin("5802BA5F7916D120")),
		new EncryptTC(keys_simp[2],algos_simp[1],ZERO8,Text.bin("5802BA5F7916D120")),
		new EncryptTC(keys_simp[3],algos_simp[0],ZERO16,Text.bin("7DF76B0C1AB899B33E42F047B91B546F")),
		new EncryptTC(keys_simp[3],algos_simp[2],ZERO16,Text.bin("7DF76B0C1AB899B33E42F047B91B546F")),

		//AES key vectors from http://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38a.pdf
		//EBC-AES
		new EncryptTC(nist_keys[0],nist_algos[0],AES_PLAINTEXT,Text.bin("3ad77bb40d7a3660a89ecaf32466ef97f5d3d58503b9699de785895a96fdbaaf")),
		new EncryptTC(nist_keys[1],nist_algos[0],AES_PLAINTEXT,Text.bin("bd334f1d6e45f25ff712a214571fa5cc974104846d0ad3ad7734ecb3ecee4eef")),
		new EncryptTC(nist_keys[2],nist_algos[0],AES_PLAINTEXT,Text.bin("f3eed1bdb5d2a03c064b5a7e3db181f8591ccb10d410ed26dc5ba74a31362870")),
		//CBC-AES
		new EncryptTC(nist_keys[0],nist_algos[1],AES_PLAINTEXT,Text.bin("7649abac8119b246cee98e9b12e9197d5086cb9b507219ee95db113a917678b2")),
		new EncryptTC(nist_keys[1],nist_algos[1],AES_PLAINTEXT,Text.bin("4f021db243bc633d7178183a9fa071e8b4d9ada9ad7dedf4e5e738763f69145a")),
		new EncryptTC(nist_keys[2],nist_algos[1],AES_PLAINTEXT,Text.bin("f58c4c04d6e5f1ba779eabfb5f7bfbd69cfc4e967edb808d679f777bc6702c7d")),
		//CFB-AES
		new EncryptTC(nist_keys[0],nist_algos[2],AES_PLAINTEXT,Text.bin("3b3fd92eb72dad20333449f8e83cfb4ac8a64537a0b3a93fcde3cdad9f1ce58b")),
		new EncryptTC(nist_keys[1],nist_algos[2],AES_PLAINTEXT,Text.bin("cdc80d6fddf18cab34c25909c99a417467ce7f7f81173621961a2b70171d3d7a")),
		new EncryptTC(nist_keys[2],nist_algos[2],AES_PLAINTEXT,Text.bin("dc7e84bfda79164b7ecd8486985d386039ffed143b28b1c832113c6331e5407b")),
		//OFB-AES
		new EncryptTC(nist_keys[0],nist_algos[3],AES_PLAINTEXT,Text.bin("3b3fd92eb72dad20333449f8e83cfb4a7789508d16918f03f53c52dac54ed825")),
		new EncryptTC(nist_keys[1],nist_algos[3],AES_PLAINTEXT,Text.bin("cdc80d6fddf18cab34c25909c99a4174fcc28b8d4c63837c09e81700c1100401")),
		new EncryptTC(nist_keys[2],nist_algos[3],AES_PLAINTEXT,Text.bin("dc7e84bfda79164b7ecd8486985d38604febdc6740d20b3ac88f6ad82a4fb08d")),
		//CTR-AES
		new EncryptTC(nist_keys[0],nist_algos[4],AES_PLAINTEXT,Text.bin("874d6191b620e3261bef6864990db6ce9806f66b7970fdff8617187bb9fffdff")),
		new EncryptTC(nist_keys[1],nist_algos[4],AES_PLAINTEXT,Text.bin("1abc932417521ca24f2b0459fe7e6e0b090339ec0aa6faefd5ccc2c6f4ce8e94")),
		new EncryptTC(nist_keys[2],nist_algos[4],AES_PLAINTEXT,Text.bin("601ec313775789a5b7a7f504bbf3d228f443e3ca4d62b59aca84e990cacaf5c5")),

		//GCM-AES  from http://csrc.nist.gov/groups/STM/cavp/documents/mac/gcmtestvectors.zip
		new EncryptTC(nist_keys[3],nist_algos[5],"".getBytes(),Text.bin("250327c674aaf477aef2675748cf6971")),
		new EncryptTC(nist_keys[4],nist_algos[6],"".getBytes(),Text.bin("b6e6f197168f5049aeda32dafbdaeb")),
		new EncryptTC(nist_keys[5],nist_algos[7],"".getBytes(),Text.bin("209fcc8d3675ed938e9c7166709dd946")),
		new EncryptTC(nist_keys[6],nist_algos[8],
				Text.bin("6081f9455583c4a35ed9400799e209fb7e75a7887868aa4bb0c9f7b78f67125678e03c618e615bfad03ab077315b7787418f50"),
				Text.bin("18eca8d7ec92b6209c8d3c82d10c876047b470e22b74346ad609f44cc338b38c881103636fd056634907c28e32efb32dcddb23 de01691b9b99851636c7c8d5")),

	};

	static void digest() throws Exception {
		TEF t = new TEF();
		byte[] dig = new byte[64];
		for (DigestTC tc : digestTC) {
			Log.debug("%s on %s", tc.digest.toString(), Text.hex(tc.datain));
			try {
			int l = t.tef_digest(tc.digest, tc.datain, tc.datain.length, dig);
			Log.debug("digest: %s", Text.hex(dig, 0, l));
			if (tc.dataout != null) {
				check(dig,tc.dataout,tc.dataout.length);
			}
			}catch (NoSuchAlgorithmException e) {
				Log.error("%s", e.getMessage());
			}
		}

	}

	static void keygenerate() throws Exception {
		TEF t = new TEF();

		for (KeyGenerateTC tc : generateTC) {
			try {
				t.tef_key_generate(tc.kt, tc.bits);
			}
			catch (Exception e) {
				Log.error(e, "%s/%d: %s", tc.kt, tc.bits);
			}
		}

		for (KeyGenerateTC tc : generateTC_exc) {
			try {
				t.tef_key_generate(tc.kt, tc.bits);
				throw new RuntimeException("Exception not thrown");
			}
			catch (Exception e) {
				Log.info("%s/%d: %s", tc.kt, tc.bits, e.getMessage());
			}
		}
	}

	static void encrypt() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[1000];

		for (EncryptTC tc : gpapi_encryptTC) {
			tef_cipher_token keyid = t.tef_key_import_raw(tc.key.kt, tc.key.key, tc.key.key.length);
			int r=t.tef_encrypt(keyid, tc.algo, tc.datain, tc.datain.length, edata);
			Log.debug("%s/%s  r[%d]=%s", tc.key.kt, tc.algo, r, Text.hex(edata, 0, r));
			if (tc.dataout != null) {
				check(edata,tc.dataout,tc.dataout.length);
			}
		}
	}

	static void decrypt() throws Exception {

	}


	static byte[] TEE_ATTR_DH_BASE_VALUE01 = Text.bin(
			"1c:e0:f6:69:26:46:11:97:ef:45:c4:65:8b:83:b8:ab:\n" +
			"04:a9:22:42:68:50:4d:05:b8:19:83:99:dd:71:37:18:\n" +
			"cc:1f:24:5d:47:6c:cf:61:a2:f9:34:93:f4:1f:55:52:\n" +
			"48:65:57:e6:d4:ca:a8:00:d6:d0:db:3c:bf:5a:95:4b:\n" +
			"20:8a:4e:ba:f7:e6:49:fb:61:24:d8:a2:1e:f2:f2:2b:\n" +
			"aa:ae:29:21:10:19:10:51:46:47:31:b6:cc:3c:93:dc:\n" +
			"6e:80:ba:16:0b:66:64:a5:6c:fa:96:ea:f1:b2:83:39:\n" +
			"8e:b4:61:64:e5:e9:43:84:ee:02:24:e7:1f:03:7c:23");
	static byte[] TEE_ATTR_DH_PRIME_VALUE01 = Text.bin(
			"e0:01:e8:96:7d:b4:93:53:e1:6f:8e:89:22:0c:ce:fc:\n" +
			"5c:5f:12:e3:df:f8:f1:d1:49:90:12:e6:ef:53:e3:1f:\n" +
			"02:ea:cc:5a:dd:f3:37:89:35:c9:5b:21:ea:3d:6f:1c:\n" +
			"d7:ce:63:75:52:ec:38:6c:0e:34:f7:36:ad:95:17:ef:\n" +
			"fe:5e:4d:a7:a8:6a:f9:0e:2c:22:8f:e4:b9:e6:d8:f8:\n" +
			"f0:2d:20:af:78:ab:b6:92:ac:bc:4b:23:fa:f2:c5:cc:\n" +
			"d4:9a:0c:9a:8b:cd:91:ac:0c:55:92:01:e6:c2:fd:1f:\n" +
			"47:c2:cb:2a:88:a8:3c:21:0f:c0:54:db:29:2d:bc:45");
	static byte[] TEE_ATTR_DH_PRIVATE_VALUE_VALUE01 = Text.bin(
			"53:8d:3d:64:27:4a:40:05:\n" +
			"9b:9c:26:e9:13:e6:91:53:\n" +
			"23:7b:55:83 ");
	static byte[] TEE_ATTR_DH_PUBLIC_VALUE_VALUE01 = Text.bin(
			"bb:e9:18:dd:4b:2b:94:1b:10:0e:88:35:28:68:fc:62:\n" +
			"04:38:a6:db:32:a6:9e:ee:6c:6f:45:1c:a3:a6:d5:37:\n" +
			"77:75:5b:c1:37:0a:ce:fe:2b:8f:13:a9:14:2c:5b:44:\n" +
			"15:78:86:30:d6:95:b1:92:20:63:a3:cf:9d:ef:65:61:\n" +
			"27:4d:24:01:e7:a1:45:f2:d8:b9:3a:45:17:f4:19:d0:\n" +
			"5e:f8:cb:35:59:37:9d:04:20:a3:bf:02:ad:fe:a8:60:\n" +
			"b2:c3:ee:85:58:90:f3:b5:57:2b:b4:ef:d7:8f:37:68:\n" +
			"78:7c:71:52:9d:5e:0a:61:4f:09:89:92:39:f7:4b:01 ");
	static byte[] TEE_ATTR_DH_PUBLIC_VALUE_VALUE02 = Text.bin(
			"a3:f5:7d:be:9e:2f:0a:da:a9:4e:4e:6a:f0:e0:71:47:\n" +
			"0e:2e:41:2e:de:73:2a:62:14:c3:7c:26:d4:e9:9a:54:\n" +
			"ba:3d:e7:49:85:95:0e:e9:14:b2:90:22:91:dc:ff:61:\n" +
			"b2:fc:d1:d0:1b:11:14:b6:02:64:2b:26:5d:88:ea:8d:\n" +
			"bb:e2:07:0b:48:fb:01:53:55:1e:59:51:36:f2:f9:d1:\n" +
			"97:fb:66:12:84:5d:ed:b8:9b:2d:3e:2b:8c:eb:2a:72:\n" +
			"40:9d:55:4c:ed:eb:55:02:ff:8c:b0:2e:03:65:3f:41:\n" +
			"b1:ac:a3:30:6b:ff:6d:f4:6d:e6:e1:0f:86:7c:43:64");
	static byte[] TEE_ATTR_SECRET_VALUE_1024_SHARED_SECRET_DH_VALUE01 = Text.bin(
			"4e:6a:cf:fd:7d:14:27:65:eb:f4:c7:12:41:4f:e4:b6:\n" +
			"ab:95:7f:4c:b4:66:b4:66:01:28:9b:b8:20:60:42:82:\n" +
			"72:84:2e:e2:8f:11:3c:d1:1f:39:43:1c:bf:fd:82:32:\n" +
			"54:ce:47:2e:21:05:e4:9b:3d:7f:11:3b:82:50:76:e6:\n" +
			"26:45:85:80:7b:c4:64:54:66:5f:27:c5:e4:e1:a4:bd:\n" +
			"03:47:04:86:32:29:81:fd:c8:94:cc:a1:e2:93:09:87:\n" +
			"c9:2c:15:a3:8b:c4:2e:b3:88:10:e8:67:c4:43:2f:07:\n" +
			"25:9e:c0:0c:db:bb:0f:b9:9e:17:27:c7:06:da:58:dd"
			);

	static byte[] TEE_ATTR_RSA_MODULUS_VALUE01 = Text.bin(
			"f0:1a:95:cd:5f:9f:1c:bc:5c:2e:c8:00:3b:fa:\n" +
			"e0:d5:72:ea:fc:9e:74:e1:02:66:a8:13:3f:0c:e6:\n" +
			"24:cb:1c:a5:df:64:fb:06:d7:13:ce:aa:6c:ee:16:\n" +
			"7b:f8:92:af:c4:5b:46:18:c6:30:b6:04:1c:3a:2e:\n" +
			"d7:ca:b8:b5:00:78:89:a0:69:37:84:59:99:0c:2f:\n" +
			"00:e5:3b:e1:18:e0:b9:2e:77:1d:32:7e:5f:f4:18:\n" +
			"f3:9f:58:c6:83:e2:7a:cb:89:18:c2:09:84:7e:9d:\n" +
			"96:e0:b9:49:75:ef:cf:ff:f0:b6:18:d3:7a:c1:6f:\n" +
			"0c:55:33:be:9d:63:06:d6:9f:c1:a5:e9:bd:b1:b2:\n" +
			"5d:5c:f9:ab:a9:b5:6a:4e:a4:fa:44:32:d6:71:2e:\n" +
			"5f:a6:25:f8:40:24:c4:5b:61:55:1b:ac:a3:0a:11:\n" +
			"8e:65:20:da:2c:0d:df:db:47:6b:61:18:4d:fe:fd:\n" +
			"2a:7e:77:40:44:43:c6:33:6c:e5:1b:8d:80:f9:97:\n" +
			"a2:e4:b9:34:3e:28:94:9f:bd:a8:2b:0a:4d:1a:a8:\n" +
			"06:e5:99:4e:b9:13:45:c8:f6:0f:d0:4d:bf:e7:8f:\n" +
			"ed:ca:8e:f8:8d:87:5f:d4:b4:1a:2c:c9:a7:67:7e:\n" +
			"b2:1b:c1:ce:b6:83:7c:ce:b4:3d:85:c7:53:30:7c:\n" +
			"fe:85");
	static byte[] TEE_ATTR_RSA_PUBLIC_EXPONENT_VALUE01 = Text.bin("010001");
	static byte[] TEE_ATTR_RSA_PRIVATE_EXPONENT_VALUE01 = Text.bin(
			"a5:0d:e1:84:f9:02:ec:42:20:2c:98:98:70:a3:\n" +
			"1a:04:21:a7:a0:59:5d:87:80:9b:09:57:91:b4:50:\n" +
			"51:62:bf:22:d7:db:17:25:b0:9c:91:29:5f:10:9c:\n" +
			"ac:44:48:b2:43:8d:6b:36:84:a7:df:b8:1b:9f:73:\n" +
			"ac:2c:53:a5:39:d9:a2:e2:7e:f2:07:2d:80:a4:7b:\n" +
			"7b:66:1a:2f:b7:66:64:66:a8:c3:8d:7e:8a:7f:c6:\n" +
			"d7:52:e7:38:30:59:74:88:8e:8a:52:79:30:77:c9:\n" +
			"e5:7a:3e:65:5d:89:a9:b7:0b:c6:62:72:9e:a4:72:\n" +
			"ae:4b:b3:f2:89:47:15:e0:5b:45:4d:99:5b:13:6c:\n" +
			"90:be:e5:b5:98:ad:87:99:1a:57:d4:1f:f1:52:71:\n" +
			"5b:51:40:dc:51:35:f6:6c:ae:a3:f9:0f:3a:ed:28:\n" +
			"fc:a5:60:2f:4b:4f:31:ac:48:3e:5b:ba:e4:2b:58:\n" +
			"79:e6:b4:6b:5e:56:0a:b2:db:68:ed:24:d8:5e:6f:\n" +
			"30:59:8d:8c:a3:00:68:f5:42:95:1a:0b:a8:1c:fb:\n" +
			"df:29:81:10:32:02:cc:51:a4:17:14:3e:ef:89:41:\n" +
			"de:f8:2d:64:69:30:e8:8a:ad:96:f6:f4:82:83:9a:\n" +
			"77:e7:de:12:31:f7:15:ec:ce:ed:83:68:88:84:e5:\n" +
			"64:81");

	static byte[] NONCE1_VALUE_AES_GCM = Text.bin("00:8d:49:3b:30:ae:8b:3c:96:96:76:6c:fa"); //len=13
	static byte[] NONCE2_VALUE_AES_GCM = Text.bin("ca:fe:ba:be:fa:ce:db:ad:de:ca:f8:88"); //len=12
	static byte[] AAD1_VALUE = Text.bin("00:01:02:03:04:05:06:07");

	static byte[] TEE_ATTR_DSA_PRIME_768_VALUE01 = Text.bin("f6ad2071e15a4b9c2b7e5326da439dc1474c1ad16f2f85e92cea89fcdc746611cf30ddc85e33f583c19d10bc1ac3932226246fa7b9e0dd2577b5f427594c39faebfc598a32e174cb8a680357f862f20b6e8432a530652f1c2139ae1faf768b83");
	static byte[] TEE_ATTR_DSA_SUBPRIME_160_VALUE01 = Text.bin("8744e4ddc6d019a5eac2b15a15d7e1c7f66335f7");
	static byte[] TEE_ATTR_DSA_BASE_768_VALUE01  = Text.bin("9a0932b38cb2105b9300dcb866c066d9cec643192fcb2834a1239dba28bd09fe01001e0451f9d6351f6e564afbc8f8c39b1059863ebd0985090bd55c828e9fc157ac7da3cfc2892a0ed9b932390582f2971e4a0c483e0622d73166bf62a59f26");
	static byte[] TEE_ATTR_DSA_PRIVATE_VALUE_160_VALUE01 = Text.bin("704a46c6252a95a39b40e0435a691badae52a5c0");
	static byte[] TEE_ATTR_DSA_PUBLIC_VALUE_768_VALUE01 = Text.bin("529ded98a2320985fc84b65a9dc8d4fe41ada6e3593d704f0898c14ec24634ddf5f1db47cc4915fce1e2674d2ecd98d58b598e8ddfaff30e8826f50aab4027b5aab887c19ad96d7e57de5390ad8e5557b41a8019c90d80607179b54eb0ad4d23");

	static byte[] DATA_FOR_CRYPTO1 = Text.bin("00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:\n" +
			"0a:0b:0c:0d:0e:0f:00:01:02:03:04:05:06:07:08:09:\n" +
			"0f:0e:0d:0c:0b:0a:09:08:07:06:05:04:03:02:01:00:\n" +
			"00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:\n" +
			"0a:0b:0c:0d:0e:0f:00:01:02:03:04:05:06:07:08:09:\n" +
			"0f:0e:0d:0c:0b:0a:09:08:07:06:05:04:03:02:01:00");
	static byte[] DATA_FOR_CRYPTO1_PART1 = Arrays.copyOfRange(DATA_FOR_CRYPTO1, 0, 32);
	static byte[] DATA_FOR_CRYPTO1_PART2 = Arrays.copyOfRange(DATA_FOR_CRYPTO1, 32, 64);
	static byte[] DATA_FOR_CRYPTO1_PART3 = Arrays.copyOfRange(DATA_FOR_CRYPTO1, 64, 96);

	static EncryptKey[] gpapi_keys = {
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("60:3d:eb:10:15:ca:71:be:2b:73:ae:f0:85:7d:77:81:" +
					"1f:35:2c:07:3b:61:08:d7:2d:98:10:a3:09:14:df:f4"))
	};
	static TEF.tef_algorithm[] gpapi_algos = {
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE)
			.set(tef_algorithm_param_e.TEF_IV, NONCE2_VALUE_AES_GCM)
			.set(tef_algorithm_param_e.TEF_AAD, AAD1_VALUE)
			.set(tef_algorithm_param_e.TEF_TAGLEN, 104),
	};

	static DigestTC gpapi_digestTC[] = {
		new DigestTC(tef_digest_e.TEF_MD5,DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA1,DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA224,DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA256,DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA384,DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA512,DATA_FOR_CRYPTO1,null),
	};
	static EncryptTC gpapi_encryptTC[] = {
			new EncryptTC(gpapi_keys[0],gpapi_algos[0],DATA_FOR_CRYPTO1,Text.bin("A726EA73EB43D77C9E977070")),
		};


	static void gp_digest() throws Exception {
		TEF t = new TEF();
		byte[] dig = new byte[64];
		for (DigestTC tc : gpapi_digestTC) {
			int l = t.tef_digest(tc.digest, tc.datain, tc.datain.length, dig);
			Log.debug("digest %s[%d]: %s", tc.digest.toString(), l, Text.hex(dig, 0, l));
			if (tc.dataout != null) {
				check(dig,tc.dataout,tc.dataout.length);
			}
		}
	}

	static void gp_dsa() throws Exception {
		//byte[] hash = Text.bin("C1A0104D625B8E378A96130514AA1BEAA7BE2E04");
		DSA dsa = new DSA(
				new BigInteger(1, TEE_ATTR_DSA_PRIME_768_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_SUBPRIME_160_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_BASE_768_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_PRIVATE_VALUE_160_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_PUBLIC_VALUE_768_VALUE01)
				);

		MessageDigest md = MessageDigest.getInstance("SHA1");
		byte[] hash = md.digest(DATA_FOR_CRYPTO1);
		Log.info("bits(hash) = %d", hash.length*8);
		byte[] sign = dsa.signDigest(hash);
		//byte[] sign = Text.bin("39260379165BAF89BE9C53CEDBEC97EDFA111119712B524423552903EEAF668149A7FA184F79F7D9");
		dsa.verifyDigest(sign, hash);

		byte[] p = Text.bin("86F5CA03DCFEB225063FF830A0C769B9DD9D6153AD91D7CE27F787C43278B447E6533B86B18BED6E8A48B784A14C252C5BE0DBF60B86D6385BD2F12FB763ED8873ABFD3F5BA2E0A8C0A59082EAC056935E529DAF7C610467899C77ADEDFC846C881870B7B19B2B58F9BE0521A17002E3BDD6B86685EE90B3D9A1B02B782B1779");
		byte[] q = Text.bin("996F967F6C8E388D9E28D01E205FBA957A5698B1");
		byte[] g = Text.bin("07B0F92546150B62514BB771E2A0C0CE387F03BDA6C56B505209FF25FD3C133D89BBCD97E904E09114D9A7DEFDEADFC9078EA544D2E401AEECC40BB9FBBF78FD87995A10A1C27CB7789B594BA7EFB5C4326A9FE59A070E136DB77175464ADCA417BE5DCE2F40D10A46A3A3943F26AB7FD9C0398FF8C76EE0A56826A8A88F1DBD");
		byte[] y = Text.bin("4B5E43C17DF5692C56B57566CF35C8C3D9A41CC55D01A15B5956B1A2EC669EDE"
                +"8FC45E59C852CD586788468C6B7719FD1B3CFC57A765152529CFB6EEEB7142EE"
                +"2A9B1CD661458E4362EE1C0CA973EBFE6EFC23C99F26E9E185CB98A3D90E46C4"
                +"47C126BFDCA5524692D6945520C71FDA8F668189A09A9C4AA242981AEAAD6BAD");
		byte[] x = Text.bin("58D0FD3469AD7A39708D1B47858B82953B9D3991");

		dsa = new DSA(
				new BigInteger(1, p),
				new BigInteger(1, q),
				new BigInteger(1, g),
				new BigInteger(1, x),
				new BigInteger(1, y)
				);
		md = MessageDigest.getInstance("SHA-224");
		hash = md.digest("DSA SHA1".getBytes());
		Log.info("hash = %s", Text.hex(hash));
		sign = dsa.signDigest(hash);
		Log.info("sign = %s", Text.hex(sign));
		dsa.verifyDigest(sign, hash);
	}

	static void mgf_test() throws Exception {
		byte[] seed = "hello".getBytes();
		byte[] t1 = RSA.mgf(seed, 10, MessageDigest.getInstance("SHA-256"));
		check(t1, Text.bin("DA75447E22F9F99E1BE0"));

		t1 = RSA.mgf(seed, 15, MessageDigest.getInstance("SHA-256"));
		check(t1, Text.bin("DA75447E22F9F99E1BE09A00CF1A07"));

		seed = "foo".getBytes();
		t1 = RSA.mgf(seed, 3, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("1AC907"));
		t1 = RSA.mgf(seed, 5, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("1AC9075CD4"));

		seed = "bar".getBytes();
		t1 = RSA.mgf(seed, 5, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("BC0C655E01"));
		t1 = RSA.mgf(seed, 50, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("BC0C655E016BC2931D85A2E675181ADCEF7F581F76DF2739DA74FAAC41627BE2F7F415C89E983FD0CE80CED9878641CB4876"));
		t1 = RSA.mgf(seed, 50, MessageDigest.getInstance("SHA-256"));
		check(t1, Text.bin("382576A7841021CC28FC4C0948753FB8312090CEA942EA4C4E735D10DC724B155F9F6069F289D61DACA0CB814502EF04EAE1"));
}

	static byte[] empa = Text.bin("017c5c1b 2b369af7 a8cb9de0 133f1d09 56042c69 c0198187 cfa2b307 31fe6e6e 450a2bfc c9574830 1e3e786a 770487ae c9e08e94 0d2d05c7 df840f36 15bce7a4 56c64272 7de409ef cf9c5\n" +
			"8c5 5f76de4c 895370fe ce8c155a 13a6f934 bc4176bb eae90f37 4289242f 317d3d2b 177923f7 0b4a2638 0b1ce215 19db2968 ca0a1e50 d781d4fd 2a89a1e0 19dd509f ad209696 0f6260db b8392e30 4b46e7a3 a1485\n" +
			"149 5ca68291 67492a69 bb86457b 59932cc9 5bbbb0a7 5588ddcf 017d0fc9 e2edfaf1 0bd5ac34 f3b693a6 088a0f2d aa7dd102 8480f458 176c5b74 a83642f2 8383d61d e83b2ed9 f724b8dd 29c16717 b4c1f4e0 6c7c9\n" +
			"a9a bef453ca 6a9f5eac b4548dbc");

	static void rsa_sign() throws Exception {
		RSA rsa = new RSA(
				new BigInteger(1, TEE_ATTR_RSA_PUBLIC_EXPONENT_VALUE01),
				new BigInteger(1, TEE_ATTR_RSA_PRIVATE_EXPONENT_VALUE01),
				new BigInteger(1, TEE_ATTR_RSA_MODULUS_VALUE01),
				false
				);

		byte[] sign;

		MessageDigest md = MessageDigest.getInstance("SHA1");
		byte[] hash = md.digest(DATA_FOR_CRYPTO1);
		int nBits = TEE_ATTR_RSA_MODULUS_VALUE01.length*8;

		byte[] padEMSA_PSS = RSA.padEMSA_PSS(hash, nBits-1, md);
		Log.info("padEMSA_PSS[%d] = %s", padEMSA_PSS.length, Text.hex(padEMSA_PSS));
		check(padEMSA_PSS, empa);

		Log.info("");
		//sign = rsa.sign(padEMSA_PSS);
		//Log.info("sign[%d] = %s\n", sign.length, Text.hex(sign));

		check("unpad", RSA.unpadEMSA_PSS(padEMSA_PSS, hash, nBits-1, md));
	}

	static void dh_test() throws Exception {
		BigInteger prime = new BigInteger(1, TEE_ATTR_DH_PRIME_VALUE01);  // p (prime number)
		BigInteger base = new BigInteger(1, TEE_ATTR_DH_BASE_VALUE01);    // g (generator)
		BigInteger priv = new BigInteger(1, TEE_ATTR_DH_PRIVATE_VALUE_VALUE01);//xA (private exponent of user A)
		BigInteger pub = new BigInteger(1, TEE_ATTR_DH_PUBLIC_VALUE_VALUE02);  //yB (public exponent of user B)

		Log.info("Prime = %s", prime.toString(16));
		Log.info("Base = %s", base.toString(16));
		Log.info("Priv = %s", priv.toString(16));
		Log.info("Pub = %s", pub.toString(16));

		DH dh = new DH();
		dh.setParams(prime, base);
		BigInteger sA = dh.deriveKey(priv, pub);

		Log.info("shared[%d] = %s", (sA.bitLength()+7)/8, sA.toString(16));
	}

	public static void main(String[] args) {
		//CryptX_Provider.register();
		//listProviders();
		//try { generate(); } catch (Exception e) { Log.error(e); }
		//try { encrypt(); } catch (Exception e) { Log.error(e); }
		//try { decrypt(); } catch (Exception e) { Log.error(e); }
		//try { digest(); } catch (Exception e) { Log.error(e); }

		//Log.info("");
		//try { gp_digest(); } catch (Exception e) { Log.error(e); }
		//try { mgf_test(); } catch (Exception e) { Log.error(e); }
		try { gp_dsa(); } catch (Exception e) { Log.error(e); }

		//try { rsa_sign(); } catch (Exception e) { Log.error(e); }
		//try { dh_test(); } catch (Exception e) { Log.error(e); }
	}
}
