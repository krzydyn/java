package test;

import text.TxUtils;

public class Anagrams {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(TxUtils.isAnagram("abc", "cba"));
		System.out.println(TxUtils.isAnagram("abbc", "cbaa"));
		System.out.println(TxUtils.isAnagram("abcd", "cbaa"));
		System.out.println(TxUtils.isAnagram("abbd", "cbaa"));
	}

}
