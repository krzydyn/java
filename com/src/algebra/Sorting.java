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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sorting {
	public static int opCnt=0;
	private static void quickSort(int[] a, int l,int r) {
		int e = a[(l+r)/2]; //pivot element
		int i=l,j=r;
		do {
			while (a[i] < e) ++i;
			while (e < a[j]) --j;
			if (i <= j) {
				if (i!=j) {
					int x=a[i]; a[i]=a[j]; a[j]=x;
					++opCnt; ++opCnt;
				}
				++i; --j;
			}
		} while (i<=j);
		if (l < j) quickSort(a, l, j);
		if (i < r) quickSort(a, i, r);
	}

	public static void quickSort(int[] a) {
		opCnt=0;
		quickSort(a,0,a.length-1);
	}

	public static void selectionSort(int[] a) {
		opCnt=0;
		for (int i=0; i<a.length; ++i) {
			int e=a[i];
			int iv=i;
			for (int j=i+1; j<a.length; ++j) {
				int x=a[j];
				if (x < e) { e=x; iv=j; }
			}
			if (iv!=i) {
				e=a[i]; a[i]=a[iv]; a[iv]=e;
				++opCnt; ++opCnt;
			}
		}
	}

	public static void insertionSort(int[] a) {
		opCnt=0;
		for (int n=1; n < a.length; ++n) {
			int i,v = a[n];
			for (i=n; i > 0; --i) {
				if (v >= a[i-1]) break;
				a[i] = a[i-1];
				++opCnt;
			}
			if (i != n) {
				a[i]=v;
				++opCnt;
			}
		}
	}

	static public void comboSort(int[] a) {
		opCnt=0;
		int gap = a.length;
		boolean swapped=false;
		while (gap > 1 || swapped) {
			gap = gap * 10 / 13; //empirical
			if (gap==0) gap=1;
			else if (gap==9||gap==10) gap=11;

			swapped = false;
			for (int i = 0; i + gap < a.length; ++i) {
				if (a[i] > a[i + gap]) {
					int t = a[i];
					a[i] = a[i+gap];
					a[i+gap] = t;
					swapped = true;
					++opCnt; ++opCnt;
				}
			}
		}
	}

	static public <T extends Object> void comboSort(List<T> a, Comparator<T> cmp) {
		opCnt=0;
		int gap = a.size();
		boolean swapped=false;
		while (gap > 1 || swapped) {
			gap = gap * 10 / 13; //empirical
			if (gap==0) gap=1;
			else if (gap==9||gap==10) gap=11;

			swapped = false;
			for (int i = 0; i + gap < a.size(); ++i) {
				if (cmp.compare(a.get(i), a.get(i + gap)) > 0) {
					Collections.swap(a, i, i + gap);
					swapped = true;
					++opCnt; ++opCnt;
				}
			}
		}
	}

	static private void insertionSort(int[] a, int gap) {
		for (int i = gap; i < a.length; ++i) {
			int temp = a[i];
			int j = i;
			while (j >= gap && temp < a[j-gap]) {
				a[j] = a[j-gap];
				j -= gap;
				++opCnt;
			}
			if (i!=j) {
				a[j] = temp;
				++opCnt;
			}
		}
	}
	// Shell sort is generalization of insertion sort algorithm
	// time complexity unknown (depends on gap sequence)
	public static void shellSort(int[] a) {
		final int gaps[] = {1, 4, 10, 23, 57, 132, 301, 701};
		opCnt=0;
		int gap = a.length;
		int gapi=-1;
		while (gap > 1) {
			if (gapi < 0) {
				if (2*gap > gaps[gaps.length-1]) {
					gap = 100*gap/225;
				}
				else {
					gapi=gaps.length-1;
					while (gap < gaps[gapi]) --gapi;
					if (gapi>0) --gapi;
					gap = gaps[gapi];
				}
			}
			else gap = gaps[--gapi];
			if (gap<=0) gap=1;
			insertionSort(a, gap);
		}
	}

	static public <T extends Comparable<T>> void heapSort(List<T> a) {
		HeapTree.sort(a);
	}
}
