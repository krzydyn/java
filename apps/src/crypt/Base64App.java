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


	static String inter_cert = "MIIEpDCCAowCAQAwXzELMAkGA1UEBhMCUEwxDDAKBgNVBAgMA01hejEMMAoGA1UE\n" +
			"BwwDV2F3MQ4wDAYDVQQKDAVTUlBPTDEOMAwGA1UECwwFU2Vjb3MxFDASBgNVBAMM\n" +
			"C0lORVJNRURJQVRFMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEArhND\n" +
			"rnpcc5uFpFUV2Jxvxo9IhQI00glD+HRSWmryolUpQD57lvikvkHYM7vTpAMQJ5U2\n" +
			"FuzGoB1feS9euJCKpK/5K3FXrQkZfqDqYyO3OvzT2RWsCX7tOq3sGV26ZzIq/aAq\n" +
			"EE/LrfLJardgwgr7MJxpt6TUYVIyPGwkR25acMgIpDfjEMDGbJyotF+Ko2QjvlYt\n" +
			"+oeQYv7VSrwy18JwiQYxs4HmJbiFsc/sZU10dWJO10q/hjibKd++8MNIZVwQ7VBY\n" +
			"6mEMmSfEZPDnqI6s/VUs4S1kR/KevXu8Z4WtF4yDq7XxauiVb4OcBwrDQ/l6x9uR\n" +
			"MCujHfnbFouxCVQobIUUydmkjsuseTMGvAEuvNfZUY3Zv6yMH/grgs5cpfeotFYC\n" +
			"8t1MZu/o+sJbAyDPAU3hCcgFDWYi50/hNERsqjFsk4fHu1lBhZ45LCH5wkRJTp8n\n" +
			"xLE2npb/gdTyzbYDCn3Oyt6nlRu+PwkBlNEwTYSaD0BqLXDzAL+Cgegdnn7sxGlW\n" +
			"WiePhjmK37/87dN8YVfVPD1lcCyUy8AbNZ869wxkeyu3huOd32LgZDEiOZ5GDoef\n" +
			"w4/tEThoiKrbb+dLfE/xWduX4r0qyc9pSpQuaNan/GiWOoaAmoq/uw9NWFqYMWQW\n" +
			"WlhDnqI5dKevavBJfnW7kAadx1vE3j/LOxBFTZ0CAwEAAaAAMA0GCSqGSIb3DQEB\n" +
			"CwUAA4ICAQCLkk8kr90aAlD3q1VGR/LAsUro6D8/tA1p3ySHKyvdgyfjIknlLfm1\n" +
			"Yr72NHaTBRZCKScuWY0NRN5vWv+flENSPc8livNz7lRt1nZ2Iwgx1OLgYTJJCoqn\n" +
			"mx6Jy6KkrGV0aszBqClH7XCoSapLBvQ71ts07nB+5J7PxWYzb3FZ2eRE+2tyD4Y3\n" +
			"P5rHQbVMR2LXirpMDV+t400n0cedLxtC/H/Zq2hnmkoN+Vno5h3LfXipPPG8Sa0+\n" +
			"YKYpXtuVWE1Vu5BlUqziB5G0BeSFslBmXzqLCm+8Z3asnT9iH5OFQFpjUGsVwpre\n" +
			"0RU4XciZketvNyJ9T2voGJiYM8YVQAEwYlqxOZzb0STWv5p49tWX6gIH3xUmdpmO\n" +
			"8Kv2xywq/FuNGe9PEwr7a6GJDZfjcd6Y/N2oDnnTyU9Dzdbdln/ZJBrBd1eFtjZl\n" +
			"P60NitqZPF7gqlrf+i610Zowd+FOxDGPbf6HjXkCQFBM0MXl4hE+tH77DMloC5w9\n" +
			"0PlG0P0p2u5n7KR9naAKhCjZXn8WSEfj7/klrOmkqSsORPcHKXh54tTGNHwDn4hO\n" +
			"O5WoQEzy08HW/rU/9c6oddlXg+/gj0juBrDucQlqzDTU0Nhrv4rIyGdXFHPk5imO\n" +
			"tBOHWgVYEAVufA5xsAKMUh0oHVT0o3cqOvgzejI8BkyOepj1V9qMuQ==";

	static String pkcs7 = "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\n" +
			"H6DJPpSr+80BmJyey+f1A9v7jHj1w86yll3gMA==\n";

	public static void parseTLV(int ind, byte[] b, int offs, int len) {
		String[] elems = {"version","modulus","publicExponent","privateExponent",
				"prime1","prime2","exponent1","exponent2","coefficient","otherPrimeInfos",
		};
		int r=0,el=0;
		TLV tlv = new TLV();
		String indent = Text.repeat("  ", ind);
		for (int i=0; i < len; i+=r) {
			r = tlv.read(b, offs+i, len-i);
			if (r == 0) break;
			if (tlv.isConstructed()) {
				Log.prn("%sSEQUENCE (read=%d) T=%x L=%d", indent, r, tlv.tag(), tlv.l);
				parseTLV(ind+1, b, tlv.getValueOffset(), tlv.l);
			}
			else {
				//if (el < elems.length)
				//	Log.prn("%s (read=%d)  %s", elems[el], r, tlv.toString());
				//else
					Log.prn("%sElem[%d] (read=%d):  %s", indent, el, r, tlv.toString());
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
/*		Log.prn("%s", Text.hex(bin));
		parseTLV(0, bin,0,bin.length);

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

		Log.prn("-----------------------------------");
		bin = Base64.decode(inter_cert);
		Log.prn("%s", Text.hex(bin));
		parseTLV(bin,0,bin.length);
*/
		Log.prn("-----------------------------------");
		bin = Base64.decode(pkcs7);
		Log.prn("%s", Text.hex(bin));
		parseTLV(0,bin,0,bin.length);
	}

}