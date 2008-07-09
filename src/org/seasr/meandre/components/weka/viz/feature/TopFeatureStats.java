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

import weka.core.Instances;

public class TopFeatureStats{

    int _numTopFeatures;

    int[] _topFeatureIdxs;

    double[] _featureAvgs;

    double[] _featureMedians;

    double[][] _featureMediansPerClass;

    Instances _sourceData;    


    /**
     * Creates a stats object for the top 'numTopFeatures' in the input
     * 'dataSet'. The top features will be those with the highest average
     * across all instances in the data set.
     */
    public TopFeatureStats(Instances dataSet, int numTopFeatures){
        _sourceData = dataSet;
        _numTopFeatures = numTopFeatures;
        
        _featureAvgs = new double[_numTopFeatures];
        _featureMedians = new double[_numTopFeatures];
        _featureMediansPerClass = new double[_numTopFeatures][];
        
 
 		//-get top features
		//find the best features, store their indices
        IndexedDouble[] bestFeatureScores = 
                ClassificationFeaturesVis.findBestFeatures(_sourceData,
				_numTopFeatures);
        _topFeatureIdxs = IndexedDouble.extractIndices(bestFeatureScores);

		//the category for every instance is used in several places.
		//this is an array of ints, one entry per instance that holds an
		//int identifier for the class/category
		int[] categoryAssignments = 
                ClassificationFeaturesVis.extractCategoryAssignments(_sourceData);

         for (int featureRankIdx = 0; 
				featureRankIdx < _numTopFeatures; 
				featureRankIdx++) {       
			int currentFeatureIdx = _topFeatureIdxs[featureRankIdx];

			//get the data column values for the current feature            
			double[] attributeValues = 
                    ClassificationFeaturesVis.extractAttributeValues(_sourceData, 
					currentFeatureIdx);

            //average over all instances
            _featureAvgs[featureRankIdx] = 
                    bestFeatureScores[featureRankIdx].getValue();

            //median over all instances
			_featureMedians[featureRankIdx] = 
                    ClassificationFeaturesVis.calcMedian(attributeValues);
            
            //medians over instances in each class
            _featureMediansPerClass[featureRankIdx] =
                    ClassificationFeaturesVis.calcCategoryMedians(attributeValues,
					categoryAssignments);
         }
    }

    /**
     * The number of top features this FeatureStats instance calculated. 
     */
    public int getNumTopFeatures(){
        return _numTopFeatures;
    }

    /**
     * The number of classes (categories) that instances of the source data
     * set could take on. (So if the class attribute had values of either
     * 'A' or 'B', this would be '2')
     */
    public int getNumClasses(){
        return _featureMediansPerClass[0].length;
    }

    /**
     * The attribute index in the source data set for the feature with rank
     * 'featureRank'. Valid values are 0 (the highest ranked feature) to
     * numTopFeatures - 1 (the lowest ranked feature being tracked).
     */
    public int getTopFeatureIndex(int featureRank){
        return _topFeatureIdxs[featureRank];
    }

    /**
     * The attribute label in the original data set for the feature ranked
     * 'featureRank'. Valid values are 0 (the highest ranked feature) to
     * numTopFeatures - 1 (the lowest ranked feature being tracked).
     */
    public String getTopFeatureName(int featureRank){
        String lbl = _sourceData.attribute(_topFeatureIdxs[featureRank]).name();
        return lbl;
    }

    
    /** The average value over all instances in the source data of the 
     * attribute with rank 'featureRank'.
     */
    public double getOverallAverage(int featureRank){
        return _featureAvgs[featureRank];
    }

    /** The median value over all instances in the source data of the 
     * attribute with rank 'featureRank'.
     */
    public double getOverallMedian(int featureRank){
        return _featureMedians[featureRank];
    }

    /** The median value over instances in the source data of the 
     * attribute with rank 'featureRank' and class variable 'classIndex'.
     * 'classIndex' corresponds to the class label returned by getClassIndex().
     */
    public double getClassMedian(int featureRank, int classIndex){
        return _featureMediansPerClass[featureRank][classIndex];
    }

    public String getClassLabel(int classIndex){
        return _sourceData.classAttribute().value(classIndex);
    }

    /**
     * the original data set that the stats were calculated for.
     */
    public Instances getSourceData(){
        return _sourceData;
    }


    /** formats a human readable table (it will be as many lines long
     * as there are numTopFeatures).
     */
    public String toLongString(){
        StringBuffer sb = new StringBuffer();

        sb.append("TopFeatureStats:");
        sb.append("\n\tNumTopFeatures = " + _numTopFeatures);
        sb.append("\n\tNumClasses = " + getNumClasses());
        sb.append("\n");
        for(int i = 0; i < _numTopFeatures; i++){
            sb.append("(FeatureRank:" + i + ")");
            sb.append("(FeatureName:" + getTopFeatureName(i) + ")");
            sb.append("(OverallAverage:" + getOverallAverage(i) + ")");  
            sb.append("(OverallMedian:" + getOverallMedian(i) + ")");
            for(int j = 0; j < getNumClasses(); j ++){
                sb.append("(Median(" + getClassLabel(j) + "):");
                sb.append(getClassMedian(i, j) + ")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }




}
