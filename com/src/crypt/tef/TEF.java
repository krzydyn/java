package crypt.tef;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import sys.Log;
import text.Text;

public class TEF implements TEF_Types {
	static final byte[] ZERO_IV = new byte[16];

	public tef_cipher_token tef_key_generate(tef_key_type_e key_type, int key_bit_len)
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

		return new tef_cipher_token(key_type, gen.generateKey());
	}

	public tef_cipher_token tef_key_import_raw(tef_key_type_e key_type, byte[] data, int dataLen) {
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
		Log.debug("import key %s/%d from %s",keyAlgo, dataLen*8, Text.hex(data));
		return new tef_cipher_token(key_type, new javax.crypto.spec.SecretKeySpec(data,0,dataLen,keyAlgo));
	}


	int tef_set_param(tef_algorithm algorithm, tef_algorithm_param_e param, Object o) {
		algorithm.set(param, o);
		return 0;
	}
	Object tef_get_param(tef_algorithm algorithm, tef_algorithm_param_e param) {
		return algorithm.get(param);
	}

	public int tef_encrypt(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] data, int dataLen, byte[] edata) throws GeneralSecurityException {
		javax.crypto.Cipher cipher;
		String algoName = keyid.transformName()+"/"+algorithm.getName();
		cipher = javax.crypto.Cipher.getInstance(algoName);

		byte[] iv=null;
		if (algorithm.params.containsKey(tef_algorithm_param_e.TEF_IV)) {
			iv = (byte[])algorithm.params.get(tef_algorithm_param_e.TEF_IV);
		}
		else if (algorithm.chaining != tef_chaining_mode_e.TEF_ECB) {
			iv = ZERO_IV;
		}
		AlgorithmParameterSpec pspec = null;
		if (algorithm.chaining == tef_chaining_mode_e.TEF_GCM) {
			int taglen = (Integer)algorithm.params.get(tef_algorithm_param_e.TEF_TAGLEN);
			pspec = new GCMParameterSpec(taglen, iv);
		}
		else if (iv != null){
			pspec = new IvParameterSpec(iv, 0, cipher.getBlockSize());
		}
		if (pspec!=null) {
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.getKey(), pspec);
		}
		else {
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.getKey());
		}

		if (algorithm.params.containsKey(tef_algorithm_param_e.TEF_AAD)) {
			cipher.updateAAD((byte[])algorithm.params.get(tef_algorithm_param_e.TEF_AAD));
		}

		int r = cipher.doFinal(data, 0, dataLen, edata);
		if (algorithm.chaining == tef_chaining_mode_e.TEF_GCM) {
			int tl = (Integer)algorithm.params.get(tef_algorithm_param_e.TEF_TAGLEN)/8;
			if (r >= tl) {
				byte[] tag = new byte[tl];
				//for (int i=0; i < tl; ++i)tag[i]=edata
				System.arraycopy(edata, r-tl, tag, 0, tl);
				algorithm.params.put(tef_algorithm_param_e.TEF_TAG, tag);
				r -= tl;
			}
		}
		return r;
	}

	int tef_decrypt(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] edata, int edataLen, byte[] data) throws GeneralSecurityException {
		javax.crypto.Cipher cipher;
		String algoName = keyid.transformName()+"/"+algorithm.getName();
		cipher = javax.crypto.Cipher.getInstance(algoName);

		byte[] iv=null;
		if (algorithm.params.containsKey(tef_algorithm_param_e.TEF_IV)) {
			iv = (byte[])algorithm.params.get(tef_algorithm_param_e.TEF_IV);
		}
		else if (algorithm.chaining != tef_chaining_mode_e.TEF_ECB) {
			iv = ZERO_IV;
		}
		AlgorithmParameterSpec pspec = null;
		if (algorithm.chaining == tef_chaining_mode_e.TEF_GCM) {
			int taglen = (Integer)algorithm.params.get(tef_algorithm_param_e.TEF_TAGLEN);
			pspec = new GCMParameterSpec(taglen, iv); // iv = nonce
		}
		else if (iv != null){
			pspec = new IvParameterSpec(iv, 0, cipher.getBlockSize());
		}
		if (pspec!=null) {
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyid.getKey(), pspec);
		}
		else {
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keyid.getKey());
		}

		if (algorithm.params.containsKey(tef_algorithm_param_e.TEF_AAD)) {
			cipher.updateAAD((byte[])algorithm.params.get(tef_algorithm_param_e.TEF_AAD));
		}

		return cipher.doFinal(edata, 0, edataLen, data);
	}

	public int tef_digest(tef_digest_e digest, byte[] data, int dataLen, byte[] dig) throws GeneralSecurityException {
		MessageDigest md = MessageDigest.getInstance(digest.getName());
		md.update(data, 0, dataLen);
		byte[] d = md.digest();
		if (d.length > dig.length) throw new ShortBufferException();
		System.arraycopy(d, 0, dig, 0, d.length);
		return d.length;
	}

	/*
	 * CBC-MAC https://en.wikipedia.org/wiki/ISO/IEC_9797-1
	 */
	public int tef_mac_calc(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] data, int dataLen, byte[] mac) throws GeneralSecurityException {
		javax.crypto.Cipher cipher;
		String algoName = keyid.transformName()+"/"+algorithm.getName();
		cipher = javax.crypto.Cipher.getInstance(algoName);
		int bs = cipher.getBlockSize();

		byte[] iv=null;
		if (algorithm.params.containsKey(tef_algorithm_param_e.TEF_IV)) {
			iv = (byte[])algorithm.params.get(tef_algorithm_param_e.TEF_IV);
		}
		else if (algorithm.chaining != tef_chaining_mode_e.TEF_ECB) {
			iv = ZERO_IV;
		}
		AlgorithmParameterSpec pspec = null;
		if (algorithm.chaining == tef_chaining_mode_e.TEF_GCM) {
			int taglen = (Integer)algorithm.params.get(tef_algorithm_param_e.TEF_TAGLEN);
			pspec = new GCMParameterSpec(taglen, iv); // iv = nonce
		}
		else if (iv != null){
			pspec = new IvParameterSpec(iv, 0, cipher.getBlockSize());
		}
		if (pspec!=null) {
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.getKey(), pspec);
		}
		else {
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keyid.getKey());
		}

		if (bs > mac.length) throw new ShortBufferException();
		for (int i=0; i < mac.length; ++i) mac[i]=0;
		for (int i=0; i < dataLen; i += bs) {
			cipher.update(data, i, bs, mac);
		}
		cipher.doFinal(mac, 0);
		return bs;
	}

	/*
	 * CMAC rfc4493
	 */
	public int tef_cmac_calc(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] data, int dataLen, byte[] mac) throws GeneralSecurityException {
		// 1. Generate_Subkey
		// 2. AES-CMAC
		return 0;
	}

	//keyid can be symmetric or asymmetric
	public int tef_sign_calc(tef_cipher_token keyid, tef_digest_e digest, byte[] data, int dataLen, byte[] sign) {
		return 0;
	}
	//keyid can be symmetric or asymmetric
	int tef_sign_verify(tef_cipher_token keyid, tef_digest_e digest, byte[] data, int dataLen, byte[] sign) {
		return 0;
	}
}

