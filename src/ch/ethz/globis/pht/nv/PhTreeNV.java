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
package ch.ethz.globis.pht.nv;

import static ch.ethz.globis.pht.PhTreeHelper.DEBUG;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ch.ethz.globis.pht.PhEntry;
import ch.ethz.globis.pht.PhPredicate;
import ch.ethz.globis.pht.PhTreeHelper;
import ch.ethz.globis.pht.util.Bits;
import ch.ethz.globis.pht.util.PhMapperKey;
import ch.ethz.globis.pht.util.PhTreeQStats;
import ch.ethz.globis.pht.util.StringBuilderLn;

/**
 * n-dimensional index (quad-/oct-/n-tree).
 *
 * Storage:
 * - classic: One node per combination of bits. Unused nodes can be cut off.
 * - use prefix-truncation -> a node may contain a series of unique bit combinations
 *
 * - To think about: Use optimised tree storage: 00=branch-down 01=branch-up 02=skip 03=ghost (after delete)
 *   -> 0=skip 10=down 11=up?
 *
 *
 * Hypercube: expanded byte array that contains 2^DIM references to sub-nodes (and posts, depending 
 * on implementation)
 * Linearization: Storing Hypercube as paired array of index<->non_null_reference 
 *
 * TODO When using smart increments in queries, and when iterating over an LHC/BLHC, the frequent
 * binary searches could be speed up by adjusting the starting position of the binary search,
 * starting at currentPosition+1 instead of starting at 0.
 *
 * TODO
 * Queries: apply HC-pos to valTemplate only inside getPostPOB. This should help for high-dims.
 * 
 * TODO
 * In query iterator, checkPos() should return:
 * - the increment  [1..x]
 * - [0 or -1] for abort
 * --> currentNew = currentOld + checkPos();
 *     if (currentNew <= currentOld) { abort; }    //This covers also overflows!
 * --> if checkPos()-inc < CONST: use normal increments, otherwise use binary search for next value
 *     (with adjusted start- and end(?) positions)
 *     CONST = 
 *     a) nPost/(1<<DIM) *10^3   //Density * constant
 *     b) (1<<DIM) >> len(nPost)
 *     There also should be some threshold, because binary searches are expensive. 
 * 
 *     
 * TODO
 * BLHC: use BLHC only if LHC goes over certain threshold.
 * 
 * TODO   
 * Query: When iterating through entries in a node, do not always check ranges for all entries.
 * Range checks are only required if the node intersects with any of the ranges, i.e. when
 * the min/max values of the node do no lie completely inside the ranges. 
 *     
 * TODO Merge IteratorPos with NodeIterator.
 * TODO Remove use of iterator in delete(..) and remove checks for valTemplate=null in iterator.
 *
 * TODO
 * TODO Inverse result of insert()! Should return true for successful insert (see Java Collections?)
 * TODO
 *
 * @author ztilmann (Tilmann Zaeschke)
 *
 */
public abstract class PhTreeNV {

    public static PhTreeNV create(int dim) {
    	//return new PhTree1(dim, depth);
    	//return new PhTree2_CB(dim, depth);
    	return new PhTreeVProxy(dim);
    }
    
    public PhTreeNV() {
    	debugCheck();
    }
    
    protected final void debugCheck() {
    	if (DEBUG) {
    		System.err.println("*************************************");
    		System.err.println("** WARNING ** DEBUG IS ENABLED ******");
    		System.err.println("*************************************");
    	}
//    	if (BLHC_THRESHOLD_DIM > 6) {
//    		System.err.println("*************************************");
//    		System.err.println("** WARNING ** BLHC IS DISABLED ******");
//    		System.err.println("*************************************");
//    	}
    }
    
    public abstract int size();
    
    public final void printQuality() {
    	System.out.println("Tree quality");
    	System.out.println("============");
        System.out.println(getQuality());
    }

    public abstract int getNodeCount();
    
    protected final void assertExists(int i, long... la) {
        if (!contains(la)) {
            //printTree();
            throw new IllegalStateException("i="+i + ": " + 
            		Arrays.toString(la) + " = " + Bits.toBinary(la, getDEPTH()));
        }
    }

    public abstract PhTreeQStats getQuality();
    

    
    // ===== Adrien =====
    public void accept(PhTreeVisitor v) {
    	v.visit(this);
    }
    
    public abstract static class PhTreeVisitor {
    	public abstract void visit(PhTreeNV tree);
    }
    
    
    public abstract PhTreeHelper.Stats getStats();

    public abstract PhTreeHelper.Stats getStatsIdealNoNode();
    

    protected final int align8(int n) {
    	return (int) (8*Math.ceil(n/8.0));
    }
    
    /**
     * A value-set is an object with n=DIM values.
     * @param valueSet
     * @return true if the value already existed
     */
    public abstract boolean insert(long... valueSet);

    public abstract boolean contains(long... valueSet);

    

    /**
     * Print entry. An entry is an array of DIM boolean-arrays, each of which represents a number.
     * @param entry
     */
    protected final void printEntry(StringBuilderLn sb, long[] entry) {
        sb.appendLn(Bits.toBinary(entry, getDEPTH()));
    }

    /**
     * A value-set is an object with n=DIM values.
     * @param valueSet
     * @return true if the value was found
     */
    public abstract boolean delete(long... valueSet);


    public final void print() {
        System.out.println("Tree: ****************************************");
        System.out.print(toStringPlain());
        System.out.println("nodes: " + getNodeCount() + " ******************************");
    }

    public abstract String toStringPlain();
    
    public final void printTree() {
        System.out.println("Tree: ****************************************");
        System.out.print(toStringTree());
        System.out.println("nodes: " + getNodeCount() + " ******************************");
    }

    
    public abstract String toStringTree();
    
    
	public abstract Iterator<long[]> queryExtent();


	/**
	 * Performs a range query. The parameters are the min and max values.
	 * @param min
	 * @param max
	 * @return Result iterator.
	 */
	public abstract PhIteratorNV query(long[] min, long[] max);
    
 
	public abstract int getDIM();

	public abstract int getDEPTH();

	/**
	 * Locate nearest neighbours for a given point in space.
	 * @param nMin number of values to be returned. More values may be returned with several have
	 * 				the same distance.
	 * @param v
	 * @return List of neighbours.
	 */
	public abstract List<long[]> nearestNeighbour(int nMin, long... v);

	/**
	 * Update the key of an entry. Update may fail if the old key does not exist, or if the new
	 * key already exists.
	 * @param oldKey
	 * @param newKey
	 * @return true iff the key was found and could be updated, otherwise false.
	 */
	public abstract boolean update(long[] oldKey, long[] newKey);
	
	/**
	 * Same as {@link #queryIntersect(double[], double[])}, except that it returns a list
	 * instead of an iterator. This may be faster for small result sets. 
	 * @param lower
	 * @param upper
	 * @return List of query results
	 */
	public abstract List<PhEntry<Object>> queryAll(long[] min, long[] max);

	public abstract <R> List<R> queryAll(long[] min, long[] max, int maxResults, 
			PhPredicate filter, PhMapperKey<R> mapper);
	
	public interface PhIteratorNV extends Iterator<long[]> { 
		public boolean hasNextKey();
		public long[] nextKey();
	}
}

