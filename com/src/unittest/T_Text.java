package unittest;

import sys.UnitTest;
import text.Text;

public class T_Text extends UnitTest {
	static void anagrams() {
		check(Text.isAnagram("abc", "cba"), "anagram");
		check(!Text.isAnagram("abbc", "cbaa"), "not anagram");
		check(!Text.isAnagram("abcd", "cbaa"), "not anagram");
		check(!Text.isAnagram("abbd", "cbaa"), "not anagram");
	}
}
