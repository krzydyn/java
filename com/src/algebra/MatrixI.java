/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package algebra;

import java.awt.Dimension;

public class MatrixI {
	private final int w,h;
	private final int[] v;

	public MatrixI(int w,int h) {
		this.w=w; this.h=h;
		this.v = new int[w*h];
	}
	public MatrixI(int w,int h, int[] v) {
		this.w=w; this.h=h;
		this.v = new int[w*h];
		for (int i=0; i < v.length; ++i) this.v[i]=v[i];
	}
	public MatrixI(int w, int... a) {
		this.w=w;
		this.h=(a.length+w-1)/w;
		this.v = new int[this.w*this.h];
		System.arraycopy(a, 0, this.v, 0, a.length);
	}
	public MatrixI(String t) {
		Dimension dim=new Dimension();
		v=loadFrom(t, dim);
		w=dim.width; h=dim.height;
	}
	public int getWidth() {return w;}
	public int getHeight() {return h;}
	public int get(int x,int y) {return v[y*w+x];}
	public int set(int x,int y,int v) {return this.v[y*w+x]=v;}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int y=0; y < h; ++y) {
			b.append("|");
			for (int x=0; x < w; ++x)
				b.append(" "+v[y*w+x]);
			b.append(" |\n");
		}
		return b.toString();
	}

	static private int[] loadFrom(String s, Dimension dim) {
		String[] rows=s.split("\\||\n");
		dim.height=rows.length;
		dim.width=0;
		int[] v=null;
		for (int y=0; y < rows.length; ++y) {
			String cols[] = rows[y].split(" ");
			if (dim.width==0) {
				dim.width=cols.length;
				v=new int[dim.width*dim.width];
			}
			for (int x=0; x < cols.length; ++x) {
				v[y*dim.width+x] = Integer.parseInt(cols[x]);
			}
		}
		return v;
	}

	public boolean equals(MatrixI o) {
		if (w != o.w || h != o.h) return false;
		int n=w*h;
		for(int i = 0 ; i < n ; ++i) {
			if (v[i]!=o.v[i]) return false;
		}
		return true;
	}

	public MatrixI zero() {
		int n=w*h;
		for(int i = 0 ; i < n ; ++i)  v[i]=0;
		return this;
	}
	public MatrixI unit() {
		int n=w*h;
		for(int i = 0 ; i < n ; ++i)  v[i]=0;
		int hw = Math.min(w, h);
		for(int i = 0 ; i < hw; ++i) set(i,i,1);
		return this;
	}

	public MatrixI mul(MatrixI m) {
		if (w != m.h) throw new RuntimeException("can't mult matrix this.w="+w+"!=m.h="+m.h);
		MatrixI r = new MatrixI(m.w, h);
		for(int i = 0 ; i < r.h ; ++i)
			for(int k = 0 ; k < m.h ; ++k) {
				for(int j = 0 ; j < r.w ; ++j) {
					r.v[i*r.w+j]+=v[i*w+k]*m.v[k*m.w+j];
				}
			}

		return r;
	}

	public MatrixI pow(int n) {
		if (n==1) return this;
		MatrixI t=new MatrixI(w, h).unit();
		while(n!=0) {
			if ((n&1)!=0) t.mul(this);
			mul(this);
			n>>>=1;
		}
		return this;
	}

	public MatrixI mul(MatrixI m, int mod) {
		int n=w*h;
		int[] tv = new int[n];
		for(int i = 0 ; i < n ; ++i) tv[i]=0;

		for(int y = 0 ; y < h ; ++y)
			for(int k = 0 ; k < h ; ++k) {
				long c=v[y*w+k];
				for(int x = 0 ; x < w ; ++x)
					tv[y*w+x]=(int)((v[y*w+x] + c*m.v[k*w+x])%mod);
			}

		for(int i = 0 ; i < n; ++i) v[i]=tv[i];
		return this;

	}
	public MatrixI pow(int n, int mod) {
		if (n==1) return this;
		MatrixI t=new MatrixI(w, h).unit();
		while(n!=0) {
			if ((n&1)!=0) t.mul(this,mod);
			mul(this,mod);
			n>>>=1;
		}
		return this;
	}
}
