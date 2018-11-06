package crypt;

public class Cesar {
	static private String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final int delta;
	public Cesar(int d) {
		if (d < 0) d = -d;
		d %= alphabet.length();
		delta = d;
	}
	public String encrypt(String s) {
		StringBuilder b = new StringBuilder();
		for (int i=0; i < s.length(); ++i) {
			char c = Character.toUpperCase(s.charAt(i));
			int p = alphabet.indexOf(c);
			if (p < 0) c = ' ';
			else c = alphabet.charAt((p + delta)%alphabet.length());
			b.append(c);
		}
		return b.toString();
	}

	public String decrypt(String s) {
		StringBuilder b = new StringBuilder();
		for (int i=0; i < s.length(); ++i) {
			char c = Character.toUpperCase(s.charAt(i));
			int p = alphabet.indexOf(c);
			if (p < 0) c = ' ';
			else c = alphabet.charAt((p + alphabet.length() - delta)%alphabet.length());
			b.append(c);
		}
		return b.toString();
	}
}
