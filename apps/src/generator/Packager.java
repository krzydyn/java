package generator;

import java.nio.charset.Charset;

import crypt.Base64;
import sys.Log;
import text.Text;

/*
 * generating package
 * [image] -> concat(header,image,manifest)
 *
 * padding image
 * [srcImage] -> strip -> [image] -> pad (1024 byte block) -> [image.pdded]
 */
public class Packager {
	static String PLAIN_KEY = "Y2FlZTI3MGJlN2IwZjMyNTM3OWRlZDU0OGQxMGMwZmZiZmJhYTc5NTY5MzY3Y2Q5ZTIzZWNjMmZiY2ExOGViZg0K";
	static byte[] key = Base64.decode(PLAIN_KEY);


	String mUserName = "";
	String mServerPath = "";
	String mServerPath_proxy = "";
	String mTmpFolder = "";
	long mNetworkTimeout = 1000;

	String cookiePath = "";

	String curl_RequCookie = "curl --dump-header " + cookiePath +
			" -w '\nHTTP:%{http_code}\nContent-Type:%{content_type}\n'" +
			" -F \"user=\"" + mUserName + "\"" +
			" -F \"day=1\"" +
			" http://" + mServerPath +
			"issueToken.do" +
			" --noproxy " + mServerPath_proxy +
			" --max-time " + mNetworkTimeout;

	String cookie = "";
	String tempPadFileName;
	String modelNames;


	String ln259_RequestCookie = "curl --dump-header ./ismscookie.txt -w '" +
			"\nHTTP:%{http_code}\n" +
			"Content-Type:%{content_type}\n" +
			"'" +
			" -F \"user=abuild\" -F \"day=1\" http://10.40.68.214/issueToken.do --noproxy 10.40.68.214 --max-time 20";

	String ln324_VerifyCookie = "curl -b ./ismscookie.txt http://10.40.68.214/checkCookie.do"
			+ " --noproxy 10.40.68.214 > ./tempFilesSrEuf9/outputCookie.txt --max-time 20";

	String ln605_RequestEncryptionISMS = "curl -b ./ismscookie.txt"
			+ " -F \"file=@./tempFiles2bYrst/temp.elf.padded\""
			+ " -F \"modelName=KantM\""
			+ " -F \"hash=ce240202a70dbea5d219ee1852bc5dd1918d8ba8\""
			+ " -F \"mode=enc\""
			+ " -F \"pad=nopad\""
			+ " -o \"./tempFiles2bYrst/temp.elf.zip\""
			+ " http://10.40.68.214/encryptTA.do"
			+ " --noproxy 10.40.68.214"
			+ " --max-time 20"
			+ " -D -"
			+ " | grep hash: | sed s/hash:// | tr -d \" \\\\r\"";

	String ln707_RequestSignatureISMS = "curl -b ./ismscookie.txt"
			+ " -F \"file=@./tempFiles2bYrst/temp.elf.tmp\""
			+ " -F \"hash=4da5743cff075548ec0bf97c01ce41454abd798b\""
			+ " -F \"privilege=platform\""
			+ " -F \"mode=sign\""
			+ " -o \"./tempFiles2bYrst/temp.elf.sign\""
			+ " http://10.40.68.214/signTA.do"
			+ " --noproxy 10.40.68.214"
			+ " --max-time 20"
			+ " -D -"
			+ " | grep hash: | sed s/hash:// | tr -d \" \\\\r\"";

	String ln802_RequestDSignatureISMS = "curl -b ./ismscookie.txt"
			+ " -F \"file=@./tempFilesGrZfeE/temp.elf.sign\""
			+ " -F \"hash=42e00937fc7e8a9df0e1ad54f0913e4783058bdc\""
			+ " -F \"privilege=platform\""
			+ " -F \"mode=verify\""
			+ " -o \"./tempFilesGrZfeE/temp.elf.dsign\""
			+ " http://10.40.68.214/signTA.do"
			+ " --noproxy 10.40.68.214"
			+ " --max-time 20 -D - | grep hash: | sed s/hash:// | tr -d \" \\\\r\"";

	String RequestEncryptionISMS = "curl -b " + cookie +
			" -F \"file=@" + tempPadFileName + "\"" +
			" -F \"modelName=" + modelNames + "\"" +
			//" -F \"hash=" + getHash(mTmpFolder, tempPadFileName, SHA_TA) + "\"" +

			"";

	public static void main(String[] args) {
		String strkey = new String(key, Charset.forName("ASCII"));
		Log.debug("key: (%d) %s", strkey.length(), strkey);
		byte[] k = Base64.decode(strkey);
		Log.debug("key: (%d %d) %s", k.length, k.length*8, Text.hex(k));
	}
}

/*
płyta głowna 199 *
https://allegro.pl/oferta/asus-p8h61-i-itx-h661-1155-7516860384?utm_source=google&utm_medium=cpc&utm_campaign=_ELKTRK_PLA_Komputery&ev_adgr=Pozosta%C5%82e&gclid=Cj0KCQjwrdjnBRDXARIsAEcE5YlbYxImxjDUFAiUTMfmOjA3IqcNKP1wB4VS_8U8zzz8_j7oOVlALWIaAmrwEALw_wcB
procek 200-250
https://archiwum.allegro.pl/oferta/procesor-intel-core-i3-4160t-lga1150-tdp-35w-i7264324017.html
pamięć
zasilacz
dysk

*/