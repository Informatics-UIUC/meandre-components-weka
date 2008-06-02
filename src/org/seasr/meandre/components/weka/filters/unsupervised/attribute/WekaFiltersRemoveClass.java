package org.seasr.meandre.components.weka.filters.unsupervised.attribute;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.seasr.meandre.components.weka.WekaConstants;

import weka.core.Instances;
import weka.filters.Filter;

@Component(creator="Mary Pietrowicz", description="Filters out a class attribute, useful for clustering algs",
		tags="weka filter remove", name="WekaFiltersRemoveClass")
public class WekaFiltersRemoveClass implements ExecutableComponent
{
	//INPUT
	@ComponentInput(description="Data instances", name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(description = "Verbose output? (Y/N)",
			name = "verbose", defaultValue = "N")
	final static String PROPERTY1 = WekaConstants.VERBOSE;

	//OUTPUT
	@ComponentOutput(description="The revised instance set", name=WekaConstants.FILTERED_INSTANCES)
	final String DATA_OUTPUT_1=WekaConstants.FILTERED_INSTANCES;

	public void dispose(ComponentContextProperties ccp)
	{
	   System.out.println("Disposing WekaFiltersRemoveClass...");
	}

	/*
	 * Filters out a class attribute from an instance set.
	 *
	 * Inputs:
	 * instances:  The instance set to process.
	 *
	 * Properties:
	 * verbose: Verbose output if set to true.
     *
     * Outputs:
     * filtered_instances: The set of filtered instances.
	 *
	 * @throws ComponentExecutionException If a fatal condition arises during
     *         the execution of a component, a ComponentExecutionException
     *         should be thrown to signal termination of execution required.
     * @throws ComponentContextException A violation of the component context
     *         access was detected
	 *
	 */
	public void execute(ComponentContext cc)
	throws ComponentExecutionException, ComponentContextException
	{
		System.out.println("Firing WekaFiltersRemoveClass...");
		try
		{
			Instances instances = (Instances)(cc.getDataComponentFromInput(WekaConstants.INSTANCES));
            Instances filtered_instances = null;

			// Process user-selected options
			// Verbose property
			String verbose = cc.getProperty(PROPERTY1);
			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				System.out.println("Original Instances: ");
				System.out.println(instances);
			}


			weka.filters.unsupervised.attribute.Remove remove_filter =
	            	new weka.filters.unsupervised.attribute.Remove();

			int class_index = instances.classIndex()+1;
			System.out.println("Class index is: "+class_index);
            remove_filter.setAttributeIndices(""+class_index);
	        remove_filter.setInvertSelection(false);
	        remove_filter.setInputFormat(instances);
	       
			filtered_instances = Filter.useFilter(instances, remove_filter);

			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				System.out.println("Filtered Instances: ");
				System.out.println(filtered_instances);
			}

			cc.pushDataComponentToOutput(DATA_OUTPUT_1, filtered_instances);
		}
		catch (ComponentContextException ex1)
		{
			ex1.printStackTrace();
			throw new ComponentContextException("Error in WekaFiltersRemoveClass: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			th.printStackTrace();
			throw new ComponentExecutionException("Error in WekaFiltersRemoveClass: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		System.out.println("Initializing WekaFiltersRemoveClass...");
	}
}

