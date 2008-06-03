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

import java.util.logging.Logger;


// WARNING:  This component has not been tested.
//           Use it at your own risk.

/**
 * 
 * <p>
 * Title: WekaFiltersRemoveType
 * </p>
 * 
 * <p>
 * Description: Filters out attributes of a given type from an instance set. 
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
 * @author Mary Pietrowicz
 * @version 1.0
 */

@Component(
		name="WekaFiltersRemoveType",
		tags="weka filter remove type",
		creator="Mary Pietrowicz",
		description="Filters out unwanted attributes of a given type from an instance set."
		)
public class WekaFiltersRemoveType implements ExecutableComponent
{
	//INPUT
	@ComponentInput(
	description="Data instances, of Weka type Instances.", 
	name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(
	description = "Verbose output? (Y/N)",
	name = "verbose", 
	defaultValue = "N")
	final static String PROPERTY1 = WekaConstants.VERBOSE;

	@ComponentProperty(
	description="Type of attribute to delete (nominal/numeric/string/date/none)",
	name="attr_type", 
	defaultValue=WekaConstants.NONE)
	final static String PROPERTY2 = WekaConstants.ATTR_TYPE;

	//OUTPUT
	@ComponentOutput(
	description="The revised instance set with attribute types removed.", 
	name=WekaConstants.FILTERED_INSTANCES)
	final String DATA_OUTPUT_1=WekaConstants.FILTERED_INSTANCES;

	/* The logger object to use for output. */
	private static Logger logger = null;
	
	public void dispose(ComponentContextProperties ccp)
	{
	   logger.info("Disposing WekaFiltersRemoveType...");
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
		logger.info("Firing WekaFiltersRemoveType...");
		try
		{
			Instances instances = (Instances)(cc.getDataComponentFromInput(WekaConstants.INSTANCES));
            Instances filtered_instances = null;

			// Process user-selected options
			// Verbose property
			String verbose = cc.getProperty(PROPERTY1);
			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				logger.info("Original Instances: "+instances);
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
              logger.info("tString is: "+tString);
			  
              logger.info("type to delete: "+options_list[0]);

//System.out.println("Available options: ");
//for (Enumeration enumer = remove_filter.listOptions(); enumer.hasMoreElements();)
//{
//	System.out.println(enumer.nextElement());
//}
//System.out.println(remove_filter.listOptions());

			  remove_filter.setOptions(options_list);
//	          remove_filter.setInvertSelection(false);
	          remove_filter.setInputFormat(instances);

// System.out.println("RemoveType: "+remove_filter.getOptions());

			  filtered_instances = Filter.useFilter(instances, remove_filter);

			  if (verbose.compareToIgnoreCase("Y") == 0)
			  {
				  logger.info("Filtered Instances: "+filtered_instances);
			  }
			}
			cc.pushDataComponentToOutput(DATA_OUTPUT_1, filtered_instances);
		}
		catch (ComponentContextException ex1)
		{
			logger.severe("ComponentContextException in WekaFiltersRemovetype: "+ex1.getMessage());
			throw new ComponentContextException("Error in WekaFiltersRemoveType: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			logger.severe("ComponentExecutionException in WekaFiltersRemoveType: "+th.getMessage());
			throw new ComponentExecutionException("Error in WekaFiltersRemoveType: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		logger = ccp.getLogger();
		logger.info("Initializing WekaFiltersRemoveType...");
	}
}

