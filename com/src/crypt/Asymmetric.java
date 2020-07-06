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
		int cnt = 0;
		BigInteger p;
		do {
			p = BigInteger.probablePrime(bits, rnd);
			++cnt;
		} while(!p.shiftRight(1).isProbablePrime(100));
		Log.debug("Safe %d-bit prime in %d iterations", bits, cnt);
		return p;
	}

	// FIPS-186.4 Appendix A.2
	static public BigInteger findGenerator(BigInteger p, BigInteger q) {
		BigInteger g;
		BigInteger e = p.subtract(ONE).divide(q);
		byte hb[] = new byte[p.bitLength()/8];
		do {
			rnd.nextBytes(hb);
			BigInteger h = new BigInteger(1, hb); // to check: 1 < h < p-1
			g = h.modPow(e, p);
		} while (g.equals(BigInteger.ONE));

		return g;
	}

	// https://www.geeksforgeeks.org/primitive-root-of-a-prime-number-n-modulo-n/
/*
1- Euler Totient Function phi = n-1 [Assuming n is prime]
1- Find all prime factors of phi.
2- Calculate all powers to be calculated further
   using (phi/prime-factors) one by one.
3- Check for all numbered for all powers from i=2
   to n-1 i.e. (i^ powers) modulo n.
4- If it is 1 then 'i' is not a primitive root of n.
5- If it is never 1 then return i;.
*/
	// p is prime, q is co-prime
	// g in range [2, q-2] is generator if and only if g^((q-1)/2) != 1 mod p
	static public BigInteger primitiveRoot(BigInteger p, BigInteger q) {
		BigInteger g;
		BigInteger e = q.shiftRight(1);
		byte hb[] = new byte[p.bitLength()/8];
		do {
			rnd.nextBytes(hb);
			g = new BigInteger(1, hb); // to check: 1 < h < p-1
		} while (!g.modPow(e, p).equals(ONE));

		return g;
	}

}
