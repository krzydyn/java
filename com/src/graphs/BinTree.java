package graphs;

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
		Node v = searchNode(root, e, true);
		return v != null;
	}
	public boolean remove(T e) {
		Node v = searchNode(root, e, false);
		return removeNode(v);
	}

	public boolean removeRoot() {
		return removeNode(root);
	}

	@SuppressWarnings("unchecked")
	public T min() {
		Node v = minNode(root);
		if (v == null) return null;
		return (T)v.e;
	}
	@SuppressWarnings("unchecked")
	public T max() {
		Node v = maxNode(root);
		if (v == null) return null;
		return (T)v.e;
	}

	@SuppressWarnings("unchecked")
	private Node searchNode(Node v, T e,boolean add) {
		if (root != null && v == null) return null;
		Node p = null;
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
	private boolean removeNode(Node v) {
		if (v == null) return false;

		if (v.l == null && v.r == null) {
			// no child
			if (v.p == null) root = null;
			else if (v.p.l == v) v.p.l = null;
			else v.p.r = null;
		}
		else if (v.l == null) {
			// one child (right)
			v.r.p = v.p;
			if (v.p == null) root = v.r;
			else if (v.p.l == v) v.p.l = v.r;
			else v.p.r = v.r;
		}
		else if (v.r == null) {
			// one child (left)
			v.l.p = v.p;
			if (v.p == null) root = v.l;
			else if (v.p.l == v) v.p.l = v.l;
			else v.p.r = v.l;
		}
		else {
			// both children
			//find max in left or min in right subtree
			// put it in place of v
			Node x = nextNode(v);
			//remove x from there
			removeNode(x);
			//put it in place of v
			if (v.p == null) root = x;
			x.p = v.p;
			x.l = v.l;
			x.r = v.l;
			++nElems;
		}

		v.p = v.l = v.r = null;
		--nElems;
		return true;
	}

	private Node nextNode(Node v) {
		Node p=v;
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

	private Node minNode(Node v) {
		if (v == null) return null;
		while (v.l != null) v = v.l;
		return v;
	}
	private Node maxNode(Node v) {
		if (v == null) return null;
		while (v.r != null) v = v.r;
		return v;
	}

	private void rotLeft(Node n) {
	}
	private void rotRight(Node n) {
	}

	@Override
	public String toString() {
		StringBuilder st=new StringBuilder((nElems+1)*2);
		for (Node v=minNode(root); v != null; v = nextNode(v)) {
			st.append(v.e.toString());
			st.append(" ");
		}
		return st.toString();
	}
}
