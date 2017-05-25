package algebra;

import java.util.ArrayList;
import java.util.List;

public class BinTree<T extends Comparable<T>> {
	private final List<T> tree;

	public BinTree() {
		this(0);
	}
	public BinTree(int capa) {
		if (capa<=0) tree = new ArrayList<T>();
		else tree = new ArrayList<T>(capa);
	}
	public int size() { return tree.size(); }
	public Object[] asArray() { return tree.toArray(); }

	private static final int parent(int i) {return (i-1)/2;}
	private static final int left(int i) {return 2*i+1;}
	//private int right(int i) {return 2*i+2;}

	private void swap(int i, int j) {
		T t = tree.get(i);
		tree.set(i, tree.get(j));
		tree.set(j, t);
		Sorting.rdCnt+=2;
		Sorting.wrCnt+=2;
	}

	private int moveUp(int v) {
		int p=0;
		while (v>0) {
			p=parent(v);
			Sorting.rdCnt+=2;
			++Sorting.opCnt;
			int r = tree.get(p).compareTo(tree.get(v));
			if (left(p)==v) {
				if (r > 0) break;
				swap(p,v);
				int s = tree.size();
				//Log.debug("u %s",toString());Log.debug("u moveDown(%d,%d)",p,s);
				moveDown(v, s);
			}
			else {
				swap(p,v);
				int s = tree.size();
				//Log.debug("u %s",toString());Log.debug("u moveDown(%d,%d)",p,s);
				moveDown(v, s);
			}

			v=p;
		}
		return p;
	}
	private void moveDown(int p, int s) {
		int v;
		while ((v = left(p)) < s) {
			++Sorting.opCnt;
			if (tree.get(p).compareTo(tree.get(v)) < 0) {}
			else if (v+1 < s) {
				++Sorting.opCnt; Sorting.rdCnt+=2;
				if (tree.get(p).compareTo(tree.get(v+1)) <= 0) break;
				++v;
			}
			else break;
			swap(p, v);
			p = v;
		}
	}

	public void add(T e) {
		tree.add(e);
		int p,s = tree.size();
		if (s==1) return ;
		int i = s-1;
		//Log.debug("%s",toString());Log.debug("moveUp(%d)",i);
		p=moveUp(i);
		//Log.debug("%s",toString());Log.debug("moveDown(%d,%d)",p,s);
		moveDown(p, s);
	}
	public T remove(int i) {
		int s = tree.size();
		if (i < 0 || i >= s) throw new ArrayIndexOutOfBoundsException(i);
		if (s <= 2) return tree.remove(i);
		--s;
		if (i < s) {
			swap(i, s);
			//Log.debug("%s",toString());
			//Log.debug("moveUp(%d)",i);
			int p=moveUp(i);
			//Log.debug("%s",toString());
			//Log.debug("moveDown(%d,%d)",p,s);
			moveDown(p, s);
		}
		return tree.remove(s);
	}

	public T get(int i) { return tree.get(i); }

	public int search(T e) {
		int v = 0;
		while (v < tree.size()) {
			int r = tree.get(v).compareTo(e);
			if (r == 0) return v;
			v = left(v);
			if (r < 0) ++v;
		}
		return -v;
	}

	public int next(int v) {
		int p=v;
		for (;;) {
			v = left(p)+1;
			if (v < tree.size()) return min(v);
			v=p;
			while (v!=0) {
				p = parent(v);
				if (v == left(p)) return p;
				v=p;
			}
			if (p == 0) break;
		}
		return -1;
	}

	public int min(int v) {
		int p=-1;
		while (v < tree.size()) {
			p = v;
			v = left(v);
		}
		return p;
	}
	public int max(int v) {
		int p=-1;
		while (v < tree.size()) {
			p = v;
			v = left(v)+1;
		}
		return p;
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
