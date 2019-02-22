package crypt;

import java.security.MessageDigest;

// https://www.programcreek.com/java-api-examples/index.php?source_dir=javacardsign-master/pkihostapi/src/net/sourceforge/javacardsign/service/CryptoUtils.java
public class Padding {

	public byte[] enpadZ(byte[] msg, int size) {
		if (msg.length < size) {
			byte[] m = new byte[size];
			System.arraycopy(msg, 0, m, size - msg.length, msg.length);
			return m;
		}
		return msg;
	}
	public byte[] enpadPKCS15(byte[] msg, int size) {
		return null;
	}

	public byte[] enpadPSS(byte[] msg, int size) {
		return null;
	}

	static byte[] pssPad(byte[] salt, byte[] text, boolean doHash, int emLen) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");

            byte[] h1 = null;
            if (doHash) {
                h1 = md.digest(text);
            } else {
                h1 = text; // text is already the hash to be signed
            }
            int hLen = h1.length;
            int sLen = 20;
            int psLen = emLen - sLen - hLen - 2;

            byte[] output = new byte[emLen];

            md.update(output, 0, (short) 8);
            md.update(h1, 0, hLen);
            byte[] tmpHash = md.digest(salt);
            output[psLen] = (byte) 0x01;

            int hOffset = emLen - hLen - 1;
            System.arraycopy(tmpHash, 0, output, hOffset, hLen);
            System.arraycopy(salt, 0, output, psLen + 1, salt.length);
            output[emLen - 1] = (byte) 0xbc;

            int counter = 0;
            int outOffset = 0;
            byte[] c = new byte[4];
            while (outOffset < hOffset) {
                c[c.length - 1] = (byte) counter;
                md.update(output, hOffset, hLen);
                tmpHash = md.digest(c);
                if (outOffset + hLen > hOffset) {
                    hLen = hOffset - outOffset;
                }
                for (int i = 0; i < hLen; i++) {
                    output[outOffset++] ^= tmpHash[i];
                }
                counter++;
            }
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}
}
