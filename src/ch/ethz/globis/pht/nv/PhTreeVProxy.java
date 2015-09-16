/*
 * Copyright 2011-2015 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.pht.nv;

import java.util.Iterator;
import java.util.List;

import ch.ethz.globis.pht.PhEntry;
import ch.ethz.globis.pht.PhPredicate;
import ch.ethz.globis.pht.PhTree;
import ch.ethz.globis.pht.PhTree.PhIterator;
import ch.ethz.globis.pht.PhTreeHelper;
import ch.ethz.globis.pht.util.PhMapperKey;
import ch.ethz.globis.pht.util.PhTreeQStats;

/**
 * A proxy class that allows Value-PhTrees to be used as key-only PhTrees.
 * 
 * @author ztilmann
 *
 */
public class PhTreeVProxy extends PhTreeNV {

	public final Object VALUE_PLACEHOLDER = new Object(); 
	
	private final PhTree<Object> tree;
	
	public PhTreeVProxy(int dim) {
		tree = PhTree.create(dim);
	}
	
	public PhTreeVProxy(PhTree<Object> tree) {
		this.tree = (PhTree<Object>) tree;
	}
	
	@Override
	public int size() {
		return tree.size();
	}

	@Override
	public int getNodeCount() {
		return tree.getNodeCount();
	}

	@Override
	public PhTreeQStats getQuality() {
		return tree.getQuality();
	}

	@Override
	public PhTreeHelper.Stats getStats() {
		return tree.getStats();
	}

	@Override
	public PhTreeHelper.Stats getStatsIdealNoNode() {
		return tree.getStatsIdealNoNode();
	}

	@Override
	public boolean insert(long... key) {
		return tree.put(key, VALUE_PLACEHOLDER) != null;
	}

	@Override
	public boolean contains(long... key) {
		return tree.contains(key);
	}

	@Override
	public boolean delete(long... key) {
		return tree.remove(key) != null;
	}

	@Override
	public String toStringPlain() {
		return tree.toStringPlain();
	}

	@Override
	public String toStringTree() {
		return tree.toStringTree();
	}

	@Override
	public Iterator<long[]> queryExtent() {
		return new IteratorProxy<Object>(tree.queryExtent());
	}

	@Override
	public PhIteratorNV query(long[] min, long[] max) {
		return new IteratorProxy<Object>(tree.query(min, max));
	}

	@Override 
	public String toString() {
		return tree.toString();
	}
	
	@Override
	public int getDIM() {
		return tree.getDIM();
	}

	@Override
	public int getDEPTH() {
		return tree.getDEPTH();
	}

	@Override
	public List<long[]> nearestNeighbour(int nMin, long... v) {
//		ArrayList<long[]> ret = new ArrayList<>();
//		for (PhEntry<Object> e: ) {
//			ret.add(e.getKey());
//		}
//		return ret;
		return tree.nearestNeighbour(nMin, v);
	}

	private static class IteratorProxy<T> implements PhIteratorNV {

		private final PhIterator<T> iter;
		
		private IteratorProxy(PhIterator<T> iter) {
			this.iter = iter;
		}
		
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public long[] next() {
			return iter.nextKey();
		}

		@Override
		public void remove() {
			iter.remove();
		}

		@Override
		public boolean hasNextKey() {
			return iter.hasNext();
		}

		@Override
		public long[] nextKey() {
			return iter.nextKey();
		}
		
	}

	@Override
	public boolean update(long[] p1, long[] p2) {
		return tree.update(p1, p2) != null;
	}
	
	@Override
	public List<PhEntry<Object>> queryAll(long[] min, long[] max) {
		return tree.queryAll(min, max);
	}

	@Override
	public <R> List<R> queryAll(long[] min, long[] max, int maxResults, 
			PhPredicate filter, PhMapperKey<R> mapper) {
		if (mapper != null) {
			return tree.queryAll(min, max, maxResults, filter, 
					((e) -> mapper.map(e.getKey()))
					);
		} else {
			return tree.queryAll(min, max, maxResults, null, null);
		}
	}

}
