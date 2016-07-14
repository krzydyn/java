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

public class Sudoku {
	final private String values = ".123456789ABCDEFGHIJKLMNOPQRSTUWXYZ@%";
	final private int ORDER;
	final private int DIM;
	final private int[][] a;
	final private boolean[][] fixed;
	private boolean initFixed = true;

	private boolean[][] usedRow;
	private boolean[][] usedCol;
	private boolean[][] usedBox;

	public Sudoku(int order) {
		ORDER = order;
		DIM = order*order;
		a = new int[DIM][DIM];
		fixed = new boolean[DIM][DIM];
		usedRow = new boolean[DIM][DIM];
		usedCol = new boolean[DIM][DIM];
		usedBox = new boolean[DIM][DIM];
	}
	public void clear() {
		initFixed=true;
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) a[y][x]=0;
	}
	public void reset() {
		if (initFixed) clear();
		else {
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) if (!fixed[y][x]) a[y][x]=0;
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
	public String toString() {
		StringBuilder b=new StringBuilder();
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x)
				b.append(String.format("%s",values.charAt(a[y][x])));
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
	private void setUsed(int x,int y,int v) {
		usedRow[y][v]=true;
		usedCol[x][v]=true;
		int box=x/ORDER+(y/ORDER)*ORDER;
		usedBox[box][v]=true;
	}
	private void resetUsed(int x,int y,int v) {
		usedRow[y][v]=false;
		usedCol[x][v]=false;
		int box=x/ORDER+(y/ORDER)*ORDER;
		usedBox[box][v]=false;
	}
	private boolean canSet(int x,int y,int v) {
		int box=x/ORDER+(y/ORDER)*ORDER;
		return !usedRow[y][v] && !usedCol[y][v] && !usedBox[box][v];
	}
	private boolean isAllowed(int x,int y,int v) {
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

	public boolean solve() {
		int p,x,y;
		if (initFixed) {
			for (y=0; y<DIM; ++y)
				for (x=0; x<DIM; ++x) {
					fixed[y][x] = a[y][x] != 0;
					usedRow[y][x]=false;
					usedCol[y][x]=false;
					usedBox[y][x]=false;
				}
			initFixed=false;
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
			for (; v<DIM; ++v) {
				//if (isAllowed(x, y, v+1)) break;
				if (canSet(x, y, v)) break;
			}

			if (v < DIM) {
				a[y][x] = v+1; ++p;
				setUsed(x,y,v);
				continue;
			}

			//backtrack
			if (p == 0) return false;
			resetUsed(x, y, a[y][x]);
			a[y][x] = 0; --p;
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
