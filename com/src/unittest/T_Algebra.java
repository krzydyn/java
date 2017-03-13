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
package unittest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import puzzles.GameBoard.Rect;
import puzzles.GameBoard.Sheet;
import algebra.Combinations;
import algebra.Expression;
import algebra.HeapTree;
import algebra.Maths;
import algebra.MatrixI;
import algebra.Permutate;
import algebra.Sorting;
import sys.Log;
import sys.UnitTest;

public class T_Algebra extends UnitTest {
	static final int sortN = 100000;
	static void marix() {
		MatrixI m1 = new MatrixI(3, 2, new int[]{1,0,2, -1,3,1});
		MatrixI m2 = new MatrixI(2, 3, new int[]{3,1, 2,1, 1,0});
		MatrixI r = m1.mul(m2);
		//System.out.println(r.toString());
		check("m1.mul(m2)", r.equals(new MatrixI("5 1|4 2")));
		r = m2.mul(m1);
		check("m2.mul(m1)", r.equals(new MatrixI("2 3 7|1 3 5|1 0 2")));
		//System.out.println(r.toString());
	}
	static void permutation() {
		List<Integer> l = new ArrayList<Integer>();
		l.add(2);
		l.add(4);
		l.add(7);
		int n=0;
		do {
			++n;
			//Log.debug("%d: %s", n, l.toString());
		} while (Permutate.nextPermutation(l));
		check("should be 6 permutations", n == 6);

		l.clear(); n=0;
		for (int i=0; i<6; ++i) l.add(i);
		do {
			++n;
			//Log.debug("%d: %s", n, l.toString());
		} while (Permutate.nextPermutation(l));
		check("should be 720 permutations", n == 720);
	}

	static void combinatory() {
		List<Character> l = new ArrayList<Character>();
		for (int i=0; i < 5; ++i) l.add((char)('a'+i));
		Combinations comb = new Combinations(l,2);
		int n=0;
		do {
			++n;
			comb.getSelection(l);
			Log.debug("%d: %s", n, l.toString());
		} while (comb.next());
		Log.debug("num = %d", n);
	}

	static void blackjack() {
		String[] test = {"", "AA", "XA", "9AA", "8AA", "8AAA"};
		int [] res    = {0,   12,   21,    21,    20,     21};
		for (int i=0; i < test.length; ++i) {
			int r=Maths.blackjackPoints(test[i]);
			check(String.format("%s=%d, should be %d", test[i], r, res[i]), r==res[i]);
		}
	}

	static void expression() {
		Expression e = new Expression("9/3");
		long r=e.evaluate();
		check(String.format("9/3 = %d != 3",r), r==3);
		e = new Expression("1+(1+2)*2");
		Log.prn("%s",e.toString());
		r=e.evaluate();
		check(String.format("1+(1+2)*2 = %d != 7",r), r==7);
		e = new Expression("1==2");
		r=e.evaluate();
		check(String.format("1==2 = %d != 0",r), r==0);
	}

	static void rectinrect() {
		Rect r1 = new Rect(3, 7, new Sheet(3,4));
		Rect r2 = new Rect(0, 8, new Sheet(4,3));
		check("r1 . r2", r1.intersects(r2) == true);
		check("r2 . r1", r2.intersects(r1) == true);
	}
	static void heap1() {
		Log.info("loader");
	}
	static void heap2() {
		Random rnd=new Random(100);
		HeapTree<Integer> tree=new HeapTree<>(sortN);
		for (int i=0; i < sortN; ++i)
			tree.add(rnd.nextInt(10*sortN));

		Log.info("sorting...");
		tree.sort();
	}
	static void heapsort() {
		Random rnd=new Random(100);
		List<Integer> list = new ArrayList<>(sortN);
		for (int i=0; i < sortN; ++i)
			list.add(rnd.nextInt(10*sortN));

		Log.info("sorting...");
		HeapTree.sort(list);
	}
	private static void _printStat(String l) {
		Log.prn("%s: ops %d mem %d/%d",l,Sorting.opCnt,Sorting.rdCnt,Sorting.wrCnt);
	}
	static void quicksort() {
		int[] unsorted = {9,9,7,7,5,5,3,3,2,2,1,1};
		int[] sorted = {1,1,2,2,3,3,5,5,7,7,9,9};
		int[] a = new int[unsorted.length];

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.quickSort(a);
		_printStat("quickSort");
		check(a,sorted,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.selectionSort(a);
		_printStat("selectionSort");
		check(a,sorted,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.insertionSort(a);
		_printStat("insertionSort");
		check(a,sorted,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.comboSort(a);
		_printStat("comboSort");
		check(a,sorted,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.shellSort(a);
		_printStat("shellSort");
		check(a,sorted,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.heapSort(a);
		_printStat("heapSort");
		check(a,sorted,0);
	}
}
