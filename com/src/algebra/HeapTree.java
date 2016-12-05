package algebra;

import java.util.ArrayList;
import java.util.List;

public class HeapTree<T extends Comparable<T>> {
	final List<T> heap;

	public HeapTree() {
		this(0);
	}
	public HeapTree(int capa) {
		if (capa<=0) heap = new ArrayList<>();
		else heap = new ArrayList<>(capa);
	}

	private void swap(int i, int j) {
		T t = heap.get(i);
		heap.set(i, heap.get(j));
		heap.set(j, t);
	}
	private int parent(int i) {return (i-1)/2;}
	private int left(int i) {return 2*i+1;}
	//private int right(int i) {return 2*i+2;}

	private void moveDown(int first, int last) {
		int l = left(first);
		while (l <= last) {
			if (l < last && heap.get(l).compareTo(heap.get(l+1)) < 0) ++l;
			if (heap.get(first).compareTo(heap.get(l)) < 0) {
				swap(first, l);
				first = l;
				l = left(first);
			}
			else {
				l = last+1;
			}
		}
	}

	public void add(T o) {
		heap.add(o);
		int p, v = heap.size();
		while (v>0 && heap.get(p=parent(v)).compareTo(heap.get(v)) < 0) {
			swap(p,v);
			v=p;
		}
	}
	//public void remove(int i) {}
	public T removeRoot() {
		if (heap.size() == 0) return null;
		if (heap.size() == 1) return heap.remove(0);
		T t = heap.get(0);
		heap.set(0, heap.remove(heap.size()-1));
		int v,p=0;
		while ((v=left(p)) < heap.size()) {
			int x = heap.get(v+1).compareTo(heap.get(v)) < 0 ? v : v+1;
			if (heap.get(p).compareTo(heap.get(x)) < 0) {
				swap(p,x);
				p = x+1;
			}
			else break;
		}
		return t;
	}
	public T root() {
		return heap.get(0);
	}
}
