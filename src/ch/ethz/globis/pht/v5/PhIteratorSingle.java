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
package ch.ethz.globis.pht.v5;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.ethz.globis.pht.v5.PhTree5.Node;
import ch.ethz.globis.pht.v5.PhTree5.NodeIterator;

class PhIteratorSingle<T> implements Iterator<long[]> {

	private final int DIM;
	private final PhIteratorStack<T> stack;
	private final long[] valTemplate;
	private long[] next = null;
	private final long[] rangeMin;
	private final long[] rangeMax;
	
	public PhIteratorSingle(Node<T> root, int attrID, long min, long max, int DIM, int DEPTH) {
		this.DIM = DIM;
		this.stack = new PhIteratorStack<T>(DEPTH);
		this.valTemplate = new long[DIM];
		this.rangeMin = new long[DIM];
		this.rangeMax = new long[DIM];
		if (root == null) {
			//empty index
			return;
		}
		for (int i = 0; i < DIM; i++) {
			rangeMin[i] = Long.MIN_VALUE;
			rangeMax[i] = Long.MAX_VALUE;
		}
		rangeMin[attrID] = min;
		rangeMax[attrID] = max;
		NodeIterator<T> p2 = NodeIterator.create(root, valTemplate, rangeMin, rangeMax, DIM, true);
		if (p2 != null) {
			stack.push(p2);
			findNextElement();
		}
	}

	private void findNextElement() {
		while (!stack.isEmpty()) {
			NodeIterator<T> p = stack.peek();
			if (findNextElementInNode(p)) {
				return;
			} 
			stack.pop();
		}
		//finished
		next = null;
	}
	
	private boolean findNextElementInNode(NodeIterator<T> p) {
		while (p.hasNext()) {
			if (p.isNextSub()) {
				PhTree5.applyArrayPosToValue(p.getCurrentPos(), p.node().getPostLen(), 
						valTemplate, p.isDepth0());
				Node<T> sub = p.getCurrentSubNode();
				NodeIterator<T> p2 = NodeIterator.create(sub, valTemplate, rangeMin, rangeMax, DIM, 
						false);
				p.increment();
				if (p2 != null) {
					stack.push(p2);
					if (findNextElementInNode(p2)) {
						return true;
					}
					stack.pop();
					// no matching (more) elements found
				}
			} else {
				next = p.getCurrentPost();
				p.increment();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public long[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		long[] res = next;
		findNextElement();
		return res;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not implemented yet.");
	}
}