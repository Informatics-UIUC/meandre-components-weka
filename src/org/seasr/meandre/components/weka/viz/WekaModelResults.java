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

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

//for annotations
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;

	/** Display results from weka modeling.
	 *
	 * @author Loretta Auvil;
	 *
	 */

@Component(creator="Loretta Auvil",
		description="This component displays results of modeling with weka."+
		" Displaying performance, confusion matrix and top records for each"+
		" class along with how the top attributes values for each record.",
		name="WekaModelResults",  tags="weka model evaluation results")


	public class WekaModelResults implements ExecutableComponent, WebUIFragmentCallback {

	// input data
	@ComponentInput(description="model to be evaluated", name="eval")
	final static String DATA_INPUT_1 ="eval";
	@ComponentInput(description="Weka training instances", name="instances")
	final static String DATA_INPUT_2 ="instances";

	// properties
	//@ComponentProperty(description="file name", name="fileName", defaultValue = "true" )
	//final static String DATA_PROPERTY_1 ="corpusTag";

		/** The blocking semaphore */
		private Semaphore sem = new Semaphore(1,true);

		/** The model evaluation */
		private Classifier _classifier = null;
		private Instances _instances = null;

		/** The instance ID */
		private String sInstanceID = null;

		/** This method gets call when a request with no parameters is made to a
		 * component WebUI fragment.
		 *
		 * @param response The response object
		 * @throws WebUIException Some problem encountered during execution and something went wrong
		 */
		public void emptyRequest(HttpServletResponse response)
		throws WebUIException {
			try {
				response.getWriter().println(getViz());
			} catch (IOException e) {
				throw new WebUIException(e);
			}
		}

		/** A simple message.
		 *
		 * @return The HTML containing the page
		 */
		private String getViz(){

			StringBuffer sb = new StringBuffer();

			// Load the script for bar charts from a website
			BufferedReader script;
			URL urlScript;
			try {
				urlScript = new URL("http://www.ncsa.uiuc.edu/~lauvil/BarChartScript.txt");
				try {
					script = new BufferedReader(
							new InputStreamReader(
									urlScript.openStream()));
					String line;
					try {
						while (( line = script.readLine()) != null){
							sb.append(line+"\n");
						}
						script.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			sb.append("<center><strong><em>Modeling Results</em></strong></center><br>");

			sb.append("<a href=\"/"+sInstanceID +"?done=true ");
			sb.append("title=\"Done with WebUI fragment\" ");
			sb.append("tooltip=\"Done with WebUI fragment\" >");
			sb.append("Done with WebUI fragment</a> <br> <br>");

			//sb.append("\nNumFolds = "+( _classifier.getNumFolds()+"<br>");

			//Instances instances = classifier

			String cname = _classifier.getClass().getName();
			String name;
			if (cname.startsWith("weka.classifiers.")) {
			    name = cname.substring("weka.classifiers.".length());
			} else {
			    name = cname;
			}

			sb.append("Classifer = "+name+"<br>");
			Evaluation eval;
			try {
				eval = new Evaluation(_instances);
				eval.evaluateModel(_classifier, _instances);
				sb.append("\nTotal number of Attributes = "+_instances.numAttributes()+"<br>");
				sb.append("\nTotal number of Instances = "+_instances.numInstances()+"<br>");
				sb.append("\nNumber of Classes = "+_instances.numClasses()+"<br>");
				sb.append("\nClass Feature = "+_instances.relationName()+"<br>");
				sb.append("\nError Rate = "+eval.errorRate()+"<br>");
				sb.append("\nCorrect = "+eval.pctCorrect()+" = "+eval.correct()+" out of "+ eval.numInstances()+"<br>");
				sb.append("\nIncorrect = "+eval.pctIncorrect()+" = "+eval.incorrect()+" out of "+ eval.numInstances()+"<br>");
				sb.append("\nFalseNeg = "+eval.numFalseNegatives(0)+"<br>");
				sb.append("\nFalsePos = "+eval.numFalsePositives(0)+"<br>");
				sb.append("\nTrueNeg = "+eval.numTrueNegatives(0)+"<br>");
				sb.append("\nTruePos = "+eval.numTruePositives(0)+"<br>");
				//results of class for an instance

				double test[] = _classifier.distributionForInstance(_instances.firstInstance());

				for (int i=0;i<test.length;i++)
					sb.append("\ntest"+i+"="+test[i]+"<br>");
				for (int i=0;i<20;i++){
					sb.append("\nAttStats"+i+_instances.attribute(i).name()+_instances.attributeStats(i)+"<br>");
				}
				sb.append("\n"+eval.confusionMatrix().toString()+"<br>");
				sb.append("\n"+eval.toSummaryString()+"<br>");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sb.append("\nProbability of Class: Sentimental:   0.432 <br>");
			sb.append("\nProbability of Class: UnSentimental: 0.362<br>");

			sb.append("\n<SCRIPT>\n");
			// Create an BarChart instance
			sb.append("var chart = new BarChart \n");
			// Add 4 values
			sb.append("chart.addValue(\"Test\",100)\n");
			sb.append("chart.addValue(\"Test2\",50)\n");
			sb.append("chart.addValue(\"Test3\",75)\n");
			sb.append("chart.addValue(\"Test4\",10)\n");
			// Specify additional parameters
			// Size of the bars
			sb.append("chart.barWidth=200\n");
			sb.append("chart.barHeight=10\n");
			// Align bars to the left or right
			sb.append("chart.barHOrientation = \"right\"\n");
			// Set the caption and its alignment
			sb.append("chart.setCaption(\"Top Attributes for Class: Sentimental\",\"Top\")\n");
			// Where to display the labels
			sb.append("chart.formatLabel(\"right\")\n");
			// Set the units
			sb.append("chart.unitLabel = \"Occurrences\"\n");
			// Set the background color
			sb.append("chart.bgColor = \"lightblue\"\n");
			// Generate the HTML
			sb.append("document.write(chart.draw())\n");
			sb.append("</SCRIPT>\n");


			return sb.toString();
		}

		/** This method gets called when a call with parameters is done to a given component
		 * webUI fragment
		 *
		 * @param target The target path
		 * @param request The request object
		 * @param response The response object
		 * @throws WebUIException A problem occurred during the call back
		 */
		public void handle(HttpServletRequest request, HttpServletResponse response)
		throws WebUIException {
			String sDone = request.getParameter("done");
			if ( sDone!=null ) {
				sem.release();
			}
			else
				emptyRequest(response);
		}

		/** Call at the end of an execution flow.
		 *
		 *
		 */
		public void dispose(ComponentContextProperties ccp) {

		}

		/** When ready for execution.
		 *
		 * @param cc The component context
		 * @throws ComponentExecutionException An exeception occurred during execution
		 * @throws ComponentContextException Illigal access to context
		 */
		public void execute(ComponentContext cc) throws ComponentExecutionException, ComponentContextException {
			try {

				_classifier  = (Classifier)cc.getDataComponentFromInput(DATA_INPUT_1);
				_instances  = (Instances)cc.getDataComponentFromInput(DATA_INPUT_2);
				sInstanceID = cc.getExecutionInstanceID();

				sem.acquire();
				cc.startWebUIFragment(this);
				sem.acquire();

				System.out.println(">>>Done");
				cc.stopWebUIFragment(this);
				System.out.flush();
			}
			catch ( Exception e ) {
				throw new ComponentExecutionException(e);
			}
		}

		/** Called when a flow is started.
		 *
		 */
		public void initialize(ComponentContextProperties cpp) {

		}
	}
