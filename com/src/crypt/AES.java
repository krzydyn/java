package crypt;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import sys.Log;

/**
 * AES algorithm (Rijndael)
 * http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf
 * http://csrc.nist.gov/publications/nistpubs/800-38a/sp800-38a.pdf
 * http://csrc.nist.gov/archive/aes/rijndael/Rijndael-ammended.pdf#page=19
 * https://cr.yp.to/aes-speed/aesspeed-20080926.pdf
 * Key lengths (in bits): 128, 192, 256
 * @author k.dynowski
 *
 */
public class AES {
	static final int BLOCKSIZE = 16;
	static final byte[] sBox = new byte[256];   // S-box
	static final byte[] invSbox = new byte[256];  // iS-box
	static final int[] rCon = new int[10];		// Round constants
	static { generateSbox(); generateRcon(); }

	static final private void generateSbox() {
	    byte[] t = invSbox;
	    int x, i;
	    for (i = 0, x = 1; i < 256; i ++) {
	        t[i] = (byte)(x & 0xFF);
	        x ^= (x << 1) ^ ((x >> 7) * 0x11B);
	    }

	    sBox[0] = 0x63;
	    for (i = 0; i < 255; i ++) {
	        x = t[255 - i];
	        x |= x << 8;
	        x ^= (x >> 4) ^ (x >> 5) ^ (x >> 6) ^ (x >> 7);
	        sBox[t[i]] = (byte)((x ^ 0x63) & 0xFF);
	    }

	    for (i = 0; i < 256;i++) {
	         invSbox[sBox[i]]=(byte)i;
	    }
	}
	static final int ROOT = 0x11b;
	static final private void generateRcon() {

		int r = 1;
        rCon[0] = r << 24;
        for (int i = 1; i < rCon.length; i++) {
            r <<= 1;
            if (r >= 0x100) {r ^= ROOT;}
            rCon[i] = r << 24;
        }
	}

	private final byte[] key;
	private final byte[] iv = new byte[BLOCKSIZE];
	private final byte[] state = new byte[BLOCKSIZE];
	private int[] rek;

	AES(byte[] k,byte[] i) {
		key=k;
		System.arraycopy(i, 0, iv, 0, iv.length);
	}

	private int mult(int a, int b) {
        int sum = 0;

        while (a != 0) {
            if ((a & 1) != 0) sum = sum ^ b;

            // bit shift left mod 0x11b si es necesario;
            if ((b & 0x80) == 0) b = b << 1;
            else b = (b << 1) ^ ROOT;

            a = a >>> 1;
        }
        return sum;

	}

	private void expandKey(byte[] cipherKey) {
		int Nk = (cipherKey.length*8) >>> 5;
        int Nr = Nk + 6;
        int Nw = 4*(Nr + 1);
        rek = new int[Nw];

        int temp, r = 0;
        for (int i = 0, k = 0; i < Nk; i++, k += 4) {
			rek[i] =
			    ((cipherKey[k    ]       ) << 24) |
			    ((cipherKey[k + 1] & 0xff) << 16) |
			    ((cipherKey[k + 2] & 0xff) <<  8) |
			    ((cipherKey[k + 3] & 0xff));
        }
        for (int i = Nk, n = 0; i < Nw; i++, n--) {
			temp = rek[i - 1];
			if (n == 0) {
				n = Nk;
			    temp =
			        ((sBox[(temp >>> 16) & 0xff]       ) << 24) |
			        ((sBox[(temp >>>  8) & 0xff] & 0xff) << 16) |
			        ((sBox[(temp       ) & 0xff] & 0xff) <<  8) |
			        ((sBox[(temp >>> 24)       ] & 0xff));
			    temp ^= rCon[r++];
			} else if (Nk == 8 && n == 4) {
                temp =
                    ((sBox[(temp >>> 24)       ]       ) << 24) |
                    ((sBox[(temp >>> 16) & 0xff] & 0xff) << 16) |
                    ((sBox[(temp >>>  8) & 0xff] & 0xff) <<  8) |
                    ((sBox[(temp       ) & 0xff] & 0xff));
            }
            rek[i] = rek[i - Nk] ^ temp;
        }
        temp = 0;
    }

	private void init() {
		System.arraycopy(iv, 0, state, 0, iv.length);
	}

	private void update(byte[] src, byte[] dst, int offs) {

	}
	private void finish(byte[] dst, int offs) {

	}

	public void cbc_encrypt(byte[] msg, byte[] out, int length)
	{
	    int p = 0;
	    init();
	    for (p = 0; p < length; p += BLOCKSIZE) {
	        update(msg, out, p);
	    }
	    finish(out, p);
	}

	public void javax_encrypt(byte[] msg, byte[] out, int length) {
		Key aesKey = new SecretKeySpec(key, "AES");
		try {
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, aesKey);
			cipher.doFinal(msg, 0, length, out);
		}catch(Exception e) {
			Log.error(e);
		}
	}
	public void javax_decrypt(byte[] msg, byte[] out, int length) {
		Key aesKey = new SecretKeySpec(key, "AES");
		try {
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, aesKey);
			cipher.doFinal(msg, 0, length, out);
		}catch(Exception e) {
			Log.error(e);
		}
	}

	static void shift_rows(byte[] state)
	{
	    byte tmp;
	    /* just substitute row 0 */
	    state[0] = sBox[state[0]];
	    state[4] = sBox[state[4]];
	    state[8] = sBox[state[8]];
	    state[12] = sBox[state[12]];

	    /* rotate row 1 */
	    tmp = sBox[state[1]];
	    state[1] = sBox[state[5]];
	    state[5] = sBox[state[9]];
	    state[9] = sBox[state[13]];
	    state[13] = tmp;

	    /* rotate row 2 */
	    tmp = sBox[state[2]];
	    state[2] = sBox[state[10]];
	    state[10] = tmp;
	    tmp = sBox[state[6]];
	    state[6] = sBox[state[14]];
	    state[14] = tmp;

	    /* rotate row 3 */
	    tmp = sBox[state[15]];
	    state[15] = sBox[state[11]];
	    state[11] = sBox[state[7]];
	    state[7] = sBox[state[3]];
	    state[3] = tmp;
	}
	static void inv_shift_rows(byte[] state)
	{
	    byte tmp;
	    /* restore row 0 */
	    state[0] = invSbox[state[0]];
	    state[4] = invSbox[state[4]];
	    state[8] = invSbox[state[8]];
	    state[12] = invSbox[state[12]];

	    /* restore row 1 */
	    tmp = invSbox[state[13]];
	    state[13] = invSbox[state[9]];
	    state[9] = invSbox[state[5]];
	    state[5] = invSbox[state[1]];
	    state[1] = tmp;

	    /* restore row 2 */
	    tmp = invSbox[state[2]];
	    state[2] = invSbox[state[10]];
	    state[10] = tmp;
	    tmp = invSbox[state[6]];
	    state[6] = invSbox[state[14]];
	    state[14] = tmp;

	    /* restore row 3 */
	    tmp = invSbox[state[3]];
	    state[3] = invSbox[state[7]];
	    state[7] = invSbox[state[11]];
	    state[11] = invSbox[state[15]];
	    state[15] = tmp;
	}

	static void mix_sub_columns(byte[] state)
	{
	    byte[] tmp = new byte[BLOCKSIZE];
	    byte[] x2_sbox=sBox;//fake
	    byte[] x3_sbox=sBox;//fake

	    /* mixing column 0 */
	    tmp[0] = (byte)(x2_sbox[state[0]] ^ x3_sbox[state[5]] ^ sBox[state[10]] ^ sBox[state[15]]);
	    tmp[1] = (byte)(sBox[state[0]] ^ x2_sbox[state[5]] ^ x3_sbox[state[10]] ^ sBox[state[15]]);
	    tmp[2] = (byte)(sBox[state[0]] ^ sBox[state[5]] ^ x2_sbox[state[10]] ^ x3_sbox[state[15]]);
	    tmp[3] = (byte)(x3_sbox[state[0]] ^ sBox[state[5]] ^ sBox[state[10]] ^ x2_sbox[state[15]]);

	    /* mixing column 1 */
	    tmp[4] = (byte)(x2_sbox[state[4]] ^ x3_sbox[state[9]] ^ sBox[state[14]] ^ sBox[state[3]]);
	    tmp[5] = (byte)(sBox[state[4]] ^ x2_sbox[state[9]] ^ x3_sbox[state[14]] ^ sBox[state[3]]);
	    tmp[6] = (byte)(sBox[state[4]] ^ sBox[state[9]] ^ x2_sbox[state[14]] ^ x3_sbox[state[3]]);
	    tmp[7] = (byte)(x3_sbox[state[4]] ^ sBox[state[9]] ^ sBox[state[14]] ^ x2_sbox[state[3]]);

	    /* mixing column 2 */
	    tmp[8] = (byte)(x2_sbox[state[8]] ^ x3_sbox[state[13]] ^ sBox[state[2]] ^ sBox[state[7]]);
	    tmp[9] = (byte)(sBox[state[8]] ^ x2_sbox[state[13]] ^ x3_sbox[state[2]] ^ sBox[state[7]]);
	    tmp[10] = (byte)(sBox[state[8]] ^ sBox[state[13]] ^ x2_sbox[state[2]] ^ x3_sbox[state[7]]);
	    tmp[11] = (byte)(x3_sbox[state[8]] ^ sBox[state[13]] ^ sBox[state[2]] ^ x2_sbox[state[7]]);

	    /* mixing column 3 */
	    tmp[12] = (byte)(x2_sbox[state[12]] ^ x3_sbox[state[1]] ^ sBox[state[6]] ^ sBox[state[11]]);
	    tmp[13] = (byte)(sBox[state[12]] ^ x2_sbox[state[1]] ^ x3_sbox[state[6]] ^ sBox[state[11]]);
	    tmp[14] = (byte)(sBox[state[12]] ^ sBox[state[1]] ^ x2_sbox[state[6]] ^ x3_sbox[state[11]]);
	    tmp[15] = (byte)(x3_sbox[state[12]] ^ sBox[state[1]] ^ sBox[state[6]] ^ x2_sbox[state[11]]);

	    System.arraycopy(tmp, 0, state, 0, state.length);
	}


}
/*
AES_CTX ctx;
NIST SP800-38A: CBC Example Vector F2.1-2 & F2.5-6
http://csrc.nist.gov/publications/nistpubs/800-38a/sp800-38a.pdf
uint8_t key128[] =  {0x2b,0x7e,0x15,0x16,0x28,0xae,0xd2,0xa6,
                             0xab,0xf7,0x15,0x88,0x09,0xcf,0x4f,0x3c};
uint8_t key256[] =  {0x60,0x3d,0xeb,0x10,0x15,0xca,0x71,0xbe,
                             0x2b,0x73,0xae,0xf0,0x85,0x7d,0x77,0x81,
                             0x1f,0x35,0x2c,0x07,0x3b,0x61,0x08,0xd7,
                             0x2d,0x98,0x10,0xa3,0x09,0x14,0xdf,0xf4};
uint8_t iv[] =          {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,
                             0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f};
uint8_t nistData[] = {0x6b,0xc1,0xbe,0xe2,0x2e,0x40,0x9f,0x96,
                             0xe9,0x3d,0x7e,0x11,0x73,0x93,0x17,0x2a,
                             0xae,0x2d,0x8a,0x57,0x1e,0x03,0xac,0x9c,
                             0x9e,0xb7,0x6f,0xac,0x45,0xaf,0x8e,0x51,
                             0x30,0xc8,0x1c,0x46,0xa3,0x5c,0xe4,0x11,
                             0xe5,0xfb,0xc1,0x19,0x1a,0x0a,0x52,0xef,
                             0xf6,0x9f,0x24,0x45,0xdf,0x4f,0x9b,0x17,
                             0xad,0x2b,0x41,0x7b,0xe6,0x6c,0x37,0x10};
*/