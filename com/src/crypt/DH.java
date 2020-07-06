package crypt;

import java.math.BigInteger;
import sys.Log;

// 1. p,g - prime and generator exchanged by users A,B (p,g are both publicly available)
//      g - is a primitive root of p.
//      p - should be "safe prime": p=2q+1, where q is prime
// 2. generate their private keys and keep them secret
//    xA - private key of user A (xA < p)
//    xB - private key of user B (xB < p)
// 3. calculate public keys and send to the other user
//    yA = g ^ xA mod p   - user A send to user B
//    yB = g ^ xB mod p   - user B send to user A
// 4. calculate shared secret key (note: sA==sB)
//    sA = yB ^ xA mod p
//    sB = yA ^ xB mod p
public class DH extends Asymmetric {
	private BigInteger p; //prime
	private BigInteger g; //generator

	public DH(int p_bits) {
		p = safePrime(p_bits);
		BigInteger q = p.shiftRight(1); // p=2*q+1

		// g in range [2, q-2] is generator if and only if g^((q-1)/2) != 1 mod p
		g = findGenerator(p, q);
		Log.debug("P=%s", p.toString(16));
		Log.debug("G=%s", g.toString(16));
	}
	public DH(BigInteger p, BigInteger g) {
		this.p = p;
		this.g = g;
	}

	public BigInteger derivePublicKey(BigInteger xA) {
		return g.modPow(xA, this.p);
	}

	public BigInteger deriveSharedKey(BigInteger xA, BigInteger yB) {
		return yB.modPow(xA, this.p);
	}
}
