package img;

public class Colors {
	public static void rgbError(int c1,int c2,int[] err){
		 err[0]=((c1>>16)&0xff)-((c2>>16)&0xff);
		 err[1]=((c1>>8)&0xff)-((c2>>8)&0xff);
		 err[2]=(c1&0xff)-(c2&0xff);
	}
	/**
	 * Calculate error in range 0..1
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static float rgbError(int c1,int c2){
		float err=0;
		err+=Math.abs(((c1>>16)&0xff)-((c2>>16)&0xff))/255f;
		err+=Math.abs(((c1>>8)&0xff)-((c2>>8)&0xff))/255f;
		err+=Math.abs((c1&0xff)-(c2&0xff))/255f;
		return err/3f;
	}

	public static void rgbGet(float[] s,int c){
		s[0]=((c>>16)&0xff)/255f;
		s[1]=((c>>8)&0xff)/255f;
		s[2]=(c&0xff)/255f;
		if (s.length>3) s[3] = ((c>>24)&0xff)/255f;
	}
	public static int rgb(float[] s){
		int c,r;
		r=(int)(s[0]*255);
		if (r<0) r=0; else if (r>255) r=255;
		c=r;
		r=(int)(s[1]*255);
		if (r<0) r=0; else if (r>255) r=255;
		c<<=8; c|=r;
		r=(int)(s[2]*255);
		if (r<0) r=0; else if (r>255) r=255;
		c<<=8; c|=r;
		if (s.length>3) {
			r=(int)(s[3]*255);
			if (r<0) r=0; else if (r>255) r=255;
			c|=r<<24;
		}
		return c;
	}
}
