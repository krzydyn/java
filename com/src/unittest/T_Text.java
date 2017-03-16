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
		Log.info(Text.diff("ala ma kota", "ala da kota"));
		Log.info(Text.join(",", Text.lcsub_simple("ala ma kota", "ala kota")));
		Log.info(Text.join(",", Text.lcsub("ala ma kota", "ala kota")));
		Log.info(Text.join(",", Text.lcsub_simple("ABCDEG","BCDGK")));
		Log.info(Text.join(",", Text.lcsub("ABCDEG","BCDGK")));
	}
	static void lev() {
		check("levenst",Text.levenshteinDistance("",""),0);
		check("levenst",Text.levenshteinDistance("","a"),1);
		check("levenst",Text.levenshteinDistance("frog","fog"),1);
	}
}
