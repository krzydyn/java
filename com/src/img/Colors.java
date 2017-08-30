package img;

// www.easyrgb.com/math.html

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
	public static float rgbErrorSq(int c1,int c2){
		float err=0, e;
		e=(((c1>>16)&0xff)-((c2>>16)&0xff))/255f;
		err += e*e;
		e=(((c1>>8)&0xff)-((c2>>8)&0xff))/255f;
		err += e*e;
		e=((c1&0xff)-(c2&0xff))/255f;
		err += e*e;
		return err/3f;
	}

	public static float invGamma(float c) {
		if ( c <= 0.04045 ) return c/12.92f;
		return (float)Math.pow(((c+0.055)/(1.055)),2.4);
	}
	public static float gamma(float i) {
		if (i <= 0.0031308) return i*12.92f;
		return (float) (1.055f*Math.pow(i,1.0/2.4)-0.055f);
	}

	public static float invCmy(float c) {
		return 1f - c;
	}
	public static float cmy(float i) {
		return 1f - i;
	}

	public static void rgb2float(int rgb, float[] s){
		s[0]=((rgb>>16)&0xff)/255f;
		s[1]=((rgb>>8)&0xff)/255f;
		s[2]=(rgb&0xff)/255f;
		if (s.length>3) s[3] = ((rgb>>24)&0xff)/255f;
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

	/*
	 * h : 0..255 => 0..360 degrees
	 * s : 0..255 =>
	 * v : 0..255 =>
	 */
	public static void rgb2hsv(int rgb,float[] hsv){
		rgb2float(rgb, hsv);

	}
	public static int hsv2rgb(float h, float s, float v) {
		if (s <= 0.0) {
			int r = (int)(v*255);
			return (r<<16) + (r<<8) + r;
		}
		float region = h / 43;
	    float remainder = (h - (region * 43)) * 6;
		return 0;
	}

	public static float luminance(int rgb) {
		float r = invGamma(((rgb>>16)&0xff)/255f);
		float g = invGamma(((rgb>>8)&0xff)/255f);
		float b = invGamma((rgb&0xff)/255f);
		return 0.2126f*r + 0.7152f*g + 0.0722f*b;
	}
	/**
	 * Relative luminance
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static float luminanceRelative(float r,float g, float b) {
		return 0.2126f*r + 0.7152f*g + 0.0722f*b;
	}
	public static float luminance2(float r,float g, float b) {
		//return (float)Math.sqrt(r*r*0.241 + g*g*0.691 + b*b*0.068);
		return r*0.299f + g*0.587f + b*0.114f;
	}
	public static int luminance2(int rgb) {
		int r = (rgb>>16)&0xff;
		int g = (rgb>>8)&0xff;
		int b = rgb&0xff;
		float l = r*0.299f + g*0.587f + b*0.114f;
		return l > 255f ? 255 : (int)(l+0.5f);
	}

	public static int quick_luminance(int rgb) {
		int r = (rgb>>16)&0xff;
		int g = (rgb>>8)&0xff;
		int b = rgb&0xff;
		return ((r<<1)+(g<<2)+b)>>>3;
	}

	/**
	 *  @param y - relative luminance
	 *
	 */
	public static float lightness(float y) {
		return 0f;
	}

}
