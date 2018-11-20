package fb;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
		String encoding = "UTF8";

		String file = msgPath + "/aleksandragorska_cadb2b2e31/message.json";
		try(Reader f = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
		JsonStreamParser p = new JsonStreamParser(f);
		System.out.println(Text.vis("Ustawi³eœ"));
		if (p.hasNext()) {
			JsonElement e = p.next();
			JsonArray msg = e.getAsJsonObject().get("messages").getAsJsonArray();
			for (JsonElement m : msg) {
				String cont = m.getAsJsonObject().get("content").getAsString();
				//cont = Text.utf2uc(cont);
				System.out.println(cont);
				System.out.println(Text.vis(cont));
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
