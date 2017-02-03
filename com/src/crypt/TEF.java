package crypt;

import sys.Log;

public class TEF {
	static interface tef_cipher_token {}
	static enum tef_key_type_e {
	    TEF_DES,
	    TEF_AES,
	    TEF_RSA,
	    TEF_DSA,
	    TEF_ECDSA;
	    private String name;
	    String getName() {
	    	if (name==null) name=name().substring(4);
	    	return name;
	    }
	    @Override
		public String toString() {return getName();}
	}
	static enum tef_chaining_mode_e {
	    TEF_ECB,
	    TEF_CBC,
	    TEF_PCBC,
	    TEF_CFB,
	    TEF_OFB,
	    TEF_CTR,
	    TEF_GCM;
	    private String name;
	    String getName() {
	    	if (name==null) name=name().substring(4);
	    	return name;
	    }
	    @Override
		public String toString() {return getName();}
	}
	static enum tef_padding_mode_e {
		TEF_PADDING_NONE,
		TEF_PADDING_PKCS5;
	    private String name;
	    String getName() {
	    	if (name==null) name=name().substring(4);
	    	return name;
	    }
	    @Override
		public String toString() {return getName();}
	}
	static class tef_algorithm_info {
		final tef_chaining_mode_e chain;
		final tef_padding_mode_e pad;
		tef_algorithm_info(tef_chaining_mode_e chain, tef_padding_mode_e pad) {
			this.chain = chain; this.pad = pad;
		}
		String getName() {
			return chain + "/" + pad;
		}
	    @Override
		public String toString() {return getName();}
	}

	tef_cipher_token tef_key_generate(tef_key_type_e key_type, tef_algorithm_info algorithm, int key_bit_len) {
		String cipherName=key_type + "/" + algorithm;
		Log.debug("generate cipher %s", cipherName);
		return null;
	}
}
