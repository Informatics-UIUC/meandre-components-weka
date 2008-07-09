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

import org.meandre.core.*;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;

/**
 * This component takes a data set as a Weka instance object as input and builds a Decision Tree model
 * based on weka's J48 implementation.
 * <p/>
 * The model is passed as output.
 *
 * @author Loretta Auvil
 */

@Component(creator = "Loretta Auvil",
           description = "This component takes a data set as a Weka instance object" +
                         " as input and builds a Decision Tree model based on weka's J48 implementation.",
           name = "WekaJ48Tree", tags = "weka model predict J48 decision tree")
public class WekaJ48Tree implements ExecutableComponent {

    @ComponentInput(description = "Weka training instances", name = "instances")
    final static String DATA_INPUT_1 = "instances";

    @ComponentOutput(description = "Weka model", name = "model")
    final static String DATA_OUTPUT_1 = "model";
    
    @ComponentOutput(description = "Classifier String output", name = "classifierSource")
    final static String DATA_OUTPUT_2 = "classifierSource";

    @ComponentOutput(description = "classifiedGraph", name = "classifiedGraph")
    final static String DATA_OUTPUT_3 = "classifiedGraph";


    @ComponentProperty(description = "Indicates whether the decision tree should be pruned or not, indicate Y or N.",
                       name = "prune", defaultValue = "Y")
    final static String PROPERTY_PRUNE = "prune";

    @ComponentProperty(description = "Indicates the minimum number of instances per leaf.",
                       name = "minPerLeaf", defaultValue = "2")
    final static String PROPERTY_MINPERLEAF = "minPerLeaf";

    /*
     @ComponentProperty(description="Indicates the confidence threshold for pruning.",
            name="confidence", defaultValue = ".25" )
    final static String PROPERTY_CONFIDENCE ="confidence";
    @ComponentProperty(description="Indicates whether to use reduced error pruning (no subtree raising is performed), indicate Y or N.",
            name="reduceErrorPruning", defaultValue = "Y" )
    final static String PROPERTY_REDUCEERRORPRUNING ="reduceErrorPruning";
    @ComponentProperty(description="Indicates the number of folds for reduced error pruning one fold is used as the pruning set.",
            name="foldsErrPruning", defaultValue = "3" )
    final static String PROPERTY_FOLDSERRPRUNING ="foldsErrPruning";
    @ComponentProperty(description="Indicates whether binary splits should be used for nominal attributes, indicate Y or N.",
            name="binarySplits", defaultValue = "Y" )
    final static String PROPERTY_BINARYSPLITS ="binarySplits";
    @ComponentProperty(description="Indicates whether subtree raising is performed, indicate Y or N.",
            name="subtreeRaising", defaultValue = "N" )
    final static String PROPERTY_SUBTREERAISING ="subtreeRaising";
    @ComponentProperty(description="Indicates whether Laplace smoothing is used for predicted probablilities, indicate Y or N.",
            name="useLaplace", defaultValue = "Y" )
    final static String PROPERTY_USELAPLACE ="useLaplace";
    */
    //Valid options are:
    //-U Use unpruned tree.
    //-C confidence Set confidence threshold for pruning. (Default: 0.25)
    //-M number Set minimum number of instances per leaf. (Default: 2)
    //-R Use reduced error pruning. No subtree raising is performed.
    //-N number Set number of folds for reduced error pruning. One fold is used as the pruning set. (Default: 3)
    //-B Use binary splits for nominal attributes.
    //-S Don't perform subtree raising.
    //-L Do not clean up after the tree has been built.
    //-A If set, Laplace smoothing is used for predicted probabilities.
//String[] options = new String[1];
//options[0] = "-U";                  // unpruned tree
//classifier.setOptions(options);     // set the options

    /**
     * This method is called when the Menadre Flow execution is completed.
     */
    public void initialize(ComponentContextProperties ccp) {
        // TODO Auto-generated method stub

    }

    /**
     * This component takes a data set a Weka instance object as input and builds a Decision Tree model
     * based on weka's J48 implementation.
     * The model is passed as output.
     *
     * @throws ComponentExecutionException If a fatal condition arises during
     *                                     the execution of a component, a ComponentExecutionException
     *                                     should be thrown to signal termination of execution required.
     * @throws ComponentContextException   A violation of the component context
     *                                     access was detected
     */

    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {

    	Instances instances = (Instances) (cc.getDataComponentFromInput(DATA_INPUT_1));

        try {
            J48 classifier = new J48();
            //Set all properties
            String prune = cc.getProperty(PROPERTY_PRUNE);
            if (prune.equalsIgnoreCase("N"))
                classifier.setUnpruned(true);
            else
                classifier.setUnpruned(false);

            int minPerLeaf = Integer.parseInt(cc.getProperty(PROPERTY_MINPERLEAF));
            classifier.setMinNumObj(minPerLeaf);
            /*
                   float confidenceFactor = Float.parseFloat(cc.getProperty(PROPERTY_CONFIDENCE));
                   classifier.setConfidenceFactor(confidenceFactor);
                   String reduceErrPruning = cc.getProperty(PROPERTY_REDUCEERRORPRUNING);
                   if (reduceErrPruning.equalsIgnoreCase("Y"))
                       classifier.setReducedErrorPruning(true);
                   else
                       classifier.setReducedErrorPruning(false);

                   int foldsErrPruning = Integer.parseInt(cc.getProperty(PROPERTY_FOLDSERRPRUNING));
                   classifier.setNumFolds(foldsErrPruning);
                   String binarySplits = cc.getProperty(PROPERTY_BINARYSPLITS);
                   if (binarySplits.equalsIgnoreCase("Y"))
                       classifier.setBinarySplits(true);
                   else
                       classifier.setBinarySplits(false);
                   String subtreeRaising = cc.getProperty(PROPERTY_SUBTREERAISING);
                   if (subtreeRaising.equalsIgnoreCase("Y"))
                       classifier.setSubtreeRaising(true);
                   else
                       classifier.setSubtreeRaising(false);
                   String useLaplace = cc.getProperty(PROPERTY_USELAPLACE);
                   if (useLaplace.equalsIgnoreCase("Y"))
                       classifier.setUseLaplace(true);
                   else
                       classifier.setUseLaplace(false);
                   */
            classifier.buildClassifier(instances);
            System.out.println("\nDecision Tree Model built on complete dataset\n" + classifier.graph());
            
            // evaluate classifier and print some statistics
            System.out.println("\nApplying the Decision Tree Model built on complete dataset\n");
            Evaluation eval = new Evaluation(instances);
            eval.evaluateModel(classifier, instances);
            eval.confusionMatrix().toString();
            System.out.println(eval.toSummaryString("\nResults\n=======\n", true));
           
            cc.pushDataComponentToOutput(DATA_OUTPUT_1, classifier);
            String s = classifier.toSource("weka.classifiers.trees.J48");
            System.out.println("s is: " + classifier.toString());
            cc.pushDataComponentToOutput(DATA_OUTPUT_2, s);
            cc.pushDataComponentToOutput(DATA_OUTPUT_3, classifier.graph());
            System.out.flush();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
        }

    }

    /**
     * This method is invoked when the Meandre Flow is being prepared for
     * getting run.
     */
    public void dispose(ComponentContextProperties ccp) {
        // TODO Auto-generated method stub

    }
	}
