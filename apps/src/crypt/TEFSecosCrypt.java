package crypt;

import crypt.TEF.tef_chaining_mode_e;
import crypt.TEF.tef_key_type_e;
import crypt.TEF.tef_padding_mode_e;

public class TEFSecosCrypt {

	public static void main(String[] args) {
		TEF t = new TEF();

		//  DES/ECB/NoPadding
		t.tef_key_generate(
				tef_key_type_e.TEF_DES,
				new TEF.tef_algorithm_info(tef_chaining_mode_e.TEF_ECB, tef_padding_mode_e.TEF_PADDING_NONE),
				128);
	}
}
