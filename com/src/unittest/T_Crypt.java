package unittest;

import java.math.BigInteger;

import crypt.Asymmetric;
import crypt.DH;
import crypt.DSA;
import sys.Log;
import text.Text;

public class T_Crypt extends UnitTest {

	static void _bigint2str() {
		BigInteger b = BigInteger.valueOf(0x1234);
		Log.debug("0x1234: %s", Text.hex(b.toByteArray()));
		b = BigInteger.valueOf(-0x1234);
		Log.debug("-0x1234: %s", Text.hex(b.toByteArray()));
		b = BigInteger.valueOf(0xfecc);
		Log.debug("0xfecc: %s", Text.hex(b.toByteArray()));
	}
	static void _gen_prime1() {
		int bits = 1024;
		for (int i = 0; i < 5; ++i)
			Asymmetric.genPrime1(bits);
		bits *= 2;
		for (int i = 0; i < 5; ++i)
			Asymmetric.genPrime1(bits);
		// for more bits it takes really long
	}

	static void _gen_prime2() {
		int bits = 1024;
		for (int i = 0; i < 5; ++i)
			Asymmetric.genPrime2(bits);
		bits *= 2;
		for (int i = 0; i < 5; ++i)
			Asymmetric.genPrime2(bits);
	}

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
	static void _dsa() {
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
		byte[] p = Text.bin("2D3BB42364095359D9029CAAB33252937718F589FC2AE5E5FA2556E4DBAF6A5F5E49DBE77666C61EEC2436E52D5FF1B9AD7872131E21FFAB8AE2DB790F16E3EACA6B162B12A2F1352D6C0359CB1724D96028456D2F8C120BE480B66FDCFA3088918C7830BAA2305EF6E591B26CAFA780B39731E87927C602BC04F11F44A453A3");
		byte[] q = Text.bin("710EC0AD5F8B0CA0674CE133454656BAA5411A07");
		byte[] g = Text.bin("057420043032D1F3D6BCD35C387C0BCDE5E2922EE0266240EB70FDB387BD30EAE660B1D4B73CD89561C0CEA057B1B5FE19048589811F9CFEB557B2BE2B155F2CC35D9A9679F4810A0B6B1AB87C3F32F1F508A95BBD6098C1DCF7AE75743FE8D27263A58948983D645BE44AA07C12B12DE8B8DC2233690DEEFF5BAAC78591078D");
		byte[] y = Text.bin("15BCEE67E676BA3DC708CB4EADDEB5A93F85E51C9136CDC84BF2C0FCEA702E67A81FFC3B4FB80B976AA8FF0F423E019C24A87354B003F6D6945527FB93EC1706417714DCCFEF657E3A5AF92CE0795D809A271951B796FACA8889EB08332647FE121EDA51AD2DF4731B05A4771B469302CBB6453AF98695953D5B8A389E41B423");
		byte[] x = Text.bin("0698E417200AE09764056711411FC75DF0BE9B1F");
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

	static void dh() {
		DH a = new DH(2048);
		DH b = new DH(a.getBasePrime(), a.getGenerator());

		a.generateXY();
		b.generateXY();

		BigInteger sh_a = a.deriveShared(b.getPublicKey());
		BigInteger sh_b = b.deriveShared(a.getPublicKey());

		Log.debug("shared A: %s", sh_a.toString(16));
		Log.debug("shared B: %s", sh_b.toString(16));
		check("shared", sh_a.equals(sh_b));
	}
}
