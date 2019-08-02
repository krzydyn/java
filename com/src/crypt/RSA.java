/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package crypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

import sys.Log;
import text.Text;

/*
 * RSA-PSS (RFC 4055)
 * mbed TLS fully supports RSASSA-PSS directly in its RSA module.
 * In order to use RSA as specified in PKCS#1 v2.1 with for instance SHA1 as the hash method,
 * you should initialize your RSA context with:
 *
 * mbedtls_rsainit( &rsa, RSA_PKCS_V21, MBEDTLS_MD_SHA256);
 *
 * After loading the RSA key into that context, you can then use it to sign using the RSASSA-PSS scheme
 * by using the generic mbedtls_rsapkcs1_sign() for signing and mbedtls_rsapkcs1_verify() for verification
 * or the more specific mbedtls_rsarsassa_pss_sign() and mbedtls_rsarsassa_pss_verify().
 */

public class RSA extends Asymmetric {
	final static BigInteger[] pubExpCandidates = {
		BigInteger.valueOf(3),
		BigInteger.valueOf(5),
		BigInteger.valueOf(17),
		BigInteger.valueOf(257),
		BigInteger.valueOf(65537),
	};

	private BigInteger p,q;
	private BigInteger e,d,N;

	public RSA(BigInteger p, BigInteger q, BigInteger e) {
		this.p = p;
		this.q = q;
		this.e = e;
		N = p.multiply(q);
		BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
		d = e.modInverse(phi);
		Log.debug("modulus[%d] = %s", N.toByteArray().length, Text.hex(N.toByteArray()));
		Log.debug("e[%d] %s", e.toByteArray().length, Text.hex(e.toByteArray()));
		Log.debug("d[%d] %s", d.toByteArray().length, Text.hex(d.toByteArray()));
	}

	public RSA(BigInteger e, BigInteger d, BigInteger N, boolean pq) {
		this.e = e; // public exponent
		this.d = d; // private exponent
		this.N = N; // modulus

		if (!pq) return ;
		//recover p,q
		// http://csrc.nist.gov/publications/nistpubs/800-56B/sp800-56B.pdf (Appendix C)
		//1. k=d*e-1
		BigInteger k = d.multiply(e).subtract(ONE);
		if (k.testBit(0)) { //prime factors not found
			throw new RuntimeException("no factors found");
		}
		//2. write k as k = 2^t * r (divide k by 2 until get odd number)
		BigInteger r = k;
		BigInteger t = ZERO;
		do {
			r = r.divide(TWO);
			t = t.add(ONE);
		} while (!r.testBit(0));
		//3. loop 1 .. 100
		BigInteger y = null;
		boolean found=false;
		for (int i=0; i < 100; ++i) {
			//3.a
			BigInteger g = genBigInteger(N);
			//3.b
			y = g.modPow(r, N);
			//3.c
			if (y.equals(ONE) || y.equals(N.subtract(ONE))) continue;
			//3.d
			for (BigInteger j=ONE; j.compareTo(t)<0; j.add(ONE)) {
				//3.d.I
				BigInteger x = y.modPow(TWO, N);
				//3.d.II
				if (x.equals(ONE)) {
					found=true;
					break;
				}
				//3.d.III
				if (x.equals(N.subtract(ONE))) {
					break;
				}
				//3.d.IV
				y=x;
			}
			if (found) break;

			//3.e
			BigInteger x = y.modPow(TWO, N);
			//3.f
			if (x.equals(ONE)) {
				found=true;
				break;
			}
			//3.g (continue loop i)
		}
		//4.
		if (!found) {
			throw new RuntimeException("no factors found");
		}
		//5.
		p = y.subtract(ONE).gcd(N);
		q = N.divide(p);

		Log.debug("p[%d] %s", p.toByteArray().length, Text.hex(p.toByteArray()));
		Log.debug("q[%d] %s", q.toByteArray().length, Text.hex(q.toByteArray()));
	}

	public RSA(int bits) {
		p = BigInteger.probablePrime(bits/2, rnd);
		q = BigInteger.probablePrime(bits/2, rnd);
		N = p.multiply(q);

		BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
		for (int i=0; i < pubExpCandidates.length; ++i) {
			e = pubExpCandidates[pubExpCandidates.length-1-i];
			if (e.compareTo(N) < 0) break;
		}
		d = e.modInverse(phi);
		Log.notice("Input: bits=%d",bits);
		Log.debug("modulus[%d] = %s", N.toByteArray().length, Text.hex(N.toByteArray()));
		Log.debug("e[%d] %s", e.toByteArray().length, Text.hex(e.toByteArray()));
		Log.debug("d[%d] %s", d.toByteArray().length, Text.hex(d.toByteArray()));
	}

	static BigInteger genBigInteger(BigInteger max) {
		BigInteger r = new BigInteger(max.bitLength(), rnd);
		if (r.compareTo(max) >= 0) {
			for (int bit=max.bitLength()-1; bit > 0; --bit) {
				if (r.testBit(bit)) {
					r.clearBit(bit);
					break;
				}
			}
		}
		else r.setBit(max.bitLength()-2);
		return r;
	}

	/*
	Select a value of e from {3, 5, 17, 257, 65537}
	repeat
	   p = genprime(k/2)
	until (p mod e) !=  1
	repeat
	   q = genprime(k - k/2)
	until (q mod e) !=  1
	N = p*q
	phi = (p-1)*(q-1)
	d = modinv(e, phi)
	return (N, e, d)
	*/
	public RSA(int bits, BigInteger e) {
		do {
			p = BigInteger.probablePrime(bits/2, rnd);
		} while (p.subtract(ONE).gcd(e).compareTo(ONE) != 0);

		do {
			q = BigInteger.probablePrime(bits-bits/2, rnd);
		} while (q.subtract(ONE).gcd(e).compareTo(ONE) != 0);

		this.e = e;
		N = p.multiply(q);
		BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
		d = e.modInverse(phi);

		Log.notice("Input: bits=%d, exponet (e)",bits);
		Log.debug("e[%d] %s", e.toByteArray().length, Text.hex(e.toByteArray()));
		Log.debug("d[%d] %s", d.toByteArray().length, Text.hex(d.toByteArray()));
		Log.debug("modulus[%d] = %s", N.toByteArray().length, Text.hex(N.toByteArray()));
	}

	/*
	 * RSAEP ((n, e), m)
	 * Input: (n, e) RSA public key
	 *         m     message representative, an integer between 0 and n – 1
	 * Output: c     ciphertext representative, an integer between 0 and n – 1
	 * Error: “message representative out of range”
	 */
	public byte[] encrypt(byte[] msg) {
		int keylen = (N.bitLength()+7)/8;
		Log.debug("encr(N.len=%d, keylen=%d)", N.bitLength(), keylen);
		if (msg.length > keylen) throw new RuntimeException("message too long");
		//int m = (0xff << (N.bitLength()&7))&0xff;
		//if (msg.length == keylen && (msg[0]&m) != 0)
		//	throw new RuntimeException(String.format("message too long, N.bitLength()=%d", N.bitLength()));
		return i2osp(new BigInteger(1, msg).modPow(e, N), keylen);
	}

	public byte[] decrypt(byte[] cmsg) {
		int keylen = (N.bitLength()+7)/8;
		if (cmsg.length > keylen) throw new RuntimeException("message too long");
		// First form (n,d) is used
		//return new BigInteger(1, cmsg).modPow(d, N).toByteArray();
		byte[] x = new BigInteger(1, cmsg).modPow(d, N).toByteArray();
		//TODO Second form (p,q,dP,dQ,qInv) and (rb,db,ti) is used
		if (x[0] == 0) return Arrays.copyOfRange(x, 1, x.length);
		return x;
	}

	/**
	 * Sign a message digest of the information to be sent.
	 * Represents this digest as an integer m between 1 and n-1..
	 * Send this signature s to the recipient.
	*/
	byte[] sign(byte[] hash) {
		return decrypt(hash);
	}


	public byte[] encrypt_OAEP(String param, byte[] msg, MessageDigest md) throws Exception {
		byte[] em = padOAEP(param, msg, (N.bitLength() + 7)/8 - 1, md);
		return encrypt(em);
	}

	public byte[] decrypt_OAEP(String param, byte[] msg, MessageDigest md) throws Exception {

		byte[] em = decrypt(msg);
		return unpadOAEP(param, em, md);
	}

	/**
	 * Verify a message digest of the information received.
	 * Take sender public key (n, e) to compute integer v = m^e mod n.
	 * Extract the message digest from this integer and compare to given value.
	 * If both message digests are identical, the signature is valid.
	 */
	boolean verify(byte[] hash, byte[] signature) {
		return Arrays.equals(hash, encrypt(signature));
	}

	final private static byte[] ZERO8 = new byte[8];
	final private static byte[] PAD_00 = {0x00};
	final private static byte[] PAD_01 = {0x01};
	final private static byte[] PAD_0102 = {0x01, 0x02};
	final private static byte[] PAD_BC = {(byte)0xbc};

	/*
	 * Padding schemes
	 * 1. Basic scheme
	 *     m(M) = Hash(M)
	 * 2. ANSI X9.31
	 *     m(M) = 6b bb … bb ba || Hash(M) || 3x cc
	 *     where x = 3 for SHA-1, 1 for RIPEMD-160
	 * 3. PKCS #1 v1.5 (widely deployed, SSL certs)
	 *     m(M) = 00 01 ff … ff 00 || HashAlgID || Hash(M)
	 * 4. Bellare-Rogaway FDH (Full Domain Hashing)
	 *     m(M) = Full-Length-Hash(m)
	 * 5. Bellare-Rogaway PSS (Probabilistic Signature Scheme, Eurocrypt ’96)
	 *     m(M) » H || G(H) xor salt
	 *     where H = Hash(salt, M), salt is random, and G is a mask generation function
	 */

	/*
	 * https://csrc.nist.gov/projects/cryptographic-algorithm-validation-program/component-testing
	 * https://csrc.nist.gov/projects/cryptographic-algorithm-validation-program/digital-signatures
	 *
	 * ftp://ftp.rsasecurity.com/pub/pkcs/pkcs-1/pkcs-1v2-1.pdf
	 * http://www.inf.pucrs.br/calazans/graduate/TPVLSI_I/RSA-oaep_spec.pdf !!!!
	 *
	 * RSAES-OAEP - improved Encryption/decryption Scheme; based on the Optimal Asymmetric Encryption Padding scheme
	 * Padding OAEP (optimal asymmetric encryption padding)
	 *
	 */
	static public byte[] padOAEP(String param, byte[] msg, int emLen, MessageDigest md) throws Exception {
		return padOAEP(param, msg, emLen, md, null);
	}
	static public byte[] padOAEP(String param, byte[] msg, int emLen, MessageDigest md, byte[] seed) throws Exception {
		int hLen = md.getDigestLength();
		if (emLen < msg.length + 2*hLen + 1) throw new RuntimeException("message too long");
		byte[] pHash = (param != null) ? md.digest(param.getBytes()) : md.digest();

		byte[] ps = new byte[emLen - msg.length - 2*hLen - 1];
		byte[] db = concat(pHash, ps, PAD_01, msg);

		Log.debug(">>> padOAEP for emLen=%d", emLen);
		Log.debug("DB[%d]: %s", db.length, Text.hex(db));

		if (seed == null) {
			seed = new byte[hLen];
			rnd.nextBytes(seed);
		}

		byte[] dbMask = mgf1(seed, emLen - hLen, md);
		byte[] maskedDB = i2osp(xor(db, dbMask), dbMask.length);
		byte[] seedMask = mgf1(maskedDB, hLen, md);
		byte[] maskedSeed = i2osp(xor(seed, seedMask), seedMask.length);
		//Log.debug("seed[%d]: %s", seed.length, Text.hex(seed));
		//Log.debug("dbMask[%d]: %s", dbMask.length, Text.hex(dbMask));
		Log.debug("maskedDB[%d]: %s", maskedDB.length, Text.hex(maskedDB));
		//Log.debug("seedMask[%d]: %s", seedMask.length, Text.hex(seedMask));
		Log.debug("maskedSeed[%d]: %s", maskedSeed.length, Text.hex(maskedSeed));
		return concat(maskedSeed, maskedDB);
	}

	static public byte[] unpadOAEP(String param, byte[] emsg, MessageDigest md) {
		int emLen = emsg.length;
		int hLen = md.getDigestLength();
		if (emLen < 2*hLen + 1) throw new RuntimeException("decoding error");

		byte[] maskedSeed = Arrays.copyOfRange(emsg, 0, hLen);
		byte[] maskedDB = Arrays.copyOfRange(emsg, hLen, emLen);
		Log.debug("maskedSeed[%d]: %s", maskedSeed.length, Text.hex(maskedSeed));
		Log.debug("maskedDB[%d]: %s", maskedDB.length, Text.hex(maskedDB));
		byte[] seedMask = mgf1(maskedDB, hLen, md);
		byte[] seed = i2osp(new BigInteger(1, maskedSeed).xor(new BigInteger(1, seedMask)), hLen);
		byte[] dbMask = mgf1(seed, emLen - hLen, md);
		Log.debug("seed[%d]: %s", seed.length, Text.hex(seed));
		Log.debug("dbMask[%d]: %s", dbMask.length, Text.hex(dbMask));
		byte[] db = i2osp(new BigInteger(1, maskedDB).xor(new BigInteger(1, dbMask)), dbMask.length);

		Log.debug("DB[%d]: %s", db.length, Text.hex(db));

		byte[] msg = null;
		for (int i = hLen; i < db.length; ++i) {
			if (db[i] == 0x01) {
				msg =  Arrays.copyOfRange(db, i+1, db.length);
				break;
			}
		}
		if (msg == null) throw new RuntimeException("decoding error");

		return msg;
	}

	static public byte[] padPKCS1v15(byte[] msg, int emLen) {
		if (emLen < msg.length + 3)
			throw new RuntimeException("message too long");
		byte[] ps = new byte[emLen - msg.length - 3];
		return concat(PAD_0102, ps, PAD_00, msg);
	}

	/*
	 * https://www.emc.com/collateral/white-papers/h11300-pkcs-1v2-2-rsa-cryptography-standard-wp.pdf
	 */
	static public byte[] padEMSA_PKCS1v15(byte[] msg, int emLen, MessageDigest md) {
		byte[] H = md.digest(msg);
		byte[] id_MD2 = Text.bin("2A864886F70D0202");
		byte[] id_MD5 = Text.bin("2A864886F70D0205");
		byte[] id_SHA1 = Text.bin("2B0E03021A");
		byte[] id_SHA2 = Text.bin("608648016503040203");
		byte[] id_hash = null;
		if (md.getAlgorithm().equals("md5"))
			id_hash = id_MD5;
		/*
		byte[] id_SHA256 = Text.bin("608648016503040203");
		byte[] id_SHA384 = Text.bin("608648016503040203");
		byte[] id_SHA512 = Text.bin("608648016503040203");
		*/
		/* T coding:
		 * MD2: (0x)30 20 30 0c 06 08 2a 86 48 86 f7 0d 02 02 05 00 04 10 || H
		 * MD5: (0x)30 20 30 0c 06 08 2a 86 48 86 f7 0d 02 05 05 00 04 10 || H
		 * SHA-1: (0x)30 21 30 09 06 05 2b 0e 03 02 1a 05 00 04 14 || H
		 * SHA-256: (0x)30 51 30 0d 06 09 60 86 48 01 65 03 04 02 03 05 00 04 20 || H
		 * SHA-384: (0x)30 51 30 0d 06 09 60 86 48 01 65 03 04 02 03 05 00 04 30 || H
		 * SHA-512: (0x)30 51 30 0d 06 09 60 86 48 01 65 03 04 02 03 05 00 04 40 || H
		 */
		byte[] T = H; //TODO T = DER(hash_algo_id(T=06), null(T=05), digest(T=04))
		if (emLen < T.length + 11)
			throw new RuntimeException("intended encoded message length too short”");
		return padPKCS1v15(T, emLen);
	}

	/*
	 *  https://www.javatips.net/api/junrar-android-master/src/gnu/crypto/sig/rsa/EMSA_PSS.java
	 *  https://github.com/pyca/cryptography/blob/master/vectors/cryptography_vectors/asymmetric/RSA/pkcs-1v2-1d2-vec/pss-vect.txt
	 */
	static public byte[] padEMSA_PSS(byte[] mHash, int emBits, MessageDigest md) {
		md.reset();
		int emLen = (emBits + 7)/8;
		int hLen = md.getDigestLength();
		int sLen = hLen;
		if (emLen < hLen + sLen + 2)
			throw new RuntimeException("intended encoded message length too short");
		byte[] seed = new byte[sLen];
		if (sLen > 0) rnd.nextBytes(seed);
		md.update(ZERO8); md.update(mHash); md.update(seed);
		byte[] H = md.digest();
		byte[] ps = new byte[emLen - sLen - hLen - 2];
		byte[] db = concat(ps, PAD_01, seed);
		byte[] dbMask = mgf1(H, emLen - hLen - 1, md);
		xor(db, dbMask);
		if (8*emLen > emBits) {
			db[0] &= 0xff >> (8*emLen - emBits);
		}
		return concat(db, H, PAD_BC);
	}
	static public boolean unpadEMSA_PSS(byte[] em, byte[] mHash, int emBits, MessageDigest md) {
		md.reset();
		int emLen = (emBits + 7)/8;
		int hLen = md.getDigestLength();
		int sLen = hLen;
		if (emLen < hLen + sLen + 2) throw new RuntimeException("inconsistent");
		if (em[em.length-1] != (byte)0xbc) throw new RuntimeException("inconsistent");
		if ((em[0] & (0xff << (emBits&7))) != 0) throw new RuntimeException("inconsistent");

		byte[] db = Arrays.copyOf(em, emLen - hLen - 1);
		//Log.info("maskedDB[%d] %s", db.length, Text.hex(db));
		byte[] H = Arrays.copyOfRange(em, emLen - hLen - 1, emLen - 1);
		byte[] dbMask = mgf1(H, emLen - hLen - 1, md);
		xor(db, dbMask);
		db[0] &= 0xff >> (emBits&7); // db = concat(ps, pad1, seed);
		//Log.info("DB[%d] %s", db.length, Text.hex(db));
		for (int i = 0; i < db.length - sLen - 1; ++i) {
			if (db[i] != 0x00) throw new RuntimeException("inconsistent");
		}
		if (db[db.length - sLen - 1] != 0x01) throw new RuntimeException("inconsistent");

		byte[] seed = Arrays.copyOfRange(db, db.length - sLen, db.length);
		md.update(ZERO8); md.update(mHash); md.update(seed);
		byte[] mH = md.digest();
		return Arrays.equals(mH, H);
	}


	/*
	 * https://rsapss.hboeck.de/rsapss.pdf
	 * 1. take input msg and salt and run them through hash function -> H
	 * 2. calc mask of H which has length of RSA key modulus minus length(H)
	 * 3. maskDB = mask xor with salt, pad zero
	 * 4. result = H || maskDB
	 */
	static public byte[] padPSS_orig96(byte[] msg, int keylen, MessageDigest md) {
		int hLen = md.getDigestLength();
		int sLen = hLen;
		if (keylen < hLen+sLen) throw new RuntimeException();
		byte[] seed = new byte[sLen];
		if (sLen > 0) rnd.nextBytes(seed);
		byte[] w = mgf1(concat(seed, msg), hLen, md);
		byte[] expw = mgf1(w, keylen - hLen, md);
		byte[] smask = Arrays.copyOf(expw, sLen);
		byte[] rmask = Arrays.copyOfRange(expw, sLen, expw.length);
		byte[] mask = i2osp(new BigInteger(1,seed).xor(new BigInteger(1,smask)), keylen);
		return concat(w, mask, rmask);
	}

	/*
	 * Chinese Remainder Theorem (CRT)
	 * calc helpers:
	 *   dP = (1/e) mod (p-1) = d mod (p-1)
	 *   dQ = (1/e) mod (q-1) = d mod (q-1)
	 *   qInv = (1/q) mod p  where p > q
	 *
	 * decrypt cyphered text c:
	 *   m1 = c^dP mod p
	 *   m2 = c^dQ mod q
	 *   hq = qInv(m1 - m2) mod p
	 *   m = m2 + hq
	 */
}
