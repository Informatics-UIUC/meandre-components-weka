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
//import weka.filters.unsupervised.attribute.Remove;

@Component(creator="Mary Pietrowicz", description="Filters out unwanted attributes from instances",
		tags="weka filter remove", name="WekaFiltersRemove")
public class WekaFiltersRemove implements ExecutableComponent
{
	//INPUT
	@ComponentInput(description="Data instances", name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(description = "Verbose output? (Y/N)",
			name = "verbose", defaultValue = "N")
	final static String PROPERTY1 = WekaConstants.VERBOSE;

	@ComponentProperty(description="List of attributes to ignore, e.g., first,-3, 5, 6-last",
		    name="attr_list", defaultValue=WekaConstants.NONE)
    final static String PROPERTY2 = WekaConstants.ATTR_LIST;

	//OUTPUT
	@ComponentOutput(description="The revised instance set", name=WekaConstants.FILTERED_INSTANCES)
	final String DATA_OUTPUT_1=WekaConstants.FILTERED_INSTANCES;

	public void dispose(ComponentContextProperties ccp)
	{
	   System.out.println("Disposing Remove...");
	}

	/*
	 * Filters out unwanted attributes from an instance set.
	 *
	 * Inputs:
	 * instances:  The instance set to process.
	 *
	 * Properties:
	 * verbose: Verbose output if set to true.
	 * attr_list: The list of attributes to remove from the instance list.
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
		System.out.println("Firing Remove...");
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

			String attr_list = cc.getProperty(PROPERTY2);

			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				System.out.println("attr_list: "+attr_list);
			}

			weka.filters.unsupervised.attribute.Remove remove_filter =
	            	new weka.filters.unsupervised.attribute.Remove();

            // WARNING:  Do NOT change the order of the next 3 lines of code.
			//           WEKA BUG
			//           The setAttributIndices call must be first, or the filter
			//           will not work!!!
			remove_filter.setAttributeIndices(""+attr_list);
	        remove_filter.setInvertSelection(false);
	        remove_filter.setInputFormat(instances);
	        // END WARNING
	      
	        String[] filter_opts = remove_filter.getOptions();
	        System.out.println(filter_opts[0]);
	        System.out.println("getAttributeIndices returns: "+remove_filter.getAttributeIndices());

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
			throw new ComponentContextException("Error in WekaFiltersRemove: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			th.printStackTrace();
			throw new ComponentExecutionException("Error in WekaFiltersRemove: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		System.out.println("Initializing Remove...");
	}
}
