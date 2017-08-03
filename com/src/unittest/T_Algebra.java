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

import graphs.BinTree;
import graphs.HeapTree;
import graphs.SortedArray;
import graphs.Sorting;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import puzzles.GameBoard.Rect;
import puzzles.GameBoard.Sheet;
import algebra.Combinations;
import algebra.Expression;
import algebra.Maths;
import algebra.MatrixI;
import algebra.Permutate;
import algebra.Tools2D;
import sys.Log;
import sys.UnitTest;
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

	static void nCr() {
		List<Long> list = new ArrayList<Long>(50);
		for (int n=0; n < 60; ++n) {
			list.add(1L);
			for (int k=0; k <= n; ++k) {
				long r=Combinations.newton(n, k);
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
		SortedArray<Integer> a = new SortedArray<Integer>();
		for (int i=0; i < inp.length; ++i) {
			a.add(inp[i]);
		}
		System.out.println(a.toString());
	}
	static void bintree() {
		BinTree<Integer> tree = new BinTree<Integer>();
		Random rnd=new Random(2);
		for (int i=0; i < 10; ) {
			int x = rnd.nextInt(50);
			if (!tree.add(x)) continue;
			System.out.printf("add(%d): %s\n",x,tree.toString());
			++i;
		}

		System.out.println();
		while (tree.size() > 0) {
			tree.removeRoot();
			System.out.println(tree.toString());
		}
	}

	private static void _printStat(String l) {
		Log.prn("%s: ops %d mem %d/%d",l,Sorting.opCnt,Sorting.rdCnt,Sorting.wrCnt);
	}
	static void quickersort() {
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

	static double input_pnts[] = {
		0.3215348546593775,0.03629583077160248,
		0.02402358131857918,-0.2356728797179394,
		0.04590851212470659,-0.4156409924995536,
		0.3218384001607433,0.1379850698988746,
		0.11506479756447,-0.1059521474930943,
		0.2622539999543261,-0.29702873322836,
		-0.161920957418085,-0.4055339716426413,
		0.1905378631228002,0.3698601009043493,
		0.2387090918968516,-0.01629827079949742,
		0.07495888748668034,-0.1659825110491202,
		0.3319341836794598,-0.1821814101954749,
		0.07703635755650362,-0.2499430638271785,
		0.2069242999022122,-0.2232970760420869,
		0.04604079532068295,-0.1923573186549892,
		0.05054295812784038,0.4754929463150845,
		-0.3900589168910486,0.2797829520700341,
		0.3120693385713448,-0.0506329867529059,
		0.01138812723698857,0.4002504701728471,
		0.009645149586391732,0.1060251100976254,
		-0.03597933197019559,0.2953639456959105,
		0.1818290866742182,0.001454397571696298,
		0.444056063372694,0.2502497166863175,
		-0.05301752458607545,-0.06553921621808712,
		0.4823896228171788,-0.4776170002088109,
		-0.3089226845734964,-0.06356112199235814,
		-0.271780741188471,0.1810810595574612,
		0.4293626522918815,0.2980897964891882,
		-0.004796652127799228,0.382663812844701,
		0.430695573269106,-0.2995073500084759,
		0.1799668387323309,-0.2973467472915973,
		0.4932166845474547,0.4928094162538735,
		-0.3521487911717489,0.4352656197131292,
		-0.4907368011686362,0.1865826865533206,
		-0.1047924716070224,-0.247073392148198,
		0.4374961861758457,-0.001606279519951237,
		0.003256207800708899,-0.2729194320486108,
		0.04310378203457577,0.4452604050238248,
		0.4916198379282093,-0.345391701297268,
		0.001675087028811806,0.1531837672490476,
		-0.4404289572876217,-0.2894855991839297,
	};
	static double expected_pnts[] = {
		-0.161920957418085,-0.4055339716426413,
		0.05054295812784038,0.4754929463150845,
		0.4823896228171788,-0.4776170002088109,
		0.4932166845474547,0.4928094162538735,
		-0.3521487911717489,0.4352656197131292,
		-0.4907368011686362,0.1865826865533206,
		0.4916198379282093,-0.345391701297268,
		-0.4404289572876217,-0.2894855991839297,
	};
	static void convex_hull() {
		List<Point2D> pnts = new ArrayList<Point2D>();
		for (int i=0; i < input_pnts.length; i+=2)
			pnts.add(new Point2D.Double(input_pnts[i],input_pnts[i+1]));
		List<Point2D> h = Tools2D.hullAndrew(pnts);
		Log.debug(Text.join("\n", h));
	}
}
