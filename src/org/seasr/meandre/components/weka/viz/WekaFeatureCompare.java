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

package org.seasr.meandre.components.weka.viz;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import org.seasr.meandre.components.weka.viz.feature.StaticWebPage;
import org.seasr.meandre.components.weka.viz.feature.ClassificationFeaturesVis;


/**
 * A component that displays (html) an analysis of the discrimating power
 * of a set of features used to build a model.
 */
@Component(creator = "Peter Groves",
           description = "This component displays a comparison of the most frequently" +
                         " existing features in a data set. Histogram plots are shown " +
                         " for the feature in each class, so the uniqueness of the feature " +
                         " to the class is apparent.",
           tags = "weka model feature vis", name = "WekaFeatureCompare")

public class WekaFeatureCompare
        implements ExecutableComponent {

    //////////////////////////////////
    //Component Meta-data definitions
    /////////////////////////////////

    @ComponentInput(description = "Weka instances",
                    name = "Labeled_Instances")
    final static String INSTANCES_INPUT = "Labeled_Instances";

	@ComponentProperty(description = "How many features to include in " +
			"the analysis. Features will be chosen by highest average value.",
			name = "Num_Top_Features", defaultValue = "10")
	final static String NUM_FEATURES_PROPERTY = "Num_Top_Features";

    //////////////////////
    //local fields
    /////////////////////
    /**
     * the testing instances evalauted by a model.
     */
    Instances _inputData = null;

	/**how many features to include in the output vis.*/
	int _numTopFeatures = 10;

	/**handles supporting image files and holds the main html doc*/
	StaticWebPage _webPage = null;

  
    /////////////////////
    //Meandre Interfaces
    //////////////////////

    /**
     * When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException   Illigal access to context
     */
    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {

        try {


            _inputData = (Instances) cc.getDataComponentFromInput(
                    INSTANCES_INPUT);
			String numStr = cc.getProperty(NUM_FEATURES_PROPERTY);
			_numTopFeatures = Integer.parseInt(numStr);
			ClassificationFeaturesVis visMaker = new ClassificationFeaturesVis(
					_inputData,	_numTopFeatures, cc);

			_webPage = visMaker.getWebPage();
			_webPage.runSession();
			
        }
        catch (Exception e) {
            throw new ComponentExecutionException(e);
		}

	}

	/**
     * Called when a flow is started.
     */
	public void initialize(ComponentContextProperties ccp) {	}


	/**
     * Call at the end of an execution flow.
     */
	public void dispose(ComponentContextProperties ccp) { }

}
