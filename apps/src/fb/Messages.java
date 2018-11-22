package fb;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import sys.Log;
import text.Text;
import ui.MainPanel;

@SuppressWarnings("serial")
public class Messages extends MainPanel {

	static String msgPath = "C:\\Users\\U¿ytkownik\\Documents\\facebook-krzydyn\\messages\\inbox";

	static class Message {

	}

	final void openFile() {
		String file = msgPath + "/aleksandragorska_cadb2b2e31/message.json";
		try(Reader f = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)) {
		JsonStreamParser p = new JsonStreamParser(f);
		if (p.hasNext()) {
			JsonElement e = p.next();
			JsonArray msg = e.getAsJsonObject().get("messages").getAsJsonArray();
			for (JsonElement m : msg) {
				String cont = m.getAsJsonObject().get("content").getAsString();
				String str = new String(cont.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
				String uc = Text.utf2uc(cont);
				System.out.println(Text.vis(cont));
				System.out.println(Text.vis(str));
				System.out.println(Text.vis(uc));
			}
		}
		} catch (Exception e) {
			Log.error(e);
		}
	}

	public static void main(String[] args) {
		new Messages().openFile();
	}
}
