package crypt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Arrays;

import sys.Log;
import text.Text;

public class DSA extends Asymmetric {
	private BigInteger p,q,g;
	private BigInteger x,y;
	private BigInteger k = null;

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
		p = BigInteger.probablePrime(p_bits, rnd);
		do {
			q = BigInteger.probablePrime(q_bits, rnd);
		} while (q.subtract(ONE).gcd(p).compareTo(ONE) != 0);

		g = TWO.modPow(p.subtract(ONE).divide(q), p);

		x = new BigInteger(q.bitLength(), rnd);
		y = g.modPow(x, p);
	}

	/**
	 * Initialize DSA with domain parameters
	 */
	public DSA(BigInteger p, BigInteger q, BigInteger g) {
		this.p = p; //prime
		this.q = q; //subprime
		this.g = g; //base
	}
	public void setK(BigInteger k) {
		this.k = k;
	}
	public void setXY(BigInteger x, BigInteger y) {
		this.x = x; //priv
		this.y = y; //pub
	}

	public void print(PrintStream pr) {
		pr.printf("P %s", p.toString(16));
		pr.printf("Q %s", q.toString(16));
		pr.printf("G %s", g.toString(16));
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
		u1 = g.modPow(u1, p);
		u2 = y.modPow(u2, p);
		BigInteger v = u1.multiply(u2).mod(p).mod(q);
/*
		Log.debug("H = %s", H.toString(16));
		Log.debug("w = %s", w.toString(16));
		Log.debug("u1 = %s", u1.toString(16));
		Log.debug("u2 = %s", u2.toString(16));
		Log.debug("v = %s", v.toString(16));
*/
		return v.equals(r);
	}

	public boolean verifyDigest(byte[] sign, byte[] hash) {
		TLV der = null;
		try {
			der = TLV.load(new ByteArrayInputStream(sign));
		} catch (IOException e) {
			return false;
		}
		BigInteger r = new BigInteger(1, der.get(0).value());
		BigInteger s = new BigInteger(1, der.get(1).value());
		return verifyDigest(r,  s, hash);
	}
}
