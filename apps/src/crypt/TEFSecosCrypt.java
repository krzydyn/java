package crypt;

import java.security.Provider;
import java.security.Security;

import sys.Log;
import text.Text;
import crypt.TEF.tef_chaining_mode_e;
import crypt.TEF.tef_cipher_token;
import crypt.TEF.tef_key_type_e;
import crypt.TEF.tef_padding_mode_e;

public class TEFSecosCrypt {
	static int[] des_bitsA = {64, 128, 192};
	static int[] des_bitsB = {56, 112, 168};
	static int[] aes_bits = {128, 192, 256};

	static void listProviders() {
		for (Provider provider: Security.getProviders()) {
			  System.out.println(provider.getName());
			  for (String key: provider.stringPropertyNames())
			    System.out.println("\t" + key + "\t" + provider.getProperty(key));
			}
	}


	static void generate() throws Exception {
		TEF t = new TEF();

		for (int b : des_bitsA) {
			t.tef_key_generate(
					tef_key_type_e.TEF_DES, b,
					new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE));
		}

		for (int b : des_bitsB) {
			t.tef_key_generate(
					tef_key_type_e.TEF_DES, b,
					new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE));
		}

		for (int b : aes_bits) {
			t.tef_key_generate(
					tef_key_type_e.TEF_AES, b,
					new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE));
		}

		try {
			t.tef_key_generate(
					tef_key_type_e.TEF_DES, 256,
					new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE));
			throw new RuntimeException("Exception not thrown");
		} catch (Exception e) {
			Log.info("DES 256: %s", e.getMessage());
		}

		try {
			t.tef_key_generate(
					tef_key_type_e.TEF_AES, 512,
					new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE));
			throw new RuntimeException("Exception not thrown");
		} catch (Exception e) {
			Log.info("AES 512: %s", e.getMessage());
		}
	}

	static void encrypt() throws Exception {
		TEF t = new TEF();
		byte[] data = "156dowqgfbcad63e4o786358".getBytes();
		byte[] edata = new byte[1000];

		for (int b : des_bitsA) {
			tef_cipher_token keyid = t.tef_key_generate(
					tef_key_type_e.TEF_DES, b,
					new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_CBC, tef_padding_mode_e.TEF_PADDING_NONE));
			Log.info("ENC(%s, datalen=%d)",keyid.toString(), data.length);
			int r=t.tef_encrypt(keyid, data, data.length, edata);
			Log.info("RES(%d): %s", r, Text.hex(edata, 0, r));
		}

		for (int b : aes_bits) {
			tef_cipher_token keyid = t.tef_key_generate(
					tef_key_type_e.TEF_AES, b,
					new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_GCM, tef_padding_mode_e.TEF_PADDING_NONE));
			Log.info("ENC(%s, datalen=%d)",keyid.toString(), data.length);
			int r=t.tef_encrypt(keyid, data, data.length, edata);
			Log.info("RES(%d): %s", r, Text.hex(edata, 0, r));
		}

	}

	static void decrypt() throws Exception {

	}

	public static void main(String[] args) {
		//CryptX_Provider.register();
		listProviders();
		try { generate(); } catch (Exception e) { Log.error(e); }
		try { encrypt(); } catch (Exception e) { Log.error(e); }
		try { decrypt(); } catch (Exception e) { Log.error(e); }
	}
}
