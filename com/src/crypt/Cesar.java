package crypt;

public class Cesar {
    static String encrypt(String s, int k) {
        StringBuilder b = new StringBuilder();
        for (int i=0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') c = (char)((c-'A' + k)%26 + 'A');
            else if (c >= 'a' && c <= 'z') c = (char)((c-'a' + k)%26 + 'a');
            b.append(c);
        }
        return b.toString();
   }
}
