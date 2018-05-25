package swtest.exp05;

import java.util.Random;

public class Main {
	static byte[][] image = new byte[4096][4096];
	static byte[][] image_orig = new byte[4096][4096];
	static Random rand = new Random(100);

	static int check(byte[][] image) {
	    for (int y=0; y < 4096; ++y)
	        for (int x=0; x < 4096; ++x)
	            if (image[y][x] != image_orig[y][x]) return 0;
	    return 1;
	}

	static void drawrect(int x, int y, int w, int h, int c) {
	    for (int i=0; i < w; ++i) {
	        image[y][x+i] = (byte)c;
	        image[y+h-1][x+i] = (byte)c;
	    }
	    for (int i=0; i < h; ++i) {
	        image[y+i][x] = (byte)c;
	        image[y+i][x+w-1] = (byte)c;
	    }
	}
	static void buildpuzzle() {
	    for (int i=0; i < 1024; ++i) {
	        int w = 16+rand()%1024;
	        int h = 16+rand()%1024;
	        int x = 16+rand()%(4096-32-w);
	        int y = 16+rand()%(4096-32-h);
	        char c = (char)(1+rand()%255);
	        drawrect(x,y,w,h,c);
	    }
	}

	static void swappuzzle(int a, int b) {
	    for (int y=0; y < 256; ++y) {
	        int ya = y+a/16;
	        int yb = y+b/16;
	        for (int x=0; x < 256; ++x) {
	            int xa = x+a%16;
	            int xb = x+b%16;
	            byte c = image[ya][xa];
	            image[ya][xa] = image[yb][xb];
	            image[yb][xb] = c;
	        }
	    }
	}

	static void mixpuzzle() {
	    for (int i=0; i < 256; ++i) {
	        int a = rand()%256;
	        int b = rand()%256;
	        swappuzzle(a,b);
	    }
	}

	public static void main(String[] args) {
	    buildpuzzle();
	    //mixpuzzle();
	    Solution.solve(image);
	}


	static long randst = 100;
	static int rand() {
		//long x = randst*0x5DEECE66DL+0xBL;
		long x = randst + 17;
		randst = x;
		//return rand.nextInt()&0x7fffffff;
		return (int)(x&0x7fffffff);
	}
}
