package text;

public class FileUtils {
	static public final String expandEnv(String p) {
		if (p.startsWith("~/") || p.equals("~")) {
			p=System.getProperty("user.home")+p.substring(1);
		}
		int s=0,i,e;
		while ((s=p.indexOf('$',s))>=0) {
			i=s+1;
			char c=p.charAt(i);
			if (c=='(') {++i; e=p.indexOf(')',i);}
			else if (c=='{') {++i; e=p.indexOf('}',i);}
			else {c=' ';e=p.indexOf(' ',i);if (e<0) e=p.length(); }
			if (e<0) {
				p=p.substring(0,s);
				break;
			}
			String env=System.getenv(p.substring(i, e));
			if (c!=' ') ++e;
			if (env==null) p=p.substring(0,s)+p.substring(e);
			else p=p.substring(0,s)+env+p.substring(e);
		}
		return p;
	}
}
