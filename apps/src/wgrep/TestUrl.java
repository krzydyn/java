package wgrep;

import java.util.HashMap;
import java.util.Map;

import sys.Env;

public class TestUrl {

	public static void main(String[] args) {
		try {
			Map<String,String> props = new HashMap<>();
			props.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
			CharSequence s = Env.getRemoteContent("https://stackoverflow.com/questions/56476944/cant-get-inputsream-from-https-url-connection-timeout-connectexception", null);
			System.out.println(s);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
