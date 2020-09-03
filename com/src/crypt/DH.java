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
//    yA = g ^ xA mod p   - user A send yA to user B
//    yB = g ^ xB mod p   - user B send yB to user A
// 4. calculate shared secret key (note: sA==sB)
//    sA = yB ^ xA mod p  - user A calc his shared key
//    sB = yA ^ xB mod p  - user B calc his shared key
public class DH extends Asymmetric {
	private BigInteger p; //prime
	private BigInteger g; //generator

	public DH(int p_bits) {
		p = safePrime(p_bits);
		g = primitiveRoot(p);
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
