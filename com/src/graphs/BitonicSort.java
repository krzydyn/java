package graphs;

public class BitonicSort {
	private static void kernel(int[] a, final int p, final int q) {
		final int d = 1 << (p-q);
		for(int i=0;i<a.length;i++) {
			boolean up = ((i >> p) & 2) == 0;
			if ((i & d) == 0 && (a[i] > a[i | d]) == up) {
				int t = a[i]; a[i] = a[i|d]; a[i|d] = t;
			}
		}
	}

	public static void sort(final int logn, int[] a) {
		assert a.length == 1 << logn;
		for(int i=0;i<logn;i++) {
			for(int j=0;j<=i;j++) {
				kernel(a, i, j);
			}
		}
	}
}

