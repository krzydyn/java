/*
 *  Copyright (c) 2016 Krzysztof Dynowski All Rights Reserved
 *
 *  Contact: krzydyn@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
package sys;

import java.util.AbstractList;
import java.util.List;

public class PrimitiveArray {
	static private class ByteArray extends AbstractList<Byte> {
		private final byte[] data;
		public ByteArray(byte... data) {
			this.data=data;
		}
		@Override
		public Byte get(int index) {
			return data[index];
		}
		@Override
		public Byte set(int index, Byte element) {
			byte r = data[index];
			data[index] = element;
			return r;
		}
		@Override
		public int size() {
			return data.length;
		}
	}

	static private class IntArray extends AbstractList<Integer> {
		private final int[] data;
		public IntArray(int... data) {
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
	static private class LongArray extends AbstractList<Long> {
		private final long[] data;
		public LongArray(long... data) {
			this.data=data;
		}
		@Override
		public Long get(int index) {
			return data[index];
		}
		@Override
		public Long set(int index, Long element) {
			long r = data[index];
			data[index] = element;
			return r;
		}
		@Override
		public int size() {
			return data.length;
		}

		@Override
		public Object[] toArray() {
			// TODO Auto-generated method stub
			return super.toArray();
		}
	}

	public static List<Byte> asList(final byte[] a) {
		return new ByteArray(a);
	}
	public static List<Integer> asList(final int[] a) {
		return new IntArray(a);
	}
	public static List<Long> asList(final long[] a) {
		return new LongArray(a);
	}
}
