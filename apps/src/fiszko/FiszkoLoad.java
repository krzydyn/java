package fiszko;

import java.io.FileReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class FiszkoLoad {

	public static void main(String[] args) throws Exception {
		//Gson g = new Gson();

		JsonParser p = new JsonParser();
		JsonElement el = p.parse(new FileReader("res/fiszko.json"));
		JsonArray items = el.getAsJsonObject().get("content")
				.getAsJsonObject().get("items").getAsJsonArray();

		for (int i = 0; i < items.size(); ++i) {
			System.out.println(items.get(i));
		}
	}

}
