package crypt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Mac;

import crypt.tef.TEF;
import crypt.tef.TEF_Types;
import sys.Env;
import sys.Log;
import sys.UnitTest;
import text.Text;

public class GPtests extends UnitTest implements TEF_Types {

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

	static byte[] EmptyMsg = Text.bin("");

	static DigestTC digestTC[] = {
		new DigestTC(tef_digest_e.TEF_CRC16,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_CRC32,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_MD2,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_MD4,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_MD5,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA0,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA1,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA224,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA256,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA384,EmptyMsg,null),
		new DigestTC(tef_digest_e.TEF_SHA512,EmptyMsg,null),
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
	};
	static TEF.tef_algorithm[] algos_simp = {
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
			.set(tef_algorithm_param_e.TEF_IV, Text.bin("0000000000000000")),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
			.set(tef_algorithm_param_e.TEF_IV, Text.bin("00000000000000000000000000000000")),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE),
	};

	//AES key vectors from http://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38a.pdf
	static EncryptKey[] nist_keys = {
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("2b7e151628aed2a6abf7158809cf4f3c")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("603deb1015ca71be2b73aef0857d7781 1f352c073b6108d72d9810a30914dff4")),

		new EncryptKey(tef_key_type_e.TEF_DES, Text.bin("8aa83bf8 cbda1062 0bc1bf19 fbb6cd58 bc313d4a 371ca8b5")),
		new EncryptKey(tef_key_type_e.TEF_DES, Text.bin("4cf15134 a2850dd5 8a3d10ba 80570d38")),

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

		new EncryptTC(nist_keys[0],algos_simp[0],ZERO16,Text.bin("7DF76B0C1AB899B33E42F047B91B546F")),
		new EncryptTC(nist_keys[0],algos_simp[2],ZERO16,Text.bin("7DF76B0C1AB899B33E42F047B91B546F")),

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
		new EncryptTC(nist_keys[5],nist_algos[5],EmptyMsg,Text.bin("250327c674aaf477aef2675748cf6971")),
		new EncryptTC(nist_keys[6],nist_algos[6],EmptyMsg,Text.bin("b6e6f197168f5049aeda32dafbdaeb")),
		new EncryptTC(nist_keys[7],nist_algos[7],EmptyMsg,Text.bin("209fcc8d3675ed938e9c7166709dd946")),
		new EncryptTC(nist_keys[8],nist_algos[8],
				Text.bin("6081f9455583c4a35ed9400799e209fb7e75a7887868aa4bb0c9f7b78f67125678e03c618e615bfad03ab077315b7787418f50"),
				Text.bin("18eca8d7ec92b6209c8d3c82d10c876047b470e22b74346ad609f44cc338b38c881103636fd056634907c28e32efb32dcddb23 de01691b9b99851636c7c8d5")),

	};

	//https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38b.pdf
	static EncryptTC nist_macTC[] = {
		//AES 128
		new EncryptTC(nist_keys[0],algos_simp[3],EmptyMsg,Text.bin("bb1d6929 e9593728 7fa37d12 9b756746")),
		new EncryptTC(nist_keys[0],algos_simp[3],Text.bin("6bc1bee2 2e409f96 e93d7e11 7393172a"),Text.bin("070a16b4 6b4d4144 f79bdd9d d04a287c")),
		new EncryptTC(nist_keys[0],algos_simp[3],Text.bin("6bc1bee2 2e409f96 e93d7e11 7393172a"
			+ "ae2d8a57 1e03ac9c 9eb76fac 45af8e51 30c81c46 a35ce411"), Text.bin("dfa66747 de9ae630 30ca3261 1497c827")),
		new EncryptTC(nist_keys[0],algos_simp[3],Text.bin("6bc1bee2 2e409f96 e93d7e11 7393172a"
			+ "ae2d8a57 1e03ac9c 9eb76fac 45af8e5130c81c46 a35ce411 e5fbc119 1a0a52ef"
			+ "f69f2445 df4f9b17 ad2b417b e66c3710"), Text.bin("51f0bebf 7e3b9d92 fc497417 79363cfe")),

		//AES 192
		new EncryptTC(nist_keys[1],algos_simp[3],EmptyMsg,Text.bin("d17ddf46 adaacde5 31cac483 de7a9367")),
		new EncryptTC(nist_keys[1],algos_simp[3],Text.bin("6bc1bee2 2e409f96 e93d7e11 7393172a"),Text.bin("9e99a7bf 31e71090 0662f65e 617c5184")),
		new EncryptTC(nist_keys[1],algos_simp[3],Text.bin("6bc1bee2 2e409f96 e93d7e11 7393172a ae2d8a57"
				+ " 1e03ac9c 9eb76fac 45af8e51 30c81c46 a35ce411"), Text.bin("8a1de5be 2eb31aad 089a82e6 ee908b0e")),

		//AES 256
		new EncryptTC(nist_keys[2],algos_simp[3],EmptyMsg,Text.bin("028962f6 1b7bf89e fc6b551f 4667d983")),
		//TDES 192
		new EncryptTC(nist_keys[3],algos_simp[3],EmptyMsg,Text.bin("b7a688e1 22ffaf95")),
		//TDES 128
		new EncryptTC(nist_keys[4],algos_simp[3],EmptyMsg,Text.bin("bd2ebf9a 3ba00361")),
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

		for (EncryptTC tc : encryptTC) {
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

	static void nist_mac() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[1000];

		Log.debug("*** nist_mac ***");
		for (EncryptTC tc : nist_macTC) {
			tef_cipher_token keyid = t.tef_key_import_raw(tc.key.kt, tc.key.key, tc.key.key.length);
			Log.debug("%s/%s: data[%d]=%s", tc.key.kt, tc.algo, tc.datain.length, Text.hex(tc.datain, 0, tc.datain.length));
			int r=t.tef_cmac_calc(keyid, tc.algo, tc.datain, tc.datain.length, edata);
			Log.debug("r[%d]=%s", r, Text.hex(edata, 0, r));

			if (tc.dataout != null) {
				check(edata,tc.dataout,tc.dataout.length);
			}
		}
	}


	static class GP {
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

		static byte[] TEE_ATTR_DES_64_VALUE01 = Text.bin("cd:fe:57:b6:b6:2f:ae:6b");
		static byte[] TEE_ATTR_DES3_192_VALUE01 = Text.bin("cd:fe:57:b6:b6:2f:ae:6b:" +
				"04:73:40:f1:02:d6:a4:8c:89:5d:ad:f2:9d:62:ef:25");
		static byte[] TEE_ATTR_AES_128_VALUE01 =  Text.bin("60:3d:eb:10:15:ca:71:be:2b:73:ae:f0:85:7d:77:81");
		static byte[] TEE_ATTR_AES_256_VALUE01 =  Text.bin("60:3d:eb:10:15:ca:71:be:2b:73:ae:f0:85:7d:77:81:"+
				"1f:35:2c:07:3b:61:08:d7:2d:98:10:a3:09:14:df:f4");

		static byte[] NONCE1_VALUE_AES_GCM = Text.bin("00:8d:49:3b:30:ae:8b:3c:96:96:76:6c:fa"); //len=13
		static byte[] NONCE2_VALUE_AES_GCM = Text.bin("ca:fe:ba:be:fa:ce:db:ad:de:ca:f8:88"); //len=12
		static byte[] AAD1_VALUE = Text.bin("00:01:02:03:04:05:06:07");

		static byte[] IV1_VALUE_64bits_DES_DES3 = Text.bin("12:34:56:78:90:ab:cd:ef");
		static byte[] IV2_VALUE_128bits_AES = Text.bin("12:34:56:78:90:ab:cd:ef:12:34:56:78:90:ab:cd:ef");

		static byte[] TEE_ATTR_DSA_PRIME_768_VALUE01 = Text.bin("f6ad2071e15a4b9c2b7e5326da439dc1474c1ad16f2f85e92cea89fcdc746611cf30ddc85e33f583c19d10bc1ac3932226246fa7b9e0dd2577b5f427594c39faebfc598a32e174cb8a680357f862f20b6e8432a530652f1c2139ae1faf768b83");
		static byte[] TEE_ATTR_DSA_SUBPRIME_160_VALUE01 = Text.bin("8744e4ddc6d019a5eac2b15a15d7e1c7f66335f7");
		static byte[] TEE_ATTR_DSA_BASE_768_VALUE01  = Text.bin("9a0932b38cb2105b9300dcb866c066d9cec643192fcb2834a1239dba28bd09fe01001e0451f9d6351f6e564afbc8f8c39b1059863ebd0985090bd55c828e9fc157ac7da3cfc2892a0ed9b932390582f2971e4a0c483e0622d73166bf62a59f26");
		static byte[] TEE_ATTR_DSA_PRIVATE_VALUE_160_VALUE01 = Text.bin("704a46c6252a95a39b40e0435a691badae52a5c0");
		static byte[] TEE_ATTR_DSA_PUBLIC_VALUE_768_VALUE01 = Text.bin("529ded98a2320985fc84b65a9dc8d4fe41ada6e3593d704f0898c14ec24634ddf5f1db47cc4915fce1e2674d2ecd98d58b598e8ddfaff30e8826f50aab4027b5aab887c19ad96d7e57de5390ad8e5557b41a8019c90d80607179b54eb0ad4d23");

		static byte[] TEE_ATTR_DSA_PRIME_2048_VALUE01 = Text.bin(
				"aa:81:5c:9d:b1:c4:d3:d2:77:3c:7d:0d:4d:1d:a7:5e:\n" +
				"cf:c4:a3:9e:97:d5:fa:19:1f:fe:c8:b1:49:0a:29:0c:\n" +
				"e3:35:e5:ce:87:ea:62:0a:8a:17:de:0b:b6:47:14:e2:\n" +
				"ec:84:0b:f0:0e:6e:bd:b4:ff:b4:e3:24:ca:07:c3:c8:\n" +
				"71:73:09:af:14:10:36:2a:77:2c:9a:dd:83:8b:2b:0c:\n" +
				"ae:1e:90:ab:44:8a:da:bd:ac:d2:e5:df:59:c4:18:7a:\n" +
				"32:a2:37:19:d6:c5:7e:94:00:88:53:83:bf:8f:06:6f:\n" +
				"23:b9:41:92:0d:54:c3:5b:4f:7c:c5:04:4f:3b:40:f1:\n" +
				"70:46:95:63:07:b7:48:e8:40:73:28:44:d0:0a:9c:e6:\n" +
				"ec:57:14:29:3b:62:65:14:7f:15:c6:7f:4b:e3:8b:08:\n" +
				"2b:55:fd:ea:db:61:24:68:9f:b7:6f:9d:25:cc:28:b8:\n" +
				"ea:a9:8b:56:2d:5c:10:11:e0:dc:f9:b3:99:23:24:0d:\n" +
				"33:2d:89:dc:96:03:b7:bd:dd:0c:70:b8:3c:aa:29:05:\n" +
				"63:1b:1c:83:ca:bb:ae:6c:0c:0c:2e:fe:8f:58:13:1e:\n" +
				"d8:35:1b:f9:3e:87:5f:6a:73:a9:3c:ba:d4:70:14:1a:\n" +
				"26:87:fb:ac:f2:d7:1c:8d:de:e9:71:ad:66:07:29:ad");
		static byte[] TEE_ATTR_DSA_SUBPRIME_224_VALUE01 = Text.bin(
				"ea:34:7e:90:be:7c:28:75:d1:fe:1d:b6:22:b4:\n" +
				"76:38:37:c5:e2:7a:60:37:31:03:48:c1:aa:11");
		static byte[] TEE_ATTR_DSA_BASE_2048_VALUE01 = Text.bin(
				"20:42:09:4c:cb:c8:b8:72:3f:c9:28:c1:2f:da:67:1b:\n" +
				"83:29:5e:99:c7:43:57:6f:44:50:4b:e1:18:63:23:31:\n" +
				"9b:50:02:d2:4f:17:3d:f9:09:ea:24:1d:6e:a5:28:99:\n" +
				"04:ee:46:36:20:4b:2f:be:94:b0:68:fe:09:3f:79:62:\n" +
				"57:95:49:55:1d:3a:f2:19:ad:8e:d1:99:39:ef:f8:6b:\n" +
				"ce:c8:34:de:2f:2f:78:59:6e:89:e7:cb:52:c5:24:e1:\n" +
				"77:09:8a:56:c2:32:eb:1f:56:3a:a8:4b:c6:b0:26:de:\n" +
				"ee:6f:f5:1c:b4:41:e0:80:f2:da:fa:ea:1c:ed:86:42:\n" +
				"7d:1c:34:6b:e5:5c:66:80:3d:4b:76:d1:33:cd:44:5b:\n" +
				"4c:34:82:fa:41:50:23:46:3c:9b:f3:0f:2f:78:42:23:\n" +
				"e2:60:57:d3:aa:0d:7f:bb:66:06:30:c5:2e:49:d4:a0:\n" +
				"32:5c:73:89:e0:72:aa:34:9f:13:c9:66:e1:59:75:2f:\n" +
				"bb:71:e9:33:68:90:f9:32:43:fa:6e:72:d2:99:36:5e:\n" +
				"e5:b3:fe:26:6e:bf:11:10:56:8f:ee:44:25:c8:47:b5:\n" +
				"02:10:bd:48:4b:97:43:1a:42:85:6a:dc:a3:e7:d1:a9:\n" +
				"c9:c6:75:c7:e2:66:91:83:20:dd:5a:78:a4:8c:48:a9");
		static byte[] TEE_ATTR_DSA_PRIVATE_VALUE_224_VALUE01 = Text.bin(
				"7b:48:90:21:57:8e:79:e7:bd:3e:e7:ab:45:6f:\n" +
				"65:9f:3d:c0:7c:88:f5:c9:a3:9e:4f:8c:ee:81");
		static byte[] TEE_ATTR_DSA_PUBLIC_VALUE_2048_VALUE01 = Text.bin(
				"1a:e1:0c:78:6a:d0:90:2c:5c:68:5d:ae:5c:71:21:41:\n" +
				"8a:37:7b:88:8b:5f:2f:2b:c7:66:23:57:0f:d6:2b:cb:\n" +
				"19:0b:47:1a:d5:35:9c:5f:06:2f:88:19:28:9e:95:6d:\n" +
				"8a:a6:f9:0d:1f:8c:f1:ee:72:d3:a1:bd:fd:56:c4:78:\n" +
				"dc:29:a1:9c:45:69:b5:a6:0e:3a:8f:34:f6:06:56:ea:\n" +
				"c5:b2:5d:de:55:14:a5:c6:7b:67:54:23:20:4f:6c:ca:\n" +
				"f0:99:06:17:cc:73:55:b9:d3:ed:86:89:78:a2:52:02:\n" +
				"0a:76:9e:d5:9a:6e:da:a6:ef:e3:37:7e:ef:45:f3:f6:\n" +
				"f3:e6:41:79:cc:7d:b8:b1:43:fb:83:5c:5d:71:bf:cf:\n" +
				"a1:e2:a9:04:9b:cc:f7:fe:9a:b5:75:46:22:0f:e3:f4:\n" +
				"b7:52:1c:86:17:39:d1:38:50:7e:81:a4:6a:69:93:60:\n" +
				"54:41:dc:b9:0d:6e:e4:af:bc:42:ca:be:90:a2:54:44:\n" +
				"49:68:10:9d:7e:dd:96:94:a0:23:23:9f:1d:56:17:5d:\n" +
				"d1:fa:c1:15:91:5e:24:fa:b5:63:f4:fc:3f:26:9b:ed:\n" +
				"2f:30:08:32:d1:12:59:64:85:a7:11:41:7a:a7:3b:b4:\n" +
				"ac:72:a6:51:a1:fa:5b:ae:d3:63:6c:72:0d:39:70:08");
		static byte[] TEE_ATTR_DSA_PRIME_3072_VALUE01 = Text.bin(
				"c7:b8:6d:70:44:21:8e:36:74:53:d2:10:e7:64:33:e4:e2:7a:98:3d:b1:c5:60:bb:\n" +
				"97:55:a8:fb:7d:81:99:12:c5:6c:fe:00:2a:b1:ff:3f:72:16:5b:94:3c:0b:28:ed:\n" +
				"46:03:9a:07:de:50:7d:7a:29:f7:38:60:3d:ec:d1:27:03:80:a4:1f:97:1f:25:92:\n" +
				"66:1a:64:ba:2f:35:1d:9a:69:e5:1a:88:8a:05:15:6b:7f:e1:56:3c:4b:77:ee:93:\n" +
				"a4:49:49:13:84:38:a2:ab:8b:dc:fc:49:b4:e7:8d:1c:de:76:6e:54:98:47:60:05:\n" +
				"7d:76:cd:74:0c:94:a4:dd:25:a4:6a:a7:7b:18:e9:d7:07:d6:73:84:97:d4:ea:c3:\n" +
				"64:f4:79:2d:97:66:a1:6a:0e:23:48:07:e9:6b:8c:64:d4:04:bb:db:87:6e:39:b5:\n" +
				"79:9e:f5:3f:e6:cb:9b:ab:62:ef:19:fd:cc:2b:dd:90:5b:ed:a1:3b:9e:f7:ac:35:\n" +
				"f1:f5:57:cb:0d:c4:58:c0:19:e2:bc:19:a9:f5:df:c1:e4:ec:a9:e6:d4:66:56:41:\n" +
				"24:30:4a:31:f0:38:60:5a:3e:34:2d:a0:1b:e1:c2:b5:45:61:0e:dd:2c:13:97:a3:\n" +
				"c8:39:65:88:c6:32:9e:fe:b4:e1:65:af:5b:36:8a:39:a8:8e:48:88:e3:9f:40:bb:\n" +
				"3d:e4:eb:14:16:67:2f:99:9f:ea:d3:7a:ef:1c:a9:64:3f:f3:2c:db:c0:fc:eb:e6:\n" +
				"28:d7:e4:6d:28:1a:98:9d:43:dd:21:43:21:51:af:68:be:3f:6d:56:ac:fb:db:6c:\n" +
				"97:d8:7f:cb:5e:62:91:bf:8b:4e:e1:27:5a:e0:eb:43:83:cc:75:39:03:c8:d2:9f:\n" +
				"4a:db:6a:54:7e:40:5d:ec:df:f2:88:c5:f6:c7:aa:30:dc:b1:2f:84:d3:92:49:3a:\n" +
				"70:93:33:17:c0:f5:e6:55:26:01:fa:e1:8f:17:e6:e5:bb:6b:f3:96:d3:2d:8a:b9 ");
		static byte[] TEE_ATTR_DSA_SUBPRIME_256_VALUE01 = Text.bin(
				" 87:6f:a0:9e:1d:c6:2b:23:6c:e1:c3:15:5b:a4:8b:0c:\n" +
				"cf:da:29:f3:ac:5a:97:f7:ff:a1:bd:87:b6:8d:2a:4b ");
		static byte[] TEE_ATTR_DSA_BASE_3072_VALUE01 = Text.bin(
				" 11:0a:fe:bb:12:c7:f8:62:b6:de:03:d4:7f:db:c3:32:6e:0d:4d:31:b1:2a:8c:a9:\n" +
				"5b:2d:ee:21:23:bc:c6:67:d4:f7:2c:1e:72:09:76:7d:27:21:f9:5f:bd:9a:4d:03:\n" +
				"23:6d:54:17:4f:bf:af:f2:c4:ff:7d:ea:e4:73:8b:20:d9:f3:7b:f0:a1:13:4c:28:\n" +
				"8b:42:0a:f0:b5:79:2e:47:a9:25:13:c0:41:3f:34:6a:4e:db:ab:2c:45:bd:ca:13:\n" +
				"f5:34:1c:2b:55:b8:ba:54:93:2b:92:17:b5:a8:59:e5:53:f1:4b:b8:c1:20:fb:b9:\n" +
				"d9:99:09:df:f5:ea:68:e1:4b:37:99:64:fd:3f:38:61:e5:ba:5c:c9:70:c4:a1:80:\n" +
				"ee:f5:44:28:70:39:61:02:1e:7b:d6:8c:b6:37:92:7b:8c:be:e6:80:5f:a2:72:85:\n" +
				"bf:ee:4d:1e:f7:0e:02:c1:a1:8a:7c:d7:8b:ef:1d:d9:cd:ad:45:dd:e9:cd:69:07:\n" +
				"55:05:0f:c4:66:29:37:ee:1d:6f:4d:b1:28:07:cc:c9:5b:c4:35:f1:1b:71:e7:08:\n" +
				"60:48:b1:da:b5:91:3c:60:55:01:2d:e8:2e:43:a4:e5:0c:f9:3f:ef:f5:dc:ab:81:\n" +
				"4a:bc:22:4c:5e:00:25:bd:86:8c:3f:c5:92:04:1b:ba:04:74:7c:10:af:51:3f:c3:\n" +
				"6e:4d:91:c6:3e:e5:25:34:22:cf:40:63:39:8d:77:c5:2f:cb:01:14:27:cb:fc:fa:\n" +
				"67:b1:b2:c2:d1:aa:4a:3d:a7:26:45:cb:1c:76:70:36:05:4e:2f:31:f8:86:65:a5:\n" +
				"44:61:c8:85:fb:32:19:d5:ad:87:48:a0:11:58:f6:c7:c0:df:5a:8c:90:8b:a8:c3:\n" +
				"e5:36:82:24:28:88:6c:7b:50:0b:bc:15:b4:9d:f7:46:b9:de:5a:78:fe:3b:4f:69:\n" +
				"91:d0:11:0c:3c:bf:f4:58:03:9d:c3:62:61:cf:46:af:4b:c2:51:53:68:f4:ab:b7 ");
		static byte[] TEE_ATTR_DSA_PRIVATE_VALUE_256_VALUE01 = Text.bin(
				"34:70:83:20:55:da:de:94:e1:4c:d8:77:71:71:d1:8e:\n" +
				"5d:06:f6:6a:ef:f4:c6:14:71:e4:eb:a7:4e:e5:61:64");
		static byte[] TEE_ATTR_DSA_PUBLIC_VALUE_3072_VALUE01 = Text.bin(
				"45:6a:10:5c:71:35:66:23:48:38:bc:07:0b:8a:75:1a:0b:57:76:7c:b7:5e:99:11:\n" +
				"4a:1a:46:64:1e:11:da:1f:a9:f2:29:14:d8:08:ad:71:48:61:2c:1e:a5:5d:25:30:\n" +
				"17:81:e9:ae:0c:9a:e3:6a:69:d8:7b:a0:39:ec:7c:d8:64:c3:ad:09:48:73:e6:e5:\n" +
				"67:09:fd:10:d9:66:85:3d:61:1b:1c:ff:15:d3:7f:de:e4:24:50:6c:18:4d:62:c7:\n" +
				"03:33:58:be:78:c2:25:09:43:b6:f6:d0:43:d6:3b:31:7d:e5:6e:5a:d8:d1:fd:97:\n" +
				"dd:35:5a:be:96:45:2f:8e:43:54:85:fb:3b:90:7b:51:90:0a:a3:f2:44:18:df:50:\n" +
				"b4:fc:da:fb:f6:13:75:48:c3:93:73:b8:bc:4b:a3:da:bb:47:46:eb:d1:7b:87:fc:\n" +
				"d6:a2:f1:97:c1:07:b1:8e:c5:b4:65:e6:e4:cb:43:0d:9c:0c:e7:8d:a5:98:84:41:\n" +
				"05:4a:37:07:92:b7:30:da:9a:ba:41:a3:16:9a:f2:61:76:f7:4e:6f:7c:0c:9c:9b:\n" +
				"55:b6:2b:be:7c:e3:8d:46:95:d4:81:57:e6:60:c2:ac:b6:3f:48:2f:55:41:81:50:\n" +
				"e5:fe:e4:3a:ce:84:c5:40:c3:ba:76:62:ae:80:83:5c:1a:2d:51:89:0e:a9:6b:a2:\n" +
				"06:42:7c:41:ef:8c:38:aa:07:d2:a3:65:e7:e5:83:80:d8:f4:78:2e:22:ac:21:01:\n" +
				"af:73:2e:e2:27:58:33:7b:25:36:37:83:8e:16:f5:0f:56:d3:13:d0:79:81:88:0d:\n" +
				"68:55:57:f7:d7:9a:6d:b8:23:c6:1f:1b:b3:db:c5:d5:04:21:a4:84:3a:6f:29:69:\n" +
				"0e:78:aa:0f:0c:ff:30:42:31:81:8b:81:fc:4a:24:3f:c0:0f:09:a5:4c:46:6d:6a:\n" +
				"8c:73:d3:2a:55:e1:ab:d5:ec:8b:4e:1a:fa:32:a7:9b:01:df:85:a8:1f:3f:5c:fe ");


		static byte[] DATA_FOR_CRYPTO1 = Text.bin("00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:\n" +
				"0a:0b:0c:0d:0e:0f:00:01:02:03:04:05:06:07:08:09:\n" +
				"0f:0e:0d:0c:0b:0a:09:08:07:06:05:04:03:02:01:00:\n" +
				"00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:\n" +
				"0a:0b:0c:0d:0e:0f:00:01:02:03:04:05:06:07:08:09:\n" +
				"0f:0e:0d:0c:0b:0a:09:08:07:06:05:04:03:02:01:00");
		static byte[] DATA_FOR_CRYPTO1_PART1 = Arrays.copyOfRange(DATA_FOR_CRYPTO1, 0, 32);
		static byte[] DATA_FOR_CRYPTO1_PART2 = Arrays.copyOfRange(DATA_FOR_CRYPTO1, 32, 64);
		static byte[] DATA_FOR_CRYPTO1_PART3 = Arrays.copyOfRange(DATA_FOR_CRYPTO1, 64, 96);

		static EncryptKey key_AES256 = new EncryptKey(tef_key_type_e.TEF_AES, TEE_ATTR_AES_256_VALUE01);
		static EncryptKey key_AES192 = new EncryptKey(tef_key_type_e.TEF_DES, TEE_ATTR_DES3_192_VALUE01);
		static EncryptKey key_AES128 = new EncryptKey(tef_key_type_e.TEF_AES, TEE_ATTR_AES_128_VALUE01);
		static EncryptKey key_DES64 = new EncryptKey(tef_key_type_e.TEF_DES, TEE_ATTR_DES_64_VALUE01);
		//static EncryptKey key_DES128 = new EncryptKey(tef_key_type_e.TEF_DES, TEE_ATTR_DES3_128_VALUE01);
		static EncryptKey key_DES192 = new EncryptKey(tef_key_type_e.TEF_DES, TEE_ATTR_DES3_192_VALUE01);

		static TEF.tef_algorithm alg_AES_GCM = new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE)
											.set(tef_algorithm_param_e.TEF_IV, NONCE2_VALUE_AES_GCM)
											.set(tef_algorithm_param_e.TEF_AAD, AAD1_VALUE)
											.set(tef_algorithm_param_e.TEF_TAGLEN, 104);
		static TEF.tef_algorithm alg_ECB = new TEF.tef_algorithm(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE);
		static TEF.tef_algorithm alg_CBC_IV1 = new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
											.set(tef_algorithm_param_e.TEF_IV, IV1_VALUE_64bits_DES_DES3);
		static TEF.tef_algorithm alg_CBC_IV2 = new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
											.set(tef_algorithm_param_e.TEF_IV, IV2_VALUE_128bits_AES);

		static TEF.tef_algorithm alg_CBC = new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE);
		static TEF.tef_algorithm alg_CBC_PKCS5 = new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_PKCS5);

		static TEF.tef_algorithm alg_CTR = new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CTR, tef_padding_mode_e.TEF_PADDING_NONE)
											.set(tef_algorithm_param_e.TEF_IV, IV2_VALUE_128bits_AES);;
	}

	static DigestTC gpapi_digestTC[] = {
		new DigestTC(tef_digest_e.TEF_MD5,GP.DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA1,GP.DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA224,GP.DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA256,GP.DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA384,GP.DATA_FOR_CRYPTO1,null),
		new DigestTC(tef_digest_e.TEF_SHA512,GP.DATA_FOR_CRYPTO1,null),
	};
	static EncryptTC gpapi_encryptTC[] = {
		new EncryptTC(GP.key_AES256,GP.alg_CTR,GP.DATA_FOR_CRYPTO1,null),//AES256,CTR
		new EncryptTC(GP.key_DES192,GP.alg_ECB,GP.DATA_FOR_CRYPTO1,null),//DES3,ECB
		new EncryptTC(GP.key_DES192,GP.alg_CBC_IV1,GP.DATA_FOR_CRYPTO1,null),//DES3,CBC,IV1
		new EncryptTC(GP.key_DES64,GP.alg_ECB,GP.DATA_FOR_CRYPTO1,null),//DES,ECB
		new EncryptTC(GP.key_DES64,GP.alg_CBC_IV1,GP.DATA_FOR_CRYPTO1,null),//DES,CBC,IV1
	};
	static EncryptTC gpapi_macTC[] = {
		new EncryptTC(GP.key_AES256,GP.alg_CBC,GP.DATA_FOR_CRYPTO1,Text.bin("2B10E34C1090FCA4D41FB65D73BD1251")),//AES256,NoPAdding
		new EncryptTC(GP.key_AES256,GP.alg_CBC_PKCS5,GP.DATA_FOR_CRYPTO1,Text.bin("1871FD46A975370EFBA956A409428972")),//AES256,PKCS5
		new EncryptTC(GP.key_AES128,GP.alg_CBC,GP.DATA_FOR_CRYPTO1,Text.bin("A549438FB8E7288879F0CB547368250F")),//AES128,NoPAdding
		new EncryptTC(GP.key_AES128,GP.alg_CBC_PKCS5,GP.DATA_FOR_CRYPTO1,Text.bin("7F604811FC65F95353939C7C8AD088CA")),//AES128,PKCS5
		new EncryptTC(GP.key_DES192,GP.alg_CBC,GP.DATA_FOR_CRYPTO1,Text.bin("47C55394872C9590")), //DES3,NoPadding
		new EncryptTC(GP.key_DES192,GP.alg_CBC_PKCS5,GP.DATA_FOR_CRYPTO1,Text.bin("60FA6706F98CDFB5")), //DES3,PKCS5
		new EncryptTC(GP.key_DES64,GP.alg_CBC,GP.DATA_FOR_CRYPTO1,Text.bin("896776ABB29C7A61")), //DES,NoPadding
		new EncryptTC(GP.key_DES64,GP.alg_CBC_PKCS5,GP.DATA_FOR_CRYPTO1,Text.bin("EFF100951D266923")), //DES,PKCS5
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

	static void gp_encrypt() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[1000];

		Log.debug("*** gp_encrypt ***");
		for (EncryptTC tc : gpapi_encryptTC) {
			tef_cipher_token keyid = t.tef_key_import_raw(tc.key.kt, tc.key.key, tc.key.key.length);
			int r=t.tef_encrypt(keyid, tc.algo, tc.datain, tc.datain.length, edata);
			Log.debug("%s/%s  r[%d]=%s", tc.key.kt, tc.algo, r, Text.hex(edata, 0, r));
			if (tc.dataout != null) {
				check(edata,tc.dataout,tc.dataout.length);
			}
		}
	}

	static void java_mac() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[1000];
		//HmacMD5, HmacSHA1, HMACSHA256
		javax.crypto.Mac mac = Mac.getInstance("HmacSHA1");

		Log.debug("*** java_mac ***");
		for (EncryptTC tc : gpapi_macTC) {
			tef_cipher_token keyid = t.tef_key_import_raw(tc.key.kt, tc.key.key, tc.key.key.length);
			mac.init(keyid.getKey());
			edata = mac.doFinal(GP.DATA_FOR_CRYPTO1);
			int r = edata.length;
			Log.debug("r[%d]=%s", r, Text.hex(edata, 0, r));
		}

		//mac.update(DATA_FOR_CRYPTO1);
		//mac.doFinal(edata,  0);
		edata = mac.doFinal(GP.DATA_FOR_CRYPTO1);
		int r = edata.length;
		Log.debug("r[%d]=%s", r, Text.hex(edata, 0, r));
	}

	static void gp_mac() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[1000];

		Log.debug("*** gp_mac ***");
		for (EncryptTC tc : gpapi_macTC) {
			tef_cipher_token keyid = t.tef_key_import_raw(tc.key.kt, tc.key.key, tc.key.key.length);
			Log.debug("%s/%s: data[%d]=%s", tc.key.kt, tc.algo, tc.datain.length, Text.hex(tc.datain, 0, tc.datain.length));
			int r=t.tef_mac_calc(keyid, tc.algo, tc.datain, tc.datain.length, edata);
			Log.debug("r[%d]=%s", r, Text.hex(edata, 0, r));

			if (tc.dataout != null) {
				check(edata,tc.dataout,tc.dataout.length);
			}
		}
	}

	static void gp_cmac() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[100];
		byte[] data = new byte[100];

		Log.debug("*** gp_cmac ***");
		for (EncryptTC tc : gpapi_macTC) {
			tef_cipher_token keyid = t.tef_key_import_raw(tc.key.kt, tc.key.key, tc.key.key.length);
			Log.debug("%s/%s: data[%d]=%s", tc.key.kt, tc.algo, tc.datain.length, Text.hex(tc.datain, 0, tc.datain.length));
			int r=t.tef_cmac_calc(keyid, tc.algo, tc.datain, tc.datain.length, edata);
			Log.debug("MAC[%d] = %s", r, Text.hex(edata, 0, r));
			r = t.tef_decrypt(keyid, new tef_algorithm(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE), edata, r, data);

			if (tc.dataout != null) {
				check(edata,tc.dataout,tc.dataout.length);
			}
		}
	}

	static byte[] calcSHA(int sha, byte[] m) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-"+sha);
		return md.digest(m);
	}

	static void nist_dsa() throws Exception {
		String file = "res/nist-dsa-SigVer.rsp";
		int cnt = 0;
		try (BufferedReader rd = new BufferedReader(new FileReader(Env.expandEnv(file)))) {
			String ln;
			int L = 0, N = 0, sha = 0;
			byte[] Msg;
			BigInteger P, Q, G, K, R, S;
			BigInteger X, Y;

			P = Q = G = X = Y = K = R = S = null;
			Msg = null;
			while ((ln = rd.readLine()) != null) {
				if (ln.startsWith("#")) continue;
				if (ln.startsWith("[")) {
					int b,e;
					b = ln.indexOf("L=", 1) + 2;
					e = ln.indexOf(",", b);
					L = Integer.parseInt(ln.substring(b, e));
					b = ln.indexOf("N=", e) + 2;
					e = ln.indexOf(",", b);
					N = Integer.parseInt(ln.substring(b, e));
					b = ln.indexOf("SHA-", e) + 4;
					e = ln.indexOf("]", b);
					sha = Integer.parseInt(ln.substring(b, e));
					Log.debug("L=%d, N=%d SHA-%d", L, N, sha);
					P = Q = G = X = Y = K = R = S = null;
					Msg = null;
					continue;
				}

				if (ln.startsWith("P ")) {
					int b = ln.indexOf("= ") + 2;
					P = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("Q ")) {
					int b = ln.indexOf("= ") + 2;
					Q = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("G ")) {
					int b = ln.indexOf("= ") + 2;
					G = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("Msg ")) {
					if (Msg != null) {
						DSA dsa = new DSA(P, Q, G);
						dsa.setXY(X, Y);
						dsa.setK(K);

						byte[] hash = calcSHA(sha, Msg);
						Log.debug("Test %d", cnt);
						Log.debug("X = %s", X.toString(16));
						Log.debug("SHA-%d: %s", sha, Text.hex(hash));

						byte[] sign = dsa.signDigest(hash);
						if (!dsa.verifyDigest(sign, hash)) {
							Log.error("Wrong signaure");
							break;
						}
						++cnt;
					}
					int b = ln.indexOf("= ") + 2;
					Msg = Text.bin(ln.substring(b));
				}
				else if (ln.startsWith("X ")) {
					int b = ln.indexOf("= ") + 2;
					X = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("Y ")) {
					int b = ln.indexOf("= ") + 2;
					Y = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("K ")) {
					int b = ln.indexOf("= ") + 2;
					K = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("R ")) {
					int b = ln.indexOf("= ") + 2;
					R = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("S ")) {
					int b = ln.indexOf("= ") + 2;
					S = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("Result ")) {
					int b = ln.indexOf("= ") + 2;
					boolean Result = ln.substring(b).startsWith("P") ? true : false;
					DSA dsa = new DSA(P, Q, G);
					dsa.setXY(X, Y);

					byte[] hash = calcSHA(sha, Msg);
					Log.debug("Test %d", cnt);
					Log.debug("X = %s", X.toString(16));
					Log.debug("SHA-%d: %s", sha, Text.hex(hash));

					if (dsa.verifyDigest(R, S, hash) != Result) {
						Log.error("Wrong signaure");
						break;
					}
					Msg = null;
					++cnt;
				}
			}
		}
	}

	static ECDSA createECDSA(int alg) {
		switch (alg) {
		case 192: return new ECDSA(ECDSA.p192, ECDSA.g192, null);
		}
		return null;
	}

	static void nist_ecdsa() throws Exception {
		String file = "res/nist-ecdsa-SigVer.rsp";
		int cnt = 0;
		try (BufferedReader rd = new BufferedReader(new FileReader(Env.expandEnv(file)))) {
			String ln;
			int ecAlg = 0, sha = 0;
			byte[] Msg;
			BigInteger Qx, Qy, R, S;

			Qx = Qy = R = S = null;
			Msg = null;
			while ((ln = rd.readLine()) != null) {
				if (ln.startsWith("#")) continue;
				if (ln.startsWith("[")) {
					int b,e;
					b = ln.indexOf("P-", 1) + 2;
					e = ln.indexOf(",", b);
					ecAlg = Integer.parseInt(ln.substring(b, e));
					b = ln.indexOf("SHA-", e) + 4;
					e = ln.indexOf("]", b);
					sha = Integer.parseInt(ln.substring(b, e));
					Log.debug("P=%d SHA-%d", ecAlg, sha);
					Qx = Qy = R = S = null;
					Msg = null;
					continue;
				}

				if (ln.startsWith("Qx ")) {
					int b = ln.indexOf("= ") + 2;
					Qx = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("Qy ")) {
					int b = ln.indexOf("= ") + 2;
					Qy = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("Msg ")) {
					if (Msg != null) {
						ECDSA ecdsa = createECDSA(ecAlg);

						byte[] hash = calcSHA(sha, Msg);
						Log.debug("Test %d", cnt);
						Log.debug("Alg = P-%d", ecAlg);
						Log.debug("SHA-%d: %s", sha, Text.hex(hash));

						byte[] sign = ecdsa.signDigest(hash);
						if (!ecdsa.verifyDigest(sign, hash)) break;
						++cnt;
					}
					int b = ln.indexOf("= ") + 2;
					Msg = Text.bin(ln.substring(b));
				}
				else if (ln.startsWith("R ")) {
					int b = ln.indexOf("= ") + 2;
					R = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("S ")) {
					int b = ln.indexOf("= ") + 2;
					S = new BigInteger(1, Text.bin(ln.substring(b)));
				}
				else if (ln.startsWith("Result ")) {
					int b = ln.indexOf("= ") + 2;
					boolean Result = ln.substring(b).startsWith("P") ? true : false;
					ECDSA ecdsa = createECDSA(ecAlg);

					byte[] hash = calcSHA(sha, Msg);
					Log.debug("Test %d", cnt);
					Log.debug("Alg = P-%d", ecAlg);
					Log.debug("SHA-%d: %s", sha, Text.hex(hash));

					if (ecdsa.verifyDigest(R, S, hash) != Result) break;
					Msg = null;
					++cnt;
				}
			}
		}
	}

	static void simple_dsa() throws Exception {
		DSA dsa = new DSA(
				new BigInteger("123456789", 16),
				new BigInteger("123456789", 16),
				new BigInteger("123456789", 16)
				);
		dsa.generateXY();
		dsa.print(System.out);

		dsa = new DSA(64, 16);
		dsa.generateXY();
		dsa.print(System.out);
	}

	static void gp_dsa() throws Exception {
		//byte[] hash = Text.bin("C1A0104D625B8E378A96130514AA1BEAA7BE2E04");
		DSA dsa = new DSA(
				new BigInteger(1, GP.TEE_ATTR_DSA_PRIME_768_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_SUBPRIME_160_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_BASE_768_VALUE01)
				);
		dsa.setXY(
				new BigInteger(1, GP.TEE_ATTR_DSA_PRIVATE_VALUE_160_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_PUBLIC_VALUE_768_VALUE01)
				);
		MessageDigest md = MessageDigest.getInstance("SHA1");
		byte[] hash = md.digest(GP.DATA_FOR_CRYPTO1);
		Log.info("bits(hash) = %d", hash.length*8);
		byte[] sign = dsa.signDigest(hash);
		Log.info("hash[%d] = %s", hash.length, Text.hex(hash));
		Log.info("sign[%d] = %s", sign.length, Text.hex(sign));
		//dsa.verifyDigest(sign, hash);

		dsa = new DSA(
				new BigInteger(1, GP.TEE_ATTR_DSA_PRIME_2048_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_SUBPRIME_224_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_BASE_2048_VALUE01)
				);
		dsa.setXY(
				new BigInteger(1, GP.TEE_ATTR_DSA_PRIVATE_VALUE_224_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_PUBLIC_VALUE_2048_VALUE01)
				);
		md = MessageDigest.getInstance("SHA-224");
		hash = md.digest(GP.DATA_FOR_CRYPTO1);
		Log.info("bits(hash) = %d", hash.length*8);
		sign = dsa.signDigest(hash);
		Log.info("hash[%d] = %s", hash.length, Text.hex(hash));
		Log.info("sign[%d] = %s", sign.length, Text.hex(sign));
		dsa.verifyDigest(sign, hash);

		dsa = new DSA(
				new BigInteger(1, GP.TEE_ATTR_DSA_PRIME_3072_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_SUBPRIME_256_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_BASE_3072_VALUE01)
				);
		dsa.setXY(
				new BigInteger(1, GP.TEE_ATTR_DSA_PRIVATE_VALUE_256_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_DSA_PUBLIC_VALUE_3072_VALUE01)
				);

		md = MessageDigest.getInstance("SHA-256");
		hash = md.digest(GP.DATA_FOR_CRYPTO1);
		Log.info("bits(hash) = %d", hash.length*8);
		sign = dsa.signDigest(hash);
		Log.info("hash[%d] = %s", hash.length, Text.hex(hash));
		Log.info("sign[%d] = %s", sign.length, Text.hex(sign));
	}
	static void gp_dh() throws Exception {
		BigInteger prime = new BigInteger(1, GP.TEE_ATTR_DH_PRIME_VALUE01);  // p (prime number)
		BigInteger base = new BigInteger(1, GP.TEE_ATTR_DH_BASE_VALUE01);    // g (generator)
		BigInteger priv = new BigInteger(1, GP.TEE_ATTR_DH_PRIVATE_VALUE_VALUE01);//xA (private exponent of user A)
		BigInteger pub = new BigInteger(1, GP.TEE_ATTR_DH_PUBLIC_VALUE_VALUE02);  //yB (public exponent of user B)

		Log.info("Prime = %s", prime.toString(16));
		Log.info("Base = %s", base.toString(16));
		Log.info("Priv = %s", priv.toString(16));
		Log.info("Pub = %s", pub.toString(16));

		DH dh = new DH(prime, base);
		BigInteger sA = dh.deriveSharedKey(priv, pub);

		Log.info("shared[%d] = %s", (sA.bitLength()+7)/8, sA.toString(16));
	}

	static void gp_asym_encr() throws Exception {
		RSA rsa_pub = new RSA(
				new BigInteger(1, GP.TEE_ATTR_RSA_PUBLIC_EXPONENT_VALUE01),
				null,
				new BigInteger(1, GP.TEE_ATTR_RSA_MODULUS_VALUE01),
				false
				);
		RSA rsa_prv = new RSA(
				new BigInteger(1, GP.TEE_ATTR_RSA_PUBLIC_EXPONENT_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_RSA_PRIVATE_EXPONENT_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_RSA_MODULUS_VALUE01),
				true
				);

		byte[] data = GP.DATA_FOR_CRYPTO1;
		MessageDigest md = MessageDigest.getInstance("SHA1");

		//byte[] em = RSA.padOAEP(null, data, GP.TEE_ATTR_RSA_MODULUS_VALUE01.length, md);
		//Log.debug("padOAEP(hashPAD) [%d]: %s", emsg.length, Text.hex(emsg));
		//byte[] edata = rsa_pub.encrypt(em);
		byte[] edata = Text.bin("B6F108A55A6CD55B6026AA1800EE93D34A04F88572BEAAB9C6A0FCBBB73B3D0EC36723CA3671B54D7E56A7BB42AFE85E498192F95B5480A02D83267B6BE1AD7CDA87E8D08459289E9C8181E61DE9C76FCD36964176EAFA8AAC7E4126D6F8B0E995C144C13493683F4EF3B8B37C6D7CD4ABC2116D7E79A3242E53458A8FCDD7BD0D8A77806993FB8C966A2BA7F10A06EAD4CD001361FB8B416E3E45079E73CCBF489A1A5804820DAF1DB7AA054BEF686E28FCF31636C39D6A41F76270808341E297DFDF24B3B38BB8549986FFB8FC721E62566E8AE171FDDE707B0E96F5415EEB82A42A423012BA350F8AC9C6B28541CDA2BA6F98CEC3A4B8B0133E8D5C16BC55");
		Log.debug("encr(EM) [%d]: %s", edata.length, Text.hex(edata));

		byte[] rdata = rsa_prv.decrypt(edata);
		Log.debug("EM [%d]: %s", rdata.length, Text.hex(rdata));

		//data = RSA.unpadOAEP(null,rdata, md);
		data = rdata;
		Log.debug("m [%d]: %s", data.length, Text.hex(data));
	}

	static void gp_check_rsa() throws Exception {
		byte[] pVal = Text.bin("3330374346453835E066C9ACD4F62E00805175130000000000000000C32A900101000000485275130002000000501000C7872C0008F22E004854751300000000F7FFFF7F0802FFFF40547513FFFFFF7F000000000000000000000000000000000000000000000000000000000000000000000000000000000000000032303438E066C9AC00000000D4F62E00B22A900118F92E003C52751348527513745A9001AD2A9001005010009D862C00C8F72E00AC252F00081C1000FCFFFFFFAC252F0017702C003C527513E066C9AC000000000B958C01B22A9001745A900100080000485275134630314139354344354639463143424335433245433830303342464145304435E0527513F85A3100A8527513F85A3100E0527513F900000098527513A85275139453751300C0E201E0537513B133E10141413643454531363742463839324146A4432B000000000000000000423630343143334132454437434142384235303000000000506A31002053751300000000E05275135334E1018C268A01B37C0700117B9C7C0000000001000000F85A3100A4432B004454751343363833E0537513A053751320537513945375139453751340547513AB34E1018C268A01B37C0700117B9C7C0000000001000000F85A3100B0537513A867310078537513A8673100B05375134100000068537513785375136454751300C0E201B0547513");
		byte[] qVal = Text.bin("B133E1013C54751300C0E201E8537513F85A3100B0537513F85A3100E8537513A0040000A0537513B05375139C54751300C0E201E8547513B133E101F0537513000000002054751328713100147E2B00000000000000000014000000D8537513E85375139C54751300C0E20100000000506A31002854751300000000E85375135334E101A61CBB00B5D8060087F0967C0000000001000000F85A3100147E2B004C5575133416BB00E8547513A8547513285475139C5475139C54751348557513AB34E101A61CBB00B5D8060087F0967C0000000001000000F85A3100147E2B004C557513E8547513E8547513A8547513186F310098547513D936E101C051310058543100C8563100F85A3100085D3100605F31008863310098653100A8673100186F3100287131001455751328713100E8218A01A6000000605475130B00000004000000A61CBB00B5D8060087F0967C000000000100000000000000000000004C557513186F3100E416BB00ED00000000000000A61CBB00FB37E1011055751301000000A61CBB00B5D8060087F0967C0000000001000000F85A3100147E2B004C55751300C22100FC3BBB00C4B2BC00485575134855751329412C001B412C0015182C00C4B2BC00F938E101FC3BBB004C5575137CCC2100186F310000C0E20170050000F85A3100010000005055751300000000E066C9AC01000000C4B2BC00");
		BigInteger p = new BigInteger(1, pVal);
		BigInteger q = new BigInteger(1, qVal);
		BigInteger n = p.multiply(q);
		Log.debug("N = %s", n.toString(16));
	}

	/*
	 * M, the message to be encrypted:
	 *    d4 36 e9 95 69 fd 32 a7 c8 a0 5b bc 90 d3 2c 49
	 * P, encoding parameters:
	 *    NULL
	 * pHash=Hash(P):
	 *    da 39 a3 ee 5e 6b 4b 0d 32 55 bf ef 95 60 18 90 af d8 07 09
	 * DB = pHash‖P S‖01‖M: (107)
	 *    da 39 a3 ee 5e 6b 4b 0d 32 55 bf ef 95 60 18 90 af d8 07 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0000 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0000 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 d4 36 e9 95 69fd 32 a7 c8 a0 5b bc 90 d3 2c 49
	 * seed, a random octet string:
	 *    aa fd 12 f6 59 ca e6 34 89 b4 79 e5 07 6d de c2 f0 6c b5 8f
	 * dbMask = MGF(seed,107):
	 *    06 e1 de b2 36 9a a5 a5 c7 07 d8 2c 8e 4e 93 24 8a c7 83 de e0 b2 c0 4626 f5 af f9 3e dc fb 25 c9 c2 b3 ff 8a e1 0e 83 9a 2d db 4c dc fe 4f f477 28 b4 a1 b7 c1 36 2b aa d2 9a b4 8d 28 69 d5 02 41 21 43 58 11 59 1be3 92 f9 82 fb 3e 87 d0 95 ae b4 04 48 db 97 2f 3a c1 4e af f4 9c 8c 3b7c fc 95 1a 51 ec d1 dd e6 12 64
	 * maskedDB = DB xor dbMask:
	 *    dc d8 7d 5c 68 f1 ee a8 f5 52 67 c3 1b 2e 8b b4 25 1f 84 d7 e0 b2 c0 4626 f5 af f9 3e dc fb 25 c9 c2 b3 ff 8a e1 0e 83 9a 2d db 4c dc fe 4f f477 28 b4 a1 b7 c1 36 2b aa d2 9a b4 8d 28 69 d5 02 41 21 43 58 11 59 1be3 92 f9 82 fb 3e 87 d0 95 ae b4 04 48 db 97 2f 3a c1 4f 7b c2 75 19 5281 ce 32 d2 f1 b7 6d 4d 35 3e 2d
	 * seedMask = MGF(maskedDB,20)
	 *    41 87 0b 5a b0 29 e6 57 d9 57 50 b5 4c 28 3c 08 72 5d be a9
	 * maskedSeed = seed xor seedMa
	 *    eb 7a 19 ac e9 e3 00 63 50 e3 29 50 4b 45 e2 ca 82 31 0b 26
	 * EM=maskedSeed‖maskedDB:
	 *    eb 7a 19 ac e9 e3 00 63 50 e3 29 50 4b 45 e2 ca 82 31 0b 26 dc d8 7d 5c68 f1 ee a8 f5 52 67 c3 1b 2e 8b b4 25 1f 84 d7 e0 b2 c0 46 26 f5 af f93e dc fb 25 c9 c2 b3 ff 8a e1 0e 83 9a 2d db 4c dc fe 4f f4 77 28 b4 a1b7 c1 36 2b aa d2 9a b4 8d 28 69 d5 02 41 21 43 58 11 59 1b e3 92 f9 82fb 3e 87 d0 95 ae b4 04 48 db 97 2f 3a c1 4f 7b c2 75 19 52 81 ce 32 d2f1 b7 6d 4d 35 3e 2d
	 *
	 * DA39A3EE5E6B4B0D3255BFEF95601890AFD80709000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001D436E99569FD32A7C8A05BBC90D32C49
	 * DA39A3EE5E6B4B0D3255BFEF95601890AFD807090000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001D436E99569FD32A7C8A05BBC90D32C49
	 */
	static void oaep_test_vectors() throws Exception {
		byte[] db = Text.bin("da 39 a3 ee 5e 6b 4b 0d 32 55 bf ef 95 60 18 90 af d8 07 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0000 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0000 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 d4 36 e9 95 69fd 32 a7 c8 a0 5b bc 90 d3 2c 49");
		byte[] mdb = Text.bin("dc d8 7d 5c 68 f1 ee a8 f5 52 67 c3 1b 2e 8b b4 25 1f 84 d7 e0 b2 c0 4626 f5 af f9 3e dc fb 25 c9 c2 b3 ff 8a e1 0e 83 9a 2d db 4c dc fe 4f f477 28 b4 a1 b7 c1 36 2b aa d2 9a b4 8d 28 69 d5 02 41 21 43 58 11 59 1be3 92 f9 82 fb 3e 87 d0 95 ae b4 04 48 db 97 2f 3a c1 4f 7b c2 75 19 5281 ce 32 d2 f1 b7 6d 4d 35 3e 2d");
		byte[] msg = Text.bin("d4 36 e9 95 69 fd 32 a7 c8 a0 5b bc 90 d3 2c 49");
		byte[] em = RSA.padOAEP(null, msg, 128-1, MessageDigest.getInstance("SHA1"),
				Text.bin("aa fd 12 f6 59 ca e6 34 89 b4 79 e5 07 6d de c2 f0 6c b5 8f"));
		Log.debug("DB [%d]: %s", db.length, Text.hex(db));
		Log.debug("MaskedDB [%d]: %s", mdb.length, Text.hex(mdb));
		Log.debug("EM [%d]: %s", em.length, Text.hex(em));

		//msg = RSA.unpadOAEP(null, em, MessageDigest.getInstance("SHA1"));
		//Log.debug("M [%d]: %s", msg.length, Text.hex(msg));
	}

	static void rsa_sign() throws Exception {
		byte[] empa = Text.bin(
				  " 017c5c1b 2b369af7 a8cb9de0 133f1d09 56042c69 c0198187 cfa2b307 31fe6e6e 450a2bfc c9574830 1e3e786a 770487ae"
				+ " c9e08e94 0d2d05c7 df840f36 15bce7a4 56c64272 7de409ef cf9c58c5 5f76de4c 895370fe ce8c155a 13a6f934 bc4176bb"
				+ " eae90f37 4289242f 317d3d2b 177923f7 0b4a2638 0b1ce215 19db2968 ca0a1e50 d781d4fd 2a89a1e0 19dd509f ad209696"
				+ " 0f6260db b8392e30 4b46e7a3 a1485149 5ca68291 67492a69 bb86457b 59932cc9 5bbbb0a7 5588ddcf 017d0fc9 e2edfaf1"
				+ " 0bd5ac34 f3b693a6 088a0f2d aa7dd102 8480f458 176c5b74 a83642f2 8383d61d e83b2ed9 f724b8dd 29c16717 b4c1f4e0"
				+ " 6c7c9a9a bef453ca 6a9f5eac b4548dbc");

		RSA rsa = new RSA(
				new BigInteger(1, GP.TEE_ATTR_RSA_PUBLIC_EXPONENT_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_RSA_PRIVATE_EXPONENT_VALUE01),
				new BigInteger(1, GP.TEE_ATTR_RSA_MODULUS_VALUE01),
				false
				);

		byte[] sign;

		MessageDigest md = MessageDigest.getInstance("SHA1");
		byte[] hash = md.digest(GP.DATA_FOR_CRYPTO1);
		int nBits = GP.TEE_ATTR_RSA_MODULUS_VALUE01.length*8;

		byte[] padEMSA_PSS = RSA.padEMSA_PSS(hash, nBits-1, md);
		Log.info("padEMSA_PSS[%d] = %s", padEMSA_PSS.length, Text.hex(padEMSA_PSS));
		//check(padEMSA_PSS, empa); //<- for seed = zero

		sign = rsa.sign(padEMSA_PSS);
		Log.info("sign[%d] = %s\n", sign.length, Text.hex(sign));

		check("unpad", RSA.unpadEMSA_PSS(padEMSA_PSS, hash, nBits-1, md));
	}

	static void mgf_test() throws Exception {
		byte[] seed = "hello".getBytes();
		byte[] t1 = RSA.mgf1(seed, 10, MessageDigest.getInstance("SHA-256"));
		check(t1, Text.bin("DA75447E22F9F99E1BE0"));

		t1 = RSA.mgf1(seed, 15, MessageDigest.getInstance("SHA-256"));
		check(t1, Text.bin("DA75447E22F9F99E1BE09A00CF1A07"));

		seed = "foo".getBytes();
		t1 = RSA.mgf1(seed, 3, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("1AC907"));
		t1 = RSA.mgf1(seed, 5, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("1AC9075CD4"));

		seed = "bar".getBytes();
		t1 = RSA.mgf1(seed, 5, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("BC0C655E01"));
		t1 = RSA.mgf1(seed, 50, MessageDigest.getInstance("SHA-1"));
		check(t1, Text.bin("BC0C655E016BC2931D85A2E675181ADCEF7F581F76DF2739DA74FAAC41627BE2F7F415C89E983FD0CE80CED9878641CB4876"));
		t1 = RSA.mgf1(seed, 50, MessageDigest.getInstance("SHA-256"));
		check(t1, Text.bin("382576A7841021CC28FC4C0948753FB8312090CEA942EA4C4E735D10DC724B155F9F6069F289D61DACA0CB814502EF04EAE1"));
	}

	public static void main(String[] args) {
		//CryptX_Provider.register();
		//listProviders();
		//try { keygenerate(); } catch (Exception e) { Log.error(e); }
		//try { encrypt(); } catch (Exception e) { Log.error(e); }
		//try { decrypt(); } catch (Exception e) { Log.error(e); }
		//try { digest(); } catch (Exception e) { Log.error(e); }
		try { simple_dsa(); } catch (Exception e) { Log.error(e); }
		//try { nist_mac(); } catch (Exception e) { Log.error(e); }
		//try { nist_dsa(); } catch (Exception e) { Log.error(e); }
		//try { nist_ecdsa(); } catch (Exception e) { Log.error(e); }

		//Log.info("");
		//try { gp_digest(); } catch (Exception e) { Log.error(e); }
		//try { gp_encrypt(); } catch (Exception e) { Log.error(e); }
		//try { gp_mac(); } catch (Exception e) { Log.error(e); }
		//try { gp_cmac(); } catch (Exception e) { Log.error(e); }
		//try { java_mac(); } catch (Exception e) { Log.error(e); }
		//try { gp_dsa(); } catch (Exception e) { Log.error(e); }
		//try { gp_dh(); } catch (Exception e) { Log.error(e); }
		//try { mgf_test(); } catch (Exception e) { Log.error(e); }
		//try { rsa_sign(); } catch (Exception e) { Log.error(e); }
		//try { gp_asym_encr(); } catch (Exception e) { Log.error(e); }
		//try { oaep_test_vectors(); } catch (Exception e) { Log.error(e); }
		//try { gp_check_rsa(); } catch (Exception e) { Log.error(e); }
	}
}
