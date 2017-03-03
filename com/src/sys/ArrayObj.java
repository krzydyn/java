package sys;

import java.util.AbstractList;
import java.util.Collection;

public class ArrayObj<E> extends AbstractList<E> {
	private final Object[] elems;
	public ArrayObj(E[] e) {this.elems = e; }

	@Override
	public int size() {
		return elems.length;
	}

	@Override
	public Object[] toArray() {
		return elems;
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
	public void clear() {
		for (int i=0; i < elems.length; ++i)
			elems[i]=null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		return (E)elems[index];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E set(int index, E element) {
		E r = (E)elems[index];
		elems[index] = element;
		return r;
	}
}
