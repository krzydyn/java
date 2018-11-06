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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.Random;

import sys.Log;
import text.Text;

public class RSA {
	final static BigInteger ZERO = BigInteger.ZERO;
	final static BigInteger ONE = BigInteger.ONE;
	final static BigInteger TWO = BigInteger.valueOf(2);
	final static BigInteger[] pubExpCandidates = {
		BigInteger.valueOf(3),
		BigInteger.valueOf(5),
		BigInteger.valueOf(17),
		BigInteger.valueOf(257),
		BigInteger.valueOf(65537),
	};

	private BigInteger e,d,N;
	private BigInteger p,q;

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
		Random r = new Random();

		do {
			p = BigInteger.probablePrime(bits/2, r);
		} while (p.subtract(ONE).gcd(e).compareTo(ONE) != 0);

		do {
			q = BigInteger.probablePrime(bits-bits/2, r);
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
		return new BigInteger(msg).modPow(e, N).toByteArray();
	}

	public byte[] decrypt(byte[] msg) {
		return new BigInteger(msg).modPow(d, N).toByteArray();
	}

	public byte[] getPublicKey() {
		byte[] e = this.e.toByteArray();
		byte[] N = this.N.toByteArray();
		ByteArrayOutputStream b = new ByteArrayOutputStream(4+e.length+N.length);
		try {
			DataOutputStream dt=new DataOutputStream(b);
			dt.writeShort(e.length);
			dt.write(e);
			dt.writeShort(N.length);
			dt.write(N);
			dt.flush();
		}catch (Exception ex) {
			return null;
		}
		return b.toByteArray();
	}

	public byte[] getPrivateKey() {
		byte[] d = this.d.toByteArray();
		byte[] N = this.N.toByteArray();
		ByteArrayOutputStream b = new ByteArrayOutputStream(4+d.length+N.length);
		try {
			DataOutputStream dt=new DataOutputStream(b);
			dt.writeShort(d.length);
			dt.write(d);
			dt.writeShort(N.length);
			dt.write(N);
			dt.flush();
		}catch (Exception ex) {
			return null;
		}
		return b.toByteArray();
	}

	byte[] sign(byte[] data) {
		/*
		Creates a message digest of the information to be sent.
		Represents this digest as an integer m between 1 and n-1..
		Take private key (n, d) to compute the signature s = m^d mod n.
		Sends this signature s to the recipient.
		*/
		return encrypt(data);
	}

	boolean verify(byte[] data, byte[] sign) {
		/*
		Creates a message digest of the information received.
		Take sender public key (n, e) to compute inteer v = m^e mod n.
		Extracts the message digest from this integer.
		If both message digests are identical, the signature is valid.
		*/
		return false;
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
