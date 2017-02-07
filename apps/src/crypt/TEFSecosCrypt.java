package crypt;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import sys.Log;
import text.Text;

public class TEFSecosCrypt implements TEF_Types {

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
		new GenerateTC(tef_key_type_e.TEF_DES, 56),
		new GenerateTC(tef_key_type_e.TEF_AES, 256),
	};


	static void generate() throws Exception {
		TEF t = new TEF();

		for (GenerateTC tc : generateTC) {
			try {
				t.tef_key_generate(tc.kt, tc.bits, null);
			}
			catch (Exception e) {
				Log.error(e, "%s/%d: %s", tc.kt, tc.bits);
			}
		}

		for (GenerateTC tc : generateTC_exc) {
			try {
				t.tef_key_generate(tc.kt, tc.bits, null);
				throw new RuntimeException("Exception not thrown");
			}
			catch (Exception e) {
				Log.info("%s/%d: %s", tc.kt, tc.bits, e.getMessage());
			}
		}
	}

	static class EncryptKey {
		EncryptKey(tef_key_type_e kt, byte[] key, byte[] iv) {
			this.kt=kt; this.key=key; this.iv=iv;
		}
		tef_key_type_e kt;
		byte[] key;
		byte[] iv;
	}
	static EncryptKey[] keys = {
			new EncryptKey(tef_key_type_e.TEF_DES, "12345678".getBytes(),"12345678".getBytes()),
			new EncryptKey(tef_key_type_e.TEF_DES, "1234567812345678".getBytes(),"1234567812345678".getBytes()),
			new EncryptKey(tef_key_type_e.TEF_DES, "123456781234567812345670".getBytes(),"12345678".getBytes()),

			new EncryptKey(tef_key_type_e.TEF_AES, "1234567812345678".getBytes(),"1234567812345678".getBytes()),
			new EncryptKey(tef_key_type_e.TEF_AES, "123456781234567812345670".getBytes(),"12345678".getBytes()),
			new EncryptKey(tef_key_type_e.TEF_AES, "12345678123456781234567812345678".getBytes(),"1234567812345678".getBytes()),
	};

	static void encrypt() throws Exception {
		TEF t = new TEF();
		byte[] data16 = "156dowqgfbcad63e4o786358sddaalkm".getBytes();
		byte[] dataX = "156dsdflgsdlkfagfgsowqgfbcad63e4o786358".getBytes();
		byte[] edata = new byte[1000];

		tef_chaining_mode_e modes[] = tef_chaining_mode_e.values();
		tef_padding_mode_e pades[] = tef_padding_mode_e.values();

		// no padding
		for (EncryptKey key : keys) {
			for (tef_chaining_mode_e mode : modes) {
				if (key.kt == tef_key_type_e.TEF_DES) {
					if (mode == tef_chaining_mode_e.TEF_GCM) continue; //only for AES
				}

				tef_cipher_token keyid = t.tef_key_import_raw(key.kt, key.key, key.key.length,
						new TEF.tef_algorithm_info(mode, tef_padding_mode_e.TEF_PADDING_NONE));
				Log.info("ENC(%s, datalen=%d)",keyid.toString(), data16.length);
				try {
					int r=t.tef_encrypt(keyid, null, data16, data16.length, edata);
					Log.info("RES(%d): %s", r, Text.hex(edata, 0, r));
				}
				catch (NoSuchAlgorithmException e) {
					Log.error(e.getMessage());
				}
			}
		}

		// padding modes
		for (EncryptKey key : keys) {
			for (tef_chaining_mode_e mode : modes) {
				if (key.kt == tef_key_type_e.TEF_DES) {
					if (mode == tef_chaining_mode_e.TEF_GCM) continue; //only for AES
				}

				for (tef_padding_mode_e pad : pades) {
					if (pad == tef_padding_mode_e.TEF_PADDING_NONE) continue;
					if (pad == tef_padding_mode_e.TEF_PADDING_PKCS7) continue;
					tef_cipher_token keyid = t.tef_key_import_raw(key.kt, key.key, key.key.length,
							new TEF.tef_algorithm_info(mode, pad));
					Log.info("ENC(%s, datalen=%d)",keyid.toString(), dataX.length);
					try {
						int r=t.tef_encrypt(keyid, null, dataX, dataX.length, edata);
						Log.info("RES(%d): %s", r, Text.hex(edata, 0, r));
					}
					catch (NoSuchAlgorithmException e) {
						Log.error(e.getMessage());
					}
				}
			}
		}
	}

	static void decrypt() throws Exception {

	}

	public static void main(String[] args) {
		//CryptX_Provider.register();
		//listProviders();
		try { generate(); } catch (Exception e) { Log.error(e); }
		try { encrypt(); } catch (Exception e) { Log.error(e); }
		try { decrypt(); } catch (Exception e) { Log.error(e); }
	}
}
