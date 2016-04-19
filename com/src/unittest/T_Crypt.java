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

package unittest;

import java.math.BigInteger;

import crypt.AES2;
import crypt.AES3;
import crypt.Base64;
import crypt.Prime;
import crypt.RSA;
import sys.Log;
import sys.UnitTest;
import text.Text;

public class T_Crypt extends UnitTest {
	static void testPrimes() throws Exception {
		int primes[] = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151};
		for (int i =0; i < primes.length; ++i) {
			check(primes[i]==Prime.PRIMES[i], String.format("error at %d %d!=%d",i,primes[i],Prime.PRIMES[i]));
		}
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
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {new RSA(1024);}
		});
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {new RSA(1024,BigInteger.valueOf(3));}
		});
		checkNoThrow(new RunThrowable() {
			@Override
			public void run() {new RSA(1024,BigInteger.valueOf(0x10001));}
		});
	}

	static void aes2() {
		byte[] key={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		String msg="Test message    ";
		AES2 aes=new AES2();
		aes.setKey(key);
		byte [] orig=msg.getBytes();
		for(int i=0; i<1000; ++i) aes.encrypt(orig);
		byte[] result = aes.encrypt(orig);
		Log.info("encr(%s) = %s", msg, Text.hex(result));
		String rmsg = new String(aes.decrypt(result));
		check(msg, rmsg);
	}
	static void aes3() {
		byte[] key={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		String msg="Test message    ";
		AES3 aes=new AES3(key);

		byte [] orig=msg.getBytes();
		for(int i=0; i<1000; ++i) aes.encrypt(orig);
		byte[] result = aes.encrypt(orig);
		Log.info("encr(%s) = %s", msg, Text.hex(result));
		String rmsg = new String(aes.decrypt(result));
		check(msg, rmsg);
	}
}
