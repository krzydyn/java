package unittest;

import crypt.Base64;
import sys.UnitTest;

public class T_Crypt extends UnitTest {
	static void encodeBase64() throws Exception {
		check(Base64.encode(new byte[] {'1'}), "MQ");
		check(Base64.encode(new byte[] {'2','3'}), "MjM");
		check(Base64.encode(new byte[] {'4','5','6'}), "NDU2");
		check(Base64.encode(new byte[] {'7','8','9','0'}), "Nzg5MA");
		check(Base64.encode(new byte[] {'7','8','9','0', 'A'}), "Nzg5MEE");
		check(Base64.encode(new byte[] {'7','8','9','0', 'A', 'B'}), "Nzg5MEFC");
	}
	static void decodeBase64() throws Exception {
		check(Base64.decode("MQ"), new byte[] {'1'});
		check(Base64.decode("MjM"), new byte[] {'2','3'});
		check(Base64.decode("NDU2"), new byte[] {'4','5','6'});
		check(Base64.decode("Nzg5MA"), new byte[] {'7','8','9','0'});
		check(Base64.decode("Nzg5MEE"), new byte[] {'7','8','9','0', 'A'});
		check(Base64.decode("Nzg5MEFC"), new byte[] {'7','8','9','0', 'A', 'B'});
	}
}
