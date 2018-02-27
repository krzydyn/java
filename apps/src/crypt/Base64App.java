package crypt;

import sys.Log;
import text.Text;

public class Base64App {

	static String rsa_prv =
			"MIIBOQIBAAJBAIOLepgdqXrM07O4dV/nJ5gSA12jcjBeBXK5mZO7Gc778HuvhJi+\n"+
			"RvqhSi82EuN9sHPx1iQqaCuXuS1vpuqvYiUCAwEAAQJATRDbCuFd2EbFxGXNxhjL\n"+
			"loj/Fc3a6UE8GeFoeydDUIJjWifbCAQsptSPIT5vhcudZgWEMDSXrIn79nXvyPy5\n"+
			"BQIhAPU+XwrLGy0Hd4Roug+9IRMrlu0gtSvTJRWQ/b7m0fbfAiEAiVB7bUMynZf4\n"+
			"SwVJ8NAF4AikBmYxOJPUxnPjEp8D23sCIA3ZcNqWL7myQ0CZ/W/oGVcQzhwkDbck\n"+
			"3GJEZuAB/vd3AiASmnvOZs9BuKgkCdhlrtlM6/7E+y1p++VU6bh2+mI8ZwIgf4Qh\n"+
			"u+zYCJfIjtJJpH1lHZW+A60iThKtezaCk7FiAC4= \n"
			;

	/*
	 * RSAPrivateKey ::= SEQUENCE {
      version           Version,
      modulus           INTEGER,  -- n
      publicExponent    INTEGER,  -- e
      privateExponent   INTEGER,  -- d
      prime1            INTEGER,  -- p
      prime2            INTEGER,  -- q
      exponent1         INTEGER,  -- d mod (p-1)
      exponent2         INTEGER,  -- d mod (q-1)
      coefficient       INTEGER,  -- (inverse of q) mod p
      otherPrimeInfos   OtherPrimeInfos OPTIONAL
	}
	 */

	public static void parseTLV(byte[] b, int offs, int len) {
		String[] elems = {"version","modulus","publicExponent","privateExponent",
				"prime1","prime2","exponent1","exponent2","coefficient","otherPrimeInfos",
		};
		int r=0,el=0;
		TLV tlv = new TLV();
		for (int i=offs; i < len; i+=r) {
			r = tlv.read(b, i, len-i);
			if (r == 0) break;
			Log.prn("r=%d TLV:  %s", r, tlv.toString());
			if (tlv.isConstructed()) {
				Log.prn("Constructed:");
				parseTLV(b, tlv.getValIdx(), tlv.l);
			}
			else {
				/*if (el < elems.length)
					Log.prn("%s:\n  %s", elems[el], tlv.toString());
				else
					Log.prn("Elem(%d):\n  %s", el, tlv.toString());
					*/
				++el;
			}
		}

	}

	//https://adywicaksono.wordpress.com/2007/07/16/compare-tcpip-ss7-protocol-stack-based-on-osi-layer/
	//45...
	static String map_mt_req =
			"17 09 000732080700101015"+
			"18 09 040797440000000019"+
			"19 9e 400a915413131215000070206091553200a0050003e10201363c180c0693ddc2b78d0f447dbbf320213b9c9683d0e139681e4e9341e8f41c647ecbcbe9b31b047fb3d3e33c283d0789c66f375dfeb697e5f374982d0289eb74103a3d0785e170f93b3c4683de66503bcd4ed3c3f23c28eda697e5f6b29b9e7ebb41edfa9c0e1abfddf4b4bb5e7681926e5018d40eabdf72d01c5e2e8fd12c10ba0c9a87d3"+
			"1a 00"+
			"0e 01 01"+
			"00";

	public static void main(String[] args) {
		byte[] bin = Base64.decode(rsa_prv);
		Log.prn("%s", Text.hex(bin));
		parseTLV(bin,0,bin.length);

		//bin = Text.bin(map_mt_req);
		//parseTLV(bin,0,bin.length);
	}

}
