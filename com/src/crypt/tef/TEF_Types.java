package crypt.tef;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import text.Text;

public interface TEF_Types {
	static class tef_cipher_token {
		SecretKey key;

		@Override
		public String toString() {
			return transformName();
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
		TEF_CFB1,
		TEF_CFB8,
		TEF_OFB,
		TEF_CTR,
		TEF_GCM, //(not supported in Java7)
		;
		private String name;
		String getName() {
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
		TEF_PADDING_PKCS7("PKCS5Padding"),
		//TEF_PADDING_ISO9797_1("ISO97971Padding"),
		//TEF_PADDING_ISO9797_2("ISO97972Padding"),
		;
		private String name;
		tef_padding_mode_e(String n) {name=n;}
		String getName() {return name;}
		@Override
		public String toString() {return getName();}
	}

	static class tef_algorithm {
		final tef_chaining_mode_e chaining;
		final tef_padding_mode_e padding;
		final Map<tef_algorithm_param_e, Object> map = new HashMap<tef_algorithm_param_e, Object>();
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
				Object v = map.get(p);
				if (v==null) continue;
				b.append(" "+p.getName()+"=");
				if (v instanceof byte[]) b.append(Text.hex((byte[])v));
				else b.append(v.toString());
				b.append(";");
			}
			return b.toString();
		}

		public tef_algorithm set(tef_algorithm_param_e p, Object v) {
			map.put(p, v);
			return this;
		}
		public Object get(tef_algorithm_param_e p) {
			return map.get(p);
		}
	}

	static enum tef_algorithm_param_e {
		TEF_IV,
		TEF_AAD,
		TEF_AUTHTAG_LEN,
		TEF_AUTHTAG
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
		TEF_MD2,  //weak
		TEF_MD4,  //weak
		TEF_MD5,  //weak since 2008
		TEF_SHA0, //weak
		TEF_SHA1, //Theoretically weak
		TEF_SHA2, //strong
		TEF_SHA3, //very strong
	}

}
