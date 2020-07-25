package org.test;

import java.util.Arrays;
import java.util.List;

public class BellTest {
    static class Result {

        public static final String IMPOSSIBLE = "IMPOSSIBLE";

        public static boolean isPalindrome(String str) {
            if (str == null || "".equals(str)) {
                return false;
            }
            String revertString = new StringBuilder(str).reverse().toString();
            return str.equals(revertString);
        }

        public static String breakPalindrome(String palindromeStr) {
            if (!isPalindrome(palindromeStr)) {
                return IMPOSSIBLE;
            }
            int length = palindromeStr.length();
            StringBuilder palindromSubStr = new StringBuilder("");
            for(int i = 0; i < length/2; i++) {
                if (palindromeStr.charAt(i) == palindromeStr.charAt(length - i - 1)) {
                    palindromSubStr.append("" + palindromeStr.charAt(i));
                } else {
                    break;
                }
            }
            String subStr = palindromSubStr.toString();
            for (int i = 0; i < subStr.length(); i++) {
                char curChar = subStr.charAt(i);
                if (curChar > 'a' && curChar <= 'z') {
                    char replaceChar = (char)((int)curChar - 1);
                    replaceChar = replaceChar > 'a' ? 'a' : replaceChar;
                    return palindromeStr.substring(0, i) + replaceChar + palindromeStr.substring(i + 1);
                }
            }
            return IMPOSSIBLE;
        }

    }

    public static void main(String[] args) {
        List<String> list = Arrays.asList("bab", "aa", "aaa", "acca", "aaabbaaa", "a", null);
        list.forEach(str -> System.out.println(str + " -> " + Result.breakPalindrome(str)));
    }
}
