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
package graphs;

import java.util.ArrayList;
import java.util.List;

import algebra.Sorting;

public class HeapTree<T extends Comparable<T>> {
	private final List<T> tree;
	private final boolean lazyBuild=true;
	private boolean builded=false;

	public HeapTree(final List<T> a) {
		tree=a;
		if (!lazyBuild) buildHeap();
	}
	public HeapTree() {
		this(0);
	}
	public HeapTree(int capa) {
		if (capa<=0) tree = new ArrayList<T>();
		else tree = new ArrayList<T>(capa);
		builded=true;
	}
	public int size() { return tree.size(); }

	private void swap(int i, int j) {
		T t = tree.get(i);
		tree.set(i, tree.get(j));
		tree.set(j, t);
		Sorting.rdCnt+=2;
		Sorting.wrCnt+=2;
	}
	private final int parent(int i) {return (i-1)/2;}
	private final int left(int i) {return 2*i+1;}
	//private int right(int i) {return 2*i+2;}

	private void buildHeap() {
		if (builded) return ;
		for (int i=1; i<tree.size(); ++i)
			moveUp(i);
		builded = true;
	}

	private void moveUp(int v) {
		int p;
		Sorting.rdCnt+=2;
		++Sorting.opCnt;
		while (v>0 && tree.get(p=parent(v)).compareTo(tree.get(v)) < 0) {
			++Sorting.opCnt;
			swap(p,v);
			v=p;
		}
	}
	private void moveDown(int p, int s) {
		int v;
		while ((v = left(p)) < s) {
			if (v+1 < s) { // choose bigger child if there are two
				++Sorting.opCnt; Sorting.rdCnt+=2;
				if (tree.get(v).compareTo(tree.get(v+1)) < 0) ++v;
			}

			++Sorting.opCnt;
			if (tree.get(p).compareTo(tree.get(v)) < 0) {
				swap(p, v);
				p = v;
			}
			else break;
		}
	}

	public void add(T o) {
		tree.add(o);
		if (builded) moveUp(tree.size()-1);
		//builded=false;
	}

	public T remove(int i) {
		buildHeap();
		int s = tree.size();
		if (i < 0 || i >= s) throw new ArrayIndexOutOfBoundsException(i);
		if (s <= 2) return tree.remove(i);
		--s;
		if (i < s) {
			swap(i, s);
			moveDown(i, s);
		}
		return tree.remove(s);
	}
	public T root() {
		int s = tree.size();
		if (s == 0) return null;
		return remove(0);
	}
	public T top() {
		buildHeap();
		return tree.get(0);
	}
	public List<T> sort() {
		buildHeap();
		for (int i=tree.size()-1; i>0; --i) {
			swap(0, i);     //put max on the end
			moveDown(0, i); //fix heap tree from 0 to i-1
		}
		return tree;
	}
	private String toString(int s) {
		StringBuilder st=new StringBuilder((tree.size()+1)*2);
		int nl=1,c=1,sum=1;
		for (int i=0; i < s; ++i) {
			st.append(tree.get(i).toString());
			st.append(" ");
			if (sum==c) {
				st.append("\n");
				nl*=2;
				sum+=nl;
			}
			++c;
		}
		return st.toString();
	}
	@Override
	public String toString() {
		buildHeap();
		return toString(tree.size());
	}
}
