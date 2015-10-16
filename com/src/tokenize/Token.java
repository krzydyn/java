package tokenize;

public class Token {
	public final int cla;
	public final int ln;
	public final String rep;
	public Token(int c,int l, String s) {
		cla=c; ln=l; rep=s;
	}
}
