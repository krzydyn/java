package sys;

import java.util.AbstractList;
import java.util.Collection;

public class ImmutableArray<E> extends AbstractList<E> {

	private final Object[] elems;
	public ImmutableArray(E[] e) {this.elems = e; }

	@Override
	public int size() {
		return elems.length;
	}

	@Override
	public Object[] toArray() {
		return elems;
	}

	@Override
	public boolean add(E e) {
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		return (E)elems[index];
	}
}
