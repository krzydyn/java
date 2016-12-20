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

public class Combinatory {
	public static void selectionSort(int[] ar) {
        for (int i=0; i<ar.length; ++i) {
            int e=ar[i];
            int iv=i;
            for (int j=i+1; j<ar.length; ++j) {
                int x=ar[j];
                if (x < e) { e=x; iv=j; }
            }
            if (iv!=i) { e=ar[i]; ar[i]=ar[iv]; ar[iv]=e; }
        }
    }

    public static void insertionSort(int[] ar) {
        for (int n=1; n < ar.length; ++n) {
            int i,v = ar[n];
            for (i=n; i > 0; --i) {
                if (v >= ar[i-1]) break;
                ar[i] = ar[i-1];
            }
            ar[i]=v;
        }
    }
	static public <T extends Object> void comboSort(List<T> a, Comparator<T> cmp) {
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
		           }
		      }
		   }
	}

	static public <T extends Comparable<T>> void heapSort(List<T> a) {
		HeapTree.sort(a);
	}
}
