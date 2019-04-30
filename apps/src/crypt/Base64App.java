package crypt;

import java.io.ByteArrayInputStream;

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


	static String inter_cert = "MIIELzCCAxegAwIBAgICMDkwDQYJKoZIhvcNAQELBQAwRTELMAkGA1UEBhMCQVUx\n" +
			"EzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMg\n" +
			"UHR5IEx0ZDAeFw0xODA4MDYxNDM1NTVaFw0xOTA4MDYxNDM1NTVaMEsxCzAJBgNV\n" +
			"BAYTAlBMMRowGAYDVQQDDBFTYW1wbGVDb21tb25OYW1lMjEgMB4GA1UECgwXU2Ft\n" +
			"cGxlT3JnYW5pc2F0aW9uTmFtZTIwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIK\n" +
			"AoICAQCj0mI3lBP/cL8KLZWm1dJEnrUQuP83xnDJ6CyBbLsk+1zLfAZl/ONs1Fe/\n" +
			"JDdSvQCSAMXpd8EriTcWn4xzTDpxyrgIBoBs/W5Vq1m18g9AqfF2gT302F/jKK/1\n" +
			"r6QFL4b1VGBVylaI+gP+O1lGiJN2mJWEEosR341fTmGFS6FTc8nX9ybEFl0Gqpwn\n" +
			"t6PEAz89qz/Pgl7+YVBY+RFu25qNk03S1wOPal62fa91a8QhnjoEDadkdVs/kIdg\n" +
			"mq166NgZinI4u8olm7TWfc8Kziu7eQhx2Lvjb+68120VK+BmjA0c8sH2lt/GTygN\n" +
			"QHbrYATpdl3HvWVP5khBVrXgAOMaZ5Hc+nnm+8tf5yFwSwR8V0gLZAEYBYATgUfz\n" +
			"A/zCr0ve2B6JcfRqQ1PdPA4duaGn41OTdZJQ9Kc0XJGRzktBGruWxOYugHtHSTp+\n" +
			"Zzgyqi4mD9uq8Mcw0ydJsjFKRnJiRepic2uMrLEo5FVJ7zPcsu5bz+7tk1ERxor1\n" +
			"xF5cf3dvNBvFJx6+L65Rr9fFIlKNp7o3jwvbBbms5NmJZl+FxlZ4UdROnp9kReNq\n" +
			"sgx5e/l3XFifUNufpUwxeRLjc0f/+iZ+cluJR6IGF7JcrItKlnSh/H8PGa/yfLWl\n" +
			"5va6WwmCCetDNQNbXcSyxPwfmqxkpw97GUplO5sRGsHYKxPq1wIDAQABoyMwITAP\n" +
			"BgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBhjANBgkqhkiG9w0BAQsFAAOC\n" +
			"AQEAGAarKN+QbzhIUXpGsxhiiQW15EgkfKHeLnuNwI5GUIQBJm0Zxot2ELdoztSE\n" +
			"TPzqAs87jr1SBJQorfSAV7zI2nYymhmAvciKGdpnjbMf8OGo8i0vBAyUoIvp4Nue\n" +
			"3XCUc++f6QFV91PtNUjXR4i/YR8L839cDL9QCGLHcZgXhheRGy2n+AOcbyECznZI\n" +
			"lD2IMuGUnG3JHvkgtW8eGsHVeGkPXQF1qhvI1uADhXwr0lRk6xj25DJRpBVEANDt\n" +
			"+joMWoiBFC0w3Zr34icppwV2gwQ+9c4taRNhUsMwa5YtqCfRwhi95M6OfBLTn+MC\n" +
			"3uRKo6QfgrHxpxlEG/XSvKY4Ew==\n" +
			"";

	static String pkcs7 = "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\n" +
			"uPDgjKGo8tXy4G7n7mlMGJPhm57veUrQHgi/Nw==\n" +
			"";

	public static void parseTLV(int ind, byte[] b, int offs, int len) {
		String[] elems = {"version","modulus","publicExponent","privateExponent",
				"prime1","prime2","exponent1","exponent2","coefficient","otherPrimeInfos",
		};
		int r=0,el=0;
		TLV_BER tlv = new TLV_BER();
		String indent = Text.repeat("    ", ind);
		for (int i=0; i < len; i+=r) {
			r = tlv.read(b, offs+i, len-i);
			if (r == 0) break;
			if (tlv.isConstructed()) {
				Log.prn("%sCONSTR (read=%d) T=%x L=%d", indent, r, tlv.tagAsLong(), tlv.vl);
				parseTLV(ind+1, b, tlv.getValueOffset(), tlv.vl);
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

	public static void parseTLV2(byte[] b) {
		ByteArrayInputStream is = new ByteArrayInputStream(b);
		try {
			TLV t = TLV.load(is);
			Log.debug("TLV: %s", Text.hex(t.toByteArray()));
		} catch (Exception e) {
			Log.error(e);
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

	static String opensslSIG_SHA1 = "302C02144B7DBDF74AAD8DE4945C35BD5EA87955075BBCFE02146CC3573E09766D150FF7D7EE22AC9B55DFD5A5A9";
	static String opensslSIG_SHA224 = "303D021D00D9C131ED53023AA1040300A3E93E52AC263ED5997509164BFDBAB667021C0DC53494E298E51C718271FE41F6E78926D0E733489F184435D92712";
	static String dsaSIG_SHA224 = "303D021C645121F72308C6769B49ED9F295DB8DFBCBE6D3C34E74393DAFB5878021D00CEE911F028B854F6E1B7B88C77D09C0E9FE810EF9EF9263755B8B678";
	static String pkcs1DER = "30 51 30 0d 06 09 60 86 48 01 65 03 04 02 03 05 00 04 30";

	static String emvData = "84667DD7C0F1F32A02606588789A57A7A9EEE1583544CB8A6B140D071924D35D949ED5ED7A54B69A84667DD7C0F1F32A02606588789A57A7A9EEE1583544CB8A0723DB6DF0FCE6CF0000000000000000";

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
		parseTLV(0,bin,0,bin.length);

		Log.prn("-----------------------------------");
		Log.prn("-----------------------------------");

*/
		/*
		bin = Base64.decode(pkcs7);
		Log.prn("%s", Text.hex(bin));
		parseTLV(0,bin,0,bin.length);
		Log.prn("%s\n", Base64.encode(bin));
		*/


		/*bin = Text.bin(opensslSIG_SHA1);
		parseTLV(0,bin,0,bin.length);

		bin = Text.bin(opensslSIG_SHA224);
		parseTLV(0,bin,0,bin.length);

		bin = Text.bin(dsaSIG_SHA224);
		parseTLV(0,bin,0,bin.length);

		bin = Text.bin(pkcs1DER);
		parseTLV(0,bin,0,bin.length);*/

		bin = Text.bin(emvData);
		parseTLV(0,bin,0,bin.length);
		parseTLV2(bin);
	}

}