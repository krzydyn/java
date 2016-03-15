package text.tokenize;

public class Token {
	public final int cla;
	public final int ln;
	public final String rep;
	public Token(int c,int l, String s) {
		cla=c; ln=l; rep=s;
	}
	
	@SuppressWarnings("serial")
	static public class TokenException extends RuntimeException {
		TokenException(Token tok) {
			super(String.format("ln(%d): wrong token (%d,'%s')",tok.ln,tok.cla,tok.rep));
		}
		TokenException(Token tok, String msg) {
			super(String.format("ln(%d): wrong token (%d,'%s'), %s",tok.ln,tok.cla,tok.rep,msg));
		}
	}
}
