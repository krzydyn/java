package crypt;

import java.math.BigInteger;
import sys.Log;

// 1. p,g - prime and generator exchanged by users A,B (p,g are both publicly available)
//     g - is a primitive root of p.
//     p - should be "safe prime": p=2q+1, where q is prime
// 2. generate their private keys and keep them secret
//    xA - private key of user A (xA < p)
//    xB - private key of user B (xB < p)
// 3. calculate public keys and send to the other user
//    yA = g ^ xA mod p   - user A send yA to user B
//    yB = g ^ xB mod p   - user B send yB to user A
// 4. calculate shared secret key (note: sA==sB)
//    sA = yB ^ xA mod p  - user A calc his shared key
//    sB = yA ^ xB mod p  - user B calc his shared key
public class DH extends Asymmetric {
	private BigInteger p;  // base prime
	private BigInteger g;  // generator
	private BigInteger x;  // private key
	private BigInteger y;  // public key

	// NIST SP800-56 doesn't assume a safe prime. In fact, a strict reading of NIST SP800-56 would appear to forbid it
	public DH(int p_bits) {
		//p = safePrime(p_bits);
		p = BigInteger.probablePrime(p_bits, rnd);
		g = primitiveRoot(p);
		Log.debug("P=%s", p.toString(16));
		Log.debug("G=%s", g.toString(16));
	}
	public DH(BigInteger p, BigInteger g) {
		this.p = p;
		this.g = g;
	}

	public BigInteger getBasePrime() { return p; }
	public BigInteger getGenerator() { return g; }
	public BigInteger getPublicKey() { return y; }

	public void generateXY() {
		x = new BigInteger((p.bitLength()+3)/4, rnd);
		y = g.modPow(x, p);
		Log.debug("X[%d]=%s", x.bitLength(), x.toString(16));
		Log.debug("Y[%d]=%s", y.bitLength(), y.toString(16));
	}

	public BigInteger deriveSharedKey(BigInteger peerY) {
		return peerY.modPow(x, this.p);
	}
}
