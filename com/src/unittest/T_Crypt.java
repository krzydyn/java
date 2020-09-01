package unittest;

import java.math.BigInteger;

import crypt.DH;
import crypt.DSA;
import sys.Log;
import text.Text;

public class T_Crypt extends UnitTest {

	static void _expmod() {
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
		byte[] hash = new byte[20];
		byte[] h1 =Text.bin("213123455325");
		System.arraycopy(h1, 0, hash, 0, h1.length);

		DSA dsa = new DSA(1024, 160);
		dsa.generateXY();
		byte[] sign = dsa.signDigest(hash);
		Log.debug("sign: %s", Text.hex(sign));
		if (!dsa.verifyDigest(sign, hash))
			Log.error("verify failed");
		else
			Log.info("verify OK");
	}
	static void _dsa_pq() {
		byte[] p = Text.bin("8CEBFBCD79983F2C4E6C8D9F5B985295DC0E9B57AC1E6833BEA35ED0A714B98B590FA1B8D6F152258FD31A7D96468A8986DB3D6549E4A46CB7EA96870735222D4296BA235242BBF618A8C8AD4D9450A601353E6431B5CDF44DE9988164BEDE9A381D350F26A539EF5DC634B9405E9649DF125ABDC731C57819AD56761F81D05F");
		byte[] q = Text.bin("8A4D115589447E1EEE1F9EE0976C1A2C94FC6FFD");
		byte[] g = Text.bin("2ED12D211C55D11EDE4BCB1A08092836B056B07B553013DB078E6E7FB5F32BB0D6EE2AE16F1BF690AF8AE34251B6BC7585CD56DD1AFB857385E580F811B6ABEC7BEF7ABE0D754BDC70308F4D399D6E6689757E420EEF9DE2253BB2AD13E6CEED990019C3F00131F565A5DBC68C18CC426613AF52A6DF8D00A4F639F551953F80");
		byte[] y = Text.bin("0FCC5285B0B8690F5FB82D2918048B853DB148491E8D5AE5CC10A8C970ED87DA1F0AB8F5137D1F7E432E7E11626BEAF1E519E1659CB89EB330C56CB7E717EAAFC05AD7562DC5DAB1515F01FCDCAFC45C4510FC0AE684ECDED40613F321DFCFC12EB2399EF086C1BA7643F2710A579055575B5B66BB3066893E62130023E15127");
		byte[] x = Text.bin("7889AA50AD80A5396D0D3CB28AD6321CCB47BD52");
		byte[] hash = new byte[20];
		byte[] h1 =Text.bin("213123455325");
		System.arraycopy(h1, 0, hash, 0, h1.length);

		DSA dsa = new DSA(new BigInteger(1,p), new BigInteger(1,q), new BigInteger(1,g));
		dsa.setXY(new BigInteger(1,x),new BigInteger(1,y));
		byte[] sign = dsa.signDigest(hash);
		Log.debug("sign: %s", Text.hex(sign));
		if (!dsa.verifyDigest(sign, hash))
			Log.error("verify failed");
		else
			Log.info("verify OK");
	}

	static void _dh() {
		DH dh = new DH(128);
	}
}
