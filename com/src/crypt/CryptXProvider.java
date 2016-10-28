package crypt;

import java.security.Provider;
import java.security.Security;
import sys.Log;

@SuppressWarnings("serial")
final public class CryptXProvider extends Provider {

	//public static class Service;

	final String BLOCK_MODES = "ECB|CBC|PCBC|CTR|CTS|CFB|OFB" +
			"|CFB8|CFB16|CFB24|CFB32|CFB40|CFB48|CFB56|CFB64" +
			"|OFB8|OFB16|OFB24|OFB32|OFB40|OFB48|OFB56|OFB64";

	final String BLOCK_PADS = "ISO9797_1PADDING|ISO9797_2PADDING";


	protected CryptXProvider() {
		super("CryptX", 1.0, "implements additions padding modes for symmetric algos");
		put("Cipher.AES", CryptX_AES.class.getName());
		put("Alg.Alias.Cipher.Rijndael", "AES");
		put("Cipher.AES SupportedModes", BLOCK_MODES);
		put("Cipher.AES SupportedPaddings", BLOCK_PADS);
		put("Cipher.AES SupportedKeyFormats", "RAW");
	}

	public static void register() {
		Provider p = new CryptXProvider();
		Security.addProvider(p);
		Log.info("%s registered", p.getClass().getSimpleName());
	}
}
