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

import java.awt.*;
import java.awt.image.BufferedImage;

import org.seasr.meandre.components.weka.viz.feature.IndexedDouble;

/**
 * creates a raster image drawing of a set of histograms. There is one master
 * histogram that encompasses all data but then one histogram each for each of
 * an arbitrary number of subsets of the data (the categories).
 */ 
public class CategoryHistogram{
	

	/**how many bins the input data will be split into. more bins typically means
	 * greater resolution but an excessive number of bins can create meaningless
	 * plots (e.g. every bin would have zero or one members). 
	 */
	int _numBins = 30;

	/**number of pixels tall the output image will be */
	int _pixelImageHeight = 80;

	/**number of pixels wide each glyph (dist plot for a category) will be */
	int _pixelPlotWidth = 40;

	/**number of pixels padding between each glyph/plot*/
	int _pixelHorizontalPlotBuffer = 20;

	/** offset of first plot from the left edge */
	int _pixelLeftPadding = 5;

	/** the histogram plots for each category get a unique color */
	Color[] _colors = null;

	/**the color of the axis, labels, etc.*/
	static Color TRACE_COLOR = Color.BLACK;

	/**the color of the all data histogram (background hist)*/
	static Color FADED_COLOR = Color.GRAY;

	/** all data */
	double[] _masterDataDistribution = null;

	/**which category each member of _masterDataDistribution belongs to */
	int[] _categoryAssignments = null;

	/** one name for each valid value in _categoryAssignments, indexed by
	 * categoryAssignment value.
	 */
	String[] _categoryNames = null;

	//////////////
	//Constructors
	//////////////
	/**
	 * The default constructor. 
	 * 
	 * @param masterData a set of doubles to be represented as a series of 
	 * histograms.
	 * @param categoryAssignments for each datum in masterData, a category
	 * assignment as an int in the range [0, numCategories-1]
	 * @param categoryNames an array of length numCategories with a name
	 * for each category.
	 */
	public CategoryHistogram(double[] masterData, int[] categoryAssignments,
			String[] categoryNames){
		_masterDataDistribution = masterData;
		_categoryAssignments = categoryAssignments;
		_categoryNames = categoryNames;
		
		if(_colors == null){
			_colors = makeColors(categoryNames.length);
		}
	}

	/**
	 * initialize by setting all variable properties.
	 * 
	 * @param masterData a set of doubles to be represented as a series of 
	 * histograms.
	 * @param categoryAssignments for each datum in masterData, a category
	 * assignment as an int in the range [0, numCategories-1]
	 * @param categoryNames an array of length numCategories with a name
	 * for each category.
	 * @param colors an array of length numCategories, with the color each 
	 * category will be displayed with.
	 * @param numBins the resolution of the histograms. A larger number means
	 * more detail, but too many and the bins will all have zero or one 
	 * members. rule of thumb: masterData.length / 10
	 * @param numPixelsImageHeight The number of pixels high the output
	 * image will be.
	 * @param numPixelsWidthPerPlot The number of pixels wide each plot will 
	 * be. The total image width is numPixelsWidthPerPlot * (1 + numCategories)
	 */
	public CategoryHistogram(double[] masterData, int[] categoryAssignments,
			String[] categoryNames, Color[] colors, int numBins, 
			int numPixelsImageHeight, int numPixelsWidthPerPlot){

		this(masterData, categoryAssignments, categoryNames);
		_numBins = numBins;
		_pixelImageHeight = numPixelsImageHeight;
		_pixelPlotWidth = numPixelsWidthPerPlot;
		_colors = colors;
		
	}


	//////////////
	//public 
	/////////////
	/**
	 * Does the necessary calculations on the data set and renders the 
	 * raster image (png).
	 * 
	 * @return the raster image of the histograms.
	 */
	public BufferedImage renderImage(){

		//a category is synonymous with the class of a Weka instance
		int numCats = _categoryNames.length;

		IndexedDouble[] masterOrdering = IndexedDouble.mapFromDoubles(
				_masterDataDistribution);
		IndexedDouble[][] catOrderings = IndexedDouble.filterByCategory(
				masterOrdering, _categoryAssignments);

		IndexedDouble.sortArray(masterOrdering);
        double dataMin = masterOrdering[0].getValue();
        double dataMax = masterOrdering[masterOrdering.length - 1].getValue();

        double[][] masterHistRegions = calcBinHistogram(dataMin, dataMax,
        		masterOrdering, _numBins);

        double[][][] catHistRegions = new double[numCats][][];
        for (int c = 0; c < numCats; c++) {
            catHistRegions[c] = calcBinHistogram(dataMin, dataMax,
                         catOrderings[c], _numBins);
        }

        String dataMinLabel = doubleToFormattedString(dataMin);
        String dataMaxLabel = doubleToFormattedString(dataMax);

        BufferedImage img = drawDistributionsPlot(_pixelImageHeight,
                                                  _pixelPlotWidth, 
												  _pixelHorizontalPlotBuffer,
												  _pixelLeftPadding,
												  masterHistRegions, 
												  catHistRegions, 
												  _categoryNames,
                                                  _colors, 
												  dataMinLabel, 
												  dataMaxLabel);
        return img;
	}


	///////////////
	//private
	//////////////

    /**
     * @return double[][] where returnArray[i] is region info for region i.
     *         returnArray[i][0] = boundary upper bound fraction [0.0, 1.0]
     *         returnArray[i][1] = boundary size fraction [0.0, 1.0]
     *         returnArray[i][2] = region magnitude frac [0.0, 1.0]
     */

    public static double[][] calcBinHistogram(double dataMin, double dataMax,
			IndexedDouble[] orderedData, int numBins) {
		
		//let's ensure the data is really ordered
		IndexedDouble.sortArray(orderedData);

        double dataRange = dataMax - dataMin;
        double binStep = dataRange / numBins;
        double[][] regions = new double[numBins][3];
        int nextIdx = 0;
        for (int b = 0; b < numBins; b++) {
            double binThreshold = binStep * b + dataMin;
            int binTally = 0;
            while ((nextIdx < orderedData.length) &&
                   (orderedData[nextIdx].getValue() < binThreshold)) {
                binTally++;
                nextIdx++;
            }
            double startFrac = ((double) b) / ((double) numBins);
            double endFrac = ((double) (b + 1)) / ((double) numBins);
            double fracRange = 1.0 / ((double) numBins);
            double fracMagnitude = ((double) binTally);// /
                                   //((double) orderedData.length);
            regions[b][0] = endFrac;
            regions[b][1] = fracRange;
            regions[b][2] = fracMagnitude;
        }
        return regions;
    }

    /**
     * Does the actual drawing of the graphic. Typically not used directly.
     * 
     * @param imagePixHeight
     * @param glyphPixWidth
     * @param glyphPixBuffer
     * @param leftPixPad
     * @param allDataHistRegions
     * @param categoriesDataHistRegions
     * @param categoryLabels
     * @param categoryColors
     * @param dataMinLabel
     * @param dataMaxLabel
     * @return
     */

    public static BufferedImage drawDistributionsPlot(
            int imagePixHeight,
            int glyphPixWidth,
			int glyphPixBuffer,
			int leftPixPad,
            double[][] allDataHistRegions,
            double[][][] categoriesDataHistRegions,
            String[] categoryLabels,
            Color[] categoryColors,
            String dataMinLabel,
            String dataMaxLabel) {

        int numCats = categoryLabels.length;
        int imagePixWidth = leftPixPad +
                            (glyphPixWidth + glyphPixBuffer) * (1 + numCats);
        BufferedImage pixelsData = new BufferedImage(imagePixWidth, imagePixHeight,
                                                     BufferedImage.TYPE_INT_RGB);
        //the graphics context to draw on the image
        Graphics gtx = pixelsData.createGraphics();
        gtx.setColor(Color.white);
        gtx.fillRect(0, 0, imagePixWidth, imagePixHeight);
        gtx.setColor(categoryColors[0]);

        Color traceColor = Color.BLACK;

        //numPlots = one per class plus the "all data"
        int numPlots = numCats + 1;

        //how much total space each mini plot takes up
        int glyphPixStep = glyphPixBuffer + glyphPixWidth;

        //add some padding to the glyph
        int topPix = 2;
        int bottomPix = imagePixHeight - 2;
		int plotHeight = bottomPix - topPix;


        double largestBarFrac = Double.NEGATIVE_INFINITY;
        for (int i = 1; i < allDataHistRegions.length; i++) {
            double mag = allDataHistRegions[i][2];
            if (mag > largestBarFrac) {
                largestBarFrac = mag;
            }
        }

        //use so that all rectanglular regions
        //have proportional areas
        //double widthMultiplier = (double) glyphPixWidth / largestBarFrac;
        double widthMultiplier = (double) glyphPixWidth / allDataHistRegions.length;

		int axisTickSize = 2;
		
        for (int i = 0; i < numPlots; i++) {
            int axisOffset = leftPixPad + ((glyphPixBuffer + glyphPixWidth) * i);
			axisOffset += axisTickSize;


            //draw the axis
            gtx.setColor(traceColor);
			int leftTickPix = axisOffset - axisTickSize;
			int rightTickPix = axisOffset + axisTickSize;

			//top tick line
            gtx.drawLine(leftTickPix, topPix, rightTickPix, topPix);
			//bttm line
            gtx.drawLine(leftTickPix, bottomPix, rightTickPix, bottomPix);
			//vert line
            gtx.drawLine(axisOffset, topPix, axisOffset, bottomPix);
            
			//a local graphics context translated to a new coordinate system
			//the new coordinate system has (0,0) on the axis just drawn
            Graphics lgtx = gtx.create(axisOffset, topPix, glyphPixStep, 
					plotHeight);

            //plot the master distribution for each
			drawHistogram(lgtx, FADED_COLOR, plotHeight, widthMultiplier,
					allDataHistRegions);

            if (i > 0) { //the first one has no category histogram
                int catIndex = i - 1;
                double[][] catHist = categoriesDataHistRegions[catIndex];
				double widthWeighting = ((double)catHist.length / 
						(double)allDataHistRegions.length);
				widthWeighting *= widthMultiplier;
				drawHistogram(lgtx, categoryColors[catIndex], plotHeight, 
						widthWeighting,	catHist);
		    }
        }
        return pixelsData;
    }

	/**
	 * used by drawDistributionsPlot. do not use directly.
	 */
	private static void drawHistogram(Graphics gtx, Color color, int pixelHeight,
			double widthWeighting, double[][] histogramDef){
		int numBins = histogramDef.length;
		int x, y, pixBarLength, pixBarHeight, pixBarVertStart = 0;
		x = 1;
		for (int b = 0; b < numBins; b++) {
			pixBarLength = (int) (widthWeighting * histogramDef[b][2]);
			pixBarHeight = (int) (pixelHeight * histogramDef[b][1]) + 1;
			pixBarVertStart = pixelHeight - (int)(histogramDef[b][0] * pixelHeight);

			y = pixBarVertStart;
			gtx.setColor(color);
			gtx.fillRect(x, y, pixBarLength, pixBarHeight);
		}
	}	

	/** creates an array of colors by cycling through a set of standard
	 * colors.
	 */
	private Color[] makeColors(int numColors){
		Color[] sourceColors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE};
		Color[] retColors = new Color[numColors];
		for(int i = 0; i < numColors; i++){
			retColors[i] = sourceColors[(i % 4)];
		}
		return retColors;
	}

    /**
     * used to impose a common formatting for numerical values with the html.
     */
    private static String doubleToFormattedString(double dbl) {
        String s = String.format("%5.3g", dbl);
        return s;
    }

    private static void log(String msg) {
        System.out.println("CategoryHistogram: " + msg);
        //
    }
}
