package test_SO;

public class Mart1 {

	public int solution(int N) {
		// F(k) = k*(k+1)/2
		//      = (k^2 + k)/2
		// F(k) <= N
		// k^2+k <= 2*N;
		// find max k
		int k = (int)Math.sqrt(N);
		while (k*k+k < 2*N) ++k;
		while (k*k+k > 2*N) --k;
		return k;
	}

	public static void main(String[] args) {
		System.out.println(new Mart1().solution(17));
	}
}