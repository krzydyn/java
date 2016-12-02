package crypt;

import crypt.GenSecosCrypt.Log;

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

	public static void parseTLV(byte[] b) {
		String[] elems = {"version","modulus","publicExponent","privateExponent",
				"prime1","prime2","exponent1","exponent2","coefficient","otherPrimeInfos",
		};
		int r=0,el=0;
		TLV tlv = new TLV();
		for (int i=0; i < b.length; i+=r) {
			r = tlv.read(b, i, b.length-i);
			if (tlv.isComplex()) {
				Log.prn("RSAPrivateKey");
				parseTLV(tlv.getVal());
			}
			else {
				Log.prn("%s:\n  %s", elems[el], tlv.toString());
				++el;
			}
		}

	}

	public static void main(String[] args) {
		byte[] bin = Base64.decode(rsa_prv);
		//Log.prn("%s", Text.hex(bin));
		parseTLV(bin);
	}

}
