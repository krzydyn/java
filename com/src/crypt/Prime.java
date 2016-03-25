package crypt;

import java.math.BigInteger;

public class Prime {
	static public final int PRIMES[] = new int[100];
	static  {
		int p=0;
		PRIMES[p++]=2;

		for (int i = 3; p < PRIMES.length; i+=2) {
			PRIMES[p++]=i;
			int sq = (int)Math.sqrt(i);
			for (int x=0; x < p-1; ++x) {
				int p1 = PRIMES[x];
				if (p1 > sq) break;
				if (i%p1 == 0) {--p;break;}
			}
		}
	}

	static public boolean isPrime(BigInteger p) {
		if (!p.testBit(0)) return false;
		for (int i=1; i < 8; ++i) {
			if (p.remainder(BigInteger.valueOf(PRIMES[i])).equals(BigInteger.ZERO)) return false;
		}
		return true;
	}

	//Fermat's little theorem
	static public boolean isFermatPrime(BigInteger p) {
		return p.isProbablePrime(2);
	}

	static public BigInteger genFermatPrime(int bits) {
		return BigInteger.probablePrime(bits, null);
	}
}
