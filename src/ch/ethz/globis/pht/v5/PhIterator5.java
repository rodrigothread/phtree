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

import java.util.NoSuchElementException;

import ch.ethz.globis.pht.PhEntry;
import ch.ethz.globis.pht.PhTree.PhIterator;
import ch.ethz.globis.pht.v5.PhTree5.Node;
import ch.ethz.globis.pht.v5.PhTree5.NodeIterator;

public final class PhIterator5<T> implements PhIterator<T> {

	private final int DIM;
	private final PhIteratorStack<T> stack;
	private final long[] valTemplate;
	private final long[] rangeMin;
	private final long[] rangeMax;
	private long[] nextKey = null;
	private T nextVal = null;
	
	public PhIterator5(Node<T> root, long[] rangeMin, long[] rangeMax, int DIM, int DEPTH) {
		this.DIM = DIM;
		this.stack = new PhIteratorStack<T>(DEPTH);
		this.valTemplate = new long[DIM];
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
		if (root == null) {
			//empty index
			return;
		}

		NodeIterator<T> p2 = NodeIterator.create(root, valTemplate, rangeMin, rangeMax, 
				DIM, true);
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
		nextKey = null;
		nextVal = null;
	}
	
	private boolean findNextElementInNode(NodeIterator<T> p) {
		while (p.hasNext()) {
			if (p.isNextSub()) {
				//leave this here. We could move applyToArrayPos somewhere else, but we have to
				//take care that it is only applied AFTER the previous subNodes has been traversed,
				//otherwise we may mess up the valTemplate which is used in the previous Subnode.
				PhTree5.applyArrayPosToValue(
						p.getCurrentPos(), p.node().getPostLen(), valTemplate, p.isDepth0());
				Node<T> sub = p.getCurrentSubNode();
				NodeIterator<T> p2 = NodeIterator.create(sub, valTemplate, rangeMin, rangeMax, DIM, 
						false);
				if (p2 != null) {
					stack.push(p2);
					if (findNextElementInNode(p2)) {
						p.increment();
						return true;
					}
					stack.pop();
					// no matching (more) elements found
				} else {
					// infix comparison failed
				}
				p.increment();
			} else {
				nextKey = p.getCurrentPost(); 
				nextVal = p.getCurrentPostVal();
				p.increment();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public long[] nextKey() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		long[] ret = nextKey;
		findNextElement();
		return ret;
	}

	@Override
	public T nextValue() {
		T ret = nextVal;
		nextKey();
		return ret;
	}

	@Override
	public boolean hasNext() {
		return nextKey != null;
	}

	@Override
	public PhEntry<T> nextEntry() {
		PhEntry<T> ret = new PhEntry<T>(nextKey, nextVal);
		nextKey();
		return ret;
	}
	
	@Override
	public T next() {
		return nextValue();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}