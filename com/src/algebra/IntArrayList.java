package algebra;

import java.util.AbstractList;

public class IntArrayList extends AbstractList<Integer> {
	private final int[] data;
	IntArrayList(int... data) {
		this.data=data;
	}
	@Override
	public Integer get(int index) {
		return data[index];
	}
	@Override
    public Integer set(int index, Integer element) {
        int r = data[index];
        data[index] = element;
        return r;
    }
	@Override
	public int size() {
		return data.length;
	}

}
