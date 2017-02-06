package crypt;

import java.security.Provider;
import java.security.Security;
import sys.Log;

// http://codeandme.blogspot.com/2013/07/writing-your-own-jca-extensions-full.html
// https://www.cis.upenn.edu/~bcpierce/courses/629/jdkdocs/guide/security/HowToImplAProvider.html

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

	public static void register() {
		Provider p = new CryptX_Provider();
		Security.addProvider(p);
		Log.info("%s registered", p.getClass().getSimpleName());
	}
}
