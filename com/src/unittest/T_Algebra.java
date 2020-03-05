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

import graphs.HeapTree;
import img.Tools2D;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import puzzles.BlackJack;
import puzzles.GameBoard.Rect;
import puzzles.GameBoard.Sheet;
import algebra.CircularList;
import algebra.Combination;
import algebra.Expression;
import algebra.Maths;
import algebra.MatrixI;
import algebra.Permutation;
import algebra.SortedArray;
import algebra.Sorting;
import sys.Log;
import text.Text;

public class T_Algebra extends UnitTest {
	static final int sortN = 100000;
	static void marix() {
		MatrixI m1 = new MatrixI(3, 1,0,2, -1,3,1);
		MatrixI m2 = new MatrixI(2, 3,1, 2,1, 1,0);
		MatrixI r = m1.mul(m2);
		//System.out.println(r.toString());
		check("m1.mul(m2)", r.equals(new MatrixI(2, 5,1, 4,2)));
		r = m2.mul(m1);
		check("m2.mul(m1)", r.equals(new MatrixI(3, 2,3,7, 1,3,5, 1,0,2)));
		//System.out.println(r.toString());
	}
	static void permutation() {
		List<Character> l = new ArrayList<>();
		l.add('a');
		l.add('b');
		l.add('b');
		l.add('c');
		int n=0;
		do {
			++n;
			//Log.debug("%d: %s", n, l.toString());
		} while (Permutation.nextPermutation(l));
		check("permutations", n, 12);

		l.clear(); n=0;
		for (int i=0; i<6; ++i) l.add((char)i);
		do {
			++n;
			//Log.debug("%d: %s", n, l.toString());
		} while (Permutation.nextPermutation(l));
		check("should be 720 permutations", n == 720);
	}

	static void combinatory() {
		List<Character> l = new ArrayList<>();
		for (int i=0; i < 5; ++i) l.add((char)('a'+i));
		Combination comb = new Combination(l,3, false);
		int n=0;
		do {
			++n;
			comb.getSelection(l);
			//Log.debug("%d: %s",n,Text.join(",", l));
		} while (comb.next());
		check("comb(5,3)", 10, n);

		comb.reset(true);
		n=0;
		do {
			++n;
			comb.getSelection(l);
			//Log.debug("%d: %s",n,Text.join(",", l));
		} while (comb.next());
		check("comb(10,3)", 125, n);
	}

	static void nCr() {
		List<Long> list = new ArrayList<>(50);
		for (int n=0; n < 60; ++n) {
			list.add(1L);
			for (int k=0; k <= n; ++k) {
				long r=Combination.newton(n, k);
				long er = list.get(k);
				check(String.format("nCr(%d,%d)",n,k), er, r);
			}
			long er = list.get(0);
			for (int k=1; k <= n; ++k) {
				long p=list.get(k);
				list.set(k, er+p);
				er=p;
			}
		}
	}

	static void gcd() {
		long t0;
		long[] a = new long[10000];
		t0 = System.currentTimeMillis();
		for (int i=0; i < 10000; ++i) {
			a[i]=Maths.gcd_simple(10007+i, 119+i*i);
		}
		t0 = System.currentTimeMillis();
		for (int i=0; i < 10000; ++i) {
			check("gcd2",a[i],Maths.gcd(10007+i, 119+i*i));
		}
		Log.info("gcd2 : %d", System.currentTimeMillis()-t0);
		t0 = System.currentTimeMillis();
		for (int i=0; i < 10000; ++i) {
			a[i]=Maths.gcd_simple(10007+i, 119+i*i);
		}
		Log.info("gcd : %d", System.currentTimeMillis()-t0);
		/*t0 = System.currentTimeMillis();
		for (int i=0; i < 10000; ++i) {
			check("gcd3",a[i],Maths.gcd3(10007+i, 119+i*i));
		}
		Log.info("gcd3 : %d", System.currentTimeMillis()-t0);*/
	}

	static void blackjack() {
		String[] test = {"", "AA", "XA", "9AA", "8AA", "8AAA"};
		int [] res    = {0,   12,   21,    21,    20,     21};
		for (int i=0; i < test.length; ++i) {
			int r=BlackJack.getPoints(test[i]);
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
		Log.info("heap warmup");
		Random rnd=new Random(100);
		HeapTree<Integer> tree=new HeapTree<>(sortN);
		for (int i=0; i < 20; ++i)
			tree.add(rnd.nextInt(10*sortN));
		tree.sort();
	}
	static void heap3() {
		Random rnd=new Random(100);
		HeapTree<Integer> tree=new HeapTree<>(sortN);
		for (int i=0; i < sortN; ++i)
			tree.add(rnd.nextInt(10*sortN));

		List<Integer> l=tree.sort();
		check("size",l.size(),sortN);
	}
	static void heap2() {
		Random rnd=new Random(100);
		List<Integer> list = new ArrayList<>(sortN);
		for (int i=0; i < sortN; ++i)
			list.add(rnd.nextInt(10*sortN));

		Sorting.heapSort(list);
		check("size",list.size(),sortN);
	}
	static void sortedArray() {
		int[] inp = {4,2,6,1,3,5,7};
		SortedArray<Integer> a = new SortedArray<>();
		for (int i=0; i < inp.length; ++i) {
			a.add(inp[i]);
		}
		System.out.println(a.toString());
	}

	private static void _printStat(String l) {
		Log.prn("%s: cmp %d   rd/wr %d/%d",l,Sorting.opCnt,Sorting.rdCnt,Sorting.wrCnt);
	}
	static void _sortalgos(int[] unsorted, int[] verific) {
		int[] a = new int[unsorted.length];

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.quickSort(a);
		_printStat("quickSort");
		check(a,verific,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.selectionSort(a);
		_printStat("selectionSort");
		check(a,verific,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.insertionSort(a);
		_printStat("insertionSort");
		check(a,verific,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.comboSort(a);
		_printStat("comboSort");
		check(a,verific,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.shellSort(a);
		_printStat("shellSort");
		check(a,verific,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.heapSort(a);
		_printStat("heapSort");
		check(a,verific,0);

		System.arraycopy(unsorted, 0, a, 0, a.length);
		Sorting.treeSort(a);
		_printStat("bintreeSort");
		check(a,verific,0);
	}

	static void sortalgos() {
		Random rnd = new Random(10);
		int[] verif = {1,1,2,2,3,3,5,5,7,7,9,9};

		Log.prn(">>> sorted");
		int[] t1 = {1,1,2,2,3,3,5,5,7,7,9,9};
		_sortalgos(t1, verif);

		Log.prn(">>> rev-sorted");
		int[] t2 = {9,9,7,7,5,5,3,3,2,2,1,1};
		_sortalgos(t2, verif);

		int[] t3 = new int[10000];
		for (int i=0; i < t3.length; ++i)
			t3[i] = rnd.nextInt(t3.length)+1;
		int[] verif3 = Arrays.copyOf(t3,t3.length);
		Arrays.sort(verif3);
		Log.prn(">>> random");
		_sortalgos(t3, verif3);
	}

	static void testCircularList() {
		List<Integer> list = new CircularList<>();
		for (int i=0; i < 3; ++i) list.add(i+1);
		Log.debug(list.toString());
		list = new CircularList<>();
		for (int i=0; i < 3; ++i) list.add(0, i+1);
		Log.debug(list.toString());
	}

	static double input_pnts[] = {
		0,3,  1,1,  2,2,  4,4,  0,0,  1,2,  3,1,  3,3
	};
	static void convex_hull() {
		List<Point2D> pnts = new ArrayList<>();
		for (int i=0; i < input_pnts.length; i+=2)
			pnts.add(new Point2D.Double(input_pnts[i],input_pnts[i+1]));
		List<Point2D> ha = Tools2D.hullAndrew(pnts);
		Log.debug("Andrew Hull: %s", Text.join("\n", ha));
		List<Point2D> hg = Tools2D.hullGraham(pnts);
		Log.debug("Graham Hull: %s", Text.join("\n", hg));
		List<Point2D> hq = Tools2D.hullQuick(pnts);
		Log.debug("Quick Hull: %s", Text.join("\n", hq));
	}
}
