package algebra;

import java.util.ArrayList;
import java.util.List;

import sys.Log;

public class HeapTree<T extends Comparable<T>> {
	private final List<T> heap;
	private final boolean lazyBuild=true;
	private boolean builded=false;

	private HeapTree(List<T> a) {
		heap=a;
		//buildHeap();
	}
	public HeapTree() {
		this(0);
	}
	public HeapTree(int capa) {
		if (capa<=0) heap = new ArrayList<T>();
		else heap = new ArrayList<T>(capa);
		if (!lazyBuild) builded=true;
	}

	private void swap(int i, int j) {
		T t = heap.get(i);
		heap.set(i, heap.get(j));
		heap.set(j, t);
	}
	private int parent(int i) {return (i-1)/2;}
	private int left(int i) {return 2*i+1;}
	//private int right(int i) {return 2*i+2;}

	private void buildHeap() {
		if (builded) return ;
		for (int i=1; i<heap.size(); ++i)
			moveUp(i);
		builded = true;
	}

	private void moveUp(int v) {
		int p;
		while (v>0 && heap.get(p=parent(v)).compareTo(heap.get(v)) < 0) {
			swap(p,v);
			v=p;
		}
	}
	private void moveDown(int p, int s) {
		int v;
		while ((v = left(p)) < s) {
			if (v+1 < s && heap.get(v).compareTo(heap.get(v+1)) < 0)
				++v;
			if (heap.get(p).compareTo(heap.get(v)) < 0) {
				swap(p, v);
				p = v;
			}
			else break;
		}
	}

	public void add(T o) {
		heap.add(o);
		if (builded) moveUp(heap.size()-1);
	}

	public T remove(int i) {
		buildHeap();
		int s = heap.size();
		if (i < 0 || i >= s) throw new ArrayIndexOutOfBoundsException(i);
		if (s <= 2) return heap.remove(i);
		--s;
		if (i < s) {
			swap(i, s);
			moveDown(i, s);
		}
		return heap.remove(s);
	}
	public T root() {
		buildHeap();
		int s = heap.size();
		if (s == 0) return null;
		if (s <= 2) return heap.remove(0);
		--s;
		swap(0, s);            // put max on the end
		moveDown(0, s);        // fix heap tree
		return heap.remove(s); // remove & return last object
	}
	public T top() {
		buildHeap();
		return heap.get(0);
	}
	public List<T> sort() {
		buildHeap();
		for (int i=heap.size()-1; i>0; ) {
			swap(0, i);     // put max on the end
			--i;            // shrink heap
			moveDown(0, i); //fix heap tree
		}
		return heap;
	}
	public void print() {
		buildHeap();
		StringBuilder s=new StringBuilder(heap.size());
		int nl=1,c=1,sum=1;
		for (Object i : heap) {
			s.append(i.toString());
			s.append(" ");
			if (sum==c) {
				s.append("\n");
				nl*=2;
				sum+=nl;
			}
			++c;
		}
		Log.prn("tree:\n%s", s.toString());
	}

	static public <T extends Comparable<T>> void sort(List<T> a) {
		HeapTree<T> h = new HeapTree<T>(a);
		h.sort();
	}
}
