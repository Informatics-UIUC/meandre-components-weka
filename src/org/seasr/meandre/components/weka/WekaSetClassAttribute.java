/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright © 2008, NCSA.  All rights reserved.
 * 
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 * 
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 */

package org.seasr.meandre.components.weka;

import weka.core.Instances;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;

import java.util.logging.Logger;

/**
 * A component that sets the class(/label/output) attribute for a set of
 * data instances by specifying the column index.
 *
 */

/**
 * 
 * <p>
 * Title: WekaSetClassAttribute
 * </p>
 * 
 * <p>
 * Description: A component that sets the class(/label/output) attribute for a 
 * set of data instances by specifying the column index.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Automated Learning Group, NCSA
 * </p>
 * 
 * @author Peter Groves
 * @author Mary Pietrowicz
 * @version 1.1
 */

@Component(
		name="WekaSetClassAttribute",
		tags="weka attribute class",
		creator="Peter Groves",
		description="Sets which attribute for a set of instances should" +
				" be considered the class variable. Weka classifiers use " +
				" the class attribute as the attribute to be predicted. "
		)
public class WekaSetClassAttribute implements ExecutableComponent {

	//////////////////////////////////
	//Component Meta-data definitions
	/////////////////////////////////
	
	@ComponentInput(
	description = "A set of instances that has an attribute" +
			" to classify but it has not been specified.",
	name = "Instance_Set")
	final static String DATA_INPUT = "Instance_Set";

	@ComponentOutput(
	description = "The same set of instances that was input, " +
			"but with the attribute at the index specified in this Component's " +
			"properties set as the Class Attribute",
	name = "Marked_Instance_Set")
	final static String DATA_OUTPUT = "Marked_Instance_Set";

	@ComponentProperty(
	description = "Attribute (column) index to mark as " +
			"the class attribute. If negative, will count backwards from the " +
			"last attribute index",
	name = "Class_Attribute_Index", 
	defaultValue = "-1")
	final static String INDEX_PROPERTY = "Class_Attribute_Index";

	///////////////
	//Component Logic
	///////////////

	/**
	 * sets the data set attribute with index attributeIndex to be the class
	 * variable for the data set. attributeIndex can be negative to mean
	 * count backwards from the end.
	 */
	public static Instances setClass(Instances dataSet, int attributeIndex){
		if(attributeIndex >= 0){
			dataSet.setClassIndex(attributeIndex);
		}else{
			dataSet.setClassIndex(dataSet.numAttributes() + attributeIndex);
		}
		return dataSet;
	}


	/////////////////////
	//Meandre Interfaces
	//////////////////////

	/** 
	 * This method takes an Instances object and an index pointing to the class 
	 * object, marks the class attributed, and outputs the instance set with the
	 * class attribute marked.
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

			String colIdxStr = cc.getProperty(INDEX_PROPERTY);
			int colIdx = Integer.parseInt(colIdxStr);

			Instances markedData = setClass(targetData, colIdx);
			cc.pushDataComponentToOutput(DATA_OUTPUT, markedData);

		}
		catch ( Exception e ) {
			throw new ComponentExecutionException(e);
		}

	}

	/** 
	 * This method is called when the Meandre Flow execution begins.
     **/	
	public void initialize(ComponentContextProperties ccp) {	}

    /**
	 * This method is called when the Meandre Flow execution ends.
     **/	
	public void dispose(ComponentContextProperties ccp) { }

}
