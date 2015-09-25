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

import java.util.List;

import ch.ethz.globis.pht.util.PhIteratorBase;
import ch.ethz.globis.pht.util.PhMapper;
import ch.ethz.globis.pht.util.PhTreeQStats;
import ch.ethz.globis.pht.v8.PhTree8;

/**
 * k-dimensional index (quad-/oct-/n-tree).
 * Supports key/value pairs.
 *
 *
 * @author ztilmann (Tilmann Zaeschke)
 *
 */
public interface PhTree<T> {

    
    public int size();
    
    public int getNodeCount();
    
    public PhTreeQStats getQuality();
    
    public abstract PhTreeHelper.Stats getStats();

    public abstract PhTreeHelper.Stats getStatsIdealNoNode();
    

    /**
     * Insert an entry associated with a k dimensional key.
     * @param key
     * @param value
     * @return the previously associated value or {@code null} if the key was found
     */
    public abstract T put(long[] key, T value);

    public abstract boolean contains(long ... key);

    public abstract T get(long ... key);

    
    /**
     * Remove the entry associated with a k dimensional key.
     * @param key
     * @return the associated value or {@code null} if the key was found
     */
    public abstract T remove(long... key);

    public abstract String toStringPlain();
    
    public abstract String toStringTree();
    
	public abstract PhIterator<T> queryExtent();


	/**
	 * Performs a range query. The parameters are the min and max keys.
	 * @param min
	 * @param max
	 * @return Result iterator.
	 */
	public abstract PhQuery<T> query(long[] min, long[] max);

	public abstract int getDIM();

	public abstract int getDEPTH();

	/**
	 * Locate nearest neighbours for a given point in space.
	 * @param nMin number of entries to be returned. More entries may be returned with several have
	 * 				the same distance.
	 * @param key
	 * @return List of neighbours.
	 */
	public abstract List<long[]> nearestNeighbour(int nMin, long... key);
	
	/**
	 * Locate nearest neighbours for a given point in space.
	 * @param nMin number of entries to be returned. More entries may be returned with several have
	 * 				the same distance.
	 * @param dist the distance function, can be {@code null}. The default is {@link PhDistanceL}.
	 * @param dims the dimension filter, can be {@code null}
	 * @param key
	 * @return List of neighbours.
	 */
	public abstract List<long[]> nearestNeighbour(int nMin, PhDistance dist, PhDimFilter dims, 
			long... key);

	/**
	 * Update the key of an entry. Update may fail if the old key does not exist, or if the new
	 * key already exists.
	 * @param oldKey
	 * @param newKey
	 * @return the value (can be {@code null}) associated with the updated key if the key could be 
	 * updated, otherwise {@code null}.
	 */
	public T update(long[] oldKey, long[] newKey);

	/**
	 * Same as {@link #queryIntersect(double[], double[])}, except that it returns a list
	 * instead of an iterator. This may be faster for small result sets. 
	 * @param lower
	 * @param upper
	 * @return List of query results
	 */
	public List<PhEntry<T>> queryAll(long[] min, long[] max);
	
	public <R> List<R> queryAll(long[] min, long[] max, int maxResults, 
			PhPredicate filter, PhMapper<T, R> mapper);
	
	/**
	 * Create a new tree with the specified number of dimensions.
	 * 
	 * @param dim number of dimensions
	 * @return PhTree
	 */
    public static <T> PhTree<T> create(int dim) {
    	return new PhTree8<T>(dim);
    }

    public static interface PhIterator<T> extends PhIteratorBase<long[], T, PhEntry<T>> {}

    public static interface PhQuery<T> extends PhIterator<T> {
   	 
 		/**
     	 * Reset the query with the new 'min' and 'max' boundaries.
     	 * @param min
     	 * @param max
     	 */
     	void reset(long[] min, long[] max);
   	 
    	/**
    	 * Special 'next' method that avoids creating new objects internally by reusing Entry objects.
    	 * Advantage: Should completely avoid any GC effort.
    	 * Disadvantage: Returned PhEntries are not stable and are only valid until the
    	 * next call to next(). After that they may change state. Modifying returned entries may
    	 * invalidate the backing tree.
    	 * @return The next entry
    	 */
     	PhEntry<T> nextEntryReuse();
     }

    /**
     * Clear the tree.
     */
	void clear();
}

