package crypt;

public interface CipherBlock {
	final public int ENCRYPT=0;
	final public int DECRYPT=1;
	int updateBlock(int enc,byte[] data, int offs, byte[] out, int outoffs);
}
