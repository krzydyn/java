package unittest;

import crypt.Base64;
import sys.Log;
import sys.UnitTest;

public class T_Base64 extends UnitTest {
	static void encodeBase64() throws Exception {
		//Log.debug("base64 = %s", Base64.encode(new byte[] {'1','2','3','4','5','6','7','9','0'}));
		Log.debug("base64 = %s", Base64.encode(new byte[] {'5','6','7','8','9','0'}));
	}
}
