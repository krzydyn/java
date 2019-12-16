package crypt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.ECPoint;
import java.util.Arrays;
import sys.Log;
import text.Text;

// https://github.com/warner/python-ecdsa/blob/master/src/ecdsa/ecdsa.py
// https://www.bouncycastle.org/wiki/display/JA1/Elliptic+Curve+Key+Pair+Generation+and+Key+Factories
public class ECDSA extends Asymmetric {
	//TODO use java.security.spec.ECPoint

	// elliptic curve y^2 = x^3 + a*x + b (mod p)
	// where 4*a^3 + 27*b^2 (mod p) != 0
	// The public key is a point on the curve and the private key is a random number.
	// The public key is obtained by multiplying the private key with a generator point G in the curve.

	//TODO use java.security.spec.EllipticCurve
	static class EllipticCurve {
		//final static BigInteger TWO = BigInteger.valueOf(2);
		final static BigInteger THREE = BigInteger.valueOf(3);
		final private BigInteger p;   // an integer larger then 3
		final private BigInteger a,b; // elements of Fq, defines Elliptic curve on Fq

		public EllipticCurve(BigInteger p, BigInteger a, BigInteger b) {
			this.p = p;
			this.a = a;
			this.b = b;
		}
		public boolean contains(BigInteger x, BigInteger y) {
			BigInteger r = x.multiply(x).multiply(x);
			r = r.add(a.multiply(x)).add(b);
			return y.multiply(y).subtract(r).mod(p).equals(BigInteger.ZERO);
		}

		public ECPoint add(ECPoint p1, ECPoint p2) {
			if (p1 == ECPoint.POINT_INFINITY) return p2;
			if (p2 == ECPoint.POINT_INFINITY) return p1;
			BigInteger x1 =  p1.getAffineX();
			BigInteger y1 =  p1.getAffineY();
			BigInteger x2 =  p2.getAffineX();
			BigInteger y2 =  p2.getAffineY();
			if (x1.equals(x2)) {
				if (y1.add(y2).mod(p).equals(BigInteger.ZERO))
					return ECPoint.POINT_INFINITY;
				return dub(p1);
			}

			// l = (y2 - y1) * (inv(x2 - x1)) mod p
			BigInteger l = y2.subtract(y1).multiply(x2.subtract(y1).modInverse(p)).mod(p);
			BigInteger x3 = l.multiply(l).subtract(x1).subtract(x2).mod(p);
			BigInteger y3 = l.multiply(x1.subtract(x3)).subtract(y1).mod(p);

			return new ECPoint(x3, y3);
		}
		public ECPoint dub(ECPoint p1) {
			if (p1 == ECPoint.POINT_INFINITY)
				return ECPoint.POINT_INFINITY;

			BigInteger x1 =  p1.getAffineX();
			BigInteger y1 =  p1.getAffineY();
			// l = 3 * x1*x1 * a * inv(2*y1) mod p
			BigInteger l = THREE.multiply(x1).multiply(x1).add(a).multiply(TWO.multiply(y1).modInverse(p)).mod(p);
			BigInteger x3 = l.multiply(l).subtract(TWO.multiply(x1)).mod(p);
			BigInteger y3 = l.multiply(x1.subtract(x3)).subtract(y1).mod(p);

			return new ECPoint(x3, y3);
		}
		public ECPoint multiply(ECPoint p1, BigInteger k) {
			if (k.equals(BigInteger.ZERO))
				return ECPoint.POINT_INFINITY;
			if (p1 == ECPoint.POINT_INFINITY)
				return ECPoint.POINT_INFINITY;
			BigInteger e3 = THREE.multiply(k);
			ECPoint negp = new ECPoint(p1.getAffineX(), p1.getAffineY().negate());
			int i = e3.bitCount()/2;
			ECPoint res = p1;
			while (i > 1) {
				res = dub(res);
				if (e3.testBit(i) && !k.testBit(i))
					res = add(res, p1);
				if (!e3.testBit(i) && k.testBit(i))
					res = add(res, negp);
				i /= 2;
			}
			return res;
		}
	}

	EllipticCurve ec; // curve
	BigInteger dA;    //private key
	BigInteger n;   // prime number, order of base point G (n is a prime factor of E(Fq))
	ECPoint G;      // generator (n * G = I)

	ECPoint qA; //public key (qA = dA * G) packed=[04 || qa.x || dq.y]

	public ECDSA(EllipticCurve ec, ECPoint g, BigInteger d) {
		this.ec = ec;
		this.G = g;
		this.dA = d;
		if (d != null)
			this.qA = ec.multiply(g, d);
	}

	/*
	 * https://www.johannes-bauer.com/compsci/ecc/
	 * http://www-cs-students.stanford.edu/~tjw/jsbn/ec.js
	1. k = generated random (with ECIES)
	2. R = {r.x, r.y} = k * G mod n
	3. S = inv(k) * (hash + r * dA) mod n
	4. The signature is (r,s)
	NOTE: (r,s) must be encoded as DER T:30{T:02[r] T:02[s]}
	 */
	public byte[] signDigest(byte[] hash) {
		int qBytes = qA.getAffineX().bitLength()/8;
		if (qBytes < hash.length) {
			hash = Arrays.copyOfRange(hash, 0, qBytes);
		}

		BigInteger H = new BigInteger(1, hash);

		BigInteger r,s;
		for (;;) {
			BigInteger k = new BigInteger(qA.getAffineX().bitLength(), rnd);
			if (k.equals(BigInteger.ZERO)) continue;
			ECPoint xy = ec.multiply(G,k);
			r = xy.getAffineX().mod(n);
			if (r.equals(BigInteger.ZERO)) continue;
			s = k.modInverse(n).multiply(H.add(dA.multiply(r))).mod(n);
			if (s.equals(BigInteger.ZERO)) continue;
			break;
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

	public boolean verifyDigest(BigInteger r, BigInteger s, byte[] hash) {
		if (s.bitLength() > qA.getAffineX().bitLength()) throw new RuntimeException("signature too long");

		int qBytes = qA.getAffineX().bitLength()/8;
		if (qBytes < hash.length) {
			hash = Arrays.copyOfRange(hash, 0, qBytes);
		}
		BigInteger H = new BigInteger(1, hash);
		BigInteger s1 = s.modInverse(n);
		BigInteger u1 = H.multiply(s1);
		BigInteger u2 = r.multiply(s1);
		ECPoint xy = ec.add(ec.multiply(G, u1), ec.multiply(qA, u2));
		return r.equals(xy.getAffineX());
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

	final public static EllipticCurve p192 = new EllipticCurve(
			BigInteger.valueOf(-3),
			new BigInteger("64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 16),
			new BigInteger("6277101735386680763835789423207666416083908700390324961279", 16)
			);
	final public static ECPoint g192 = new ECPoint(
			new BigInteger("188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012", 16),
			new BigInteger("07192b95ffc8da78631011ed6b24cdd573f977a11e794811", 16));
}
