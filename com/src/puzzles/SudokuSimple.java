package puzzles;

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

public class SudokuSimple {
	final private String values = ".123456789";
	protected final int ORDER;
	protected final int DIM;
	protected final int[][] a;
	protected final boolean[][] hints;
	protected boolean doInit = true;

	public SudokuSimple(int order) {
		ORDER = order;
		DIM = order*order;
		a = new int[DIM][DIM];
		hints = new boolean[DIM][DIM];
	}
	public void clear() {
		doInit=true;
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				a[y][x]=0;
			}
	}
	public void reset() {
		if (doInit) clear();
		else {
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) {
					if (!hints[y][x]) a[y][x]=0;
				}
		}
	}
	public void set(int x,int y,int v) {a[y][x]=v;}
	public int get(int x,int y) {return a[y][x];}
	public boolean isEmpty(int x,int y) {return a[y][x]==0;}

	public void parse(String s) {
		String ignore = "-|= ";
		clear();
		int p=0;
		for (int i=0; i < s.length(); ++i) {
			char c=s.charAt(i);
			if (c <= ' ' || ignore.indexOf(c) >= 0) continue;
			int x=p%DIM;
			int y=p/DIM;
			int v = values.indexOf(c);
			a[y][x] = v<0 ? v=0 : v;
			++p;
		}
	}
	@Override
	public String toString() {
		StringBuilder b=new StringBuilder();
		int s=0;
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				b.append(String.format("%s",values.charAt(a[y][x])));
				if (a[y][x]>0) ++s;
			}
		b.append(" s="+s);
		return b.toString();
	}
	public void print() {
		for (int y=0; y<DIM; ++y) {
			if (y>0 && y%ORDER == 0)
				System.out.println("-----------------------");
			for (int x=0; x<DIM; ++x) {
				if (x>0 && x%ORDER == 0) System.out.print(" |");
				if (a[y][x]!=0) System.out.printf(" %d",a[y][x]);
				else System.out.printf(" .");
			}
			System.out.println();
		}
		System.out.println();
	}
	protected void setUsed(int x,int y,int v) {
		a[y][x] = v+1;
	}
	protected void resetUsed(int x,int y) {
		a[y][x]=0;
	}

	protected boolean setCheck(int x,int y,int v) {
		//horizontal
		for (int i=0; i < DIM; ++i) {
			if (a[y][i] == v) return false;
		}
		//vertical
		for (int i=0; i < DIM; ++i) {
			if (a[i][x] == v) return false;
		}
		//box
		int x0 = x - x%ORDER;
		int y0 = y - y%ORDER;
		for (y=0; y < ORDER; ++y) {
			for (x=0; x < ORDER; ++x) {
				if (a[y0+y][x0+x] == v) return false;
			}
		}
		return true;
	}

	//order of visiting fields
	protected int pmap(int mp) {
		//return DIM*DIM-mp-1; //280.677 sec
		return mp; //134.139 sec
	}

	public boolean solve() {
		final int d=DIM*DIM;
		int mp,p,x,y;
		if (doInit) {
			for (y=0; y<DIM; ++y)
				for (x=0; x<DIM; ++x) {
					hints[y][x] = a[y][x] != 0;
					if (hints[y][x]) setUsed(x, y, a[y][x]-1);
				}
			doInit=false;
			mp=0;
		}
		else {
			mp=d-1;
			while (mp > 0) {
				p=pmap(mp);
				x=p%DIM; y=p/DIM;
				if (!hints[y][x]) break;
				--mp;
			}
		}

		while (mp < d) {
			p=pmap(mp);
			x=p%DIM; y=p/DIM;
			if (hints[y][x]) {++mp; continue;}
			int v=a[y][x];
			if (v>0) resetUsed(x,y);
			for (; v<DIM; ++v) {
				if (setCheck(x,y,v)) break;
			}

			if (v < DIM) {
				++mp;
				setUsed(x,y,v);
				continue;
			}

			//backtrack
			--mp;
			while (mp >= 0) {
				p=pmap(mp);
				x=p%DIM; y=p/DIM;
				if (hints[y][x]) {--mp;continue;}
				if (a[y][x] == DIM) {
					resetUsed(x,y);
					--mp;
					continue;
				}
				break;
			}
			if (mp<0) return false;
		}
		return true;
	}
}
