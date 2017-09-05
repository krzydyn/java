package graphs;

import sys.Log;

public class BinTree<T extends Comparable<T>> {
	public static class Node<T> {
		private Node(Object o,Node<T> p) {e=o;this.p=p;}
		private final Object e;
		private int h;
		private Node<T> p,l,r; //parent,left-child,right-child

		@SuppressWarnings("unchecked")
		public T value() { return (T)e; }
		public int height() {return h;}
		public Node<T> parent() {return p;}
		public Node<T> left() {return l;}
		public Node<T> right() {return l;}
	}

	private Node<T> root;
	private int nElems;

	public BinTree() {}
	public int size() { return nElems; }

	@SuppressWarnings("unchecked")
	public T root() { return (T)root.e; }

	public boolean add(T e) {
		Node<T> v = searchNode(root, e, true);
		return v != null;
	}
	public boolean remove(T e) {
		Node<T> v = searchNode(root, e, false);
		return removeNode(v);
	}

	public boolean removeRoot() {
		return removeNode(root);
	}

	@SuppressWarnings("unchecked")
	public T min() {
		Node<T> v = minNode(root);
		if (v == null) return null;
		return (T)v.e;
	}
	@SuppressWarnings("unchecked")
	public T max() {
		Node<T> v = maxNode(root);
		if (v == null) return null;
		return (T)v.e;
	}

	@SuppressWarnings("unchecked")
	private Node<T> searchNode(Node<T> v, T e,boolean add) {
		if (root != null && v == null) return null;
		Node<T> p = null;
		int r = 0;
		while (v != null) {
			p = v;
			r = ((T)v.e).compareTo(e);
			if (r == 0) return add?null:v;
			if (r < 0) v = v.r;
			else v = v.l;
		}
		if (add) {
			v = new Node<T>(e, p);
			if (p == null) root = v;
			else {
				if (r < 0) p.r = v;
				else p.l = v;
			}
			++nElems;
		}
		return v;
	}
	private boolean removeNode(Node<T> v) {
		if (v == null) return false;
		Log.debug("removeNode(%s)",v.e);
		if (v.l == null && v.r == null) {
			Log.debug("no children");
			// no child
			if (v.p == null) root = null;
			else if (v.p.l == v) v.p.l = null;
			else v.p.r = null;
		}
		else if (v.l == null) {
			// one child (right)
			Log.debug("child right");
			v.r.p = v.p;
			if (v.p == null) root = v.r;
			else if (v.p.l == v) v.p.l = v.r;
			else v.p.r = v.r;
		}
		else if (v.r == null) {
			// one child (left)
			Log.debug("child left");
			v.l.p = v.p;
			if (v.p == null) root = v.l;
			else if (v.p.l == v) v.p.l = v.l;
			else v.p.r = v.l;
		}
		else {
			Log.debug("both children");
			// both children
			//find max in left or min in right subtree
			// put it in place of v
			Node<T> x = nextNode(v);
			//remove x from there
			removeNode(x);
			//put it in place of v
			if (v.p == null) root = x;
			x.p = v.p;
			x.l = v.l;
			x.r = v.r;
			if (v.r!=null) v.r.p=x;
			if (v.l!=null) v.l.p=x;
			++nElems;
		}

		v.p = v.l = v.r = null;
		--nElems;
		return true;
	}

	private Node<T> nextNode(Node<T> v) {
		Node<T> p=v;
		for (;;) {
			v = p.r;
			if (v != null) return minNode(v);
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

	private Node<T> minNode(Node<T> v) {
		if (v == null) return null;
		while (v.l != null) v = v.l;
		return v;
	}
	private Node<T> maxNode(Node<T> v) {
		if (v == null) return null;
		while (v.r != null) v = v.r;
		return v;
	}

	private void rotLeft(Node<T> n) {
	}
	private void rotRight(Node<T> n) {
	}

	@Override
	public String toString() {
		StringBuilder st=new StringBuilder((nElems+1)*2);
		for (Node<T> v=minNode(root); v != null; v = nextNode(v)) {
			st.append(v.e.toString());
			st.append(" ");
		}
		return st.toString();
	}
}
