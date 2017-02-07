package crypt;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import sys.Log;

public class TEF implements TEF_Types {

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

		tef_cipher_token token = new tef_cipher_token();
		token.key = gen.generateKey();
		token.algo = algorithm;
		return token;
	}

	tef_cipher_token tef_key_import_raw(tef_key_type_e key_type,
            byte[]             data,
            int                dataLen,
            tef_algorithm_info algorithm) {

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
		token.key = new javax.crypto.spec.SecretKeySpec(data,0,dataLen,keyAlgo);
		token.algo = algorithm;
		return token;
	}


	tef_algorithm_params tef_create_params() {
		return new tef_algorithm_params();
	}

	int tef_set_param(tef_algorithm_params  params, tef_algorithm_param_e param,
			byte[] data, int dataLen) {
		return 0;
	}

	int tef_encrypt(tef_cipher_token keyid, tef_algorithm_params params,
			byte[] data, int dataLen, byte[] edata) throws GeneralSecurityException {
		javax.crypto.Cipher cipher;

		//.getInstance(SERVICE, alg + '/' + mode, provider);
		//cipher = new AESCipher(new CryptX_AES(), keyid.transformName());
		//cipher = javax.crypto.Cipher.getInstance(keyid.transformName(), new CryptX_Provider());
		cipher = javax.crypto.Cipher.getInstance(keyid.transformName());
		if (params == null) {
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key);
		}
		else {
			byte[] iv=null;
			if (params.map.containsKey(tef_algorithm_param_e.TEF_IV)) {
				iv = (byte[])params.map.get(tef_algorithm_param_e.TEF_IV);
			}
			AlgorithmParameterSpec pspec = null;
			AlgorithmParameters param = AlgorithmParameters.getInstance(keyid.key.getAlgorithm());
			if (keyid.algo.chaining == tef_chaining_mode_e.TEF_GCM) {
				int taglen = (Integer)params.map.get(tef_algorithm_param_e.TEF_AUTHTAG_LEN);
				pspec = new GCMParameterSpec(taglen, iv);
			}
			else {
				pspec = new IvParameterSpec(iv);
			}
			param.init(pspec);
			//cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key, pspec);
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.key, param);

			if (keyid.algo.chaining == tef_chaining_mode_e.TEF_GCM) {
				if (params.map.containsKey(tef_algorithm_param_e.TEF_AAD)) {
					cipher.updateAAD((byte[])params.map.get(tef_algorithm_param_e.TEF_AAD));
				}
			}
		}

		return cipher.doFinal(data, 0, dataLen, edata);
	}

	int tef_decrypt(tef_cipher_token keyid, tef_algorithm_params params,
			byte[] edata, int edataLen, byte[] data) throws GeneralSecurityException {
		javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(keyid.transformName());
		cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyid.key);

		return cipher.doFinal(data, 0, edataLen, edata);
	}
}
