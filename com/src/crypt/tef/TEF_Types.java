package crypt.tef;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import text.Text;

public interface TEF_Types {
	static class tef_cipher_token {
		final private tef_key_type_e type;
		final private SecretKey key;

		public tef_cipher_token(tef_key_type_e type, SecretKey key) {
			this.type = type;
			this.key = key;
		}

		@Override
		public String toString() {
			return transformName();
		}
		public SecretKey getKey() {
			return key;
		}
		public tef_key_type_e getType() {
			return type;
		}
		public String transformName() {
			return key.getAlgorithm();
		}
	}

	static enum tef_key_type_e {
		TEF_DES,
		TEF_AES,
		TEF_RSA,
		TEF_DSA,
		TEF_ECDSA;
		private String name;
		public String getName() {
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
		TEF_CFB1,
		TEF_CFB8,
		TEF_OFB,
		TEF_CTR,
		TEF_GCM, //above Java7
		TEF_CCM, //above Java8
		;
		private String name;
		public String getName() {
			if (name==null) name=name().substring(4);
			return name;
		}
		@Override
		public String toString() {return getName();}
	}

	static enum tef_padding_mode_e {
		TEF_PADDING_NONE("NoPadding"),
		TEF_PADDING_X931("X931Padding"),
		TEF_PADDING_PKCS5("PKCS5Padding"),
		TEF_PADDING_ISO9797_M1("ISO9797_M1"),
		TEF_PADDING_ISO9797_M2("ISO9797_M2"),
		;
		public static tef_padding_mode_e TEF_PADDING_PKCS7 = TEF_PADDING_PKCS5;

		private String name;
		tef_padding_mode_e(String n) {name=n;}
		String getName() {return name;}
		@Override
		public String toString() {return getName();}
	}

	static class tef_algorithm {
		final tef_chaining_mode_e chaining;
		final tef_padding_mode_e padding;
		final Map<tef_algorithm_param_e, Object> params = new HashMap<>();
		public tef_algorithm(tef_chaining_mode_e chain, tef_padding_mode_e pad) {
			this.chaining = chain; this.padding = pad;
		}
		String getName() {
			return chaining + "/" + padding;
		}
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(getName());
			for (tef_algorithm_param_e p : tef_algorithm_param_e.values()) {
				Object v = params.get(p);
				if (v==null) continue;
				b.append(" "+p.getName());
				if (v instanceof byte[]) {
					byte[] bv = ((byte[])v);
					b.append(String.format("[%d]=%s", bv.length, Text.hex(bv)));
				}
				else b.append("="+v.toString());
				b.append(";");
			}
			return b.toString();
		}

		public tef_algorithm set(tef_algorithm_param_e p, Object v) {
			params.put(p, v);
			return this;
		}
		public Object get(tef_algorithm_param_e p) {
			return params.get(p);
		}
	}

	static enum tef_algorithm_param_e {
		TEF_IV, //=TEF_NONCE,
		TEF_AAD,
		TEF_TAGLEN,
		TEF_TAG,
		;
		private String name;
		String getName() {
			if (name==null) name=name().substring(4);
			return name;
		}
		@Override
		public String toString() {return getName();}
	}

	static enum tef_digest_e {
		TEF_CRC16,
		TEF_CRC32,
		TEF_MD2,  //weak
		TEF_MD4,  //weak
		TEF_MD5,  //weak since 2008
		TEF_SHA0, //weak
		TEF_SHA1, //Theoretically weak

		//SHA-2 (strong)
		TEF_SHA224,
		TEF_SHA256,
		TEF_SHA384,
		TEF_SHA512,

		//SHA-3 (very strong)
		TEF_SHA3_224,
		TEF_SHA3_256,
		TEF_SHA3_384,
		TEF_SHA3_512,
		;
		private String name;
		String getName() {
			if (name==null) {
				if (this == TEF_SHA224) name = "SHA-224";
				else if (this == TEF_SHA256) name = "SHA-256";
				else if (this == TEF_SHA384) name = "SHA-384";
				else if (this == TEF_SHA512) name = "SHA-512";
				else name = name().substring(4);
			}
			return name;
		}
		@Override
		public String toString() {return getName();}
	}
}
