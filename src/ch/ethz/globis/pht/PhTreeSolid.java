/*
 * Copyright 2011-2015 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.pht;

import java.util.Arrays;

import ch.ethz.globis.pht.PhTree.PhIterator;
import ch.ethz.globis.pht.PhTree.PhQuery;
import ch.ethz.globis.pht.nv.PhTreeNV;
import ch.ethz.globis.pht.pre.EmptyPPR;
import ch.ethz.globis.pht.pre.PreProcessorRange;
import ch.ethz.globis.pht.util.PhIteratorBase;

/**
 * PH-tree for storing ranged objects with floating point coordinates.
 * Stored objects are axis-aligned hyper-rectangles defined by a 'lower left'
 * and 'upper right' corner.  
 * 
 * @author Tilmann Zaeschke
 */
public class PhTreeSolid<T> implements Iterable<T> {

	private final int DIM;
	private final PhTree<T> pht;
	private final PreProcessorRange pre;
	private final long[] MIN;
	private final long[] MAX;

	/**
	 * Create a new tree with the specified number of dimensions.
	 * 
	 * @param dim number of dimensions
	 */
	public static <T> PhTreeSolid<T> create(int dim) {
		return new PhTreeSolid<T>(dim);
	}

	/**
	 * Create a new tree with the specified number of dimensions.
	 * 
	 * @param dim number of dimensions
	 */
	private PhTreeSolid(int dim) {
		this(PhTree.create(dim*2));
	}

	/**
	 * Create a new range tree backed by the the specified tree.
	 * Note that the backing tree's dimensionality must be a multiple of 2.
	 * 
	 * @param tree the backing tree
	 */
	public PhTreeSolid(PhTree<T> tree) {
		this.DIM = tree.getDIM()/2;
		if (DIM*2 != tree.getDIM()) {
			throw new IllegalArgumentException("The backing tree's DIM must be a multiple of 2");
		}
		pht = tree;
		pre = new EmptyPPR();
		MIN = new long[DIM];
		Arrays.fill(MIN, Long.MIN_VALUE);
		MAX = new long[DIM];
		Arrays.fill(MAX, Long.MAX_VALUE);
	}

	/**
	 * Inserts a new ranged object into the tree.
	 * @param lower
	 * @param upper
	 * @param value
	 * @return the previous value or {@code null} if no entry existed
	 * 
	 * @see PhTreeNV#insert(long...)
	 */
	public T put(long[] lower, long[] upper, T value) {
		long[] lVal = new long[lower.length*2];
		pre.pre(lower, upper, lVal);
		return pht.put(lVal, value);
	}

	/**
	 * Removes a ranged object from the tree.
	 * @param lower
	 * @param upper
	 * @return the value or {@code null} if no entry existed
	 * 
	 * @see PhTreeNV#delete(long...)
	 */
	public T remove(long[] lower, long[] upper) {
		long[] lVal = new long[lower.length*2];
		pre.pre(lower, upper, lVal);
		return pht.remove(lVal);
	}

	/**
	 * Check whether an entry with the specified coordinates exists in the tree.
	 * @param lower
	 * @param upper
	 * @return true if the entry was found 
	 * 
	 * @see PhTreeNV#contains(long...)
	 */
	public boolean contains(long[] lower, long[] upper) {
		long[] lVal = new long[lower.length*2];
		pre.pre(lower, upper, lVal);
		return pht.contains(lVal);
	}

	/**
	 * @see #put(long[], long[], T)
	 */
	public T put(PhEntryS<T> e, T value) {
		return put(e.lower(), e.upper(), value);
	}

	/**
	 * @see #remove(long[], long[], T)
	 */
	public T remove(PhEntryS<T> e) {
		return remove(e.lower(), e.upper());
	}

	/**
	 * @see #contains(long[], long[])
	 */
	public boolean contains(PhEntryS<T> e) {
		return contains(e.lower(), e.upper());
	}

	/**
	 * @see #queryInclude(long[], long[])
	 */
	public PhQueryS<T> queryInclude(PhEntryS<T> e) {
		return queryInclude(e.lower(), e.upper());
	}

	/**
	 * @see #queryIntersect(long[], long[])
	 */
	public PhQueryS<T> queryIntersect(PhEntryS<T> e) {
		return queryIntersect(e.lower(), e.upper());
	}

	/**
	 * Query for all bodies that are fully included in the query rectangle.
	 * @param lower 'lower left' corner of query rectangle
	 * @param upper 'upper right' corner of query rectangle
	 * @return Iterator over all matching elements.
	 */
	public PhQueryS<T> queryInclude(long[] lower, long[] upper) {
		long[] lUpp = new long[lower.length << 1];
		long[] lLow = new long[lower.length << 1];
		pre.pre(lower, lower, lLow);
		pre.pre(upper, upper, lUpp);
		return new PhQueryS<T>(pht.query(lLow, lUpp), DIM, pre, false);
	}

	/**
	 * Query for all bodies that are included in or partially intersect with the query rectangle.
	 * @param lower 'lower left' corner of query rectangle
	 * @param upper 'upper right' corner of query rectangle
	 * @return Iterator over all matching elements.
	 */
	public PhQueryS<T> queryIntersect(long[] lower, long[] upper) {
		long[] lUpp = new long[lower.length << 1];
		long[] lLow = new long[lower.length << 1];
		pre.pre(MIN, lower, lLow);
		pre.pre(upper, MAX, lUpp);
		return new PhQueryS<T>(pht.query(lLow, lUpp), DIM, pre, true);
	}

	public static class PhIteratorS<T> implements PhIteratorBase<long[], T, PhEntryS<T>> {
		private final PhIterator<T> iter;
		private final int DIM;
		protected final PreProcessorRange pre;
		private PhIteratorS(PhIterator<T> iter, int DIM, PreProcessorRange pre) {
			this.iter = iter;
			this.DIM = DIM;
			this.pre = pre;
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}
		@Override
		public T next() {
			return nextValue();
		}
		@Override
		public T nextValue() {
			PhEntry<T> pvEntry = iter.nextEntry();
			return pvEntry.getValue();
		}
		@Override
		public PhEntryS<T> nextEntry() {
			long[] lower = new long[DIM];
			long[] upper = new long[DIM];
			PhEntry<T> pvEntry = iter.nextEntry();
			pre.post(pvEntry.getKey(), lower, upper);
			return new PhEntryS<T>(lower, upper, pvEntry.getValue());
		}
		@Override
		public long[] nextKey() {
			long[] lower = new long[DIM];
			long[] upper = new long[DIM];
			PhEntry<T> pvEntry = iter.nextEntry();
			pre.post(pvEntry.getKey(), lower, upper);
			long[] ret = new long[2*DIM];
			for (int i = 0; i < DIM; i++) {
				ret[i] = lower[i];
				ret[i+DIM] = lower[i];
			}
			return ret;
		}
		@Override
		public void remove() {
			iter.remove();
		}
	}

	public static class PhQueryS<T> extends PhIteratorS<T> {
		private final long[] lLow, lUpp;
		private final PhQuery<T> q;
		private final long[] MIN;
		private final long[] MAX;
		private final boolean intersect;
		
		private PhQueryS(PhQuery<T> iter, int DIM, PreProcessorRange pre, boolean intersect) {
			super(iter, DIM, pre);
			q = iter;
			MIN = new long[DIM];
			Arrays.fill(MIN, Long.MIN_VALUE);
			MAX = new long[DIM];
			Arrays.fill(MAX, Long.MAX_VALUE);
			this.intersect = intersect;
			lLow = new long[DIM*2];
			lUpp = new long[DIM*2];
		}

		public void reset(long[] lower, long[] upper) {
			if (intersect) {
				pre.pre(MIN, lower, lLow);
				pre.pre(upper, MAX, lUpp);
			} else {
				//include
				pre.pre(lower, lower, lLow);
				pre.pre(upper, upper, lUpp);
			}
			q.reset(lLow, lUpp);
		}
	}

	/**
	 * Entries in a PH-tree with ranged objects. 
	 */
	public static class PhEntryS<T> {

		private final long[] lower;
		private final long[] upper;
		private final T value;

		/**
		 * Range object constructor.
		 * @param lower
		 * @param upper
		 */
		public PhEntryS(long[] lower, long[] upper, T value) {
			this.lower = lower;
			this.upper = upper;
			this.value = value;
		}

		/**
		 * @return the value of the entry
		 */
		public T value() {
			return value;
		}

		/**
		 * @return lower left corner of the entry
		 */
		public long[] lower() {
			return lower;
		}

		/**
		 * @return upper right corner of the entry
		 */
		public long[] upper() {
			return upper;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof PhEntryS)) {
				return false;
			}
			PhEntryS<T> e = (PhEntryS<T>) obj;
			return Arrays.equals(lower, e.lower) && Arrays.equals(upper, e.upper);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(lower) ^ Arrays.hashCode(upper);
		}

		@Override
		public String toString() {
			return "{" + Arrays.toString(lower) + "," + Arrays.toString(upper) + "} => " + value;
		}
	}

	@Override
	public PhIteratorS<T> iterator() {
		return new PhIteratorS<T>(pht.queryExtent(), DIM, pre);
	}

	/**
	 * @param lo1
	 * @param up1
	 * @param lo2
	 * @param up2
	 * @return true, if the value could be replaced.
	 * @see PhTree#update(long[], long[])
	 */
	public T update(long[] lo1, long[] up1, long[] lo2, long[] up2) {
		long[] pOld = new long[lo1.length << 1];
		long[] pNew = new long[lo1.length << 1];
		pre.pre(lo1, up1, pOld);
		pre.pre(lo2, up2, pNew);
		return pht.update(pOld, pNew);
	}

	/**
	 * @return The number of entries in the tree
	 */
	public int size() {
		return pht.size();
	}

    /**
     * Clear the tree.
     */
	void clear() {
		pht.clear();
	}
}
