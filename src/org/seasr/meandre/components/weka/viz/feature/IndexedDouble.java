/**
 * University of Illinois/NCSA
 * Open Source License
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.  
 * All rights reserved.
 * 
 * Developed by: 
 * 
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 * 
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions: 
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers. 
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the 
 *    documentation and/or other materials provided with the distribution. 
 * 
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */ 

package org.seasr.meandre.components.weka.viz.feature;

import java.util.Arrays;
import java.util.Comparator;
/**
* this class allows simple data values (doubles) to be linked
* to an index (usually the row or column its from in a table or 
* Instances). 
*
* The compare function (from the Comparator interface) references
* the double value. This allows us to use the standard in-place 
* java sorting libraries
* against a row or column in a table and be able to tell which
* indices the original data came from.
*/
public class IndexedDouble implements Comparable {
	int index = -1;
	double value = Double.NaN;

	/**
	 * create an indexed double with the input index and double value.
	 */
	public IndexedDouble(int i, double val) {
		index = i;
		value = val;
	}

	/**
	 * compares two IndexedDoubles by their doubles.
	 */
	public static int compare(Object id1, Object id2) {
		double v1 = ((IndexedDouble) id1).getValue();
		double v2 = ((IndexedDouble) id2).getValue();
		return Double.compare(v1, v2);
	}

	/**
	 * compares two IndexedDoubles by their doubles.
	 */
	public int compareTo(Object id2) {
		double v1 = getValue();
		double v2 = ((IndexedDouble) id2).getValue();
		return Double.compare(v1, v2);
	}

	/**
	 * returns true iff (this.getValue() == id2.getValue())
	 */
	public boolean equals(Object id2) {
		return (this.value == ((IndexedDouble) id2).getValue());
	}

	public int getIndex() {
		return index;
	}

	public double getValue() {
		return value;
	}

	/**
	 * sorts an array of IndexedDouble's by their double values in place.
	 */
	public static void sortArray(IndexedDouble[] ary){
		Arrays.sort(ary);
	}

	/**
	 * creates an array of integers with the indexes from each of the input
	 * array.
	 * @param ary IndexedDoubles
	 * @return array of indices
	 */
	public static int[] extractIndices(IndexedDouble[] ary){
		int numElems = ary.length;
		int[] indices = new int[numElems];
		for(int i = 0; i < numElems; i++){
			indices[i] = ary[i].getIndex();
		}
		return indices;
	}

	/**
	 * for every IndexedDouble in the input array, populates the corresponding
	 * spot in a double array with the value.
	 * @param ary
	 * @return array of doubles (values)
	 */
	public static double[] extractValues(IndexedDouble[] ary){
		int numElems = ary.length;
		double[] vals = new double[numElems];
		for(int i = 0; i < numElems; i++){
			vals[i] = ary[i].getValue();
		}
		return vals;
	}


	/** creates an array of IndexedDoubles from an array of doubles */
	public static IndexedDouble[] mapFromDoubles(double[] values){
		int s = values.length;
		IndexedDouble[] targets = new IndexedDouble[s];
		for(int i = 0; i < s; i++){
			targets[i] = new IndexedDouble(i, values[i]);
		}
		return targets;
	}

	/**
	 * separates a single set of IndexedDoubles (ids) into disjoint
	 * arrays of IndexedDoubles, based on what category they're in.
	 * The categories are specified by ints in range [0, numCategories-1]
	 * The input categoryAssignments is an array of equal length to
	 * ids with a category for each element of ids.
	 */
	public static IndexedDouble[][] filterByCategory(IndexedDouble[] ids,
			int[] categoryAssignments){

		int maxCategoryId = -1;
		int numElements = categoryAssignments.length;
		for(int i = 0; i < numElements; i++){
			if(categoryAssignments[i] > maxCategoryId){
				maxCategoryId = categoryAssignments[i];
			}
		}
		int numCats = maxCategoryId + 1;
		int[] numElemsPerCat = new int[numCats];
		for(int i = 0; i < numElements; i++){
			numElemsPerCat[categoryAssignments[i]]++;
		}
		IndexedDouble[][] catElems= new IndexedDouble[numCats][];
		int[] catPositionIters = new int[numCats];
		for(int j = 0; j < numCats; j++){
			catElems[j] = new IndexedDouble[numElemsPerCat[j]];
		}
		for(int i = 0; i < numElements; i++){
			IndexedDouble elem = ids[i];
			int cat = categoryAssignments[i];
			catElems[cat][catPositionIters[cat]] = elem;
			catPositionIters[cat]++;
		}	
		return catElems;
	}
}

