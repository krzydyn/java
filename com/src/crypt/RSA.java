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

	public RSA(BigInteger e, BigInteger d, BigInteger N) {
		this.e = e; // public exponent
		this.d = d; // private exponent
		this.N = N; // modulus

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

		/* phi = (p-1)*(q-1);
		 * while (gdc(e,phi)==1 && e < phi) e+=2;
		 */
		BigInteger phi = p.subtract(ONE).multiply(q.subtract(ONE));
		/*if (bits > 8) e = BigInteger.probablePrime(bits/4+1, r);
		else e = BigInteger.valueOf(3);
		while (phi.gcd(e).compareTo(ONE) > 0 && e.compareTo(phi) < 0) {
			e.add(TWO);
		}*/
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

	BigInteger genBigInteger(BigInteger max, Random rnd) {
		BigInteger r = new BigInteger(max.bitLength(), rnd);
		if (r.compareTo(max) >= 0) {
			for (int bit=max.bitLength()-1; bit > 0; --bit) {
				if (r.testBit(bit)) {
					r.clearBit(bit);
					//if (bit>0) r.setBit(bit-1);
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
		Log.debug("modulus[%d] = %s", N.toByteArray().length, Text.hex(N.toByteArray()));
		Log.debug("e[%d] %s", e.toByteArray().length, Text.hex(e.toByteArray()));
		Log.debug("d[%d] %s", d.toByteArray().length, Text.hex(d.toByteArray()));
	}

	public byte[] encrypt(byte[] msg) {
		if (msg.length*8 > N.bitLength()) throw new RuntimeException("message too long");
		return new BigInteger(1, msg).modPow(e, N).toByteArray();
	}

	public byte[] decrypt(byte[] msg) {
		if (msg.length*8 != N.bitLength()) throw new RuntimeException("message wrong length");
		return new BigInteger(1, msg).modPow(d, N).toByteArray();
	}

	/*
	 * Use private RSA key to sign (call decrypt!)
	 *
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
	 */
	boolean verify(byte[] hash, byte[] signature) {
		/*
		Creates a message digest of the information received.
		Take sender public key (n, e) to compute inteer v = m^e mod n.
		Extracts the message digest from this integer.
		If both message digests are identical, the signature is valid.
		*/
		return Arrays.equals(hash, encrypt(signature));
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
	 * Padding OAEP (optimal asymmetric encryption padding)
	 * r = random nonce
	 * X = (m || 00...0) XOR H(r) // pad m with zeros
	 * Y = r XOR H(X)
	 * output X || Y
	 */
	byte[] padOEAP(byte[] msg) throws Exception {
		if (msg.length >= N.bitLength()/8) return msg;

		Random rnd = new Random();
		MessageDigest md = MessageDigest.getInstance("SHA1");
		byte[] nonce = new byte[N.bitLength()/8];
		rnd.nextBytes(nonce);
		BigInteger m = new BigInteger(1, msg);
		BigInteger h = new BigInteger(1, md.digest(nonce));
		BigInteger x = m.shiftLeft(N.bitLength() - msg.length*8).xor(h);
		BigInteger y = new BigInteger(1, nonce).xor(x);
		return x.shiftLeft(y.bitLength()).or(y).toByteArray();
	}

	/*
	 * https://rsapss.hboeck.de/rsapss.pdf
	 */
	byte[] padPSS_orig96(byte[] msg, MessageDigest md) {
		byte[] zero = {0};
		int wLen = 20, seedLen = 20;
		int len = N.bitLength()/8;
		if (len < wLen+seedLen) throw new RuntimeException();
		Random rnd = new Random();
		byte[] seed = new byte[seedLen];
		rnd.nextBytes(seed);
		md.update(seed);
		byte[] h = md.digest(msg);
		byte[] w = mgf(h, wLen, md);
		byte[] masked = new BigInteger(1,w).xor(new BigInteger(1,padZeroR(seed, wLen))).toByteArray();
		return concat(zero, h, masked);
	}
	byte[] padPSS(byte[] msg, MessageDigest md) {
		byte[] padR = {(byte) 0xbc};
		int wLen = 20, seedLen = 20;
		int len = N.bitLength()/8;
		if (len < wLen+seedLen) throw new RuntimeException();
		Random rnd = new Random();
		byte[] seed = new byte[seedLen];
		rnd.nextBytes(seed);
		byte[] h = md.digest(msg);
		md.update(h);
		h = md.digest(seed);
		byte[] w = mgf(h, wLen, md);
		byte[] masked = new BigInteger(1,w).xor(new BigInteger(1,padZeroL(seed, wLen))).toByteArray();
		return concat(masked, h, padR);
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
