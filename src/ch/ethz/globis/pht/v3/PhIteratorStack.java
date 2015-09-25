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
package ch.ethz.globis.pht.v3;

import ch.ethz.globis.pht.v3.PhTree3.NodeIterator;

public class PhIteratorStack<T> {

	private final NodeIterator<T>[] stack;
	private int size = 0;
	
	@SuppressWarnings("unchecked")
	public PhIteratorStack(int DEPTH) {
		stack = new NodeIterator[DEPTH];
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void push(NodeIterator<T> p) {
		stack[size++] = p;
	}

	public NodeIterator<T> peek() {
		return stack[size-1];
	}

	public NodeIterator<T> pop() {
		NodeIterator<T> ret = stack[--size];
		stack[size] = null;
		return ret;
	}

}
