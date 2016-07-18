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

public class SudokuFast extends SudokuSimple {
	final private boolean[][] notUsedRow;
	final private boolean[][] notUsedCol;
	final private boolean[][] notUsedBox;
	final private int[][] cbox;

	public SudokuFast(int order) {
		super(order);
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
	@Override
	public void clear() {
		super.clear();
		for (int y=0; y<DIM; ++y)
			for (int x=0; x<DIM; ++x) {
				notUsedRow[y][x]=true;
				notUsedCol[y][x]=true;
				notUsedBox[y][x]=true;
			}
	}
	@Override
	public void reset() {
		if (doInit) clear();
		else {
			super.reset();
			for (int y=0; y<DIM; ++y)
				for (int x=0; x<DIM; ++x) {
					notUsedRow[y][x]=true;
					notUsedCol[y][x]=true;
					notUsedBox[y][x]=true;
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
		notUsedRow[y][v]=false;
		notUsedCol[x][v]=false;
		notUsedBox[getbox(x,y)][v]=false;
	}
	@Override
	protected void resetUsed(int x,int y) {
		int v=a[y][x]-1;
		a[y][x]=0;
		notUsedRow[y][v]=true;
		notUsedCol[x][v]=true;
		notUsedBox[getbox(x,y)][v]=true;
	}
	@Override
	protected boolean setCheck(int x,int y,int v) {
		return notUsedRow[y][v] && notUsedCol[x][v] && notUsedBox[getbox(x,y)][v];
	}

	//order of visiting fields
	@Override
	protected int pmap(int mp) {
		//return DIM*DIM-mp-1; //280.677 sec
		return mp; //134.139 sec
	}
}
