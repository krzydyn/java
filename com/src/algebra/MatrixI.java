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

public class MatrixI {
	private final int w,h;
	private final int[] v;

	public MatrixI(int w,int h) {
		this.w=w; this.h=h;
		v = new int[w*h];
	}
	public int get(int x,int y) {return v[y*w+x];}
	public int set(int x,int y,int v) {return this.v[y*w+x]=v;}

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
		int n=w*h;
		int[] tv = new int[n];
		for(int i = 0 ; i < n ; ++i) tv[i]=0;

		for(int y = 0 ; y < h ; ++y)
			for(int k = 0 ; k < h ; ++k) {
				int c=v[y*w+k];
				for(int x = 0 ; x < w ; ++x)
					tv[y*w+x]+=c*m.v[k*w+x];
			}

		for(int i = 0 ; i < n; ++i) v[i]=tv[i];
		return this;
	}

	public MatrixI pow(int n) {
		if (n==1) return this;
		MatrixI t=new MatrixI(w, h).unit();
		while(n!=0) {
			if ((n&1)!=0) t.mul(this);
			mul(this);
			n>>=1;
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
			n>>=1;
		}
		return this;
	}
}
