package crypt;

import sys.Log;
import text.Text;

public class Base64App {

	/*
	 * RSAPrivateKey ::= SEQUENCE {
      version           INTEGER,  -- Version,
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

	static String rsa_pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCxT6EoUHQw2H8/HYLejurXLzW\n" +
			"F43A7BKAOL6c7U3Wlv8gRUPbZ7JWQpTq4ooA1GgJuiQj4ockoLccmZLRyYk8/pva\n" +
			"5rDOwyslVVjtpz6GfFCKWBiQPgq38P9xgw4PYsXeYFINAUykzSkvrCnaBrVAmIUg\n" +
			"PBcoGT5v7fDbLpinqwIDAQAB"
			;
	static String rsa_prv =
			"MIIBOQIBAAJBAIOLepgdqXrM07O4dV/nJ5gSA12jcjBeBXK5mZO7Gc778HuvhJi+\n"+
			"RvqhSi82EuN9sHPx1iQqaCuXuS1vpuqvYiUCAwEAAQJATRDbCuFd2EbFxGXNxhjL\n"+
			"loj/Fc3a6UE8GeFoeydDUIJjWifbCAQsptSPIT5vhcudZgWEMDSXrIn79nXvyPy5\n"+
			"BQIhAPU+XwrLGy0Hd4Roug+9IRMrlu0gtSvTJRWQ/b7m0fbfAiEAiVB7bUMynZf4\n"+
			"SwVJ8NAF4AikBmYxOJPUxnPjEp8D23sCIA3ZcNqWL7myQ0CZ/W/oGVcQzhwkDbck\n"+
			"3GJEZuAB/vd3AiASmnvOZs9BuKgkCdhlrtlM6/7E+y1p++VU6bh2+mI8ZwIgf4Qh\n"+
			"u+zYCJfIjtJJpH1lHZW+A60iThKtezaCk7FiAC4= \n"
			;

	static String rsa_prv2 =
			"MIIEogIBAAKCAQEAs0bBjkKszSTXG1dFRdUTt40pTpMk5Eoeo9Po4uAy1SanAbfw\n" +
			"nY7yjCOo5UX6G3XoPcGt7hX4d+jpgNWCO8eZJPqflV/6lt/hBmH8lKBhmJUyBQ1E\n" +
			"8X0Fd8Phnvj5N0aQ3HVuEk5hdsRKUBdDTmvxRLe4H7fjh41s2/q+wmG49V0tD8ID\n" +
			"JIyPFiuyUmd5Rdyl62PI1ubw5HzZuJP8lpfgI9Xz1Y8WjkYcgztvYQpY7BtFymXM\n" +
			"+CUdWRtJgQeFZ6U1y0uLDZ1xUqTZw+/0tLVlaxvKJU+Pz8qAlqAvZBWMwKBkDk2R\n" +
			"ejPad1/cOcYJ8CslG9dHBZghTDITBuoDGy8kOQIDAQABAoIBADvzgW6tZVQE6do+\n" +
			"yO+dXcaX8iRQmJmvH6fxiYO1LcqR7m9or5JA52Nt2WEykXz1ZQCh8CQaPMj8nnly\n" +
			"7OZIzTHMjelJ+2bQ31NfGTQqnfiEjAGcel23TUjLvHuJDu4pLvBKZNDCXV12LtOx\n" +
			"s9RZzIkKVwxkVjZSWQEHTRt7ynpiawijuxZCO+TZtd4bSv9mUpRsDQk5sSRindYg\n" +
			"ryQzA9SeXaKjgd86xA2zKV0ZzL5fiE6aDV7xTDahtJrP8/06ozlHhLSSqVpL4jv8\n" +
			"8cXhdtbVa/Q4DUsUOrVdbk3hAIYjHK9BPAzyMtckVhzMmcIGpZlPTtu71tQA/kMp\n" +
			"quwLAfECgYEA22CHMbu9In7Tk8XOWZJ3G98/y6aFf4Ea4VCFNyx25Yi3AAvtfLuK\n" +
			"W+/WGirQjpSzP5ufXJiod1GtvS1nN6XMnlo8VSFB8hBCayrd5Ee3MOru3J6FgnyN\n" +
			"TI9MUjxSTt/2vwuFvd6N+AKjFBIgzy3a+AfssZ9rqztluOsGOlQwfvMCgYEA0TR0\n" +
			"MrZDUif7hxKWrmkjEyZeYHZXUqqXuTolOwIYqWd3I/6yOoVx1+6oHLg4pcEvmN7l\n" +
			"grlOQvmrL5LiPr67SlkmyHK+G9Hb6GiUrvk4XizFNwz66FR7mv1HxCqHCIdsoxFK\n" +
			"DdjgbpymMxeuT5EzoddaBJzzAfVrj3c8WS5HUyMCgYBaXoAyr4ixDhewNvrTES+5\n" +
			"rpSEGtvBc3iUOmw2Fz3/PftMrJ0vFb5gMwM6kkqJgZ35ZO0X4tb7GA1+8ZYkaUtK\n" +
			"LBfQZbvZsHL73JO9dwFyyESPY0nSP+ahFCK3eZvaXA40EtYMKgLHmKZ/HRyg293o\n" +
			"8LLDVM2wou47Z62QF6BGmwKBgAtbk6tlJcaCES/GHJQfdR4HCTyncBo21MHjnD3V\n" +
			"4UzhJyC3JSn0MLgOZuYYQksNb+4Wu9MbCFquf1K34LyTUku5B1f+kr+j5xJ5Cwls\n" +
			"0liaiaJxe/W9EHDbq1fFD0uxPn6j46dYzOZ0brl7YaVENs9kh6iUdMT4c/x39rjI\n" +
			"wGqNAoGASDm5pGiu9rItW6LllCfJcsUSaFP1KEGMI8Cgh76rMcTHY9yOmTsD/saD\n" +
			"aDiBDhnxY+5WppDEVAXdW/9Qj80gChkD+wEYnE1uvylWZ2ycO0xAcpg2n78HqHHr\n" +
			"Lf57iKMoKPA7aw91QslE+tbDm/aHupYPvkTFHBteFYJ/xxO9UVs="
			;

	static String cert =
			"MIICoTCCAYkCAQEwDQYJKoZIhvcNAQELBQAwWDELMAkGA1UEBhMCUEwxDDAKBgNV\n" +
			"BAgMA01hejEMMAoGA1UEBwwDV2F3MQ4wDAYDVQQKDAVTUlBPTDEOMAwGA1UECwwF\n" +
			"U2Vjb3MxDTALBgNVBAMMBFJPT1QwHhcNMTgwNzI3MTIyMzUwWhcNMTkwNzI3MTIy\n" +
			"MzUwWjBZMQswCQYDVQQGEwJQTDEMMAoGA1UECAwDTWF6MQwwCgYDVQQHDANXYXcx\n" +
			"DjAMBgNVBAoMBVNSUE9MMQ4wDAYDVQQLDAVTZWNvczEOMAwGA1UEAwwFUGFydEEw\n" +
			"gZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAOMnm9nU9wNgVrsKyp1dKscGRrgb\n" +
			"42Kd1eB7Ep2uAAFH6LDzgPZtwvRwSYgEmfn6ovjDQ5OCZTI2cyZ43lqvzrpM0JhU\n" +
			"Fgv6T1LxIrxPfM59gSkAHmIqlndMDT0rHCakYyTNwyLA0EOnbG8H4GXLoKegYCKd\n" +
			"vr22OCjCoq0paZVFAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAGQi+O+C1PeGW/9R\n" +
			"M9m3S00GFGJ5Dbqu9td3CFsq1/QLROcNutgv3y+aOyBUWYqhnV7n9cQ9uyesIuj7\n" +
			"lRbbY0T6EyVlsht2OiFgK6mPZjE8BLyQZHauuxd4YfXQfQDV2J6Atwc75QBE/l+d\n" +
			"JsAx9VqLg/OgU6i3JLpNxfUpzFm31lIAoFIYr4aef8NzoAbDcK2t1ZQpzuZjcKBQ\n" +
			"j9rNEdt70Qvr/ys+m4phdX8zUclx/EYNkG4i448hANZWLz87qk+NT1GizGorjDXn\n" +
			"BKfcjRwM2mloFq6QAgRW+hwz0H9oFIIEkuMuczoxwTEn3f2N35FqaZHsxxZ5Kp8n\n" +
			"Io4Fr8Y=\n"
			;

	public static void parseTLV(byte[] b, int offs, int len) {
		String[] elems = {"version","modulus","publicExponent","privateExponent",
				"prime1","prime2","exponent1","exponent2","coefficient","otherPrimeInfos",
		};
		int r=0,el=0;
		TLV tlv = new TLV();
		for (int i=0; i < len; i+=r) {
			r = tlv.read(b, offs+i, len-i);
			if (r == 0) break;
			if (tlv.isConstructed()) {
				Log.prn("SEQUENCE (read=%d) TLV: %s", r, tlv.toString());
				parseTLV(b, tlv.getValueOffset(), tlv.l);
			}
			else {
				if (el < elems.length)
					Log.prn("%s (read=%d)  %s", elems[el], r, tlv.toString());
				else
					Log.prn("Elem[%d] (read=%d):\n  %s", el, r, tlv.toString());
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
		byte[] bin = Base64.decode(rsa_prv2);
		Log.prn("%s", Text.hex(bin));
		parseTLV(bin,0,bin.length);

		//bin = Text.bin(map_mt_req);
		//parseTLV(bin,0,bin.length);

		Log.prn("-----------------------------------");
		bin = Base64.decode(rsa_pub);
		Log.prn("%s", Text.hex(bin));
		parseTLV(bin,0,bin.length);

		Log.prn("-----------------------------------");
		bin = Base64.decode(cert);
		Log.prn("%s", Text.hex(bin));
		parseTLV(bin,0,bin.length);
	}

}