package unittest;

import crypt.Base64;
import sys.UnitTest;

public class T_Base64 extends UnitTest {
	static void encodeBase64() throws Exception {
		check(Base64.encode(new byte[] {'1'}), "MQ");
		check(Base64.encode(new byte[] {'2','3'}), "MjM");
		check(Base64.encode(new byte[] {'4','5','6'}), "NDU2");
		check(Base64.encode(new byte[] {'7','8','9','0'}), "Nzg5MA");
	}
}
