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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Permutate {
	final int ord[];
	final List<Object> list;
	public Permutate(List<Object> l) {
		ord = new int[l.size()];
		list=new ArrayList<Object>(l);
		for (int i=0; i < l.size(); ++i) ord[i]=i;
	}

	static private void swap(int a[], int s,int e) {
	    int temp = a[s];
	    a[s] = a[e];
	    a[e] = temp;
	}
	static private void reverse(int a[], int s,int e) {
		while (s<e)
		{
		    int temp = a[s];
		    a[s] = a[e];
		    a[e] = temp;
		    ++s; --e;
		}
	}

	public boolean next(List<Object> l) {
		int i = ord.length - 2;
		while (i >= 0 && ord[i] >= ord[i+1]) --i;
		if (i < 0) return false;

        int j = ord.length - 1;
        while (ord[i] >= ord[j]) --j;

        swap(ord,i,j);
        reverse(ord, i+1,ord.length);
        for (i=0; i < ord.length; ++i)
        	l.set(i, list.get(ord[i]));
		return true;
	}

	static public <T extends Comparable<T>> boolean nextPermutation(List<T> a) {
        int i = a.size() - 2;
        while (i >= 0 && a.get(i).compareTo(a.get(i + 1)) >= 0) --i;
        if (i < 0) return false;

        int j = a.size() - 1;
        while (a.get(i).compareTo(a.get(j)) >= 0) --j;

        Collections.swap(a, i, j);
        Collections.reverse(a.subList(i + 1, a.size()));
        return true;
	}

	static public <T extends Object> boolean nextPermutation(List<T> a, Comparator<T> cmp) {
		int i = a.size() - 2;

        while (i >= 0 && cmp.compare(a.get(i),a.get(i + 1)) >= 0) i--;

        if (i < 0) return false;

        int j = a.size() - 1;
        while (cmp.compare(a.get(i),a.get(j)) >= 0) j--;

        Collections.swap(a, i, j);
        Collections.reverse(a.subList(i + 1, a.size()));
        return true;
	}
}
