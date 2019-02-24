package crypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;

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

	public static void xor(byte[] a, byte[] b) {
		int l = Math.min(a.length, b.length);
		for (int i = 0; i < l; ++i)
			a[a.length - i -1] ^= b[b.length - i -1];
	}

	/**
	 * Mask Generation Function (Full domain hashing)
	 * @param seed seed from which mask is generated
	 * @param masklen intended length in octets of the mask
	 * @param md hash function
	 * @return
	 */
	static byte[] mgf(byte[] seed, int masklen, MessageDigest md) {
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
}
