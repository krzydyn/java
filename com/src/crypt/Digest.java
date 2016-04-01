package crypt;

public interface Digest {
	public void init();
	public void update();
	public byte[] finish();
}
