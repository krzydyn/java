package unittest;

import sys.UnitTest;
import text.Text;

public class T_Text extends UnitTest {
	static void anagrams() {
		System.out.println(Text.isAnagram("abc", "cba"));
		System.out.println(Text.isAnagram("abbc", "cbaa"));
		System.out.println(Text.isAnagram("abcd", "cbaa"));
		System.out.println(Text.isAnagram("abbd", "cbaa"));
	}
}
