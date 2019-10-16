/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com
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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Combination {
	private final List<Object> set;
	private int[] pos;
	private boolean rep;

	public Combination(List<?> l, int k, boolean repetions) {
		pos = new int[k];
		set = new ArrayList<>(l);
		this.rep = repetions;
		reset();
	}
	public void save(OutputStream stream) throws IOException {
		DataOutput s = new DataOutputStream(stream);
		s.writeBoolean(rep);
		s.writeInt(pos.length);
		for (int i = 0; i < pos.length; ++i)
			s.writeInt(pos[i]);
	}
	public void load(InputStream stream) throws IOException {
		DataInput s = new DataInputStream(stream);
		rep = s.readBoolean();
		int l = s.readInt();
		if (pos == null || l != pos.length)
			pos = new int[l];
		for (int i = 0; i < pos.length; ++i)
			pos[i] = s.readInt();
	}

	public void reset() {
		if (rep)
			for (int i=0; i < pos.length; ++i) pos[i]=0;
		else
			for (int i=0; i < pos.length; ++i) pos[i]=i;
	}
	public void reset(boolean rep) {
		this.rep=rep;
		reset();
	}
	public boolean next() {
		int i = pos.length-1;
		if (rep) {
			for (int maxpos = set.size()-1; pos[i] >= maxpos; ) {
				if (i==0) return false;
				--i;
			}
			++pos[i];
			while (++i < pos.length) pos[i]=0;
		}
		else {
			for (int maxpos = set.size()-1; pos[i] >= maxpos; --maxpos) {
				if (i==0) return false;
				--i;
			}
			++pos[i];
			while (++i < pos.length) pos[i]=pos[i-1]+1;
		}
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

	public static long newton(int n,int k) {
		if (k > n) return 0;
		if (k==0 || n==k) return 1;
		if (k > n-k) k = n-k; // nCr(n,k) = nCr(n,n-k)
		long x=n;
		int d=2;
		for (int i=1; i < k; ++i) {
			x*=n-i;
			if (x<0) return 0;
			while (d <= k && x%d == 0) { x/=d; ++d; }
		}
		while (d <= k) { x/=d; ++d; }
		return x;
	}
}
