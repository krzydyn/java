package wgrep;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sys.Env;
import sys.Log;

public class ReParser {
	static final String reGalazka = "(\\d+) +(.+?) +([\\d,]+) +(szt\\.|rbh|oper) +(.*)";

	static void procFile(String f) throws IOException {
		Pattern p = Pattern.compile(reGalazka);
		BufferedReader rd = new BufferedReader(new FileReader(Env.expandEnv(f)));
		String ln;

		while ((ln=rd.readLine()) != null) {
			ln = ln.trim();
			if (ln.isEmpty()) continue;
			Matcher m = p.matcher(ln);
			if (!m.lookingAt()) {
				System.out.printf("*no-match* %s\n",ln);
				continue;
			}
			StringBuilder lo = new StringBuilder();
			for (int i=1; i <= m.groupCount(); ++i) {
				String gr = m.group(i);
				if (i > 1) gr = gr.replace(',', '.');
				if (i==m.groupCount()) lo.append(gr.replace(' ', '\t'));
				else {
					lo.append(gr);
					lo.append('\t');
				}
			}
			System.out.println(lo.toString());
		}
		rd.close();
	}

	public static void main(String[] args) {
		for (int i=0; i < args.length; ++i) {
			try {
				procFile(args[i]);
			}catch (Exception e) {
				Log.error(e);
			}
		}

	}

}
