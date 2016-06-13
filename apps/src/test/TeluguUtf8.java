package test;

public class TeluguUtf8 {
	static final String value = "ముక్కలు మొత్తం, ప్లాంట్, పి వో సంఖ్య";
	static final String v2 = "ఈ";
	static String toHex(byte[] b) {
		  String s="";
		  for (int i=0; i<b.length; ++i) s+=String.format("%02X",b[i]&0xff);
		  return s;
	}
	static String toHex(String b) {
		  String s="";
		  for (int i=0; i<b.length(); ++i) s+=String.format("%04X",b.charAt(i)&0xffff);
		  return s;
	}
	public static void main(String[] args) throws Exception {
		System.out.print(value);
		System.out.write(value.getBytes("UTF-8")); System.out.println();
		System.out.println(toHex(v2.getBytes("UTF-8")));
		System.out.println(toHex(v2));


	}

}
