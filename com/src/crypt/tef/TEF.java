package crypt.tef;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

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
		return new tef_cipher_token(key_type, new javax.crypto.spec.SecretKeySpec(data, 0, dataLen, keyAlgo));
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
		javax.crypto.Cipher cipher = createCipher(keyid, algorithm);

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

	public int tef_decrypt(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] edata, int edataLen, byte[] data) throws GeneralSecurityException {
		javax.crypto.Cipher cipher = createCipher(keyid, algorithm);

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
	 * Raw CBC-MAC
	 * https://en.wikipedia.org/wiki/ISO/IEC_9797-1
	 * http://www.crypto-it.net/eng/theory/mac.html
	 *
	 */
	public int tef_mac_calc(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] data, int dataLen, byte[] mac) throws GeneralSecurityException {
		javax.crypto.Cipher cipher = createCipher(keyid, algorithm);
		int bs = cipher.getBlockSize();
		if (bs > mac.length) throw new ShortBufferException();

		for (int i=0; i < bs; ++i) mac[i]=0;
		for (int i=0; i < dataLen; i += bs) {
			if (i + bs < dataLen)
				cipher.update(data, i, bs, mac);
			else
				cipher.update(data, i, dataLen - i, mac);
		}
		cipher.doFinal(mac, 0);

		return bs;
	}

	/*
	 * CMAC rfc4493
	 * https://tools.ietf.org/html/rfc4493#ref-NIST-CMAC
	 *
	 * SDRM_rijndaelKeySetupEnc(RoundKey, UserKey, 256);
	 * SDRM_rijndaelEncrypt(RoundKey, 14, plainText, cipherText);
	 * https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-38b.pdf
	 *
	 * TEE_MACInit(operaion *op, *iv, ivlen);
	 *    MacInitW(op->wrapper_operation, &key)
	 *       inf->MacInit(operation, key);
	 *          CeCcImpl::MacInit(operation, key);
	 *             CryptoCoreContainer instance;
	 *             instance->MAC_init(instance, key->secret.value.buffer, key->secret.value.size);
	 *
	 *             crt->MAC_init = SDRM_CMAC_init;
	 *             crt->MAC_init = SDRM_HMAC_init;
	 */
	public int tef_cmac_calc(tef_cipher_token keyid, tef_algorithm algorithm,
			byte[] data, int dataLen, byte[] mac) throws GeneralSecurityException {
		javax.crypto.Cipher cipher = createCipher(keyid, algorithm);
		int bs = cipher.getBlockSize();
		if (bs > mac.length) throw new ShortBufferException();

		BigInteger Rb = BigInteger.ZERO;
		if (bs == 8) {
			Rb = BigInteger.valueOf(0x1B);
		}
		else if (bs == 16) {
			Rb = BigInteger.valueOf(0x87);
		}
		else if (bs == 32) {
			Rb = BigInteger.valueOf(0x425);
		}
		Rb = Rb.setBit(bs*8);
		//Log.debug("Rb = %s", Text.hex(Rb.toByteArray()));

		// 1. Generate_Subkey
		byte[] L = new byte[bs];
		javax.crypto.Cipher cph = createCipher(keyid, new tef_algorithm(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE));
		cph.update(ZERO_IV, 0, bs, L);
		cph.doFinal(L, 0);
		BigInteger K0 = new BigInteger(1, L);
		BigInteger K1, K2;

		//Log.debug("L = %s", Text.hex(L));
		if (K0.testBit(bs*8-1) == false) {
			//Log.debug("K0 shiftL");
			K1 = K0.shiftLeft(1);
		}
		else {
			//Log.debug("K0 shiftL.xor");
			K1 = K0.shiftLeft(1).xor(Rb);
		}
		//Log.debug("K1 = %s", Text.hex(K1.toByteArray()));
		if (K1.testBit(bs*8-1) == false) {
			//Log.debug("K2 = K1 shiftL");
			K2 = K1.shiftLeft(1);
		}
		else {
			//Log.debug("K2 = K1 shiftL.xor");
			K2 = K1.shiftLeft(1).xor(Rb);
		}
		//Log.debug("K2 = %s", Text.hex(K2.toByteArray()));

		// 2. CMAC
		for (int i=0; i < bs; ++i) mac[i]=0;
		for (int i=0; i + bs < dataLen; i += bs) {
			cipher.update(data, i, bs, mac);
			//Log.debug("IV = %s", Text.hex(mac, 0, bs));
		}

		BigInteger mp;
		if (dataLen > 0 && dataLen%bs == 0) {
			mp = new BigInteger(1, Arrays.copyOfRange(data, dataLen-bs, dataLen)).xor(K1);
		}
		else {
			int r = dataLen%bs;
			mp = new BigInteger(1, Arrays.copyOfRange(data, dataLen-r, dataLen)).shiftLeft(1).or(BigInteger.ONE).shiftLeft((bs-r)*8-1).xor(K2);
		}
		L = mp.toByteArray();
		//Log.debug("mp = %s", Text.hex(L));
		cipher.update(L, L.length-bs, bs, mac);
		//Log.debug("IV = %s", Text.hex(mac, 0, bs));
		cipher.doFinal(mac, 0);
		//Log.debug("Final MAC = %s", Text.hex(mac, 0, bs));

		return bs;
	}

	//https://csrc.nist.gov/CSRC/media/Projects/Block-Cipher-Techniques/documents/BCM/proposed-modes/omac/omac-ad.pdf
	public int tef_omac_calc() {
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

	private javax.crypto.Cipher createCipher(tef_cipher_token keyid, tef_algorithm algorithm) throws GeneralSecurityException {
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

		if (algorithm.params.containsKey(tef_algorithm_param_e.TEF_AAD)) {
			cipher.updateAAD((byte[])algorithm.params.get(tef_algorithm_param_e.TEF_AAD));
		}

		return cipher;
	}
}

