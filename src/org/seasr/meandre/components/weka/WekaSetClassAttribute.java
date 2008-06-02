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

/**
 * A component that sets the class(/label/output) attribute for a set of
 * data instances by specifying the column index.
 *
 */

@Component(
		creator = "Peter Groves",
		description = "Sets which attribute for a set of instances should" +
				" be considered the class variable. Weka classifiers use " +
				" the class attribute as the attribute to be predicted. ",
		name = "WekaSetClassAttribute",
		tags = "weka")

public class WekaSetClassAttribute implements ExecutableComponent {

	//////////////////////////////////
	//Component Meta-data definitions
	/////////////////////////////////
	@ComponentInput(description = "A set of instances that has an attribute" +
			" to classify but it has not been specified.",
			name = "Instance_Set")
	final static String DATA_INPUT = "Instance_Set";

	@ComponentOutput(description = "The same set of instances that was input, " +
			"but with the attribute at the index specified in this Component's " +
			"properties set as the Class Attribute",
			name = "Marked_Instance_Set")
	final static String DATA_OUTPUT = "Marked_Instance_Set";

	@ComponentProperty(description = "Attribute (column) index to mark as " +
			"the class attribute. If negative, will count backwards from the " +
			"last attribute index",
			name = "Class_Attribute_Index", defaultValue = "-1")
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

			String colIdxStr = cc.getProperty(INDEX_PROPERTY);
			int colIdx = Integer.parseInt(colIdxStr);

			Instances markedData = setClass(targetData, colIdx);
			cc.pushDataComponentToOutput(DATA_OUTPUT, markedData);

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
