package test_SO;

public class Factoring {
	static double fac(double x, int pow) {
		double s=1.0;
		for (int i=0; i<pow; ++i) s*=x;
		return s;
	}
	public static void main(String[] args) {
		System.out.printf("1.01^365 = %f\n", fac(1.01,366));
		System.out.printf("0.99^365 = %f\n", fac(0.99,366));
	}
}
