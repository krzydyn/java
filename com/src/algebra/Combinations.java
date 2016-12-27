package algebra;

import java.util.ArrayList;
import java.util.List;

public class Combinations {
	final int pos[];
	final List<Object> set;

	public Combinations(List<?> l, int k) {
		pos = new int[k];
		set=new ArrayList<Object>(l);
		reset();
	}
	public void reset() {
		for (int i=0; i < pos.length; ++i) pos[i]=i;
	}
	public boolean next() {
		int i = pos.length-1;
		for (int maxpos = set.size()-1; pos[i] >= maxpos; --maxpos) {
			if (i==0) return false;
			--i;
		}
		++pos[i];
		while (++i < pos.length)
			pos[i]=pos[i-1]+1;
		return true;
	}

	public void getSelection(List<?> l) {
		@SuppressWarnings("unchecked")
		List<Object> ll = (List<Object>)l;
		if (ll.size()!=pos.length) {
			ll.clear();
			for (int i=0; i < pos.length; ++i)
				ll.add(set.get(pos[i]));
		}
		else {
			for (int i=0; i < pos.length; ++i)
				ll.set(i, set.get(pos[i]));
		}
	}
}
