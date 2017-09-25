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
	public static int rgb(float r, float g, float b){
		return (int)(r*255)<<16 + (int)(g*255)<<8 + (int)(b*255);
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

	public static int yuv2rgb(int y, int u, int v) {
		//int r = (int)(1.164*(y-16)+1.596*v+0.5);
		//int g = (int)(1.164*(y-16)-0.391*u-0.813*v+0.5);
		//int b = (int)(1.164*(y-16)+2.018*u+0.5);
		int r = (9535*y + 13074*v -148464) >> 13;
		int g = (9535*y - 6660*v - 3203*u -148464) >> 13;
		int b = (9535*y + 16531*u -148464) >> 13;
		if (r>255) r=255; if (r<0) r=0;
		if (g>255) g=255; if (g<0) g=0;
		if (b>255) b=255; if (b<0) b=0;
		return (r<<16) | (g<<8) | b;
	}

	/*
	 * h : 0..360 //hue
	 * s : 0..1   //saturation
	 * v : 0..1   //value (or lightness)
	 * https://codepen.io/katsew/pen/GZNEVE
	 */
	public static void rgb2hsv(int rgb,float[] hsv){
		rgb2float(rgb, hsv);
		float min,max;
		min=max=hsv[0];
		for (int i = 1; i < 3; ++i) {
			if (min > hsv[i]) min = hsv[i];
			if (max < hsv[i]) max = hsv[i];
		}
		if (max == 0f) {
			hsv[0] = hsv[1] = hsv[2] = 0f;
		}
		else if (max-min == 0f) {
			hsv[0]=max;
			hsv[1] = hsv[2] = 0f;
		}
		else {
			//s = (max - min) / max;
		}
	}
	//https://github.com/tmpvar/hsv2rgb
	public static int hsv2rgb(float h, float s, float v) {
		if (s <= 0.0) {
			int r = (int)(v*255);
			return (r<<16) | (r<<8) | r;
		}
		float b = (1 - s) * v;
		float vb = v - b;
		float hm = h % 60;
		if (h < 60) return rgb(v, vb * h / 60 + b, b);
		if (h < 120) return rgb(vb * (60 - hm) / 60 + b, v, b);
		if (h < 180) return rgb(b, v, vb * hm / 60 + b);
		if (h < 240) return rgb(b, vb * (60 - hm) / 60 + b, v);
		if (h < 300) return rgb(vb * hm / 60 + b, b, v);
		return rgb(v, b, vb * (60 - hm) / 60 + b);
	}

	public static float luminance(int rgb) {
		float r = invGamma(((rgb>>16)&0xff)/255f);
		float g = invGamma(((rgb>>8)&0xff)/255f);
		float b = invGamma((rgb&0xff)/255f);
		return 0.2126f*r + 0.7152f*g + 0.0722f*b;
	}

	/**
	 * Relative luminance (photometric)
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static float luminanceRelative(float r,float g, float b) {
		return 0.2126f*r + 0.7152f*g + 0.0722f*b;
	}
	/**
	 * Luminance (digital)
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static float luminance(float r,float g, float b) {
		return r*0.299f + g*0.587f + b*0.114f;
	}
	public static float luminanceHSP(float r,float g, float b) {
		return (float)Math.sqrt(r*r*0.299f + g*g*0.587f + b*b*0.114f);
	}
	public static float brightness(float r,float g, float b) {
		return (float)Math.sqrt(0.241f*r*r + 0.691f*g*g + 0.068f*b*b);
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
		return (r+(r<<1)+(g<<2)+b)>>>3; //(R+R+R+B+G+G+G+G)>>3
	}

	/**
	 *  @param y - relative luminance
	 * Priest: V^2 = 100 * Y (V = 0..10)
	 * Munsell: V^2 = 1.4742*Y - 0.004743*Y^2
	 * Kodak: V = 2.468Y^(1/3)-1.636
	 */
	public static float lightness(float y) {
		return (float)Math.sqrt(2.468*y-1.636);
	}

}
