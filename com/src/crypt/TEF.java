package crypt;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import sys.Log;
import text.Text;

public class TEF {
	static class tef_cipher_token {
		SecretKey key;
		tef_algorithm_info algo;
		@Override
		public String toString() {
			return transformName();
		}
		public String transformName() {
			return key.getAlgorithm() + "/" + algo.getName();
		}
	}

	static enum tef_key_type_e {
		TEF_DES,
		TEF_AES,
		TEF_RSA,
		TEF_DSA,
		TEF_ECDSA;
		private String name;
		String getName() {
			if (name==null) name=name().substring(4);
			return name;
		}
		@Override
		public String toString() {return getName();}
	}

	static enum tef_chaining_mode_e {
		TEF_ECB,
		TEF_CBC,
		TEF_PCBC,
		TEF_CFB,
		TEF_OFB,
		TEF_CTR,
		TEF_GCM, //(not supported in Java7)
		;
		private String name;
		String getName() {
			if (name==null) name=name().substring(4);
			return name;
		}
		@Override
		public String toString() {return getName();}
	}

	static enum tef_padding_mode_e {
		TEF_PADDING_NONE("NoPadding"),
		TEF_PADDING_PKCS5("PKCS5Padding"),
		TEF_PADDING_PKCS7("PKCS5Padding"),
		TEF_PADDING_ISO9797_1("ISO9797.1Padding"),
		TEF_PADDING_ISO9797_2("ISO9797.2Padding");
		private String name;
		tef_padding_mode_e(String n) {name=n;}
		String getName() {return name;}
		@Override
		public String toString() {return getName();}
	}

	static class tef_algorithm_info {
		final tef_chaining_mode_e chaining;
		final tef_padding_mode_e padding;
		tef_algorithm_info(tef_chaining_mode_e chain, tef_padding_mode_e pad) {
			this.chaining = chain; this.padding = pad;
		}
		String getName() {
			return chaining + "/" + padding;
		}
		@Override
		public String toString() {return getName();}
	}

	tef_cipher_token tef_key_generate(tef_key_type_e key_type, int key_bit_len, tef_algorithm_info algorithm)
			throws NoSuchAlgorithmException {
		String keyName=String.format("%s/%d[%s] (for %s)", key_type, key_bit_len, Integer.toBinaryString(key_bit_len), algorithm);
		Log.debug("generate key %s", keyName);

		KeyGenerator gen = null;
		if ("DES".equals(key_type.getName())) {
			if (key_bit_len <= 64) gen = KeyGenerator.getInstance("DES");
			else gen = KeyGenerator.getInstance("DESede");
			int mask5=0x1f;
			if ((key_bit_len&mask5)==0) { //fix key length in bits
				key_bit_len -= key_bit_len/8;
			}
			//else throw new InvalidParameterException("Wrong keysize: "+key_bit_len);
			Log.debug("generate des key %s", Integer.toBinaryString(key_bit_len));
			gen.init(key_bit_len);
		}
		else {
			gen = KeyGenerator.getInstance(key_type.getName());
			gen.init(key_bit_len);
		}


		SecretKey key = gen.generateKey();
		byte[] keybytes = key.getEncoded();
		Log.debug("key[%d]: %s", keybytes.length, Text.hex(keybytes));

		tef_cipher_token token = new tef_cipher_token();
		token.key = key;
		token.algo = algorithm;
		return token;
	}

	int tef_encrypt(tef_cipher_token keyid,byte[] data, int dataLen, byte[] edata) throws GeneralSecurityException {
		javax.crypto.Cipher cipher;

		//.getInstance(SERVICE, alg + '/' + mode, provider);
		//cipher = new AESCipher(new CryptX_AES(), keyid.transformName());
		//cipher = javax.crypto.Cipher.getInstance(keyid.transformName());
		cipher = javax.crypto.Cipher.getInstance(keyid.transformName(), new CryptX_Provider());
		cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key);

		return cipher.doFinal(data, 0, dataLen, edata);
	}

	int tef_decrypt(tef_cipher_token keyid,byte[] edata, int edataLen, byte[] data) throws GeneralSecurityException {
		javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(keyid.transformName());
		cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyid.key);

		return cipher.doFinal(data, 0, edataLen, edata);
	}
}
