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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;

import weka.core.Instances;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.Evaluation;
import org.meandre.core.ComponentContextProperties;

/** This component takes a data set as a Weka instance object as input and
 *   builds a Naive Bayes model.
* The model is passed as output.
*
* @author Loretta Auvil
*/
@Component(creator="Loretta Auvil", description="This component takes training " +
		"set as Weka instances object as input and  builds a Naive Bayes model.",
		tags="weka naive_bayes model", name="WekaNaiveBayesClassifier")
public class WekaNaiveBayesClassifier implements ExecutableComponent{

    /** This method is called when the Menadre Flow execution is completed.
    *
    */
	public void initialize(ComponentContextProperties ccp) {
		// TODO Auto-generated method stub

	}

    /** This component takes a data set as a Weka instance object as input and builds a Naive Bayes model.
    * The model is passed as output.
    *
    * @throws ComponentExecutionException If a fatal condition arises during
    *         the execution of a component, a ComponentExecutionException
    *         should be thrown to signal termination of execution required.
    * @throws ComponentContextException A violation of the component context
    *         access was detected
    */

	@ComponentInput(description="traning set instances", name="trainingSetInstances")
	private static final String DATA_INPUT_1="trainingSetInstances";
	@ComponentInput(description=" working set instances", name="workingSetInstances")
	private static final String DATA_INPUT_2="workingSetInstances";
	@ComponentInput(description=" working set document ids", name="workingSetDocumentList")
	private static final String DATA_INPUT_3="workingSetDocumentList";
	@ComponentInput(description="training hashmap", name="trainingData")
	private static final String DATA_INPUT_4="trainingData";



	@ComponentOutput(description="model generated by ty the classifier", name="classifier")
    private static final String DATA_OUTPUT_1="classifier";
	@ComponentOutput(description="prediction result", name="predictionFlowResultArray")
    private static final String DATA_OUTPUT_2="predictionFlowResultArray";
	@ComponentOutput(description="confusion matrix generated by the classifier", name="confMatrix")
	 private static final String DATA_OUTPUT_3="confMatrix";


	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException {
		Instances instances = (Instances)(cc.getDataComponentFromInput(DATA_INPUT_1));
		Instances wsetInstances = (Instances)(cc.getDataComponentFromInput(DATA_INPUT_2));
		ArrayList<String> workingSetDocumentList = (ArrayList<String>)(cc.getDataComponentFromInput(DATA_INPUT_3));

		HashMap<String,String> trainingHashMap= (HashMap<String,String>)(cc.getDataComponentFromInput(DATA_INPUT_4));


		System.out.println("1");
		wsetInstances.setClassIndex(wsetInstances.numAttributes()-1);

		System.out.println("2");
		instances.setClassIndex(instances.numAttributes() - 1);

		System.out.println("3");
		NaiveBayes classifier = new NaiveBayes();

		System.out.println("4");
		PredictionFlowResult  pfg = new PredictionFlowResult();
		System.out.println("5");


		Enumeration classenum=wsetInstances.classAttribute().enumerateValues();
		System.out.println("6");

		while(classenum.hasMoreElements()){
		pfg.addClass(classenum.nextElement());
		}
		System.out.println("7");

		if(workingSetDocumentList.size() != wsetInstances.numInstances()){
			throw new ComponentExecutionException("The number of documents in the workingset should match the number of instances.");
		}
		System.out.println("8");


		try {
			classifier.buildClassifier(instances);
			System.out.println("9");

			Evaluation eval = new Evaluation(instances);
			System.out.println("10");

			eval.evaluateModel(classifier, instances);
			System.out.println("11");


			System.out.println(eval.toSummaryString("\nResults\n=======\n", false));


			double[][] confMatrix=eval.confusionMatrix();
			System.out.println("12");

			int[] classCounts= new int[wsetInstances.classAttribute().numValues()];
			System.out.println("13");

			PredictionResult presult;
			for (int i = 0; i < wsetInstances.numInstances(); i++) {
		    	  double clsLabel = classifier.classifyInstance(wsetInstances.instance(i));
		    	  classCounts[(int) clsLabel]++;
		    	  wsetInstances.instance(i).setClassValue(clsLabel);
			      double prob_ratio[]=classifier.distributionForInstance( wsetInstances.instance(i));
		    	  for(int j=0; j < prob_ratio.length; j++){
		    		  System.out.print(prob_ratio[j] +" ");
		    	  }
		    	  presult = new PredictionResult();
		    	  presult.setDocId(workingSetDocumentList.get(i));
		    	  presult.setPredictedClass((int) clsLabel);
		    	  presult.setProbabilityRatio(prob_ratio);
		    	  presult.setClassValue(wsetInstances.classAttribute().value((int) clsLabel));
		    	  pfg.addResult(presult);

		    	  System.out.print(wsetInstances.classAttribute().value((int) clsLabel) +" ");
		    	  System.out.println("==");
		      }
			    pfg.setResultSummary(eval.toSummaryString());
			    pfg.setTrainingList(trainingHashMap);
			    System.out.println("NUM RESULTS: " + pfg.getResult().size());
			    cc.pushDataComponentToOutput(DATA_OUTPUT_1,classifier);
				cc.pushDataComponentToOutput(DATA_OUTPUT_2, pfg);
				cc.pushDataComponentToOutput(DATA_OUTPUT_3,confMatrix);

		} catch (Exception e) {
			throw new ComponentExecutionException("Error: "+ e.getMessage());
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
