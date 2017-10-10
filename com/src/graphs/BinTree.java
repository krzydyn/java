package graphs;

import java.util.Iterator;

import sys.Log;
import algebra.Sorting;

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

	private static class BinTreeIterator<T extends Comparable<T>> implements Iterator<T> {
		private final BinTree<T> tree;
		private Node<T> next;
		BinTreeIterator(BinTree<T> tree) {
			this.tree = tree;
			next = tree.minNode(tree.root);
		}
		@Override
		public boolean hasNext() {
			return next != null;
		}
		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			Node<T> cur = next;
			next = tree.nextNode(cur);
			return (T)cur.e;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private Node<T> root;
	private int nElems;

	public BinTree() {}
	public int size() { return nElems; }
	public Iterator<T> iterator() { return new BinTreeIterator<>(this); }

	@SuppressWarnings("unchecked")
	public T root() { return (T)root.e; }
	public int getHeight() { return root==null?-1:root.h;}

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
			p = v; ++Sorting.rdCnt;
			r = ((T)v.e).compareTo(e); ++Sorting.opCnt;
			if (r == 0) return add?null:v;
			if (r < 0) v = v.r;
			else v = v.l;
		}
		if (add) {
			v = new Node<T>(e, p);
			++Sorting.rdCnt; ++Sorting.wrCnt;
			if (p == null) root = v;
			else {
				if (r < 0) p.r = v;
				else p.l = v;
			}
			++nElems;
			for (int h=1; p!=null; ++h, p=p.p) {
				if (p.h >= h) break;
				p.h = h;
			}
		}
		return v;
	}
	private void updateHeight(Node<T> p) {
		for (; p!=null; p = p.p) {
			int h=0;
			if (p.l != null && p.r != null) h = Math.max(p.l.h, p.r.h)+1;
			else if (p.l != null) h = p.l.h+1;
			else if (p.r != null) h = p.r.h+1;
			if (p.h == h) break;
			p.h = h;
		}
	}
	private boolean removeNode(Node<T> v) {
		if (v == null) return false;
		//Log.debug("removeNode(%s)",v.e);
		Sorting.rdCnt+=2; Sorting.wrCnt+=2;
		Node<T> p = v.p;
		if (v.l == null && v.r == null) {
			// no child
			if (p == null) root = null;
			else if (p.l == v) p.l = null;
			else p.r = null;
			updateHeight(p);
		}
		else if (v.l == null) {
			// one child (right)
			v.r.p = p;
			if (p == null) root = v.r;
			else if (p.l == v) p.l = v.r;
			else p.r = v.r;
			updateHeight(p);
		}
		else if (v.r == null) {
			// one child (left)
			v.l.p = p;
			if (p == null) root = v.l;
			else if (p.l == v) p.l = v.l;
			else p.r = v.l;
			updateHeight(p);
		}
		else {
			// both children
			//find next Node, remove and put in place if v
			Node<T> x = nextNode(v);
			removeNode(x);

			//put it in place of v
			if (p == null) root = x;
			Sorting.rdCnt+=1; Sorting.wrCnt+=1;
			x.p = p;
			x.l = v.l;
			x.r = v.r;
			x.h = v.h;
			if (v.r!=null) v.r.p=x;
			if (v.l!=null) v.l.p=x;
			++nElems;
		}

		v.h = 0;
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
				Sorting.rdCnt+=1; Sorting.wrCnt+=1;
				p = v.p;
				if (v == p.l) return p;
				v = p;
			}
			if (v.p == null) break;
		}
		return null;
	}

	private Node<T> minNode(Node<T> v) {
		if (v == null) return null;
		while (v.l != null) { v = v.l; Sorting.rdCnt+=1; Sorting.wrCnt+=1; }
		return v;
	}
	private Node<T> maxNode(Node<T> v) {
		if (v == null) return null;
		while (v.r != null) { v = v.r; Sorting.rdCnt+=1; Sorting.wrCnt+=1; }
		return v;
	}

	//balance
	// https://appliedgo.net/balancedtree/
	private void rotLeft(Node<T> n) {
	}
	private void rotRight(Node<T> n) {
	}
	private void rotLeftRight(Node<T> n) {
	}
	private void rotRightLeft(Node<T> n) {
	}
	public void balance(Node<T> n) {
		int lh=-1,rh=-1;
		if (n.l!=null) lh=n.l.h;
		if (n.r!=null) rh=n.r.h;
		int d = rh-lh;
		if (Math.abs(d) < 2) return ;
		Log.debug("unbalanced at %s", n.e);
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
	public void print(String prefix, Node<T> n, boolean isLeft) {
		if (n != null) {
			System.out.println (prefix + (isLeft ? "L-- " : "R-- ") + n.e);
			print(prefix + "    ", n.l, true);
			print(prefix + "    ", n.r, false);
		}
	}
	public void print(String prefix, Node<T> l, Node<T> r) {
		if (l == null && r == null) return ;
		if (l != null) {
			System.out.println (prefix + "L-- " + l.e + ":" + l.h);
			print(prefix + "    ", l.l, l.r);
		}
		if (r != null) {
			System.out.println (prefix + "R-- " + r.e + ":" + r.h);
			print(prefix + "    ", r.l, r.r);
		}
	}
	public void print() {
		//calcHeight(root);
		//print("",root,false);
		if (root != null) {
			System.out.println (root.e+":"+root.h);
			print("",root.l,root.r);
		}
	}
}
