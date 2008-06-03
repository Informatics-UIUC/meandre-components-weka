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

package org.seasr.meandre.components.weka.clusterers.EM;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;

import weka.core.Instances;
import weka.clusterers.EM;

import java.util.logging.Logger;

import org.seasr.meandre.components.weka.WekaConstants;

/**
 * 
 * <p>
 * Title: WekaEMBuildClusterer
 * </p>
 * 
 * <p>
 * Description: A component which creates an EM cluster model.
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
name="WekaEMBuildClusterer",
tags="weka EM model cluster",
creator="Mary Pietrowicz",
description="Builds a Weka EM Model."
)
public class WekaEMBuildClusterer implements ExecutableComponent
{
	//INPUT
	@ComponentInput(
	description="The training instances loaded from a valid data file."+
	"Note that this algorithm does not accept class attributes.", 
	name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(
	description = "Debug output? (Y/N)",
	name = "debug", 
	defaultValue = "N")
	final static String PROPERTY1 = WekaConstants.DEBUG;

	@ComponentProperty(
	description="Number of clusters, -1 if cross validation is to be used.",
	name = "num_clusters", 
	defaultValue = WekaConstants.USE_CROSS_VALIDATION)
	final static String PROPERTY2=WekaConstants.NUM_CLUSTERS;

	@ComponentProperty(
	description="Maximum number of iterations",
	name = "max_num_iterations", 
	defaultValue = "100")
	final static String PROPERTY3=WekaConstants.MAX_NUM_ITERATIONS;

	@ComponentProperty(
	description="Random number seed",
	name = "rnd_seed", 
	defaultValue = "100")
	final static String PROPERTY4=WekaConstants.RND_SEED;

	@ComponentProperty(
	description="Minimum allowable standard deviation",
	name="std_dev", 
	defaultValue=".000001")
	final static String PROPERTY5=WekaConstants.STD_DEV;
	

	//OUTPUT
	@ComponentOutput(
	description="The generated model", 
	name=WekaConstants.MODEL)
	final String DATA_OUTPUT_1=WekaConstants.MODEL;
	
	/* The logger object to use for output. */
	private static Logger logger = null;
	
	/* The debug flag.  If true, the component will log information useful for debugging. */
	private static boolean debug = false;

	public void dispose(ComponentContextProperties ccp)
	{
	   logger.info("Disposing WekaEMBuildClusterer...");
	}

	/*
	 * Builds a clusterer EM model.
	 *
	 * Inputs:
	 * instances:  The data set to use to build the model.
	 *
	 * Properties:
	 * verbose: Verbose output if set to true.
	 * num_clusters: Number of clusters, or defaulted to using cross-validation to determine it
	 * max_num_iterations: Maximum number of iterations allowed for convergence
	 * rnd_seed: The random number generator seed
	 * std_dev: The minimum allowable minimum standard deviation
     *
     * Outputs:
     * model:  The EM model.
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
		logger.info("Firing WekaEMBuildClusterer...");
		try
		{
			Instances instances = (Instances)(cc.getDataComponentFromInput(WekaConstants.INSTANCES));

			// Process user-selected options
			// Debug property
			String verbose = cc.getProperty(PROPERTY1);
			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				debug = true;
				logger.info("EM Instances: "+instances);
			}

			// Number of clusters
			String num_clusters = cc.getProperty(PROPERTY2);
			int num_clusters_int = -1;
			if (num_clusters.compareToIgnoreCase(WekaConstants.USE_CROSS_VALIDATION) != 0)
			{
				num_clusters_int = Integer.parseInt(num_clusters);
			}

			// Maximum number of iterations for convergence
			String max_iterations = cc.getProperty(PROPERTY3);
			int max_iterations_int = Integer.parseInt(max_iterations);

			// Random number seed
			String rnd_num = cc.getProperty(PROPERTY4);
			int rnd_num_int = Integer.parseInt(rnd_num);

			// Minimum allowable std deviation
			String min_std_dev = cc.getProperty(PROPERTY5);
			float min_std_dev_flt = Float.parseFloat(min_std_dev);

			EM clusterer = new EM();
			if (num_clusters_int > 0)
			{ clusterer.setNumClusters(num_clusters_int); }
			clusterer.setMaxIterations(max_iterations_int);
			clusterer.setSeed(rnd_num_int);
			clusterer.setMinStdDev(min_std_dev_flt);
			clusterer.setDebug(debug);
			clusterer.buildClusterer(instances);

			//if (verbose.compareToIgnoreCase("Y") == 0)
			if (debug == true)
			{
                logger.info("Options: "+clusterer.getOptions());
                logger.info("String cluster representation: "+clusterer.toString());
                logger.info("Rnd seed: "+clusterer.getSeed());
                logger.info("Min std dev: "+clusterer.getMinStdDev());
                logger.info("Num clusters: "+clusterer.getNumClusters());
                logger.info("Max iterations: "+clusterer.getMaxIterations());
			}

			cc.pushDataComponentToOutput(DATA_OUTPUT_1, clusterer);
		}
		catch (ComponentContextException ex1)
		{
			if (debug == true)
			{
			  ex1.printStackTrace();
			}
			logger.severe("Error in WekaEMBuildClusterer: "+ex1.getMessage());
			throw new ComponentContextException("Error in WekaEMBuildClusterer: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			if (debug == true)
			{
			  th.printStackTrace();
			}
			logger.severe("Error in WekaEMBuildClusterer: "+th.getMessage());
			throw new ComponentExecutionException("Error in WekaEMBuildClusterer: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		logger = ccp.getLogger();
		logger.info("Initializing WekaEMBuildClusterer...");
	}
}

