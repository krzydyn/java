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
import java.util.Random;

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
		Random rnd = new Random();
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
			BigInteger g = genBigInteger(N, rnd);
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
	}

	public RSA(int bits) {
		Random rnd = new Random();
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

	static BigInteger genBigInteger(BigInteger max, Random rnd) {
		BigInteger r = new BigInteger(max.bitLength(), rnd);
		if (r.compareTo(max) >= 0) {
			for (int bit=max.bitLength()-1; bit > 0; --bit) {
				if (r.testBit(bit)) {
					r.clearBit(bit);
					break;
				}
			}
		}
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
		Random rnd = new Random();

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
		if (msg.length*8 > N.bitLength()) throw new RuntimeException("message too long");
		return new BigInteger(1, msg).modPow(e, N).toByteArray();
	}

	public byte[] decrypt(byte[] cmsg) {
		int keylen = (N.bitLength()+7)/8;
		if (cmsg.length > keylen) throw new RuntimeException("message wrong length");
		// First form (n,d) is used
		return padZeroL(new BigInteger(1, cmsg).modPow(d, N).toByteArray(), keylen);
		//TODO Second form (p,q,dP,dQ,qInv) and (rb,db,ti) is used
	}

	/*
	 * Use private RSA key to sign (call decrypt!)
	 * TODO add padding mode as parameter
	 */
	byte[] sign(byte[] hash) {
		/*
		Creates a message digest of the information to be sent.
		Represents this digest as an integer m between 1 and n-1..
		Take private key (n, d) to compute the signature s = m^d mod n.
		Sends this signature s to the recipient.
		*/
		return decrypt(hash); //use private key
	}

	/*
	 * Use public RSA key to sign (call encrypt!)
	 * TODO add padding mode as parameter
	 */
	boolean verify(byte[] hash, byte[] signature) {
		/*
		Creates a message digest of the information received.
		Take sender public key (n, e) to compute inteer v = m^e mod n.
		Extracts the message digest from this integer.
		If both message digests are identical, the signature is valid.
		*/
		return Arrays.equals(hash, encrypt(signature)); //use public key
	}

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
	 * https://github.com/pyca/cryptography/blob/master/vectors/cryptography_vectors/asymmetric/RSA/pkcs-1v2-1d2-vec/pss-vect.txt
	 * pkcs1v2-1cryptographystandardincluded24-0908240005.pdf (https://www.slideshare.net/gueste9eb7fb/pkcs1-v2)
	 *
	 * Padding OAEP (optimal asymmetric encryption padding)
	 */
	static public byte[] padOEAP(String lbl, byte[] msg, int keylen, MessageDigest md) throws Exception {
		int hLen = md.getDigestLength();
		if (msg.length >= keylen - 2*hLen - 2)
			throw new RuntimeException("message too long");
		//Random rnd = new Random();
		byte[] pad0 = {0x00};
		byte[] pad1 = {0x01};
		byte[] lHash = {};
		if (lbl != null) lHash = md.digest(lbl.getBytes());
		else lHash = md.digest();
		byte[] ps = new byte[keylen - msg.length - 2*hLen - 2];
		byte[] db = concat(lHash, ps, pad1, msg);
		byte[] seed = new byte[hLen];
		//rnd.nextBytes(seed);
		byte[] dbMask = mgf(seed, keylen - hLen - 1, md);
		byte[] maskedDB = padZeroL(new BigInteger(1, db).xor(new BigInteger(1, dbMask)).toByteArray(), keylen - hLen - 1);
		byte[] seedMask = mgf(maskedDB, hLen, md);
		byte[] maskedSeed = padZeroL(new BigInteger(1, seed).xor(new BigInteger(1, seedMask)).toByteArray(), hLen);
		return concat(pad0, maskedSeed, maskedDB);
	}

	static public byte[] padPKCS1v15(byte[] msg, int keylen) {
		if (msg.length >= keylen - 11)
			throw new RuntimeException("message too long");
		byte[] pad0 = {0x00};
		byte[] pad1 = {0x01, 0x02};
		byte[] ps = new byte[keylen - msg.length - 1];
		return concat(pad1, ps, pad0, msg);
	}

	static public byte[] padEMSA_PKCS1v15(byte[] msg, int keylen, MessageDigest md) {
		if (msg.length >= keylen - 11)
			throw new RuntimeException("message too long");
		byte[] pad0 = {0x00};
		byte[] pad1 = {0x01, 0x02};
		byte[] H = md.digest(msg);
		/* T coding:
		 * MD2: (0x)30 20 30 0c 06 08 2a 86 48 86 f7 0d 02 02 05 00 04 10 || H
		 * MD5: (0x)30 20 30 0c 06 08 2a 86 48 86 f7 0d 02 05 05 00 04 10 || H
		 * SHA-1: (0x)30 21 30 09 06 05 2b 0e 03 02 1a 05 00 04 14 || H
		 * SHA-256: (0x)30 51 30 0d 06 09 60 86 48 01 65 03 04 02 03 05 00 04 20 || H
		 * SHA-384: (0x)30 51 30 0d 06 09 60 86 48 01 65 03 04 02 03 05 00 04 30 || H
		 * SHA-512: (0x)30 51 30 0d 06 09 60 86 48 01 65 03 04 02 03 05 00 04 40 || H
		 */
		byte[] T = {};
		byte[] ps = new byte[keylen - T.length - 3];
		return concat(pad1, ps, pad0, T);
	}


	// https://www.javatips.net/api/junrar-android-master/src/gnu/crypto/sig/rsa/EMSA_PSS.java
	static public byte[] padEMSA_PSS(byte[] mHash, int emBits, MessageDigest md) {
		md.reset();
		int emLen = (emBits + 7)/8;
		int hLen = md.getDigestLength();
		int sLen = hLen;
		if (emLen < hLen + sLen + 2) throw new RuntimeException("Encoding error");
		byte[] pad8 = new byte[8];
		byte[] pad1 = {0x01};
		byte[] padBC = {(byte)0xbc};
		//Random rnd = new Random();
		byte[] seed = new byte[sLen];
		//if (sLen > 0) rnd.nextBytes(seed);
		md.update(pad8); md.update(mHash); md.update(seed);
		byte[] H = md.digest();
		//Log.info("H[%d] %s", H.length, Text.hex(H));
		byte[] ps = new byte[emLen - sLen - hLen - 2];
		byte[] db = concat(ps, pad1, seed);
		//Log.info("DB[%d] %s", db.length, Text.hex(db));
		byte[] dbMask = mgf(H, emLen - hLen - 1, md);
		//Log.info("dbMask[%d] %s", dbMask.length, Text.hex(dbMask));
		xor(db, dbMask);
		if (8*emLen > emBits) {
			db[0] &= 0xff >> (8*emLen - emBits);
		}
		//Log.info("maskedDB[%d] %s", db.length, Text.hex(db));
		return concat(db, H, padBC);
	}
	static public boolean unpadEMSA_PSS(byte[] em, byte[] mHash, int emBits, MessageDigest md) {
		md.reset();
		int emLen = (emBits + 7)/8;
		int hLen = md.getDigestLength();
		int sLen = hLen;
		if (emLen < hLen + sLen + 2) throw new RuntimeException("inconsistent");
		if (em[em.length-1] != (byte)0xbc) throw new RuntimeException("inconsistent");
		if ((em[0] & (0xff << (emBits&7))) != 0) throw new RuntimeException("inconsistent");

		byte[] pad8 = new byte[8];
		byte[] db = Arrays.copyOf(em, emLen - hLen - 1);
		//Log.info("maskedDB[%d] %s", db.length, Text.hex(db));
		byte[] H = Arrays.copyOfRange(em, emLen - hLen - 1, emLen - 1);
		byte[] dbMask = mgf(H, emLen - hLen - 1, md);
		xor(db, dbMask);
		db[0] &= 0xff >> (emBits&7); // db = concat(ps, pad1, seed);
		//Log.info("DB[%d] %s", db.length, Text.hex(db));
		for (int i = 0; i < db.length - sLen - 1; ++i) {
			if (db[i] != 0) throw new RuntimeException("inconsistent at "+i);
		}
		if (db[db.length - sLen - 1] != 1) throw new RuntimeException("inconsistent");

		byte[] seed = Arrays.copyOfRange(db, db.length - sLen, db.length);
		md.update(pad8); md.update(mHash); md.update(seed);
		byte[] mH = md.digest();
		return Arrays.compare(mH, H) == 0;
	}


	/*
	 * https://rsapss.hboeck.de/rsapss.pdf
	 * 1. take input msg and salt and run them through hash function -> H
	 * 2. calc mask of H which has length of RSA key modulus minus length(H)
	 * 3. maskDB = mask xor with salt, pad zero
	 * 4. result = H || maskDB
	 */
	static public byte[] padPSS_orig96(byte[] msg, int keylen, MessageDigest md) {
		int wLen = 20, seedLen = 20;
		if (keylen < wLen+seedLen) throw new RuntimeException();
		Random rnd = new Random();
		byte[] seed = new byte[seedLen];
		rnd.nextBytes(seed);
		byte[] w = mgf(concat(seed, msg), wLen, md);
		byte[] expw = mgf(w, keylen - wLen, md);
		byte[] smask = Arrays.copyOf(expw, seedLen);
		byte[] rmask = Arrays.copyOfRange(expw, seedLen, expw.length);
		byte[] mask = new BigInteger(1,seed).xor(new BigInteger(1,smask)).toByteArray();
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
	 *   h = qInv(m1 - m2) mod p
	 *   m = m2 + hq

	 */
}
