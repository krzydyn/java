package algebra;

import java.util.AbstractList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import jdk.internal.jline.internal.Log;

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
		if (last == null) return null;
		Node<E> x = last;
		for (int i = 0; i < index; ++i)
			x = x.next;
		return x;
	}

	private Node<E> removenext(Node<E> n) {
		if (last == null) return null;
		Node<E> rm = n.next;
		n.next = rm.next;
		--size;
		if (size == 0) last = null;
		else if (last == rm) last = n;
		return rm;
	}

	@Override
	public void add(int index, E element) {
		chechPosIndex(index);
		Node<E> n;
		if (last == null) {
			n = new Node<>(element, null);
			n.next = n;
			Log.debug("add first el %s", element);
		}
		else {
			n = new Node<>(element, last.next);
			Log.debug("add el %s after %s", element, last.next.item);
		}
		last = n;
		++size;
	}

	@Override
	public String toString() {
		for (int i=0; i<size; ++i);
		return null;
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
		E elem = p.next.item;
		p.next = p.next.next;
		--size;
		if (size == 0) last = null;
		return elem;
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
		private Node<E> lastPrev;
		private Node<E> prev;
		int nextIndex = 0;

		ListItr(int index) {
			prev = (index == size) ? null : prevnode(index);
			nextIndex = index;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < size;
		}

		@Override
		public E next() {
			if (!hasNext()) throw new NoSuchElementException();
			lastPrev = prev;
			prev = prev.next;
			++nextIndex;
			return prev.item;
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
			if (lastPrev == null) throw new IllegalStateException();
			removenext(lastPrev);
			if (last == null) lastPrev=null;
		}

		@Override
		public void set(E e) {
			if (lastPrev == null) throw new IllegalStateException();
			lastPrev.next.item = e;
		}

		@Override
		public void add(E e) {
			if (lastPrev == null) throw new IllegalStateException();
			Node<E> n = new Node<>(e, lastPrev.next);
			lastPrev.next = n;
			lastPrev = n;
			++nextIndex;
		}
	}
}
