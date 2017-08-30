package test_SO;

import java.util.Arrays;

public class OP_45932712 {
	static class RepeatComb {
		private int[] pos;
		private final String set;
		public RepeatComb(String set, int k) {
			this.set = set;
			pos = new int[k];
		}
        public int[] getState() {return Arrays.copyOf(pos, pos.length);}
        public void resume(int[] a) {pos = Arrays.copyOf(a,a.length);}
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
        int[] state;
		String text = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		RepeatComb comb = new RepeatComb(text, 2);
        int stop = 10,n=0;
		do {
            if (stop-- == 0) break;
			System.out.println(comb.getCur());
            ++n;
		} while (comb.next());
        //save state
        state = comb.getState();
        System.out.println("-------");
        stop = 10;
        //resume (with the same args of course)
        comb = new RepeatComb(text, 2);
        comb.resume(state);
		do {
            if (stop-- == 0) break;
			System.out.println(comb.getCur());
			++n;
		} while (comb.next());
		System.out.println("num: "+n);
	}
}
