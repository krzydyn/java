package crypt;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import sys.Log;
import text.Text;

public class TEF implements TEF_Types {
	static final byte[] ZERO_IV = new byte[16];

	tef_cipher_token tef_key_generate(tef_key_type_e key_type, int key_bit_len)
			throws NoSuchAlgorithmException {
		Log.debug("generate key %s, %d bits", key_type, key_bit_len);

		KeyGenerator gen = null;
		if ("DES".equals(key_type.getName())) {
			if (key_bit_len <= 64) gen = KeyGenerator.getInstance("DES");
			else gen = KeyGenerator.getInstance("DESede");
			int mask5=0x1f;
			if ((key_bit_len&mask5)==0) { //fix key length in bits
				key_bit_len -= key_bit_len/8;
			}
			gen.init(key_bit_len);
		}
		else {
			gen = KeyGenerator.getInstance(key_type.getName());
			gen.init(key_bit_len);
		}

		tef_cipher_token token = new tef_cipher_token();
		token.key = gen.generateKey();
		return token;
	}

	tef_cipher_token tef_key_import_raw(tef_key_type_e key_type, byte[] data, int dataLen) {
		int key_bit_len = dataLen*8;
		String keyAlgo;
		if ("DES".equals(key_type.getName())) {
			if (key_bit_len <= 64) keyAlgo = "DES";
			else {
				if (key_bit_len == 128) {
					byte[] ndata = new byte[24];
					System.arraycopy(data, 0, ndata, 0, 16);
					System.arraycopy(data, 0, ndata, 16, 8);
					data = ndata;
					dataLen=24;
				}
				keyAlgo = "DESede";
			}
		}
		else {
			keyAlgo = key_type.getName();
		}
		tef_cipher_token token = new tef_cipher_token();
		Log.debug("import key %s/%d from %s",keyAlgo,dataLen*8,Text.hex(data));
		token.key = new javax.crypto.spec.SecretKeySpec(data,0,dataLen,keyAlgo);
		return token;
	}


	int tef_set_param(tef_algorithm algorithm, tef_algorithm_param_e param, Object o) {
		algorithm.set(param, o);
		return 0;
	}
	Object tef_get_param(tef_algorithm algorithm, tef_algorithm_param_e param) {
		return algorithm.get(param);
	}

	int tef_encrypt(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] data, int dataLen, byte[] edata) throws GeneralSecurityException {
		javax.crypto.Cipher cipher;

		String algoName = keyid.transformName()+"/"+algorithm.getName();
		cipher = javax.crypto.Cipher.getInstance(algoName);
		byte[] iv=null;
		if (algorithm.map.containsKey(tef_algorithm_param_e.TEF_IV)) {
			iv = (byte[])algorithm.map.get(tef_algorithm_param_e.TEF_IV);
		}
		else if (algorithm.chaining != tef_chaining_mode_e.TEF_ECB) {
			Log.warn("setting IV=ZERO");
			iv = ZERO_IV;
		}
		AlgorithmParameterSpec pspec = null;
		if (algorithm.chaining == tef_chaining_mode_e.TEF_GCM) {
			int taglen = (Integer)algorithm.map.get(tef_algorithm_param_e.TEF_AUTHTAG_LEN);
			pspec = new GCMParameterSpec(taglen, iv);
		}
		else if (iv != null){
			pspec = new IvParameterSpec(iv, 0, cipher.getBlockSize());
		}
		if (pspec!=null) {
			//AlgorithmParameters param = null;
			//param=AlgorithmParameters.getInstance(keyid.key.getAlgorithm());
			//param.init(pspec);
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key, pspec);
		}
		else {
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key);
		}

		//cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key, pspec);
		//cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key, param);

		if (algorithm.map.containsKey(tef_algorithm_param_e.TEF_AAD)) {
			cipher.updateAAD((byte[])algorithm.map.get(tef_algorithm_param_e.TEF_AAD));
		}

		int r = cipher.doFinal(data, 0, dataLen, edata);
		if (algorithm.chaining == tef_chaining_mode_e.TEF_GCM) {
			int tl = (Integer)algorithm.map.get(tef_algorithm_param_e.TEF_AUTHTAG_LEN)/8;
			if (r >= tl) {
				byte[] tag = new byte[tl];
				//for (int i=0; i < tl; ++i)tag[i]=edata
				System.arraycopy(edata, r-tl, tag, 0, tl);
				algorithm.map.put(tef_algorithm_param_e.TEF_AUTHTAG, tag);
			}
		}
		return r;
	}

	int tef_decrypt(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] edata, int edataLen, byte[] data) throws GeneralSecurityException {
		javax.crypto.Cipher cipher;
		String algoName = keyid.transformName()+"/"+algorithm.getName();
		cipher = javax.crypto.Cipher.getInstance(algoName);
		cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyid.key);

		byte[] iv=null;
		if (algorithm.map.containsKey(tef_algorithm_param_e.TEF_IV)) {
			iv = (byte[])algorithm.map.get(tef_algorithm_param_e.TEF_IV);
		}
		else if (algorithm.chaining != tef_chaining_mode_e.TEF_ECB) {
			Log.warn("setting IV=ZERO");
			iv = ZERO_IV;
		}
		AlgorithmParameterSpec pspec = null;
		if (algorithm.chaining == tef_chaining_mode_e.TEF_GCM) {
			int taglen = (Integer)algorithm.map.get(tef_algorithm_param_e.TEF_AUTHTAG_LEN);
			pspec = new GCMParameterSpec(taglen, iv); // iv = nonce
		}
		else if (iv != null){
			pspec = new IvParameterSpec(iv, 0, cipher.getBlockSize());
		}
		if (pspec!=null) {
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyid.key, pspec);
		}
		else {
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyid.key);
		}

		if (algorithm.map.containsKey(tef_algorithm_param_e.TEF_AAD)) {
			cipher.updateAAD((byte[])algorithm.map.get(tef_algorithm_param_e.TEF_AAD));
		}

		return cipher.doFinal(edata, 0, edataLen, data);
	}

	int tef_digest(tef_cipher_token keyid, tef_digest_e digest,
			byte[] data, int dataLen, byte[] dig) {
		return 0;
	}

	//keyid can by symmetric or asymetric
	int tef_sign_calc(tef_cipher_token keyid, tef_digest_e digest,
			byte[] data, int dataLen, byte[] sign) {
		return 0;
	}
	//keyid can by symmetric or asymetric
	int tef_sign_verify(tef_cipher_token keyid, tef_digest_e digest,
			byte[] data, int dataLen, byte[] sign) {
		return 0;
	}
}
