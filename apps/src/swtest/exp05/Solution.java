package swtest.exp05;

public class Solution {
	static class puzzle {
		boolean used;
		long l,t,r,b;
	}

	static puzzle[] pz = new puzzle[256];
	static long bghash;

	static int count;
	static int list[] = new int[256];


	static int getcolor(byte[][] image, int p, int x, int y) {
		int ya = y+(p/16)*256;
		int xa = x+(p%16)*256;
		return image[ya][xa]&0xff;
	}

	static long calchash(long h, int c) {
		h = h*31 + (c&0xff) +1;
		if (h==0) h=1;
		return h;
	}
	static void prepare(byte[][] image, int p) {
		long h;
		int ln,pc,c;

		pz[p] = new puzzle();
		pz[p].used=false;

		// left edge hash
		ln=0; h=0; pc=getcolor(image, p, 0, 0);
		for (int i=1; i < 256; ++i) {
			c = getcolor(image, p, 0, i);
			if (pc == c) ln = 1;
			else { if (ln==0) h=calchash(h,pc); ln=0;}
			pc=c;
		}
		if (ln==0) h=calchash(h,pc);
		pz[p].l = h;

		// top edge
		ln=0; h=0; pc=getcolor(image, p, 0, 0);
		for (int i=1; i < 256; ++i) {
			c = getcolor(image, p, i, 0);
			if (pc == c) ln = 1;
			else { if (ln==0) h=calchash(h,pc); ln=0;}
			pc=c;
		}
		if (ln==0) h=calchash(h,pc);
		pz[p].t = h;

		// bottom edge
		ln=0; h=0; pc=getcolor(image, p, 0, 255);
		for (int i=1; i < 256; ++i) {
			c = getcolor(image, p, i, 255);
			if (pc == c) ln = 1;
			else { if (ln==0) h=calchash(h,pc); ln=0;}
			pc=c;
		}
		if (ln==0) h=calchash(h,pc);
		pz[p].b = h;

		// right edge
		ln=0; h=0; pc=getcolor(image, p, 255, 0);
		for (int i=1; i < 256; ++i) {
			c = getcolor(image, p, 255, i);
			if (pc == c) ln = 1;
			else { if (ln==0) h=calchash(h,pc); ln=0;}
			pc=c;
		}
		if (ln==0) h=calchash(h,pc);
		pz[p].r = h;

		printf("P[%d].hash = %X %X %X %X\n", p, pz[p].l, pz[p].t, pz[p].r, pz[p].b);
	}
	static boolean fit_lt(int p, long left, long top) {
		return pz[p].l == left && pz[p].t == top;
	}
	static boolean fit_r(int p, long right) {
		return pz[p].r == right;
	}
	static boolean fit_b(int p, long bot) {
		return pz[p].b == bot;
	}

	static boolean fitput(int p) {
		long l,t;
		if (count%16 == 0) l = bghash;
		else l = pz[list[count-1]].r;
		if (count < 16) t = bghash;
		else t = pz[list[count-16]].b;

		if (!fit_lt(p, l, t)) return false;

		if ((count+1)%16 == 0 && !fit_r(p, bghash)) return false;

		if (count>=15*16 && !fit_b(p, bghash)) return false;

		return true;
	}

	static int put(int p) {
		if (count >= 256) return 0;
		if (pz[p].used) return 0;

		list[count]=p;
		pz[p].used=true;
		++count;
		return 1;
	}
	static int remove() {
		if (count <= 0) return -2;
		--count;
		int p = list[count];
		pz[p].used=false;
		return p;
	}

	static void solve(byte[][] image) {
		count = 0;
		bghash=0;
		printf("bghash = %X\n", bghash);
		for (int i=0; i < 256; ++i) {
			prepare(image, i);
		}
		int pn=0;
		while (pn >= 0) {
			boolean ok=false;
			for (int p=pn; p < 256; ++p) {
				if (pz[p].used) continue;
				if (fitput(p)) { put(p); ok=true; break; }
				printf("%d not fit at %d\n", p, count);
				break;
			}
			if (ok) pn=0;
			else {
				pn = remove()+1;
				break;
			}
			printf("count = %d pn = %d	   \n", count, pn);
		}
		printf("\n");
		printf("count = %d\n", count);
	}

	static void printf(String f, Object... args) {
		System.out.printf(f, args);
	}
}
