package algebra;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Combinatory {

	static public <T extends Object> void comboSort(List<T> a, Comparator<T> cmp) {
		int gap = a.size();
		boolean swapped=false;
		while (gap > 1 || swapped) {
			gap = gap * 10 / 13; //empirical
			if (gap==0) gap=1;
			else if (gap==9||gap==10) gap=11;

			swapped = false;
			for (int i = 0; i + gap < a.size(); ++i) {
				if (cmp.compare(a.get(i), a.get(i + gap)) > 0) {
					Collections.swap(a, i, i + gap);
		            swapped = true;
		           }
		      }
		   }
	}

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

	static public <T extends Object> boolean nextPermutation(List<T> a, Comparator<T> cmp) {
		int i = a.size() - 2;

        while (i >= 0 && cmp.compare(a.get(i),a.get(i + 1)) >= 0) i--;

        if (i < 0) return false;

        int j = a.size() - 1;
        while (cmp.compare(a.get(i),a.get(j)) >= 0) j--;

        Collections.swap(a, i, j);
        Collections.reverse(a.subList(i + 1, a.size()));
        return true;
	}
}
