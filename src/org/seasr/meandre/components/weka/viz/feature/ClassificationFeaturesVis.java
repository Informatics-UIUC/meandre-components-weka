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

import org.meandre.core.ComponentContext;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class ClassificationFeaturesVis{

	Instances _dataTable = null;

	int _numTopFeatures = -1;

	StaticWebPage _webPage = null;

	ComponentContext _meandreRunHandle = null;

	/**
	 * constructor inputs data instances, number of features. uses defaults
	 * for color scheme, resolution of graphics, graphics sizes, etc.
	 */
	public ClassificationFeaturesVis(Instances dataSet, int numTopFeatures,
			ComponentContext cc){
		_dataTable = dataSet;
		_numTopFeatures = numTopFeatures;
			
		if(numTopFeatures > dataSet.numAttributes()){
			_numTopFeatures = dataSet.numAttributes();
		}
		_meandreRunHandle = cc;
	}

	/**
	 * 
	 * @return this vis as a StaticWebPage
	 */
	public StaticWebPage getWebPage(){
		renderPage();
		return _webPage;
	}

	private void renderPage(){
		//-initialize page
		_webPage = new StaticWebPage(_meandreRunHandle);
		StringBuffer sb = new StringBuffer();
        addLine(sb, "<html>");
        addLine(sb, "<head>");
        addLine(sb, "<title>Feature Comparison of a Model</title>");
        addLine(sb, "</head>");
        addLine(sb, "<body>");

        sb.append(renderHtmlTable(_webPage));
		sb.append(_webPage.getDismissButtonSnippet());

        addLine(sb, "</body>");
        addLine(sb, "</html>");

		_webPage.setMainPage(sb.toString());
		return;
	}

	/*private String linkToTestPage(StaticWebPage webPage){
		String testPageLoc = "test.html";
		String testPageContents = "HI";
		String lnk = webPage.addTextFile(testPageContents, testPageLoc);
		String linkTag = "<a href=\"" + lnk + "\"> Another Page </a>";
		return linkTag;
	}*/



	/**
	 * supporting images will be added to the input static web page.
	 * @return the html snippet for the table as a string
	 */
	private String renderHtmlTable(StaticWebPage webPage){
        Attribute categoryAttribute = _dataTable.classAttribute();
        int numClasses = categoryAttribute.numValues();		

		String[] categoryLabels = new String[numClasses];
		for (int i = 0; i < numClasses; i++) {
			categoryLabels[i] = categoryAttribute.value(i);
		}

		//-htmlCol header info
	    int htmlColIdx = 0;
        int numHtmlCols = 3 + numClasses + 1;
        //the html cells will map directly to the row, col in the
        //output html table
        String[][] htmlCells = new String[_numTopFeatures][numHtmlCols];
        String[] htmlColumnLabels = new String[numHtmlCols];
        htmlColumnLabels[htmlColIdx] = "Feature";
        htmlColIdx++;
        htmlColumnLabels[htmlColIdx] = "Average Frequency<br>All Docs";
        htmlColIdx++;
        htmlColumnLabels[htmlColIdx] = "Median Frequency<br>All Docs";
        htmlColIdx++;
        for (int i = 0; i < numClasses; i++) {
            htmlColumnLabels[htmlColIdx] = "Median Frequency<br>Class: " +
                                           categoryLabels[i];
            htmlColIdx++;
        }
        htmlColumnLabels[htmlColIdx] = "Distributions Comparison Plot";	

		
		
		//-get top features
		//find the best features, store their indices
        IndexedDouble[] bestFeatureScores = findBestFeatures(_dataTable,
				_numTopFeatures);
        int[] bestFeatureIdxs = IndexedDouble.extractIndices(bestFeatureScores);

		//the category for every instance is used in several places.
		//this is an array of ints, one entry per instance that holds an
		//int identifier for the class/category
		int[] categoryAssignments = extractCategoryAssignments(_dataTable);

		
		 //now a row in the table for every best feature
        for (int featureRankIdx = 0; 
				featureRankIdx < _numTopFeatures; 
				featureRankIdx++) {

			int currentFeatureIdx = bestFeatureIdxs[featureRankIdx];
			htmlColIdx = 0;//keep track of which html column we're at in this row
            int htmlRowIdx = featureRankIdx;

			//get the data column values for the current feature
			double[] attributeValues = extractAttributeValues(_dataTable, 
					currentFeatureIdx);


            //first html column is name of the feature
            String lbl = _dataTable.attribute(currentFeatureIdx).name();
            htmlCells[htmlRowIdx][htmlColIdx] = "<b>" + lbl + "<b>";
            htmlColIdx++;

            //second html column is average (normalized) frequency across all docs
            double avg = bestFeatureScores[featureRankIdx].getValue();
            htmlCells[htmlRowIdx][htmlColIdx] = doubleToFormattedString(avg);
            htmlColIdx++;

            //third htmnl column is median (normnalized) frequency across all docs
			double med = calcMedian(attributeValues);
            htmlCells[htmlRowIdx][htmlColIdx] = doubleToFormattedString(med);
            htmlColIdx++;

            //next html column through numClasses + 2 are the per class medians
            double[] categoryMedians = calcCategoryMedians(attributeValues,
					categoryAssignments);
            for (int c = 0; c < numClasses; c++) {
                htmlCells[htmlRowIdx][htmlColIdx] = doubleToFormattedString(
                        categoryMedians[c]);
                htmlColIdx++;
            }

            //next column is the plot image
			CategoryHistogram hist = new CategoryHistogram(attributeValues,
					categoryAssignments, categoryLabels);

            BufferedImage plotImage = hist.renderImage();
			String imageName = makeImageName(featureRankIdx);
			String imageUrl = webPage.addImageFile(plotImage, imageName);
	
            htmlCells[htmlRowIdx][htmlColIdx] = 
				"<img src =\"" + imageUrl + "\">";
		}
		String htmlTable = populateHtmlTable(htmlCells, htmlColumnLabels);
		return htmlTable;
	}

	private String populateHtmlTable(String[][] htmlCells, 
									String[] htmlColumnLabels){

        StringBuffer sb = new StringBuffer();
		int numHtmlCols = htmlColumnLabels.length;
        addLine(sb, "<table border=\"black\" align=\"center\">");
        for (int j = 0; j < numHtmlCols; j++) {
            htmlColumnLabels[j] = "<b><center>" + htmlColumnLabels[j] +
                                  "</center></b>";
        }
        addTableRow(sb, htmlColumnLabels);
        for (int i = 0; i < _numTopFeatures; i++) {
            addTableRow(sb, htmlCells[i]);
        }

        addLine(sb, "</table>");
        return sb.toString();

	}
    /**
     * adds an html row to a table by appending the text for the row to the
     * input string buffer.
     */
    private void addTableRow(StringBuffer sb, String[] cells) {
        addLine(sb, "<tr>");
        for (int i = 0; i < cells.length; i++) {
            sb.append("<td align=\"center\">");
            sb.append(cells[i]);
            sb.append("</td>");
            sb.append("\n");
        }
        addLine(sb, "</tr>");
    }


	/**gets a column from a data table (instances) as a double array.
	 */
	public static double[] extractAttributeValues(Instances dataTable, int attributeIdx){
		int numRows = dataTable.numInstances();
		double[] values = new double[numRows];
		for(int i = 0 ; i < numRows; i++){
			values[i] = dataTable.instance(i).value(attributeIdx);
		}
		return values;
	}

	
	public static double[] calcCategoryMedians(double[] dataValues, 
			int[] categoryAssignments){
		IndexedDouble[] indexedValues = IndexedDouble.mapFromDoubles(dataValues);
		IndexedDouble[][] indexedCatValues = IndexedDouble.filterByCategory(
				indexedValues, categoryAssignments);

		int numCats = indexedCatValues.length;
		double[] meds = new double[numCats];
		for(int c = 0; c < numCats; c++){
			indexedValues = indexedCatValues[c];
			meds[c] = calcMedianFromIndexed(indexedValues);	
		}
		return meds;
	}

	private static double calcMedianFromIndexed(IndexedDouble[] dataValues){
		IndexedDouble.sortArray(dataValues);
        double med = 0;
        if(dataValues.length > 0){
    		int medIdx = (int)((double)dataValues.length / 2.0);
	    	med = dataValues[medIdx].getValue();
        }
		return med;
	}

	public static double calcMedian(double[] dataValues){
		IndexedDouble[] indexedValues = IndexedDouble.mapFromDoubles(dataValues);
		return calcMedianFromIndexed(indexedValues);
	}


	/**
	 * finds the numTopFeatures based on highest average value of
	 * that feature.
	 */
	public static IndexedDouble[] findBestFeatures(Instances dataTable, 
			int numTopFeatures){
        
		int numAtts = dataTable.numAttributes();
        IndexedDouble[] avgs = new IndexedDouble[numAtts];
        for (int i = 0; i < numAtts; i++) {
            avgs[i] = new IndexedDouble(i, dataTable.meanOrMode(i));
        }
		IndexedDouble.sortArray(avgs);
		IndexedDouble[]  topAvgs = new IndexedDouble[numTopFeatures];
        for (int i = 0; i < numTopFeatures; i++) {
            topAvgs[i] = avgs[numAtts - 1 - i];
        }
		return topAvgs;
	}

	/**
	 * gets the class of each instance in a data table (instances)/
	 */
	public static int[] extractCategoryAssignments(Instances dataTable){
		int classIdx = dataTable.classIndex();
		double[] rawVals = dataTable.attributeToDoubleArray(classIdx);		
		int numRows = rawVals.length;
		int[] cats = new int[numRows];
		for(int i = 0; i < numRows; i++){
			cats[i] = (int)rawVals[i];
		}
		return cats;
	}

	/**
	 * returns the name of an image with the input index. the name
	 * is "image_<imageIndex>.png" where imageIndex is padding with
	 * leading zeros
	 */
	private String makeImageName(int imageIndex){
        String numStr = Integer.toString(imageIndex);
        //pad with leading zeros
        while (numStr.length() < 4) {
            numStr = "0" + numStr;
        }
		String filebase = "image" + numStr + ".png"; 
		return filebase;
	}

    private static void addLine(StringBuffer sb, String str) {
        sb.append(str);
        sb.append("\n");
    }

    /**
     * used to impose a common formatting for numerical values with the html.
     */
    public static String doubleToFormattedString(double dbl) {
        String s = String.format("%5.3g", dbl);
        return s;
    }

    private static void log(String msg) {
        System.out.println("ClassificationFeatureVis: " + msg);
        
    }
}
