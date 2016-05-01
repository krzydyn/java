package algebra;

import java.util.Collections;
import java.util.List;

public class Combinatory {
	
	static public <T extends Comparable<T>> boolean nextPermutation(List<T> a) {
        int i = a.size() - 2;
        
        while (i >= 0 && a.get(i).compareTo(a.get(i + 1)) >= 0) i--;

        if (i < 0) return false;

        int j = a.size() - 1;
        while (a.get(i).compareTo(a.get(j)) >= 0) j--;

        Collections.swap(a, i, j);
        Collections.reverse(a.subList(i + 1, a.size()));
        return true;
	}
}
