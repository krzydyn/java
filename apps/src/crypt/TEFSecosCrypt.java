package crypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

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

	static class GenerateTC {
		GenerateTC(tef_key_type_e kt, int bits) {this.kt=kt; this.bits=bits;}
		tef_key_type_e kt;
		int bits;
	}
	static GenerateTC generateTC[] = {
			new GenerateTC(tef_key_type_e.TEF_DES, 56),
			new GenerateTC(tef_key_type_e.TEF_DES, 112),
			new GenerateTC(tef_key_type_e.TEF_DES, 168),
			new GenerateTC(tef_key_type_e.TEF_DES, 64),
			new GenerateTC(tef_key_type_e.TEF_DES, 128),
			new GenerateTC(tef_key_type_e.TEF_DES, 192),
			new GenerateTC(tef_key_type_e.TEF_AES, 128),
			new GenerateTC(tef_key_type_e.TEF_AES, 192),
			new GenerateTC(tef_key_type_e.TEF_AES, 256),
	};
	static GenerateTC generateTC_exc[] = {
		new GenerateTC(tef_key_type_e.TEF_DES, 80),
		new GenerateTC(tef_key_type_e.TEF_AES, 320),
	};


	static void generate() throws Exception {
		TEF t = new TEF();

		for (GenerateTC tc : generateTC) {
			try {
				t.tef_key_generate(tc.kt, tc.bits);
			}
			catch (Exception e) {
				Log.error(e, "%s/%d: %s", tc.kt, tc.bits);
			}
		}

		for (GenerateTC tc : generateTC_exc) {
			try {
				t.tef_key_generate(tc.kt, tc.bits);
				throw new RuntimeException("Exception not thrown");
			}
			catch (Exception e) {
				Log.info("%s/%d: %s", tc.kt, tc.bits, e.getMessage());
			}
		}
	}

	final static byte[] ZERO8 = new byte[8];
	final static byte[] ZERO16 = new byte[16];
	final static byte[] ZERO24 = new byte[24];
	final static byte[] ZERO32 = new byte[32];

	static class EncryptKey {
		EncryptKey(tef_key_type_e kt, byte[] key) {
			this.kt=kt; this.key=key;
		}
		tef_key_type_e kt;
		byte[] key;
	}

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
	static EncryptKey[] keys_nist = {
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("2b7e151628aed2a6abf7158809cf4f3c")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("603deb1015ca71be2b73aef0857d7781 1f352c073b6108d72d9810a30914dff4")),

		//GCM-AES  from http://csrc.nist.gov/groups/STM/cavp/documents/mac/gcmtestvectors.zip
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("11754cd72aec309bf52f7687212e8957")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("272f16edb81a7abbea887357a58c1917")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("77be63708971c4e240d1cb79e8d77feb")),
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("2301a2bba4f569826ca3cee802f53a7c")),
	};
	static TEF.tef_algorithm[] algos_nist = {
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
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("bed48d86e1ff4bff37286a5c428c719130200dce04011edb967f5aaff6a9fb4ad0fcf0dd474e12dcfbcca7fa1ff9bb66b2624aaf1a90f33ed2bab0ee5b465174a722eaa3353bcb354165a1a852468ece974a31429c6e1de7a34e6392f24225d539eaa6b8c1183bfb37627eb16dcd81bba9d65051ff84bd63ee814bea0e1c34d2"))
				.set(tef_algorithm_param_e.TEF_AAD, Text.bin("a481e81c70e65eeb94cdf4e25b0a225a4f48b58b12cde148a3a9aa4db0d2988da27591d65827eed39ad6933f267e486c31dc586c36ebaa0c349b9c12ed33221a463737695743cebb456f0705a9895a5aac720f8a53981a231fde"))
				.set(tef_algorithm_param_e.TEF_TAGLEN, 96),

		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_PCBC, tef_padding_mode_e.TEF_PADDING_NONE),

	};

	static byte[] TEE_ATTR_DSA_PRIME_768_VALUE01 = Text.bin("f6ad2071e15a4b9c2b7e5326da439dc1474c1ad16f2f85e92cea89fcdc746611cf30ddc85e33f583c19d10bc1ac3932226246fa7b9e0dd2577b5f427594c39faebfc598a32e174cb8a680357f862f20b6e8432a530652f1c2139ae1faf768b83");
	static byte[] TEE_ATTR_DSA_SUBPRIME_160_VALUE01 = Text.bin("8744e4ddc6d019a5eac2b15a15d7e1c7f66335f7");
	static byte[] TEE_ATTR_DSA_BASE_768_VALUE01  = Text.bin("9a0932b38cb2105b9300dcb866c066d9cec643192fcb2834a1239dba28bd09fe01001e0451f9d6351f6e564afbc8f8c39b1059863ebd0985090bd55c828e9fc157ac7da3cfc2892a0ed9b932390582f2971e4a0c483e0622d73166bf62a59f26");
	static byte[] TEE_ATTR_DSA_PRIVATE_VALUE_160_VALUE01 = Text.bin("704a46c6252a95a39b40e0435a691badae52a5c0");
	static byte[] TEE_ATTR_DSA_PUBLIC_VALUE_768_VALUE01 = Text.bin("529ded98a2320985fc84b65a9dc8d4fe41ada6e3593d704f0898c14ec24634ddf5f1db47cc4915fce1e2674d2ecd98d58b598e8ddfaff30e8826f50aab4027b5aab887c19ad96d7e57de5390ad8e5557b41a8019c90d80607179b54eb0ad4d23");

	static byte[] NONCE1_VALUE_AES_GCM = Text.bin("00:8d:49:3b:30:ae:8b:3c:96:96:76:6c:fa"); //len=13
	static byte[] NONCE2_VALUE_AES_GCM = Text.bin("ca:fe:ba:be:fa:ce:db:ad:de:ca:f8:88"); //len=12
	static byte[] AAD1_VALUE = Text.bin("00:01:02:03:04:05:06:07");
	static byte[] DATA_FOR_CRYPTO1 = Text.bin("00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:\n" +
			"0a:0b:0c:0d:0e:0f:00:01:02:03:04:05:06:07:08:09:\n" +
			"0f:0e:0d:0c:0b:0a:09:08:07:06:05:04:03:02:01:00:\n" +
			"00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:\n" +
			"0a:0b:0c:0d:0e:0f:00:01:02:03:04:05:06:07:08:09:\n" +
			"0f:0e:0d:0c:0b:0a:09:08:07:06:05:04:03:02:01:00");

	static EncryptKey[] keys_gpapi = {
		new EncryptKey(tef_key_type_e.TEF_AES, Text.bin("60:3d:eb:10:15:ca:71:be:2b:73:ae:f0:85:7d:77:81:" +
					"1f:35:2c:07:3b:61:08:d7:2d:98:10:a3:09:14:df:f4"))
	};
	static TEF.tef_algorithm[] algos_gpapi = {
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE)
			.set(tef_algorithm_param_e.TEF_IV, NONCE2_VALUE_AES_GCM)
			.set(tef_algorithm_param_e.TEF_AAD, AAD1_VALUE)
			.set(tef_algorithm_param_e.TEF_TAGLEN, 104),
	};

	static class DigestTC {
		DigestTC(TEF.tef_digest_e digest, byte[] datain, byte[] dataout) {
			this.digest = digest;
			this.datain=datain; this.dataout=dataout;
		}
		TEF.tef_digest_e digest;
		byte[] datain;
		byte[] dataout;
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

	static byte[] AES_PLAINTEXT = Text.bin(
			  "6bc1bee22e409f96e93d7e117393172a ae2d8a571e03ac9c9eb76fac45af8e51"
			+ "30c81c46a35ce411e5fbc1191a0a52ef f69f2445df4f9b17ad2b417be66c3710");

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
		new EncryptTC(keys_nist[0],algos_nist[0],AES_PLAINTEXT,Text.bin("3ad77bb40d7a3660a89ecaf32466ef97f5d3d58503b9699de785895a96fdbaaf")),
		new EncryptTC(keys_nist[1],algos_nist[0],AES_PLAINTEXT,Text.bin("bd334f1d6e45f25ff712a214571fa5cc974104846d0ad3ad7734ecb3ecee4eef")),
		new EncryptTC(keys_nist[2],algos_nist[0],AES_PLAINTEXT,Text.bin("f3eed1bdb5d2a03c064b5a7e3db181f8591ccb10d410ed26dc5ba74a31362870")),
		//CBC-AES
		new EncryptTC(keys_nist[0],algos_nist[1],AES_PLAINTEXT,Text.bin("7649abac8119b246cee98e9b12e9197d5086cb9b507219ee95db113a917678b2")),
		new EncryptTC(keys_nist[1],algos_nist[1],AES_PLAINTEXT,Text.bin("4f021db243bc633d7178183a9fa071e8b4d9ada9ad7dedf4e5e738763f69145a")),
		new EncryptTC(keys_nist[2],algos_nist[1],AES_PLAINTEXT,Text.bin("f58c4c04d6e5f1ba779eabfb5f7bfbd69cfc4e967edb808d679f777bc6702c7d")),
		//CFB-AES
		new EncryptTC(keys_nist[0],algos_nist[2],AES_PLAINTEXT,Text.bin("3b3fd92eb72dad20333449f8e83cfb4ac8a64537a0b3a93fcde3cdad9f1ce58b")),
		new EncryptTC(keys_nist[1],algos_nist[2],AES_PLAINTEXT,Text.bin("cdc80d6fddf18cab34c25909c99a417467ce7f7f81173621961a2b70171d3d7a")),
		new EncryptTC(keys_nist[2],algos_nist[2],AES_PLAINTEXT,Text.bin("dc7e84bfda79164b7ecd8486985d386039ffed143b28b1c832113c6331e5407b")),
		//OFB-AES
		new EncryptTC(keys_nist[0],algos_nist[3],AES_PLAINTEXT,Text.bin("3b3fd92eb72dad20333449f8e83cfb4a7789508d16918f03f53c52dac54ed825")),
		new EncryptTC(keys_nist[1],algos_nist[3],AES_PLAINTEXT,Text.bin("cdc80d6fddf18cab34c25909c99a4174fcc28b8d4c63837c09e81700c1100401")),
		new EncryptTC(keys_nist[2],algos_nist[3],AES_PLAINTEXT,Text.bin("dc7e84bfda79164b7ecd8486985d38604febdc6740d20b3ac88f6ad82a4fb08d")),
		//CTR-AES
		new EncryptTC(keys_nist[0],algos_nist[4],AES_PLAINTEXT,Text.bin("874d6191b620e3261bef6864990db6ce9806f66b7970fdff8617187bb9fffdff")),
		new EncryptTC(keys_nist[1],algos_nist[4],AES_PLAINTEXT,Text.bin("1abc932417521ca24f2b0459fe7e6e0b090339ec0aa6faefd5ccc2c6f4ce8e94")),
		new EncryptTC(keys_nist[2],algos_nist[4],AES_PLAINTEXT,Text.bin("601ec313775789a5b7a7f504bbf3d228f443e3ca4d62b59aca84e990cacaf5c5")),

		//GCM-AES  from http://csrc.nist.gov/groups/STM/cavp/documents/mac/gcmtestvectors.zip
		new EncryptTC(keys_nist[3],algos_nist[5],"".getBytes(),Text.bin("250327c674aaf477aef2675748cf6971")),
		new EncryptTC(keys_nist[4],algos_nist[6],"".getBytes(),Text.bin("b6e6f197168f5049aeda32dafbdaeb")),
		new EncryptTC(keys_nist[5],algos_nist[7],"".getBytes(),Text.bin("209fcc8d3675ed938e9c7166709dd946")),
		new EncryptTC(keys_nist[6],algos_nist[8],
				Text.bin("6081f9455583c4a35ed9400799e209fb7e75a7887868aa4bb0c9f7b78f67125678e03c618e615bfad03ab077315b7787418f50"),
				Text.bin("18eca8d7ec92b6209c8d3c82d10c876047b470e22b74346ad609f44cc338b38c881103636fd056634907c28e32efb32dcddb23 de01691b9b99851636c7c8d5")),

	};

	static EncryptTC gpapiTC[] = {
		new EncryptTC(keys_gpapi[0],algos_gpapi[0],DATA_FOR_CRYPTO1,Text.bin("A726EA73EB43D77C9E977070")),

	};

	static DigestTC gpapi_digest_TC[] = {
		new DigestTC(tef_digest_e.TEF_SHA1,DATA_FOR_CRYPTO1,null),
	};

	static void digest() throws Exception {
		for (DigestTC tc : gpapi_digest_TC) {
			Log.debug("MessageDigest %s on %s", tc.digest.toString(), Text.hex(tc.datain));
			MessageDigest md = MessageDigest.getInstance(tc.digest.toString());
			md.update(tc.datain);
			byte[] digest = md.digest();
			Log.debug("digest: %s", Text.hex(digest));
			if (tc.dataout != null) {
				check(digest,tc.dataout,tc.dataout.length);
			}
		}
	}

	static void encrypt() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[1000];

		for (EncryptTC tc : gpapiTC) {
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

	static void dsa() throws Exception {
		//byte[] hash = Text.bin("C1A0104D625B8E378A96130514AA1BEAA7BE2E04");
		DSA dsa = new DSA(
				new BigInteger(1, TEE_ATTR_DSA_PRIME_768_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_SUBPRIME_160_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_BASE_768_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_PRIVATE_VALUE_160_VALUE01),
				new BigInteger(1, TEE_ATTR_DSA_PUBLIC_VALUE_768_VALUE01)
				);

		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.update(DATA_FOR_CRYPTO1);
		byte[] hash = md.digest();

		byte[] sign = dsa.signDigest(hash);
		//byte[] sign = Text.bin("39260379165BAF89BE9C53CEDBEC97EDFA111119712B524423552903EEAF668149A7FA184F79F7D9");
		dsa.verifyDigest(sign, hash);
	}

	public static void main(String[] args) {
		//CryptX_Provider.register();
		//listProviders();
		//try { generate(); } catch (Exception e) { Log.error(e); }
		try { encrypt(); } catch (Exception e) { Log.error(e); }
		try { decrypt(); } catch (Exception e) { Log.error(e); }
		try { digest(); } catch (Exception e) { Log.error(e); }
		try { dsa(); } catch (Exception e) { Log.error(e); }

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update("passwd".getBytes());
			//System.out.println(Text.hex(m.digest()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
}
