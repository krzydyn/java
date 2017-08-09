package test_SO;

public class Mart2 {
	public int s1(int N) {
		int r=0;
		int x=0, y=N;
		while (y >= 0) {
			while (x*x+y*y <= N*N) ++x;
			--x;
			if (y == 0) r += 2*x+1;
			else r += (2*x+1)*2;
			//System.out.printf("y[%d]: r=%d\n",y,r);
			--y;
		}
		return r;
	}
	public int solution(int N) {
		return s1(N);
	}

	public static void main(String[] args) {
		Mart2 m = new Mart2();
		//System.out.println(m.solution(2));
		for (int i=20000; i < 20000+10; ++i)
			System.out.println(m.solution(i));
	}
}
