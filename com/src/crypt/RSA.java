package crypt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.Random;

import sys.Log;
import text.Text;

public class RSA {
	private BigInteger N;
	private BigInteger e;
	private BigInteger d;

	public RSA(BigInteger e, BigInteger d, BigInteger N) {
		this.e = e;
		this.d = d;
		this.N = N;
	}

	public RSA(int bits) {
		Random r = new Random();
		BigInteger p = BigInteger.probablePrime(bits, r);
		BigInteger q = BigInteger.probablePrime(bits, r);
		N = p.multiply(q);

		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		e = BigInteger.probablePrime(bits/4+1, r);
		while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
			e.add(BigInteger.ONE);
			e.add(BigInteger.ONE);
		}
		d = e.modInverse(phi);
		Log.debug("modulus[%d] = %s", N.toByteArray().length, Text.hex(N.toByteArray()));
		Log.debug("e[%d] %s", e.toByteArray().length, Text.hex(e.toByteArray()));
		Log.debug("d[%d] = %s", d.toByteArray().length, Text.hex(d.toByteArray()));
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
L ← (p-1)(q-1)
d ← modinv(e, L)
return (N, e, d)
	 */
	public RSA(int bits, BigInteger e) {
		Random r = new Random();
		BigInteger p;
		BigInteger q;

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

}
