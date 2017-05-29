package graphs;

import sys.Log;

public class BinTree<T extends Comparable<T>> {
	static class Node {
		Node(Object o,Node p) {e=o;this.p=p;}
		Object e;
		int h;
		Node l,r,p;
	}
	private Node root;
	private int nElems;

	public BinTree() {}
	public int size() { return nElems; }

	public boolean add(T e) {
		Node v = search(e, true);
		return v != null;
	}
	public boolean remove(T e) {
		Node v = search(e, false);
		return remove(v);
	}

	public boolean removeRoot() {
		return remove(root);
	}

	@SuppressWarnings("unchecked")
	public T min() {
		Node v = min(root);
		if (v == null) return null;
		return (T)v.e;
	}
	@SuppressWarnings("unchecked")
	public T max() {
		Node v = max(root);
		if (v == null) return null;
		return (T)v.e;
	}

	@SuppressWarnings("unchecked")
	private Node search(T e,boolean add) {
		Node v = root, p = null;
		int r = 0;
		while (v != null) {
			p = v;
			r = ((T)v.e).compareTo(e);
			if (r == 0) return add?null:v;
			if (r < 0) v = v.r;
			else v = v.l;
		}
		if (add) {
			v = new Node(e, p);
			if (p == null) root = v;
			else {
				if (r < 0) p.r = v;
				else p.l = v;
			}
			++nElems;
		}
		return v;
	}
	private boolean remove(Node v) {
		if (v == null) return false;
		Node p,l,r;
		p = v.p; l = v.l; r = v.r;

		Log.debug("removing node %s", v.e);
		if (l == null || r == null) { // zero or one child
			if (p == null) {
				if (l!=null) {p=l; l=p.l; p.p=null;}
				else if (r!=null) {p=r;r=p.r; p.p=null;}
				else p=null;
			}
		}
		else { // two children
			Log.debug("Two children");
		}

		if (p == null) root = null;
		else {
			p.l=l; p.r=r;
			if (p.p == null) root = p;
		}

		/*
		if (v.l == null || v.r == null) y = v;
		else y = next(v);

		if (y.l != null) x = y.l;
		else x = y.r;

		if (x != null) x.p = y.p;

		if (y.p == null) root = y;
		else {
			if (y == y.p.l) y.p.l = x;
			else y.p.r = x;
		}
		*/
		--nElems;
		return true;
	}

	private Node next(Node v) {
		Node p=v;
		for (;;) {
			v = p.r;
			if (v != null) return min(v);
			v=p;
			while (v.p!=null) {
				p = v.p;
				if (v == p.l) return p;
				v=p;
			}
			if (v.p == null) break;
		}
		return null;
	}

	private Node min(Node v) {
		Node p=null;
		while (v != null) {
			p = v;
			v = v.l;
		}
		return p;
	}
	public Node max(Node v) {
		Node p=null;
		while (v != null) {
			p = v;
			v = v.r;
		}
		return p;
	}

	private void rotLeft(Node n) {
	}
	private void rotRight(Node n) {
	}

	@Override
	public String toString() {
		StringBuilder st=new StringBuilder((nElems+1)*2);
		for (Node v=root; v != null; v = next(v)) {
			st.append(v.e.toString());
			st.append(" ");
		}
		return st.toString();
	}
}
