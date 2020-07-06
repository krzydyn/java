package unittest;

import java.math.BigInteger;

import crypt.DH;
import crypt.DSA;
import sys.Log;
import text.Text;

public class T_Crypt extends UnitTest {

	static void expmod() {
		BigInteger b = new BigInteger(1, "12345678".getBytes());
		BigInteger e = new BigInteger(1, "23456789".getBytes());
		BigInteger n = new BigInteger(1, "987654321".getBytes());
		Log.debug("b = %s", b.toString(16));
		Log.debug("e = %s", e.toString(16));
		Log.debug("n = %s", n.toString(16));
		BigInteger r = b.modPow(e, n);
		Log.debug("r = %s", r.toString(16));


		b = new BigInteger(1, Text.bin("D2E21A8F66C3A4F08ED9BC6545BDBADA"));
		e = new BigInteger(1, Text.bin("AD5BD8DAB7D74721073EAB2162924794"));
		n = new BigInteger(1, Text.bin("AD5BD8DAB7D74721073EAB2162924795"));
		r = b.modPow(e, n);
		Log.debug("r = %s", r.toString(16));
	}
	static void dsa() {
		DSA dsa = new DSA(128,16);
	}

	static void dh() {
		DH dh = new DH(128);
	}
}
