/*
This file is part of PH-Tree:
A multi-dimensional indexing and storage structure.

Copyright (C) 2011-2015
Eidgenössische Technische Hochschule Zürich (ETH Zurich)
Institute for Information Systems
GlobIS Group
Tilmann Zaeschke
zaeschke@inf.ethz.ch or zoodb@gmx.de

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package ch.ethz.globis.pht;

import ch.ethz.globis.pht.util.BitTools;

public interface PhKey {

	long[] getKeyBits();
	
	public static class PhKeyLong<T> implements PhKey {
		long[] key;
		T value;
		
		public PhKeyLong(long[] key, T value) {
			this.key = key;
			this.value = value;
		}
		
		public void set(long[] key, T value) {
			this.key = key;
			this.value = value;
		}
		
		public T getValue() {
			return value;
		}
		
		public long[] getKey() {
			return key;
		}
		
		@Override
		public long[] getKeyBits() {
			return key;
		}
	}
	
	public static class PhKeyDouble<T> implements PhKey {
		double[] key;
		T value;
		
		public PhKeyDouble(double[] key, T value) {
			this.key = key;
			this.value = value;
		}
		
		public void set(double[] key, T value) {
			this.key = key;
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public double[] getKey() {
			return key;
		}

		@Override
		public long[] getKeyBits() {
			return BitTools.toSortableLong(key, new long[key.length]);
		}
	}
}
