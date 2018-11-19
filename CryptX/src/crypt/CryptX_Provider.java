package crypt;

import java.security.Provider;
import java.security.Security;

import sys.Log;

// https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/HowToImplAProvider.html
// http://codeandme.blogspot.com/2013/07/writing-your-own-jca-extensions-full.html
// https://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/security/HowToImplAProvider.html
// http://meri-stuff.blogspot.com/2012/04/secure-encryption-in-java.html
// http://netnix.org/2015/04/19/aes-encryption-with-hmac-integrity-in-java/

@SuppressWarnings("serial")
final public class CryptX_Provider extends Provider {

	//public static class Service;

	final String BLOCK_MODES = "ECB|CBC|PCBC|CTR|CTS|CFB|OFB" +
			"|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64" +
			"|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64";

	final String BLOCK_PADS = "ISO9797_1PADDING|ISO9797_2PADDING";


	protected CryptX_Provider() {
		super("CryptX", 1.0, "additional padding modes for symmetric algos");
		put("KeyGenerator.AES", "com.sun.crypto.provider.DESKeyGenerator");
		put("Cipher.AES", CryptX_AES.class.getName());
		//put("Cipher.AES/GCM/NoPadding", CryptX_AES.class.getName());
		//put("Cipher.DES", CryptX_AES.class.getName());
		put("Cipher.AES SupportedPaddings","NOPADDING|PKCS5PADDING|ISO10126PADDING");
		put("Cipher.AES SupportedModes","GCM");
	}

	private static boolean added = false;
	public static void register() {
		if (added) return ;
		int r=Security.addProvider(new CryptX_Provider());
		added = true;
		Log.info("%s register at index: %d", CryptX_Provider.class.getSimpleName(), r);
	}
}
