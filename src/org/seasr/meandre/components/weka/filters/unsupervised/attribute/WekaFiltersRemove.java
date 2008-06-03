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

import java.util.logging.Logger;

/**
 * 
 * <p>
 * Title: WekaFiltersRemove
 * </p>
 * 
 * <p>
 * Description: Filters out a range of attributes in a dataset. Ranges may be
 *              specified as a comma-separated list of attribute indices.  An
 *              inclusive range is specified with a "-", and "first" and "last"
 *              indicate the first and last attributes, respectively.  
 *              Example:  first-4,6,8-10,last
 *             
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
		name="WekaFiltersRemove",
		tags="weka filter remove",
		creator="Mary Pietrowicz",
		description="Filters out unwanted attributes from a dataset."
		)
public class WekaFiltersRemove implements ExecutableComponent
{
	//INPUT
	@ComponentInput(
	description="The data instances.", 
	name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(
	description = "Verbose output? (Y/N)",
	name = "verbose", 
	defaultValue = "N")
	final static String PROPERTY1 = WekaConstants.VERBOSE;

	@ComponentProperty(
	description="List of attributes to ignore, e.g., first,-3, 5, 6-last",
    name="attr_list", 
    defaultValue=WekaConstants.NONE)
    final static String PROPERTY2 = WekaConstants.ATTR_LIST;

	//OUTPUT
	@ComponentOutput(
	description="The revised instance set with attributes filtered out.", 
	name=WekaConstants.FILTERED_INSTANCES)
	final String DATA_OUTPUT_1=WekaConstants.FILTERED_INSTANCES;

	/* The logger object to use for output. */
	private static Logger logger = null;
	
	public void dispose(ComponentContextProperties ccp)
	{
	   logger.info("Disposing WekaFiltersRemove...");
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
		logger.info("Firing WekaFiltersRemove...");
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

			String attr_list = cc.getProperty(PROPERTY2);

			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				logger.info("attr_list: "+attr_list);
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
	      
	        // String[] filter_opts = remove_filter.getOptions();
	        
			filtered_instances = Filter.useFilter(instances, remove_filter);

			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				logger.info("Removed indices: "+remove_filter.getAttributeIndices());
				logger.info("Filtered Instances: "+filtered_instances);
			}

			cc.pushDataComponentToOutput(DATA_OUTPUT_1, filtered_instances);
		}
		catch (ComponentContextException ex1)
		{
			logger.severe("ComponentContextException in WekaFiltersRemove: "+ex1.getMessage());
			throw new ComponentContextException("Error in WekaFiltersRemove: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			logger.severe("ComponentExecutionException in WekaFiltersRemove: "+th.getMessage());
			throw new ComponentExecutionException("Error in WekaFiltersRemove: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		logger = ccp.getLogger();
		logger.info("Initializing WekaFiltersRemove...");
	}
}
