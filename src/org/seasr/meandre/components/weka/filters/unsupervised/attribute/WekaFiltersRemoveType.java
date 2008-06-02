package org.seasr.meandre.components.weka.filters.unsupervised.attribute;


import java.util.Enumeration;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.seasr.meandre.components.weka.WekaConstants;
import org.meandre.core.ComponentContextProperties;

import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
//import weka.filters.unsupervised.attribute.Remove;

@Component(creator="Mary Pietrowicz", description="Filters out unwanted attributes of a given type from instances",
		tags="weka filter remove", name="WekaFiltersRemoveType")
public class WekaFiltersRemoveType implements ExecutableComponent
{
	//INPUT
	@ComponentInput(description="Data instances", name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(description = "Verbose output? (Y/N)",
			name = "verbose", defaultValue = "N")
	final static String PROPERTY1 = WekaConstants.VERBOSE;

	@ComponentProperty(description="Type of attribute to delete (nominal/numeric/string/date/none)",
			name="attr_type", defaultValue=WekaConstants.NONE)
	final static String PROPERTY2 = WekaConstants.ATTR_TYPE;

	//OUTPUT
	@ComponentOutput(description="The revised instance set", name=WekaConstants.FILTERED_INSTANCES)
	final String DATA_OUTPUT_1=WekaConstants.FILTERED_INSTANCES;

	public void dispose(ComponentContextProperties ccp)
	{
	   System.out.println("Disposing WekaFiltersRemoveType...");
	}

	/*
	 * Filters out unwanted attributes from an instance set.
	 *
	 * Inputs:
	 * instances:  The instance set to process.
	 *
	 * Properties:
	 * verbose: Verbose output if set to true.
	 * attr_type: The attribute type to remove from the instance set.
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
		System.out.println("Firing WekaFiltersRemoveType...");
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

			String attr_type = cc.getProperty(PROPERTY2);
			System.out.println("attr_type: "+attr_type);

			if (verbose.compareToIgnoreCase(WekaConstants.NONE) == 0)
			{
				filtered_instances = instances;
			}
			else
			{

			   weka.filters.unsupervised.attribute.RemoveType remove_filter =
	            	new weka.filters.unsupervised.attribute.RemoveType();

			  String type_to_delete = "-T "+attr_type;
			  String[] options_list  = new String[2];
			  options_list[0] = "-T ";
			  options_list[1] = attr_type;
			  
String tString = weka.core.Utils.getOption('T', options_list);
System.out.println("tString is: "+tString);
			  
System.out.println("type to delete: "+options_list[0]);

System.out.println("Available options: ");
for (Enumeration enumer = remove_filter.listOptions(); enumer.hasMoreElements();)
{
	System.out.println(enumer.nextElement());
}
System.out.println(remove_filter.listOptions());

			  remove_filter.setOptions(options_list);
//	          remove_filter.setInvertSelection(false);
	          remove_filter.setInputFormat(instances);

	          System.out.println("RemoveType: "+remove_filter.getOptions());

			  filtered_instances = Filter.useFilter(instances, remove_filter);

			  if (verbose.compareToIgnoreCase("Y") == 0)
			  {
				  System.out.println("Filtered Instances: ");
				  System.out.println(filtered_instances);
			  }
			}
			cc.pushDataComponentToOutput(DATA_OUTPUT_1, filtered_instances);
		}
		catch (ComponentContextException ex1)
		{
			ex1.printStackTrace();
			throw new ComponentContextException("Error in WekaFiltersRemoveType: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			th.printStackTrace();
			throw new ComponentExecutionException("Error in WekaFiltersRemoveType: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		System.out.println("Initializing WekaFiltersRemoveType...");
	}
}

