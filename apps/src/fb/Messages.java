package fb;

import java.awt.BorderLayout;
import java.io.FileReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import sys.Env;
import sys.Log;
import text.Text;
import ui.MainPanel;

@SuppressWarnings("serial")
public class Messages extends MainPanel {
	static SimpleDateFormat dtfmt = new SimpleDateFormat("y-MM-d HH:mm:ss");

	static String msgPath = Env.expandEnv("~/Documents/facebook-krzydyn/messages/inbox");

	private final JTextComponent editor = new JTextPane();

	static class Message {
		long tm;
		String author;
		String content;
	}

	public Messages(String [] args) {
		editor.setEditable(false);
		editor.setFocusable(true); // this allow selection of text
		final JScrollPane sp = MainPanel.createScrolledPanel(editor);
		add(sp, BorderLayout.CENTER);
		openFile();
	}

	final void openFile() {
		String file = msgPath + "/aleksandragorska_cadb2b2e31/message.json";
		try(Reader f = new FileReader(file)) {
		JsonStreamParser p = new JsonStreamParser(f);
		if (p.hasNext()) {
			JsonElement e = p.next();
			JsonArray msgs = e.getAsJsonObject().get("messages").getAsJsonArray();
			StringBuilder txt = new StringBuilder();
			for (JsonElement m : msgs) {
				JsonObject jmsg = m.getAsJsonObject();
				Message msg = new Message();
				msg.tm = jmsg.get("timestamp_ms").getAsLong();
				msg.author = Text.utf2uc(jmsg.get("sender_name").getAsString());
				msg.content = Text.utf2uc(jmsg.get("content").getAsString());
				System.out.println(dtfmt.format(new Date(msg.tm))+" "+msg.author+":");
				System.out.println(msg.content);
				txt.append(String.format("%s %s:\n", dtfmt.format(new Date(msg.tm)), msg.author));
				txt.append(String.format("%s\n", msg.content));
			}
			editor.setText(txt.toString());
		}
		} catch (Exception e) {
			Log.error(e);
		}
	}

	public static void main(String[] args) {
		startGUI(Messages.class, args);
	}
}
