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
package puzzles;

public class SudokuFast {
	final static private String values = ".123456789ABCDEFGHIJKLMNOPQRSTUWXYZ@%";

	final private int ORDER;
	final private int DIM;
	final private int[][] a;
	final private boolean[][] fixed;
	private boolean doInit = true;

	final private boolean[][] notUsedRow;
	final private boolean[][] notUsedCol;
	final private boolean[][] notUsedBox;
	final private int[][] cbox;

	public SudokuFast(int order) {
		ORDER = order;
		DIM = order*order;
		a = new int[DIM][DIM];
		fixed = new boolean[DIM][DIM];
		notUsedRow = new boolean[DIM][DIM];
		notUsedCol = new boolean[DIM][DIM];
		notUsedBox = new boolean[DIM][DIM];
		cbox = new int[DIM][DIM];

		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				cbox[y][x]=x/ORDER+y-y%ORDER;
			}

		clear();
	}
	public void clear() {
		doInit=true;
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				a[y][x]=0;
				notUsedRow[y][x]=true;
				notUsedCol[y][x]=true;
				notUsedBox[y][x]=true;
			}
	}
	public void reset() {
		if (doInit) clear();
		else {
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) {
					notUsedRow[y][x]=true;
					notUsedCol[y][x]=true;
					notUsedBox[y][x]=true;
				}
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) {
					if (!fixed[y][x]) a[y][x]=0;
					else setUsed(x, y, a[y][x]-1);
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
				System.out.printf(" %s",values.charAt(a[y][x]));
			}
			System.out.println();
		}
		System.out.println();
	}
	private int getbox(int x,int y) {
		//return x/ORDER+y-y%ORDER;
		return cbox[y][x];
	}
	private void setUsed(int x,int y,int v) {
		notUsedRow[y][v]=false;
		notUsedCol[x][v]=false;
		notUsedBox[getbox(x,y)][v]=false;
	}
	private void resetUsed(int x,int y,int v) {
		notUsedRow[y][v]=true;
		notUsedCol[x][v]=true;
		notUsedBox[getbox(x,y)][v]=true;
	}
	private boolean canSet(int x,int y,int v) {
		//return !(usedRow[y][v] || usedCol[x][v] || usedBox[getbox(x,y)][v]);
		return notUsedRow[y][v] && notUsedCol[x][v] && notUsedBox[getbox(x,y)][v];
	}

	public boolean solve() {
		int p,x,y;
		if (doInit) {
			for (y=0; y<DIM; ++y)
				for (x=0; x<DIM; ++x) {
					fixed[y][x] = a[y][x] != 0;
					if (fixed[y][x]) setUsed(x, y, a[y][x]-1);
				}
			doInit=false;
			p=0;
		}
		else {
			p=DIM*DIM-1;
			while (p > 0) {
				x=p%DIM; y=p/DIM;
				if (!fixed[y][x]) break;
				--p;
			}
		}

		while (p < DIM*DIM) {
			x=p%DIM; y=p/DIM;
			if (fixed[y][x]) {++p; continue;}
			int v=a[y][x];
			if (v>0) resetUsed(x,y,v-1);
			a[y][x] = 0;
			for (; v<DIM; ++v) {
				if (canSet(x, y, v)) break;
			}

			if (v < DIM) {
				a[y][x] = v+1; ++p;
				setUsed(x,y,v);
				continue;
			}

			//backtrack
			--p;
			while (p >= 0) {
				x=p%DIM; y=p/DIM;
				if (fixed[y][x]) {--p;continue;}
				if (a[y][x] == DIM) {
					resetUsed(x,y,a[y][x]-1);
					a[y][x]=0; --p;
					continue;
				}
				break;
			}
			if (p<0) return false;
		}
		return true;
	}
}
