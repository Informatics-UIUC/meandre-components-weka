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
import weka.clusterers.ClusterEvaluation;

import org.seasr.meandre.components.weka.WekaConstants;

@Component(creator="Mary Pietrowicz", description="Builds Weka EM Model",
		tags="weka EM model cluster", name="WekaEMBuildClusterer")
public class WekaEMBuildClusterer implements ExecutableComponent
{
	//INPUT
	@ComponentInput(description="The training instances", name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(description = "Debug output? (Y/N)",
			name = "debug", defaultValue = "N")
	final static String PROPERTY1 = WekaConstants.DEBUG;

	@ComponentProperty(description="Number of clusters",
			name = "num_clusters", defaultValue = WekaConstants.USE_CROSS_VALIDATION)
	final static String PROPERTY2=WekaConstants.NUM_CLUSTERS;

	@ComponentProperty(description="Maximum number of iterations",
			name = "max_num_iterations", defaultValue = "100")
	final static String PROPERTY3=WekaConstants.MAX_NUM_ITERATIONS;

	@ComponentProperty(description="Random number seed",
			name = "rnd_seed", defaultValue = "100")
	final static String PROPERTY4=WekaConstants.RND_SEED;

	@ComponentProperty(description="Minimum allowable standard deviation",
			name="std_dev", defaultValue=".000001")
	final static String PROPERTY5=WekaConstants.STD_DEV;
	

	//OUTPUT
	@ComponentOutput(description="The generated model", name=WekaConstants.MODEL)
	final String DATA_OUTPUT_1=WekaConstants.MODEL;

	public void dispose(ComponentContextProperties ccp)
	{
	   System.out.println("Disposing WekaEMBuildClusterer...");
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
		System.out.println("Firing WekaEMBuildClusterer...");
		try
		{
			Instances instances = (Instances)(cc.getDataComponentFromInput(WekaConstants.INSTANCES));

			// Process user-selected options
			// Debug property
			boolean debug = false;
			String verbose = cc.getProperty(PROPERTY1);
			if (verbose.compareToIgnoreCase("Y") == 0)
			{
				System.out.println("EM Instances: ");
				System.out.println(instances);
				debug = true;
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

			if (verbose.compareToIgnoreCase("Y") == 0)
			{
                System.out.println("Options: "+clusterer.getOptions());
                System.out.println("String cluster representation: ");
                System.out.println(clusterer.toString());
                System.out.println("Rnd seed: "+clusterer.getSeed());
                System.out.println("Min std dev: "+clusterer.getMinStdDev());
                System.out.println("Num clusters: "+clusterer.getNumClusters());
                System.out.println("Max iterations: "+clusterer.getMaxIterations());
			}

			cc.pushDataComponentToOutput(DATA_OUTPUT_1, clusterer);
		}
		catch (ComponentContextException ex1)
		{
			ex1.printStackTrace();
			throw new ComponentContextException("Error in WekaEMBuildClusterer: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			th.printStackTrace();
			throw new ComponentExecutionException("Error in WekaEMBuildClusterer: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		System.out.println("Initializing WekaEMBuildClusterer...");
	}
}

