package crypt;

import java.math.BigInteger;

public class DH extends Asymmetric {
	private BigInteger p; //prime
	private BigInteger g; //generator

	void setParams(BigInteger p, BigInteger g) {
		this.p = p;
		this.g = g;
	}

	// 1. p,g - prime and generator exchanged by users A,B
	// 2. generate their private keys and keep them secret
	//    xA - private key of user A (xA < p)
	//    xB - private key of user B (xB < p)
	// 3. calculate public keys and send to the other user
	//    yA = g ^ xA mod p
	//    yB = g ^ xB mod p
	// 4. calculate shared secret key (note: sA==sB)
	//    sA = yB ^ xA mod p
	//    sB = yA ^ xB mod p
	BigInteger deriveKey(BigInteger xA, BigInteger yB) {
		return yB.modPow(xA, this.p);
	}
}
