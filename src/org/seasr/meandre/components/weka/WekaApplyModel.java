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

package org.seasr.meandre.components.weka;

import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;
import weka.classifiers.Classifier;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;


/**
 * A component that applies an existing weka model to a set of unlabeled
 * Instances. The model generates new labels and appends them to the table.
 */

@Component(
		creator = "Peter Groves",
		description = "Applies a prediction or classification model "+
				"to a set of unlabeled instances. Returns the input data" +
				" set with a new attribute for the predictions filled in.",
		name = "WekaApplyModel",
		tags = "weka")

public class WekaApplyModel implements ExecutableComponent {

	//////////////////////////////////
	//Component Meta-data definitions
	/////////////////////////////////

	@ComponentInput(
			description = "Unlabeled set of instances to be classified",
			name = "Unlabeled_Data")
	final static String DATA_INPUT = "Unlabeled_Data";

	@ComponentInput(
			description = "Unlabeled set of instances to be classified",
			name = "Classifier")
	final static String MODEL_INPUT = "Classifier";

	@ComponentOutput(
			description = "The input instances with classes determined by the" +
					" model",
			name = "Labeled_Data")
	final static String DATA_OUTPUT = "Labeled_Data";

	///////////////
	//Component Logic
	///////////////

	/** computes class values for each instance using the model and
	 * adds the class info in place. returns the modified data set.
	 * 
	 * 	@param unlabeledData a set of weka instances with no class attribute.
	 *  @return the input instances, modified in place.
	 */
	public static Instances classifyData(Instances unlabeledData,
			Classifier model){

		int numInstances = unlabeledData.numInstances();
		for(int i = 0; i < numInstances; i++){
			Instance dat = unlabeledData.instance(i);
			try{
				double pred = model.classifyInstance(dat);
				dat.setClassValue(pred);
			}catch(Exception e){
				dat.setClassValue(Instance.missingValue());
			}
		}
		return unlabeledData;
	}


	/////////////////////
	//Meandre Interfaces
	//////////////////////

	/** When ready for execution.
	 *
	 * @param cc The component context
	 * @throws ComponentExecutionException An exeception occurred during execution
	 * @throws ComponentContextException Illigal access to context
	 */
	public void execute(ComponentContext cc)
			throws ComponentExecutionException, ComponentContextException {
		try {

			Instances targetData = (Instances)cc.getDataComponentFromInput(
					DATA_INPUT);
			Classifier model = (Classifier)cc.getDataComponentFromInput(
					MODEL_INPUT);

			Instances labeledData = classifyData(targetData, model);
			cc.pushDataComponentToOutput(DATA_OUTPUT, labeledData);

		}
		catch ( Exception e ) {
			throw new ComponentExecutionException(e);
		}

	}

	/** Called when a flow is started.
	 */
	public void initialize(ComponentContextProperties ccp) {	}


	/** Call at the end of an execution flow.
	 */
	public void dispose(ComponentContextProperties ccp) { }

}
