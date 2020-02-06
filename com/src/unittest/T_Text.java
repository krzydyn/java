/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package unittest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import io.IOText;
import sys.Env;
import sys.Log;
import sys.UnitTest;
import text.Text;

public class T_Text extends UnitTest {

	static void anagrams() {
		check("anagram", Text.isAnagram("abc", "cba"));
		check("not anagram", !Text.isAnagram("abbc", "cbaa"));
		check("not anagram", !Text.isAnagram("abcd", "cbaa"));
		check("not anagram", !Text.isAnagram("abbd", "cbaa"));
	}
	static void diff() {
		Log.info(Text.join(",", Text.lcsub_simple("ala ma kota", "ala da kota")));
		Log.info(Text.join(",", Text.lcsub("ala ma kota", "ala da kota")));
		Log.info(Text.join(",", Text.lcsub_simple("ABCDEG","BCDGK")));
		Log.info(Text.join(",", Text.lcsub("ABCDEG","BCDGK")));
		Log.info(Text.diff("ala ma kota", "ala da kota"));
	}
	static void lev() {
		check("levenshtein",Text.levenshteinDistance("",""),0);
		check("levenshtein",Text.levenshteinDistance("","a"),1);
		check("levenshtein",Text.levenshteinDistance("frog","fog"),1);
	}
	static void emoticonsUTF() {
		Log.info("\uD83D\uDE0A \uD83D\uDE22 \uD83C\uDF82");
	}
	static void textIO() throws IOException {
		String data = "The fox";
		File f = new File("/tmp/test.txt");
		IOText.save(f, data);
		String data2 = IOText.load(f).toString();
		Log.info("load : '%s'", data2);
		try (FileInputStream fs = new FileInputStream(f)) {
			data2 = IOText.read(fs.getChannel(), 0, f.length()).toString();
			Log.info("load : '%s'", data2);
		}
		f.delete();
	}

	static void splitPath() {
		List<String> paths = Env.splitPaths("p1;p2;p3");
		Log.info("paths: %s", paths);
		paths = Env.splitPaths("p1;p2;p3;");
		Log.info("paths: %s", paths);
		paths = Env.splitPaths("p1;p2;p\\;3;");
		Log.info("paths: %s", paths);
		paths = Env.splitPaths("p1;p2;p3|:;");
		Log.info("paths: %s", paths);
		paths = Env.splitPaths("p1;p2;'p\\';3'");
		Log.info("paths: %s", paths);
	}
}
