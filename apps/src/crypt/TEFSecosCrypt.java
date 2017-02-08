package crypt;

import java.security.Provider;
import java.security.Security;

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
	static byte[][] keyMaterial = {
		Text.bin("0123456789ABCDEF"),
		Text.bin("0123456789ABCDEF FEDCBA9876543210"),
		Text.bin("0123456789ABCDEF FEDCBA9876543210 1032547698badcfe"),

		Text.bin("2b7e151628aed2a6abf7158809cf4f3c"),
		Text.bin("8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b"),
		Text.bin("603deb1015ca71be2b73aef0857d7781 1f352c073b6108d72d9810a30914dff4"),
	};
	static EncryptKey[] keys = {
			new EncryptKey(tef_key_type_e.TEF_DES, keyMaterial[0]),
			new EncryptKey(tef_key_type_e.TEF_DES, keyMaterial[1]),
			new EncryptKey(tef_key_type_e.TEF_DES, keyMaterial[2]),

			new EncryptKey(tef_key_type_e.TEF_AES, keyMaterial[3]),
			new EncryptKey(tef_key_type_e.TEF_AES, keyMaterial[4]),
			new EncryptKey(tef_key_type_e.TEF_AES, keyMaterial[5]),
	};
	static TEF.tef_algorithm[] algos = {
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("000102030405060708090a0b0c0d0e0f")),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CFB, tef_padding_mode_e.TEF_PADDING_NONE)
				.set(tef_algorithm_param_e.TEF_IV, Text.bin("000102030405060708090a0b0c0d0e0f")),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_OFB, tef_padding_mode_e.TEF_PADDING_PKCS5),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_CTR, tef_padding_mode_e.TEF_PADDING_PKCS5),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_PCBC, tef_padding_mode_e.TEF_PADDING_PKCS5),
		new TEF.tef_algorithm(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_PKCS5),
	};

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
		new EncryptTC(keys[0],algos[0],ZERO8,Text.bin("D5D44FF720683D0D")),
		new EncryptTC(keys[1],algos[0],ZERO8,Text.bin("08D7B4FB629D0885")),
		new EncryptTC(keys[2],algos[0],ZERO8,Text.bin("5802BA5F7916D120")),

		//AES key vectors from http://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38a.pdf
		//EBC-AES128
		new EncryptTC(keys[3],algos[0],AES_PLAINTEXT,Text.bin("3ad77bb40d7a3660a89ecaf32466ef97f5d3d58503b9699de785895a96fdbaaf")),
		new EncryptTC(keys[4],algos[0],AES_PLAINTEXT,Text.bin("bd334f1d6e45f25ff712a214571fa5cc974104846d0ad3ad7734ecb3ecee4eef")),
		new EncryptTC(keys[5],algos[0],AES_PLAINTEXT,Text.bin("f3eed1bdb5d2a03c064b5a7e3db181f8591ccb10d410ed26dc5ba74a31362870")),
		//CBC-AES128
		new EncryptTC(keys[3],algos[1],AES_PLAINTEXT,Text.bin("7649abac8119b246cee98e9b12e9197d5086cb9b507219ee95db113a917678b2")),
		new EncryptTC(keys[4],algos[1],AES_PLAINTEXT,Text.bin("4f021db243bc633d7178183a9fa071e8b4d9ada9ad7dedf4e5e738763f69145a")),
		new EncryptTC(keys[5],algos[1],AES_PLAINTEXT,Text.bin("f58c4c04d6e5f1ba779eabfb5f7bfbd69cfc4e967edb808d679f777bc6702c7d")),
		//CFB-AES128
		new EncryptTC(keys[3],algos[2],AES_PLAINTEXT,Text.bin("3b3fd92eb72dad20333449f8e83cfb4ac8a64537a0b3a93fcde3cdad9f1ce58b")),
		new EncryptTC(keys[4],algos[2],AES_PLAINTEXT,Text.bin("cdc80d6fddf18cab34c25909c99a417467ce7f7f81173621961a2b70171d3d7a")),
		new EncryptTC(keys[5],algos[2],AES_PLAINTEXT,Text.bin("dc7e84bfda79164b7ecd8486985d386039ffed143b28b1c832113c6331e5407b")),
	};

	static void encrypt() throws Exception {
		TEF t = new TEF();
		byte[] edata = new byte[1000];

		for (EncryptTC tc : encryptTC) {
			tef_cipher_token keyid = t.tef_key_import_raw(tc.key.kt, tc.key.key, tc.key.key.length);
			int r=t.tef_encrypt(keyid, tc.algo, tc.datain, tc.datain.length, edata);
			/*if (tc.algo.chaining == tef_chaining_mode_e.TEF_CBC) {
				byte[] ctj=new byte[16];
				Log.debug("datain: %s",Text.hex(tc.datain));
				System.arraycopy(tc.algo.get(tef_algorithm_param_e.TEF_IV), 0, tc.datain, 0, 16);
				for (int j = 1; j <= 999; ++j) {
					r=t.tef_encrypt(keyid, algos[0], tc.datain, tc.datain.length, ctj);
					System.arraycopy(edata, 0, tc.datain, 0, 16);
					System.arraycopy(ctj, 0, edata, 0, 16);
				}
				//533CEC6DA03BD0216C1669B86A2C3E3A
			}*/
			Log.debug("%s/%s:  r[%d]=%s, IV=%s", tc.key.kt, tc.algo, r,
					Text.hex(edata, 0, r),
					Text.hex((byte[])t.tef_get_param(tc.algo, tef_algorithm_param_e.TEF_IV)));
			if (tc.dataout != null) {
				check(edata,tc.dataout,tc.dataout.length);
			}
		}
	}

	static void decrypt() throws Exception {

	}

	public static void main(String[] args) {
		//CryptX_Provider.register();
		listProviders();
		//try { generate(); } catch (Exception e) { Log.error(e); }
		try { encrypt(); } catch (Exception e) { Log.error(e); }
		try { decrypt(); } catch (Exception e) { Log.error(e); }
	}
}
