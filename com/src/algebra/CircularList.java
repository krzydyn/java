package algebra;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import sys.Log;

public class CircularList<E> extends AbstractList<E> {
	static class Node<E> {
		E item;
		Node<E> next;
		Node(E v, Node<E> n) { this.item = v; this.next = n; }
	}
	private int size = 0;
	private Node<E> last = null;


	private void chechElemIndex(int index) {
		if (!(index >= 0 && index < size))
			throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
	}
	private void chechPosIndex(int index) {
		if (!(index >= 0 && index <= size))
			throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
	}

	private Node<E> prevnode(int index) {
		if (index == size) return null;
		Node<E> x = last;
		for (int i = 0; i < index; ++i)
			x = x.next;
		return x;
	}

	private Node<E> addnext(Node<E> p, E element) {
		Node<E> n;
		if (p == null) {
			Log.debug("addnext as last");
			if (last == null) {
				p = n = new Node<>(element, null);
			}
			else {
				p = last;
				n = new Node<>(element, p.next);
			}
			p.next = n;
			last = n;
		}
		else {
			Log.debug("addnext prev=%s", p.item);
			n = new Node<>(element, p.next);
			p.next = n;
		}
		++size;
		return n;
	}
	private Node<E> removenext(Node<E> p) {
		if (last == null) return null;
		Node<E> rm = p.next;
		p.next = rm.next;
		if (p == rm) last = null;
		else if (last == rm) last = p;
		--size;
		return rm;
	}

	@Override
	public void add(int index, E element) {
		chechPosIndex(index);
		Log.debug("add(%d,%s", index, element.toString());
		addnext(prevnode(index), element);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		Iterator<E> i = listIterator(0);
		while (i.hasNext()) {
			s.append(i.next().toString());
			s.append(", ");
		}
		if (s.length() > 0) s.setLength(s.length()-2);
		return s.toString();
	}

	@Override
	public E set(int index, E element) {
		chechElemIndex(index);
		Node<E> p = prevnode(index);
		E elem = p.next.item;
		p.next.item = element;
		return elem;
	}

	@Override
	public E remove(int index) {
		chechElemIndex(index);
		Node<E> p = prevnode(index);
		return removenext(p).item;
	}

	@Override
	public E get(int index) {
		chechElemIndex(index);
		Node<E> p = prevnode(index);
		return p.next.item;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ListItr(index);
	}

	private class ListItr implements ListIterator<E> {
		private Node<E> prev;
		int nextIndex = 0;

		ListItr(int index) {
			nextIndex = index;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < size;
		}

		@Override
		public E next() {
			if (!hasNext()) throw new NoSuchElementException();
			if (prev == null) prev = prevnode(nextIndex);
			else prev = prev.next;
			++nextIndex;
			return prev.next.item;
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public E previous() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public int previousIndex() {
			return nextIndex - 1;
		}

		@Override
		public void remove() {
			removenext(prev);
		}

		@Override
		public void set(E e) {
			prev.next.item = e;
		}

		@Override
		public void add(E e) {
			addnext(prev, e);
		}
	}
}
