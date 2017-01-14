/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
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
