package crypt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import sys.Log;
import text.Text;

// https://github.com/warner/python-ecdsa/blob/master/src/ecdsa/ecdsa.py
public class ECDSA extends Asymmetric {
	static class BigPoint {
		final static BigPoint INFINITY = new BigPoint();
		final private BigInteger x, y;

		private BigPoint() { x = y = null; }

		public BigPoint(BigInteger x, BigInteger y) {
			if (x == null || y == null) throw new NullPointerException();
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || !(obj instanceof BigPoint)) return false;
			BigPoint p = (BigPoint)obj;
			return x.equals(p.x) && y.equals(p.y);
		}
		@Override
		public int hashCode() {
			if (this == INFINITY) return -1;
			//return x.hashCode() + y.hashCode()*31;
			return Objects.hash(x, y);
		}
	}

	// elliptic curve y^2 = x^3 + a*x + b (mod p)
	// where 4*a^3 + 27*b^2 (mod p) != 0
	// The public key is a point on the curve and the private key is a random number.
	// The public key is obtained by multiplying the private key with a generator point G in the curve.

	static class EllipticCurve {
		//final static BigInteger TWO = BigInteger.valueOf(2);
		final static BigInteger THREE = BigInteger.valueOf(3);
		final private BigInteger a, b, p;

		public EllipticCurve(BigInteger a, BigInteger b, BigInteger p) {
			this.a = a;
			this.b = b;
			this.p = p;
		}
		public boolean contains(BigInteger x, BigInteger y) {
			BigInteger r = x.multiply(x).multiply(x);
			r = r.add(a.multiply(x)).add(b);
			return y.multiply(y).subtract(r).mod(p).equals(BigInteger.ZERO);
		}

		public BigPoint add(BigPoint p1, BigPoint p2) {
			if (p1 == BigPoint.INFINITY) return p2;
			if (p2 == BigPoint.INFINITY) return p1;
			if (p1.x.equals(p2.x)) {
				if (p1.y.add(p2.y).mod(p).equals(BigInteger.ZERO))
					return BigPoint.INFINITY;
				return dub(p1);
			}

			// l = (y2 - y1) * (inv(x2 - x1)) mod p
			BigInteger l = p2.y.subtract(p1.y).multiply(p2.x.subtract(p1.y).modInverse(p)).mod(p);
			BigInteger x3 = l.multiply(l).subtract(p1.x).subtract(p2.x).mod(p);
			BigInteger y3 = l.multiply(p1.x.subtract(x3)).subtract(p1.y).mod(p);

			return new BigPoint(x3, y3);
		}
		public BigPoint dub(BigPoint p1) {
			if (p1 == BigPoint.INFINITY)
				return BigPoint.INFINITY;

			// l = 3 * x1*x1 * a * inv(2*y1) mod p
			BigInteger l = THREE.multiply(p1.x).multiply(p1.x).add(a).multiply(TWO.multiply(p1.y).modInverse(p)).mod(p);
			BigInteger x3 = l.multiply(l).subtract(TWO.multiply(p1.x)).mod(p);
			BigInteger y3 = l.multiply(p1.x.subtract(x3)).subtract(p1.y).mod(p);

			return new BigPoint(x3, y3);
		}
		public BigPoint multiply(BigPoint p1, BigInteger k) {
			if (k.equals(BigInteger.ZERO))
				return BigPoint.INFINITY;
			if (p1 == BigPoint.INFINITY)
				return BigPoint.INFINITY;
			BigInteger e3 = THREE.multiply(k);
			BigPoint negp = new BigPoint(p1.x, p1.y.negate());
			int i = e3.bitCount()/2;
			BigPoint res = p1;
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
	BigInteger qA; //public key  (qA = dA * G)?
	BigInteger dA; //private key
	BigPoint G;    //base point

	public ECDSA(EllipticCurve ec, BigPoint g, BigInteger d) {
		this.ec = ec;
		this.G = g;
		this.dA = d;
		this.qA = ec.multiply(g, d).x;
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
		int qBytes = qA.bitLength()/8;
		if (qBytes < hash.length) {
			hash = Arrays.copyOfRange(hash, 0, qBytes);
		}

		BigInteger H = new BigInteger(1, hash);

		BigInteger r,s;
		for (;;) {
			BigInteger k = new BigInteger(qA.bitLength(), rnd);
			if (k.equals(BigInteger.ZERO)) continue;
			r = ec.multiply(G,k).x.mod(qA);
			if (r.equals(BigInteger.ZERO)) continue;
			s = k.modInverse(qA).multiply(H.add(dA.multiply(r))).mod(qA);
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

	public boolean verifyDigest(byte[] sign, byte[] hash) {
		return false;
	}

	final public static EllipticCurve p192 = new EllipticCurve(
			BigInteger.valueOf(-3),
			new BigInteger("0x64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1"),
			new BigInteger("6277101735386680763835789423207666416083908700390324961279")
			);
	final public static BigPoint g192 = new BigPoint(
			new BigInteger("0x188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012"),
			new BigInteger("0x07192b95ffc8da78631011ed6b24cdd573f977a11e794811"));
}
