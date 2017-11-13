package fiszko;

import java.io.FileReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FiszkoLoad {

	public static void main(String[] args) throws Exception {
		//Gson g = new Gson();

		JsonParser p = new JsonParser();
		JsonElement el = p.parse(new FileReader("res/fiszko.json"));
		JsonArray items = el.getAsJsonObject().get("content")
				.getAsJsonObject().get("items").getAsJsonArray();

		for (int i = 0; i < items.size(); ++i) {
			JsonObject item = items.get(i).getAsJsonObject();
			JsonObject q = item.get("question").getAsJsonObject();
			JsonArray a = item.get("answers").getAsJsonArray();
			System.out.println(a);

			String text = q.get("text").toString();
			String examples = q.get("examples") == null ? null : q.get("examples").toString();
			String hint = q.get("hint") == null ? null : q.get("hint").toString();
			System.out.println(text + ":" + examples);
		}
	}

}
