package algebra;

public class Matrix2I {
	private final int w,h;
	private final int[] v;
	private int M=1; // modulo

	public Matrix2I(int w,int h) {
		this.w=w; this.h=h;
		v = new int[w*h];
	}
	public int get(int x,int y) {return v[y*w+x];}
	public int set(int x,int y,int v) {return this.v[y*w+x]=v;}

	public Matrix2I zero() {
		for(int i = 0 ; i < v.length ; i++)  v[i]=0;
		return this;
	}
	public Matrix2I unit() {
		for(int i = 0 ; i < v.length ; i++)  v[i]=0;
		int hw = Math.min(w, h);
		for(int i = 0 ; i < hw; i ++) set(i,i,1);
		return this;
	}

	public Matrix2I mul(Matrix2I m) {
		int[] tv = new int[w*h];
		for(int i = 0 ; i < tv.length ; i++) tv[i]=0;

		for(int i = 0 ; i < h ; i ++)
			for(int k = 0 ; k < h ; k++)
				for(int j = 0 ; j < w ; j++) {
					if (M==1)
						tv[i*w+j]+=v[i*w+k]*m.v[k*w+j];
					else
						tv[i*w+j]=(int)((v[i*w+j] + ((long)v[i*w+k])*m.v[k*w+j])%M);
				}

		for(int i = 0 ; i < tv.length ; i++) v[i]=tv[i];
		return this;
	}

	public Matrix2I pow(int n) {
		if (n==1) return this;
		Matrix2I t=new Matrix2I(w, h).unit();
		while(n!=0) {
	        if ((n&1)!=0) t.mul(this);
	        mul(this);
	        n>>=1;
	    }

		return this;
	}
}
