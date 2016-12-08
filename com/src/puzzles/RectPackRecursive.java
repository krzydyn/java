package puzzles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import puzzles.GameBoard.Sheet;
import sys.Log;
import text.Text;

public class RectPackRecursive {
	int cachew;
	int[] cache;
	List<Sheet>[] cacheBox;
	private final Sheet box = new Sheet(0,0);
	private final Sheet rbox = new Sheet(0,0);
	private final List<Integer> grid=new ArrayList<>();

	public RectPackRecursive(Sheet r) {
		box.set(r.w,r.h);
		rbox.set(r.h,r.w);
	}
	public int solve(Sheet page) {
		cachew=page.w;
		cache = new int[cachew*(page.h+1)+1];
		//cacheBox = new ArrayList[page.w*page.h+1];
		for (int i=0; i < cache.length; ++i) cache[i]=-1;
		cache[0]=0;
		grid.clear();
		int s = Math.max(page.w, page.h);
		int m = Math.min(box.w, box.h);
		s = s/m;
		for (int i=0; i < s; ++i) {
			for (int j=0; j < s; ++j) {
				int x = i*box.w+j*box.h;
				if (x==0) continue;
				if (!grid.contains(x)) grid.add(x);
			}
		}
		Collections.sort(grid);

		Log.prn("grid: %s", Text.join(",", grid));
		Log.prn("max n %d", (page.w*page.h)/(box.w*box.h));
		int n=psolve(page);
		Log.prn("page(%d,%d) n=%d r=%d",page.w,page.h, n, page.w*page.h-n*box.w*box.h);
		return n;
	}

	public int psolve(Sheet page) {
		if (cache.length <= cachew*page.h+page.w) {
			Log.error("index = %d > %d", cachew*page.h+page.w, cache.length);
		}
		if (cache[cachew*page.h+page.w] >= 0) return cache[cachew*page.h+page.w];

		Sheet p1,p2,p3;
		int sum=-1;
		if (page.contains(box)) {
			if (page.h == box.h) sum=page.w/box.w;
			else if (page.w == box.w) sum=page.h/box.h;
		}
		else if (page.contains(rbox)) {
			if (page.h == rbox.h) sum=page.w/rbox.w;
			if (page.w == rbox.w) sum=page.h/rbox.h;
		}
		else {
			sum=0;
		}
		if (sum >= 0) {
			cache[cachew*page.h+page.w]=sum;
			return sum;
		}

		sum=0;
		for (int y : grid) {
			if (y >= page.h) break;
			for (int x : grid) {
				if (x >= page.w) break;
				int s1=0,s=0;
				p1 = new Sheet(x,y);
				s1 = psolve(p1);

				p2 = new Sheet(page.w,page.h-y);
				p3 = new Sheet(page.w-x,y);
				s = psolve(p2);
				s += psolve(p3);
				if (sum < s1+s) sum=s1+s;

				p2 = new Sheet(page.w-x,page.h);
				p3 = new Sheet(x,page.h-y);
				s = psolve(p2);
				s += psolve(p3);
				if (sum < s1+s) sum=s1+s;
			}
		}
		cache[cachew*page.h+page.w]=sum;
		return sum;
	}


	int nsolve_prv(Sheet page) {
		int n,m;
		m=n=0;
		if (!page.contains(box) && !page.contains(rbox)) {
			return 0;
		}

		if (page.h >= box.w && page.h >= box.h) {
			int rmin=page.w, bn=0,bm=0;
			for (int w=0; w+box.w < page.w; w+=box.w,++n) {
				int r = page.w-n*box.w;
				m = r/box.h;
				r = page.w-(n*box.w+m*box.h);
				if (rmin > r) {
					rmin=r;
					bn=n;
					bm=m;
				}
			}
			n=bn; m=bm;
		}
		else if (page.h >= box.w) {
			m=page.w/box.h;
		}
		else {
			n=page.w/box.w;
		}

		//Log.prn("page %dx%d n(%dx%d)+m(%dx%d) + %d",page.w,page.h,n,box.w,m,box.h,page.w-n*box.w-m*box.h);
		int s=n+m;
		if (n>0 && page.h > box.h) s += nsolve(new Sheet(n*box.w, page.h-box.h));
		if (page.h > box.w) s += nsolve(new Sheet(page.w - n*box.w, page.h-box.w));
		return s;
	}
	int nsolve(Sheet page) {
		//Log.prn("case1 %d x %d",page.w,page.h);
		int n1 = nsolve_prv(page);
		//Log.prn("case2 %d x %d",page.w,page.h);
		int n2 = nsolve_prv(page.rotated());
		return Math.max(n1, n2);
	}

	/*(49,28,8,3) -> 57 boxes
	 * (1600,1230,137,95) -> 147
	 */
	public static void main(String[] args) {
		RectPackRecursive r;
		r = new RectPackRecursive(new Sheet(5,3));
		Log.prn("---------------");
		Log.prn("r = %d",r.solve(new Sheet(28,27)));
		Log.prn("nr = %d",r.nsolve(new Sheet(28,27)));

		r = new RectPackRecursive(new Sheet(8,3));
		System.out.printf("r = %d\n",r.solve(new Sheet(49,28)));
		System.out.printf("nr = %d\n",r.nsolve(new Sheet(49,28)));
		System.out.printf("nr = %d\n",r.nsolve(new Sheet(28,49)));

		r = new RectPackRecursive(new Sheet(137,95));
		System.out.printf("nr = %d\n",r.nsolve(new Sheet(1600,1230)));
		System.out.printf("nr = %d\n",r.nsolve(new Sheet(1230,1600)));
		r.solve(new Sheet(1230,1600));
		//System.out.printf("r = %d\n",r.solve(new Box(1230,1600)));
	}
}
