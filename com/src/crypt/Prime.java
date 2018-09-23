/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package crypt;

import java.math.BigInteger;

public class Prime {
	static public final int PRIMES[] = new int[100];
	static  {
		int p=0;
		PRIMES[p++]=2; // first prime numbers
		for (int i = 3; p < PRIMES.length; i+=2) {
			PRIMES[p++]=i;
			//int sq = (int)Math.sqrt(i);
			int sq = i/2;
			for (int x=0; x < p-1; ++x) {
				int p1 = PRIMES[x];
				if (p1 > sq) break;
				if (i%p1 == 0) {--p;break;}
			}
		}
	}

	//Sieve of Eratosthenes
	static void sieve()
	{
		int a_size = 100;
		boolean[] ispri = new boolean[a_size];
		int i, j;
		for (i = 0; i < a_size; ++i)
			ispri[i] = true;
		for (i = 2; i * i < a_size ; ++i) {
			if(ispri[i] == true) {
				for (j = 2; j * i < a_size ; ++j) {
					ispri[i*j] = false;
				}
			}
		}
	}

	static public final BigInteger TWO = BigInteger.valueOf(2);

	static public boolean isPrime(BigInteger p) {
		if (p.compareTo(BigInteger.ONE) <= 0) return false;
		if (p.compareTo(TWO) == 0) return true;
		for (int i=0; i < PRIMES.length; ++i) {
			if (p.remainder(BigInteger.valueOf(PRIMES[i])).equals(BigInteger.ZERO)) return false;
		}
		return p.isProbablePrime(1);
	}

	//Fermat's little theorem: a^p == a mod p for any p-prime and a-integer [a^(p-1) == 1 mod p]
	static public boolean isFermatPrime(BigInteger p) {
		return p.isProbablePrime(2);
	}

	static public BigInteger genFermatPrime(int bits) {
		return BigInteger.probablePrime(bits, null);
	}

	/**
	 * Greatest Common Divisor
	 * @param bi
	 * @param m
	 * @return
	 */
	static public BigInteger gcd(BigInteger bi, BigInteger m) {
		return bi.gcd(m);

	}
}
