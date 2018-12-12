package crypt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import sys.Log;

public class DSA extends Asymmetric {
	private BigInteger p,q,g;
	private BigInteger x,y;

	/*
	 * Parameter generation (can be shared among users)
	 * q = generate prime of length N-bits
	 * p = choose prime of length L-bits such that p − 1 is a multiple of q
	 * g = a number whose multiplicative order modulo p is q
	 *
	 * Per-user keys
	 * x = generate private key (0 < x < q)
	 * y = g^x mod p
	 *
	 * (L,N) : (1,024, 160), (2,048, 224), (2,048, 256), and (3,072, 256)
	 * N must be <= hashbits
	 */
	public DSA(int p_bits, int q_bits) {
		Random rnd = new Random();
		p = BigInteger.probablePrime(p_bits, rnd);
		do {
			q = BigInteger.probablePrime(q_bits, rnd);
		} while (q.subtract(ONE).gcd(p).compareTo(ONE) != 0);

		g = TWO.modPow(p.subtract(ONE).divide(q), p);

		x = new BigInteger(q.bitLength(), rnd);
		y = g.modPow(x, p);
	}
	public DSA(BigInteger p, BigInteger q, BigInteger g, BigInteger x, BigInteger y) {
		this.p = p; //prime
		this.q = q; //subprime
		this.g = g; //base
		this.x = x; //priv
		this.y = y; //pub

		Log.debug("p=%s", p.toString(16));
		Log.debug("q=%s", q.toString(16));
		Log.debug("g=%s", g.toString(16));
		Log.debug("x=%s", x.toString(16));
		Log.debug("y=%s", y.toString(16));
	}

	/*
	1. k = generated random
	2. r = g^k mod q (if r == 0, generate new k)
	3. s = k^-1(H(m)+x*r) mod q (if s == 0, generate new k)
	4. The signature is (r,s)
	 */
	public byte[] signDigest(byte[] hash) {
		Random rnd = new Random();
		BigInteger H = new BigInteger(1, hash);

		BigInteger r,s;
		for (;;) {
			BigInteger k = new BigInteger(q.bitLength(), rnd);
			if (k.equals(BigInteger.ZERO)) continue;
			r = g.modPow(k, p).mod(q);
			if (r.equals(BigInteger.ZERO)) continue;
			s = k.modInverse(q).multiply(H.add(x.multiply(r))).mod(q);
			if (s.equals(BigInteger.ZERO)) continue;
			break;
		}

		return r.shiftLeft(q.bitLength()).or(s).toByteArray();
	}

	/*
	 (r,s) = signature
	1. w = s^-1 mod q
	2. u1 = H(m)*w mod q
	3. u2 = r*w mod q
	4. v = (g^u1 * y^u2 mod p) mod q
	5. sign is valid if v == r
	 */
	public boolean verifyDigest(byte[] sign, byte[] hash) {
		BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, sign.length - q.bitLength()/8));
		BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, sign.length - q.bitLength()/8, sign.length));
		BigInteger H = new BigInteger(1, hash);

		if (s.bitLength() > q.bitLength()) throw new RuntimeException("signature too long");

		BigInteger w = s.modInverse(q);
		BigInteger u1 = H.multiply(w).mod(q);
		BigInteger u2 = r.multiply(w).mod(q);
		BigInteger v = g.modPow(u1, p).multiply(y.modPow(u2, p)).mod(p).mod(q);

		if (v.equals(r)) { Log.info("verifyDSA OK");return true; }
		Log.error("not equal:  v=%s  r=%s", v.toString(16), r.toString(16));
		return false;
	}
}
