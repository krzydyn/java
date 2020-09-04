package crypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;
import sys.Log;

abstract public class Asymmetric {
	final public static BigInteger ZERO = BigInteger.ZERO;
	final public static BigInteger ONE = BigInteger.ONE;
	final public static BigInteger TWO = BigInteger.valueOf(2);

	final protected static Random rnd = new Random();

	static byte[] padZeroL(byte[] msg, int len) {
		if (msg.length == len) return msg;
		byte[] r = new byte[len];
		if (msg.length > len)
			System.arraycopy(msg, msg.length - r.length, r, 0, r.length);
		else
			System.arraycopy(msg, 0, r, r.length - msg.length, msg.length);
		return r;
	}
	static byte[] padZeroR(byte[] msg, int len) {
		if (msg.length == len) return msg;
		byte[] r = new byte[len];
		if (msg.length > len)
			System.arraycopy(msg, 0, r, 0, r.length);
		else
			System.arraycopy(msg, 0, r, 0, msg.length);
		return r;
	}

	static BigInteger op2ip(byte[] x) {
		return new BigInteger(1, x);
	}
	static byte[] i2osp(byte[] b, int len) {
		return padZeroL(b, len);
	}
	static byte[] i2osp(BigInteger x, int len) {
		return padZeroL(x.toByteArray(), len);
	}
	static byte[] i2osp(int x, int len) {
		byte[] r = new byte[len];
		for (int i = 0; i < len; ++i) {
			r[len - i - 1] = (byte)x;
			x >>>= 8;
		}
		return r;
	}
	static byte[] i2osp(int x, byte[] r) {
		for (int i = 0; i < r.length; ++i) {
			r[r.length - i - 1] = (byte)x;
			x >>>= 8;
		}
		return r;
	}

	static byte[] concat(byte[] ...arrays) {
		int l = 0;
		for (int i = 0; i < arrays.length; ++i) l += arrays[i].length;
		byte[] r = new byte[l];
		l = 0;
		for (int i = 0; i < arrays.length; ++i) {
			System.arraycopy(arrays[i], 0, r, l, arrays[i].length);
			l += arrays[i].length;
		}
		return r;
	}

	public static byte[] xor(byte[] a, byte[] b) {
		int l = Math.min(a.length, b.length);
		for (int i = 0; i < l; ++i)
			a[a.length - i -1] ^= b[b.length - i -1];
		return a;
	}

	/**
	 * Mask Generation Function (Full domain hashing)
	 * @param seed seed from which mask is generated
	 * @param masklen intended length in octets of the mask
	 * @param md hash function
	 * @return
	 */
	static byte[] mgf1(byte[] seed, int masklen, MessageDigest md) {
		int hLen = md.getDigestLength();
		int n = (masklen + hLen - 1)/hLen;
		byte[] T = new byte[masklen];
		byte[] C = new byte[4];
		md.reset();
		for (int counter = 0; counter < n; ++counter) {
			md.update(seed);
			byte[] d = md.digest(i2osp(counter, C));
			System.arraycopy(d, 0, T, counter*hLen, Math.min(hLen, masklen - counter*hLen));
		}
		return T;
	}

	static public BigInteger safePrime(int bits) {
		int iter = 0;
		BigInteger p;
		do {
			p = BigInteger.probablePrime(bits, rnd);
			++iter;
		} while(!p.shiftRight(1).isProbablePrime(100));
		Log.debug("Safe %d-bit prime in %d iterations", bits, iter);
		return p;
	}
	static public BigInteger euclideanGCD(BigInteger p, BigInteger q) {
		int iter = 0;
		while (!q.equals(ZERO)) {
			++iter;
			BigIngeter t = q;
			q = p.mod(q);
			p = t;
		}
		Log.debug("GCD in %d iterations", iter);
		return p.abs(); // Knuth
	}
/*
	Definition:
	the smallest positive integer k satisfying equation: q^k = 1 (mod p)
*/
	static public BigInteger multiplicativeOrder(BigInteger p, BigInteger q) {
		if (!p.gcd(q).equals(ONE))
			throw new RuntimeException("p,q are not coprimes");
		return null;
	}

	// FIPS-186.4 Appendix A.2  g = h ^ ((p - 1)/q) mod p
	static public BigInteger findGenerator(BigInteger p, BigInteger q) {
		if (!p.gcd(q).equals(ONE))
			Log.warn("p,q are not coprimes");
		BigInteger g;
		BigInteger e = p.subtract(ONE).divide(q);
		byte hb[] = new byte[q.bitLength()/8/2];
		do {
			rnd.nextBytes(hb);
			BigInteger h = new BigInteger(1, hb);
			g = h.modPow(e, p);
		} while (g.equals(ONE));

		return g;
	}

	// https://www.geeksforgeeks.org/primitive-root-of-a-prime-number-n-modulo-n/
/*
	Definition:
	Primitive root of a prime number n is an integer r between[1, n-1] such that
	the values of r^x(mod n) where x is in range[0, n-2] are different
*/
	// p is prime (better safe prime)
	// g in range [2, p-2] is generator if and only if g^((p-1)/2) != 1 mod p
	static public BigInteger primitiveRoot(BigInteger p) {
		int smallG[] = { 2, 3, 11, 17 };
		BigInteger g;

		BigInteger q = p.shiftRight(1); // q = (p-1)/2
		if (!p.gcd(q).equals(ONE))
			Log.warn("p,q are not coprimes");
		for (int i : smallG ) {
			g = BigInteger.valueOf(i);
			if (!g.modPow(q, p).equals(ONE))
				return g;
		}
		byte hb[] = new byte[p.bitLength()/8/2];
		do {
			rnd.nextBytes(hb);
			g = new BigInteger(1, hb); // to check: 1 < h < p-1
		} while (g.modPow(q, p).equals(ONE));

		return g;
	}

}
