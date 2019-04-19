package crypt;

import java.math.BigInteger;
import sys.Log;
import text.Text;

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
	NOTE: (r,s) must be encoded as DER T:30[T:02[r] T:02[s]]
	 */
	public byte[] signDigest(byte[] hash) {
		if (q.bitLength() > hash.length*8) {
			throw new RuntimeException("bits(q) > bits(hash)");
		}

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

		Log.debug("r = %s", Text.hex(r.toByteArray()));
		Log.debug("s = %s", Text.hex(s.toByteArray()));

		TLV der = TLV.craete(0x30);
		der.add(TLV.craete(0x02).set(r.toByteArray()));
		der.add(TLV.craete(0x02).set(s.toByteArray()));
		return der.toByteArray();
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
		TLV_BER der = new TLV_BER();
		TLV_BER tlv = new TLV_BER();
		der.read(sign, 0, sign.length);
		tlv.read(der.buf, 0, der.vl);
		BigInteger r = new BigInteger(1, tlv.toByteArray());
		int x = tlv.vi - tlv.bufOffs + tlv.vl;
		tlv.read(der.buf, x, der.vl-x);
		BigInteger s = new BigInteger(1, tlv.toByteArray());
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
