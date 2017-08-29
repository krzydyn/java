/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package algebra;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Permutation {
	private final List<Object> set;
	private int[] pos;

	public Permutation(List<?> l) {
		pos = new int[l.size()];
		set=new ArrayList<Object>(l);
		for (int i=0; i < pos.length; ++i) pos[i]=i;
	}
	public void save(OutputStream stream) throws IOException {
		DataOutput s = new DataOutputStream(stream);
		s.writeInt(pos.length);
		for (int i = 0; i < pos.length; ++i)
			s.writeInt(pos[i]);
	}
	public void load(InputStream stream) throws IOException {
		DataInput s = new DataInputStream(stream);
		pos = new int[s.readInt()];
		for (int i = 0; i < pos.length; ++i)
			pos[i] = s.readInt();
	}


	static private void swap(int a[], int s,int e) {
		int temp = a[s];
		a[s] = a[e];
		a[e] = temp;
	}
	static private void reverse(int a[], int s,int e) {
		while (s<e) {
			int temp = a[s];
			a[s] = a[e];
			a[e] = temp;
			++s; --e;
		}
	}
	public boolean next() {
		int i = pos.length - 2;
		while (i >= 0 && pos[i] >= pos[i+1]) --i;
		if (i < 0) return false;

		int j = pos.length - 1;
		while (pos[i] >= pos[j]) --j;

		swap(pos,i,j);
		reverse(pos, i+1,pos.length);
		return true;
	}

	public void getSelection(List<?> l) {
		@SuppressWarnings("unchecked")
		List<Object> ll = (List<Object>)l;
		for (int i=0; i < pos.length; ++i)
			ll.set(i, set.get(pos[i]));
	}

	static public <T extends Comparable<T>> boolean nextPermutation(List<T> a) {
		Comparator<T> cmp = new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				//if (o1 == o2) return 0;
				return o1.compareTo(o2);
			}
		};

		return nextPermutation(a, cmp);
	}

	static public <T extends Object> boolean nextPermutation(List<T> a, Comparator<T> cmp) {
		int i = a.size() - 2;
		while (i >= 0 && cmp.compare(a.get(i),a.get(i + 1)) >= 0) --i;
		if (i < 0) return false;

		int j = a.size() - 1;
		while (cmp.compare(a.get(i),a.get(j)) >= 0) --j;

		Collections.swap(a, i, j);
		Collections.reverse(a.subList(i + 1, a.size()));
		return true;
	}
}
