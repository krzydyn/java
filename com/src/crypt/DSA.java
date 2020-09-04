package crypt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Arrays;

import sys.Log;
import text.Text;

/*
 * p - DSA prime
 * q - DSA group order q (which is a prime divisor of p-1)
 * g - DSA group generator
 * y - DSA public key, y = g^x % p
 * x = DSA secret exponent
*/
public class DSA extends Asymmetric {
	private BigInteger p;  // base prime
	private BigInteger q;  // subprime
	private BigInteger g;  // generator
	private BigInteger x;  // private key
	private BigInteger y;  // public key
	private BigInteger k = null;

	/*
	 * Parameter generation (can be shared among users)
	 * q = generate prime of length N-bits
	 * p = choose prime of length L-bits such that p − 1 is a multiple of q
	 * g = number whose multiplicative order modulo p is q
	 *
	 * Per-user keys
	 * x = generate private key (0 < x < q)
	 * y = g^x mod p
	 *
	 * (L,N) : (1024, 160), (2048, 224), (2048, 256), and (3072, 256)
	 * N must be <= hashbits
	 */
	public DSA(int p_bits, int q_bits) {
		byte[] hb = new byte[(p_bits+7)/8];
		for (;;) {
			q = BigInteger.probablePrime(q_bits, rnd);
			//p = BigInteger.probablePrime(p_bits, rnd);
			rnd.nextBytes(hb);
			p = new BigInteger(1, hb).setBit(p_bits-1);
			p = p.subtract(p.mod(q)).add(ONE);
			while (!p.isProbablePrime(100))
				p = p.add(q);
			if (p.bitLength() == p_bits) break;
			if (p.bitLength() > p_bits)
				Log.error("P to large");
			else
				Log.error("P to small");
		}

		g = findGenerator(p, q);
		Log.debug("P[%d]=%s", p.bitLength(), p.toString(16));
		Log.debug("Q[%d]=%s", q.bitLength(), q.toString(16));
		Log.debug("G[%d]=%s", g.bitLength(), g.toString(16));

		if (!checkPQG())
			throw new RuntimeException("Wrong DSA params");
	}

	/**
	 * Initialize DSA with domain parameters
	 */
	public DSA(BigInteger p, BigInteger q, BigInteger g) {
		this.p = p; //prime
		this.q = q; //subprime
		this.g = g; //base
		Log.debug("P[%d]=%s", p.bitLength(), p.toString(16));
		Log.debug("Q[%d]=%s", q.bitLength(), q.toString(16));
		Log.debug("G[%d]=%s", g.bitLength(), g.toString(16));

		if (!checkPQG())
			throw new RuntimeException("Wrong DSA params");
	}

	private boolean checkPQG() {
		if (!p.isProbablePrime(100)) {
			Log.error("dsa P is not rpime");
			return false;
		}
		if (!q.isProbablePrime(100)) {
			Log.error("dsa Q is not rpime");
			return false;
		}
		if (!p.subtract(ONE).mod(q).equals(ZERO)) {
			Log.error("dsa (P-1) mod Q != 0");
			return false;
		}
		if (!g.modPow(q, p).equals(ONE)) {
			Log.error("dsa G^Q mod P != 1");
			return false;
		}
		return true;
	}
	private boolean checkXY() {
		if (q.compareTo(x) != 1) {
			Log.error("dsa Q <= X");
			return false;
		}
		if (!g.modPow(x, p).equals(y)) {
			Log.error("dsa G^X mod P != Y");
			return false;
		}
		return true;
	}

	public void generateXY() {
		x = new BigInteger(q.bitLength() - 1, rnd);
		y = g.modPow(x, p);
		Log.debug("X=%s", x.toString(16));
		Log.debug("Y=%s", y.toString(16));
		checkXY();
	}
	public void setK(BigInteger k) {
		this.k = k;
	}
	public void setXY(BigInteger x, BigInteger y) {
		this.x = x; //priv
		this.y = y; //pub
		Log.debug("X=%s", x.toString(16));
		Log.debug("Y=%s", y.toString(16));
		checkXY();
	}


	public void print(PrintStream pr) {
		pr.printf("P %s\n", p.toString(16));
		pr.printf("Q %s\n", q.toString(16));
		pr.printf("G %s\n", g.toString(16));
		if (x!=null) pr.printf("X %s\n", x.toString(16));
		if (y!=null) pr.printf("Y %s\n", y.toString(16));
	}

	/*
	1. k = generated random
	2. r = (g^k mod p) mod q (if r == 0, generate new k)
	3. s = inv(k) * (H(m)+x*r) mod q (if s == 0, generate new k)
	4. The signature is (r,s)
	NOTE: (r,s) must be encoded as DER T:30{T:02[r] T:02[s]}
	 */
	public byte[] signDigest(byte[] hash) {
		int qBytes = q.bitLength()/8;
		if (qBytes < hash.length) {
			hash = Arrays.copyOfRange(hash, 0, qBytes);
		}

		BigInteger H = new BigInteger(1, hash);

		BigInteger r,s;
		if (k == null) {
			for (;;) {
				BigInteger k = new BigInteger(q.bitLength(), rnd);
				if (k.equals(BigInteger.ZERO)) continue;
				r = g.modPow(k, p).mod(q);
				if (r.equals(BigInteger.ZERO)) continue;
				s = k.modInverse(q).multiply(H.add(x.multiply(r))).mod(q);
				if (s.equals(BigInteger.ZERO)) continue;
				break;
			}
		}
		else {
			r = g.modPow(k, p).mod(q);
			s = k.modInverse(q).multiply(H.add(x.multiply(r))).mod(q);
		}

		Log.debug("H = %s", H.toString(16));
		byte[] b = r.toByteArray();
		Log.debug("r[%d] = %s", b.length, Text.hex(b));
		b = s.toByteArray();
		Log.debug("s[%d] = %s", b.length, Text.hex(b));

		TLV der = TLV.create(0x30);
		der.add(TLV.create(0x02).setValue(r.toByteArray()));
		der.add(TLV.create(0x02).setValue(s.toByteArray()));
		return der.toByteArray();
	}

	/*
	 (r,s) = signature
	1. w = inv(s) mod q
	2. u1 = H(m)*w mod q
	3. u2 = r*w mod q
	4. v = (g^u1 * y^u2 mod p) mod q
	5. sign is valid if v == r

	https://github.com/frohoff/jdk8u-dev-jdk/blob/master/src/share/classes/sun/security/provider/DSA.java
	 */
	public boolean verifyDigest(BigInteger r, BigInteger s, byte[] hash) {
		if (s.bitLength() > q.bitLength()) throw new RuntimeException("signature too long");

		int qBytes = q.bitLength()/8;
		if (qBytes < hash.length) {
			hash = Arrays.copyOfRange(hash, 0, qBytes);
		}

		BigInteger H = new BigInteger(1, hash);
		BigInteger w = s.modInverse(q);
		BigInteger u1 = H.multiply(w).mod(q);
		BigInteger u2 = r.multiply(w).mod(q);
		BigInteger v = g.modPow(u1, p).multiply(y.modPow(u2, p)).mod(p).mod(q);

		Log.debug("r = %s", r.toString(16));
		Log.debug("s = %s", s.toString(16));
		Log.debug("H = %s", H.toString(16));
		Log.debug("w = %s", w.toString(16));
		Log.debug("u1 = %s", u1.toString(16));
		Log.debug("u2 = %s", u2.toString(16));
		Log.debug("v = %s", v.toString(16));

		return v.equals(r);
	}

	public boolean verifyDigest(byte[] sign, byte[] hash) {
		TLV der = null;
		try {
			der = TLV.load(new ByteArrayInputStream(sign));
		} catch (IOException e) {
			return false;
		}
		BigInteger r = new BigInteger(der.get(0).value());
		BigInteger s = new BigInteger(der.get(1).value());
		return verifyDigest(r,  s, hash);
	}
}
