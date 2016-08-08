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

import sys.Log;

public class SudokuFast extends SudokuSimple {
	final private boolean[][] usedInRow;
	final private boolean[][] usedInCol;
	final private boolean[][] usedInBox;
	final private int[][] cbox;
	final private int[] map;

	public SudokuFast(int order) {
		super(order);
		usedInRow = new boolean[DIM][DIM];
		usedInCol = new boolean[DIM][DIM];
		usedInBox = new boolean[DIM][DIM];
		cbox = new int[DIM][DIM];
		map = new int[DIM2];

		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				cbox[y][x]=x/ORDER+y-y%ORDER;
			}
	}
	@Override
	public void clear() {
		super.clear();
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				usedInRow[y][x]=false;
				usedInCol[y][x]=false;
				usedInBox[y][x]=false;
			}
	}
	@Override
	public void reset() {
		if (doInit) clear();
		else {
			super.reset();
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) {
					usedInRow[y][x]=false;
					usedInCol[y][x]=false;
					usedInBox[y][x]=false;
				}
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) {
					if (hints[y][x]) setUsed(x, y, a[y][x]-1);
				}
		}
	}
	final private int getbox(int x,int y) {
		return cbox[y][x];
	}
	@Override
	protected void setUsed(int x,int y,int v) {
		a[y][x] = v+1;
		usedInRow[y][v]=true;
		usedInCol[x][v]=true;
		usedInBox[getbox(x,y)][v]=true;
	}
	@Override
	protected void resetUsed(int x,int y) {
		int v=a[y][x]-1;
		a[y][x]=0;
		usedInRow[y][v]=false;
		usedInCol[x][v]=false;
		usedInBox[getbox(x,y)][v]=false;
	}
	@Override
	protected boolean isForbiden(int x,int y,int v) {
		return usedInRow[y][v] || usedInCol[x][v] || usedInBox[getbox(x,y)][v];
	}

	private void swapRows(int y1,int y2) {
		for (int x=0; x<DIM; ++x) {
			int t=map[y1*DIM+x];
			map[y1*DIM+x] = map[y2*DIM+x];
			map[y2*DIM+x] = t;
		}
	}
	private void swapBigRows(int y1,int y2) {
		Log.info("swapBigRows %d <> %d",y1,y2);
		for (int y=0; y<ORDER; ++y)
			swapRows(y1+y,y2+y);
	}
	/*private void swapCols(int x1,int x2) {
		for (int y=0; y<DIM; ++y) {
			int t=map[y*DIM+x1];
			map[y*DIM+x1] = map[y*DIM+x2];
			map[y*DIM+x2] = t;
		}
	}
	private void swapBigCols(int x1,int x2) {
		Log.info("swapBigCols");
		for (int x=0; x<ORDER; ++x)
			swapCols(x1+x,x2+x);
	}*/
	private void transpose() {
		Log.info("transpose");
		int t;
		for (int y=0; y<DIM; ++y) {
			for (int x=0; x<y; ++x) {
				t=map[y*DIM+x];
				map[y*DIM+x] = map[x*DIM+y];
				map[x*DIM+y]=t;
			}
		}
	}
	@Override
	protected void init() {
		super.init();
		for (int i=0; i < DIM2; ++i) map[i]=i;

		//box histogram
		int hist[] = new int[DIM];
		for (int y=0; y<DIM; ++y) {
			for (int x=0; x<DIM; ++x) {
				if (a[y][x]!=0)
					++hist[getbox(x,y)];
			}
		}
		//find max col/row
		int bs=0,br=0,bc=0;
		for (int y=0; y<ORDER; ++y) {
			int s=0;
			for (int x=0; x<ORDER; ++x) {
				s+=hist[y*ORDER+x];
			}
			if (bs<s) {bs=s;br=y;}
		}
		for (int x=0; x<ORDER; ++x) {
			int s=0;
			for (int y=0; y<ORDER; ++y) {
				s+=hist[y*ORDER+x];
			}
			if (bs<s) {bs=s;bc=x;}
		}

		//if max is col then transpose
		if (bc!=0) {
			transpose();
			br=bc;
		}
		//move max row to top
		if (br!=0)
			swapBigRows(0,br*ORDER);
		//printmapped();
	}

	private void printmapped() {
		for (int p=0; p<DIM2; ++p) {
			if (p%DIM == 0) {
				if (p>0) {
					System.out.println();
					if (p%(DIM*ORDER) == 0) {
						int n = 2*DIM+ORDER;
						for (int i=0; i <= n; ++i)
							System.out.print("-");
						System.out.println("-");
					}
				}
			}
			else if (p%ORDER == 0)System.out.printf(" |");
			int y=map[p]/DIM, x=map[p]%DIM;
			if (a[y][x]!=0) System.out.printf(" %d",a[y][x]);
			else System.out.printf(" .");
		}
		System.out.println();
	}

	//order of visiting fields
	@Override
	protected int pmap(int mp) {
		return map[mp];
	}
}
