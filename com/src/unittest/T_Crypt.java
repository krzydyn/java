package unittest;

import java.math.BigInteger;

import crypt.Base64;
import crypt.Prime;
import crypt.RSA;
import sys.UnitTest;

public class T_Crypt extends UnitTest {
	static void testPrimes() throws Exception {
		int primes[] = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151};
		for (int i =0; i < primes.length; ++i) {
			check(primes[i]==Prime.PRIMES[i], String.format("error at %d %d!=%d",i,primes[i],Prime.PRIMES[i]));
		}
		//Log.debug("Primes: %s", Text.join(Prime.PRIMES, ","));
	}
	static void encodeBase64() throws Exception {
		check(Base64.encode(new byte[] {'1'}), "MQ==");
		check(Base64.encode(new byte[] {'2','3'}), "MjM=");
		check(Base64.encode(new byte[] {'4','5','6'}), "NDU2");
		check(Base64.encode(new byte[] {'7','8','9','0'}), "Nzg5MA==");
		check(Base64.encode(new byte[] {'7','8','9','0', 'A'}), "Nzg5MEE=");
		check(Base64.encode(new byte[] {'7','8','9','0', 'A', 'B'}), "Nzg5MEFC");
	}
	static void decodeBase64() throws Exception {
		check(Base64.decode("MQ=AA"), new byte[] {'1'});
		check(Base64.decode("MjM"), new byte[] {'2','3'});
		check(Base64.decode("NDU2"), new byte[] {'4','5','6'});
		check(Base64.decode("Nzg5MA"), new byte[] {'7','8','9','0'});
		check(Base64.decode("Nzg5MEE"), new byte[] {'7','8','9','0', 'A'});
		check(Base64.decode("Nzg5MEFC"), new byte[] {'7','8','9','0', 'A', 'B'});
	}
	static void rsa() {
		new RSA(1024);
		new RSA(1024,BigInteger.valueOf(3));
		new RSA(1024,BigInteger.valueOf(0x10001));
	}
}
