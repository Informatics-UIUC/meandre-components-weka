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

package org.seasr.meandre.components.weka.classifiers.functions;

import java.util.Random;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

//for annotations
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;

/** This component takes a data set as a Weka instance object as input and builds a model
 * based on weka's SMO implementation.
 *
 * The model is passed as output.
 *
 * @author Loretta Auvil
 */

@Component(creator="Loretta Auvil",
		description="This component takes a data set as a Weka instance object"+
		" as input and builds a model based on weka's SMO implementation.",
		name="WekaSMO",  tags="weka model predict SMO support vector machine")

		public class WekaSMO implements ExecutableComponent{

	// input data
	@ComponentInput(description="Weka training instances", name="instances")
	final static String DATA_INPUT_1 ="instances";
	@ComponentOutput(description="Weka model", name="model")
	final static String DATA_OUTPUT_1 ="model";

	// properties
	@ComponentProperty(description="Indicates whether the complexity constant.",
			name="prune", defaultValue = "1" )
			final static String PROPERTY_C ="complexity constant";
	@ComponentProperty(description="Indicates the exponent for the polynomial kernel.",
			name="minPerLeaf", defaultValue = "1" )
			final static String PROPERTY_EXPONENT ="exponent";

	//Valid options are:
	// -C num	The complexity constant C. (default 1)
	// -E num   The exponent for the polynomial kernel. (default 1)
	// -N   Don't normalize the training instances.
	// -L  Rescale kernel.
	// -O  Use lower-order terms.
	// -A num Sets the size of the kernel cache. Should be a prime number. (default 1000003)
	// -T num Sets the tolerance parameter. (default 1.0e-3)
	// -P num Sets the epsilon for round-off error. (default 1.0e-12)
//	String[] options = new String[1];
//	options[0] = "-U";                  // unpruned tree
//	classifier.setOptions(options);     // set the options

	/** This method is called when the Menadre Flow execution is completed.
	 *
	 */
	public void initialize(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

	/** This component takes a data set a Weka instance object as input and builds a model
	 * based on weka's SMO implementation.
	 * The model is passed as output.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
	 *         the execution of a component, a ComponentExecutionException
	 *         should be thrown to signal termination of execution required.
	 * @throws ComponentContextException A violation of the component context
	 *         access was detected
	 */

	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {

		// TODO Auto-generated method stub

		// Pull the instances from the input port
		Instances instances = (Instances)(cc.getDataComponentFromInput(DATA_INPUT_1));
		try {
			SMO classifier = new SMO();

			//Set all properties
			/*String prune = cc.getProperty(PROPERTY_PRUNE);
			if (prune.equalsIgnoreCase("N"))
				classifier.setUnpruned(true);
			else
				classifier.setUnpruned(false);

			int minPerLeaf = Integer.parseInt(cc.getProperty(PROPERTY_MINPERLEAF));
			classifier.setMinNumObj(minPerLeaf);
			 */

			classifier.buildClassifier(instances);
			System.out.println("\nModel built on complete dataset\n");
			// evaluate classifier and print some statistics
			System.out.println("\nApplying the Model built on complete dataset\n");
			Evaluation eval = new Evaluation(instances);
			eval.evaluateModel(classifier, instances);
			eval.confusionMatrix().toString();
			System.out.println(eval.toSummaryString("\nResults\n=======\n", true));
			// Using Cross Validation to build and test the modeling
			//J48 newclassifier = new J48();
			//Evaluation CVeval = new Evaluation(instances);
			//CVeval.crossValidateModel(newclassifier, instances, 10, new Random(1));
			//System.out.println(CVeval.toSummaryString("\nCVResults\n=======\n", true));
			cc.pushDataComponentToOutput(DATA_OUTPUT_1,classifier);
			System.out.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}

	}

	/** This method is invoked when the Meandre Flow is being prepared for
	 * getting run.
	 *
	 */
	public void dispose(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

}
