package puzzles;

public class BlackJack {
	static public int getPoints(CharSequence hand) {
		int sum = 0;
		int aces = 0;
		for (int i=0; i < hand.length(); ++i) {
			char c=hand.charAt(i);
			if (c >= '2' && c <= '9') sum+=c-'0';
			else if (c=='A') ++aces;
			else sum+=10; //other fig
		}
		if (aces>0) {
			sum+=aces;
			while (aces>0 && sum+10<=21) {
				 sum+=10; --aces;
			}
		}
		return sum;
	}
}
