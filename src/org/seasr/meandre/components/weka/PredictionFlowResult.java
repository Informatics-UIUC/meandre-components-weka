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
import java.util.HashMap;

/**This class stores the prediction results and the class values.
 *
 * @author Amit Kumar
 * Created on Jan 25, 2008 2:08:40 PM
 *
 */
public class PredictionFlowResult {

	private String resultSummary;
	private ArrayList<PredictionResult> result = new ArrayList<PredictionResult>(10);
	private ArrayList<Object> classList = new ArrayList<Object>(4);

	private HashMap<String,String>  trainingList= new HashMap<String,String>(10);



	public void addResult(PredictionResult pr){
		result.add(pr);
	}

	public  ArrayList<PredictionResult> getResult(){
		return result;
	}


	public void setResult(ArrayList<PredictionResult> result){
		this.result = result;
	}

	public void addClass(Object classVal){
		classList.add(classVal);
	}

	public ArrayList<Object> getClassList(){
		return classList;
	}

	/**
	 * @return the resultSummary
	 */
	public String getResultSummary() {
		return resultSummary;
	}

	/**
	 * @param resultSummary the resultSummary to set
	 */
	public void setResultSummary(String resultSummary) {
		this.resultSummary = resultSummary;
	}

	/**
	 * @return the trainingList
	 */
	public HashMap<String,String> getTrainingList() {
		return trainingList;
	}

	/**
	 * @param trainingList the trainingList to set
	 */
	public void setTrainingList(HashMap<String,String> trainingList) {
		this.trainingList = trainingList;
	}

	/**
	 * @param classList the classList to set
	 */
	public void setClassList(ArrayList<Object> classList) {
		this.classList = classList;
	}

}
