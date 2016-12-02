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
	static BigInteger TWO = BigInteger.valueOf(2);
	private BigInteger e,d,N;
	private BigInteger p,q;

	public RSA(BigInteger e, BigInteger d, BigInteger N) {
		this.e = e; // public exponent
		this.d = d; // private exponent
		this.N = N; // modulus

		//recover p,q
		//N=p*q
		//phi=(p-1)*(q-1)
		//d=e^-1 mod phi
		BigInteger k = d.multiply(e).subtract(BigInteger.ONE);
		if (!k.testBit(0)) return ; //no prime factors

	}

	public RSA(int bits) {
		Random r = new Random();
		p = BigInteger.probablePrime(bits, r);
		q = BigInteger.probablePrime(bits, r);
		N = p.multiply(q);

		/* phi = (p-1)*(q-1);
		 * while (gdc(e,phi)==1 && e < phi) e+=2;
		 */
		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		if (bits > 8) e = BigInteger.probablePrime(bits/4+1, r);
		else e = BigInteger.valueOf(3);
		while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
			e.add(TWO);
		}
		d = e.modInverse(phi);
		Log.notice("Input: bits=%d",bits);
		Log.info("modulus[%d] = %s", N.toByteArray().length, Text.hex(N.toByteArray()));
		Log.info("e[%d] %s", e.toByteArray().length, Text.hex(e.toByteArray()));
		Log.info("d[%d] = %s", d.toByteArray().length, Text.hex(d.toByteArray()));
	}

	/*
Select a value of e from {3, 5, 17, 257, 65537}
repeat
   p ← genprime(k/2)
until (p mod e) ≠ 1
repeat
   q ← genprime(k - k/2)
until (q mod e) ≠ 1
N ← pq
phi ← (p-1)(q-1)
d ← modinv(e, phi)
return (N, e, d)
	 */
	public RSA(int bits, BigInteger e) {
		Random r = new Random();

		do {
			p = BigInteger.probablePrime(bits, r);
		} while (p.subtract(BigInteger.ONE).gcd(e).compareTo(BigInteger.ONE) != 0);

		do {
			q = BigInteger.probablePrime(bits, r);
		} while (q.subtract(BigInteger.ONE).gcd(e).compareTo(BigInteger.ONE) != 0);


		this.e = e;
		N = p.multiply(q);
		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		d = e.modInverse(phi);

		Log.notice("Input: bits=%d, exponet (e)",bits);
		Log.debug("modulus[%d] = %s", N.toByteArray().length, Text.hex(N.toByteArray()));
		Log.debug("e[%d] %s", e.toByteArray().length, Text.hex(e.toByteArray()));
		Log.debug("d[%d] = %s", d.toByteArray().length, Text.hex(d.toByteArray()));
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
		return null;
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
