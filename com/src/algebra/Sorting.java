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

import graphs.BinTree;
import graphs.HeapTree;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import sys.PrimitiveArray;

public class Sorting {
	public static int opCnt=0; //number of comparison operations
	public static int rdCnt=0; //number of data read operations
	public static int wrCnt=0; //number of data write operations

	public static final void swap(int[] a, int i, int j) {
		int t=a[i]; a[i]=a[j]; a[j]=t;
		rdCnt+=2;
		wrCnt+=2;
	}
	public static final void swap(List<?> a, int i, int j) {
		@SuppressWarnings("unchecked")
		final List<Object> l = (List<Object>)a;
		Object t=a.get(i); l.set(i, l.get(j)); l.set(j, t);
		rdCnt+=2;
		wrCnt+=2;
	}

	private static int partition(int[] a, int l,int r) {
		int pivot = a[r]; //pivot element
		int i = l-1;
		// elements smaller then pivot put on the left
		for (int j = l; j < r; ++j) {
			if (a[j] < pivot) {
				++i;
				swap(a,i,j);
			}
		}
		swap(a,i+1,r);
		return i+1;  // return index of pivot element, now it is in the right place
	}
	private static void quickSort(int[] a, int l,int r) {
		if (l < r) {
			int pi = partition(a, l, r);
			quickSort(a, l, pi-1);
			quickSort(a, pi+1, r);
		}
	}

	public static void quickSort(int[] a) {
		opCnt=0;rdCnt=0;wrCnt=0;
		quickSort(a,0,a.length-1);
	}

	public static void selectionSort(int[] a) {
		opCnt=0;rdCnt=0;wrCnt=0;
		for (int i=0; i<a.length; ++i) {
			int e=a[i]; ++rdCnt;
			int iv=i;
			for (int j=i+1; j<a.length; ++j) {
				int x=a[j]; ++rdCnt;
				++opCnt;
				if (x < e) { e=x; iv=j; }
			}
			if (iv!=i) swap(a,i,iv);
		}
	}

	public static void insertionSort(int[] a) {
		opCnt=0;rdCnt=0;wrCnt=0;
		for (int n=1; n < a.length; ++n) {
			int i,v = a[n]; ++rdCnt;
			for (i=n; i > 0; --i) {
				++opCnt;
				if (v >= a[i-1]) break;
				a[i] = a[i-1];
				++wrCnt;++rdCnt;
			}
			if (i != n) {
				a[i]=v;++wrCnt;
			}
		}
	}

	static public void comboSort(int[] a) {
		opCnt=0;rdCnt=0;wrCnt=0;
		int gap = a.length;
		boolean swapped=false;
		while (gap > 1 || swapped) {
			gap = gap * 10 / 13; //empirical
			if (gap==0) gap=1;
			else if (gap==9||gap==10) gap=11;

			swapped = false;
			for (int i = 0; i + gap < a.length; ++i) {
				++opCnt;
				if (a[i] > a[i + gap]) {
					swap(a,i,i+gap);
					swapped = true;
				}
			}
		}
	}

	static public <T extends Object> void comboSort(List<T> a, Comparator<T> cmp) {
		opCnt=0;rdCnt=0;wrCnt=0;
		int gap = a.size();
		boolean swapped=false;
		while (gap > 1 || swapped) {
			gap = gap * 10 / 13; //empirical
			if (gap==0) gap=1;
			//else if (gap==9||gap==10) gap=11;

			swapped = false;
			for (int i = 0; i + gap < a.size(); ++i) {
				++opCnt;
				if (cmp.compare(a.get(i), a.get(i + gap)) > 0) {
					swap(a, i, i + gap);
					swapped = true;
				}
			}
		}
	}

	static private void insertionSort(int[] a, int gap) {
		for (int i = gap; i < a.length; ++i) {
			int temp = a[i]; ++rdCnt;
			int j = i;
			while (j >= gap && temp < a[j-gap]) {
				++opCnt; ++wrCnt; ++rdCnt;
				a[j] = a[j-gap];
				j -= gap;

			}
			++opCnt; ++rdCnt;
			if (i!=j) {
				a[j] = temp;
				++wrCnt;
			}
		}
	}
	// Shell sort is generalization of insertion sort algorithm
	// time complexity unknown (depends on gap sequence)
	public static void shellSort(int[] a) {
		final int gaps[] = {1, 4, 10, 23, 57, 132, 301, 701};
		opCnt=0;rdCnt=0;wrCnt=0;
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

	static public void heapSort(int... a) {
		opCnt=0;rdCnt=0;wrCnt=0;
		HeapTree<Integer> h = new HeapTree<Integer>(PrimitiveArray.asList(a));
		h.sort();
	}
	static public <T extends Comparable<T>> void heapSort(List<T> a) {
		opCnt=0;rdCnt=0;wrCnt=0;
		HeapTree<T> h = new HeapTree<T>(a);
		h.sort();
	}

	static public void treeSort(int... a) {
		opCnt=0;rdCnt=0;wrCnt=0;
		BinTree<Integer> h = new BinTree<Integer>(true);
		for (int i=0; i < a.length; ++i)
			h.add(a[i]);
		for (int i=0; i < a.length; ++i) a[i]=-1;
		Iterator<Integer> it = h.iterator();
		for (int i=0; it.hasNext(); ++i) {
			a[i] = it.next();
		}
	}
}
