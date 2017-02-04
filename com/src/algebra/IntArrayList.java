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

import java.util.AbstractList;
import java.util.List;

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

	static List<Integer> asList(final int[] a) {
		return new AbstractList<Integer>() {
			@Override
			public Integer get(int i) { return a[i]; }
			@Override
			public int size() { return a.length; }
		};
	}
}
