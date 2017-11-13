package algebra;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SortedArray<T extends Comparable<T>> extends AbstractCollection<T> {
	private final List<T> tree;

	public SortedArray() {
		this(0);
	}
	public SortedArray(int capa) {
		if (capa<=0) tree = new ArrayList<T>();
		else tree = new ArrayList<T>(capa);
	}
	@Override
	public Iterator<T> iterator() {
		return tree.iterator();
	}
	@Override
	public int size() {
		return tree.size();
	}
	@Override
	public boolean add(T e) {
		if (e==null) return false;
		int i = findIdx(e);
		if (i >= 0) return false;
		i = -i-1;
		Sorting.rdCnt+=tree.size()-i;
		Sorting.wrCnt+=tree.size()-i+1;
		tree.add(i, e);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		if (o==null) return false;
		if (!(o instanceof Comparable<?>)) return false;
		@SuppressWarnings("unchecked")
		int i = findIdx((T)o);
		if (i < 0) return false;
		Sorting.rdCnt+=tree.size()-i;
		Sorting.wrCnt+=tree.size()-i;
		tree.remove(i);
		return true;
	}

	@Override
	public boolean contains(Object o) {
		if (o==null) return false;
		if (!(o instanceof Comparable<?>)) return false;
		@SuppressWarnings("unchecked")
		int i = findIdx((T)o);
		if (i < 0) return false;
		return true;
	}

	private int findIdx(T e) {
		int l = 0, h = tree.size();
		while (l < h) {
			int p = (l+h)/2;
			Sorting.rdCnt+=1; Sorting.opCnt+=1;
			int r = tree.get(p).compareTo(e);
			if (r == 0) return p;
			if (r < 0) l = p+1;
			else h = p;
		}
		return -l-1;
	}

	@Override
	public String toString() {
		StringBuilder st=new StringBuilder((tree.size()+1)*2);
		for (int i=0; i < tree.size(); ++i) {
			st.append(tree.get(i).toString());
			st.append(" ");
		}
		return st.toString();
	}
}
