package sys;

public class Colors {
	public static int errorSum(int rgb1, int rgb2) {
		int r = ((rgb1>>16)&0xff) - ((rgb2>>16)&0xff);
		int g = ((rgb1>>8)&0xff) - ((rgb2>>8)&0xff);
		int b = (rgb1&0xff) - (rgb2&0xff);
		return Math.abs(r)+Math.abs(g)+Math.abs(b);
	}
	public static int errorSq(int rgb1, int rgb2) {
		int r = ((rgb1>>16)&0xff) - ((rgb2>>16)&0xff);
		int g = ((rgb1>>8)&0xff) - ((rgb2>>8)&0xff);
		int b = (rgb1&0xff) - (rgb2&0xff);
		return r*r+g*g+b*b;
	}

	public static float invGamma(float c) {
		if ( c <= 0.04045 ) return c/12.92f;
		return (float)Math.pow(((c+0.055)/(1.055)),2.4);
	}

	public static float gamma(float i) {
		if (i <= 0.0031308) return i*12.92f;
		return (float) (1.055f*Math.pow(i,1.0/2.4)-0.055f);
	}

	public static float luminance(int rgb) {
		float r = invGamma(((rgb>>16)&0xff)/255f);
		float g = invGamma(((rgb>>8)&0xff)/255f);
		float b = invGamma((rgb&0xff)/255f);
		return 0.2126f*r + 0.7152f*g + 0.0722f*b;
	}
	/**
	 * Ralative luminance
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
		return (r<<1+r+g<<2+b)>>>3;
	}

	/**
	 *  @param y - relative luminance
	 *
	 */
	public static float lightness(float y) {
		return 0f;
	}

}
