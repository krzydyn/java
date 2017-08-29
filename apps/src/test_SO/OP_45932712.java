package test_SO;

import time.LapTime;
import algebra.Combination;

public class OP_45932712 {
	static class RepeatComb {
		private int[] pos;
		private String set;
		public RepeatComb(String set, int k) {
			this.set = set;
			pos = new int[k];
		}
		public boolean next() {
			int i = pos.length-1;
			for (int maxpos = set.length()-1; pos[i] >= maxpos; ) {
				if (i==0) return false;
				--i;
			}
			++pos[i];
			while (++i < pos.length) pos[i]=0;
			return true;
		}
		public String getCur() {
			StringBuilder s = new StringBuilder(pos.length);
			for (int i=0; i < pos.length; ++i)
				s.append(set.charAt(pos[i]));
			return s.toString();
		}

	}

	public static void main(String[] args) {
		String text = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		RepeatComb comb = new RepeatComb(text, 7);
		System.out.println(Combination.newton(text.length(), 7));
		LapTime time = new LapTime("ms");
		int n=0;
		long tm = System.currentTimeMillis() + 2000;
		do {
			//System.out.println(comb.getCur());
			++n;
			if (tm < System.currentTimeMillis()) {
				time.updateAbs(n);
				System.out.printf("tm: %.3f  %s\n",time.getSpeed(),comb.getCur());
				tm += 2000;
				time.nextLap();
			}
		} while (comb.next());
		System.out.println("num: "+n);
	}
}
