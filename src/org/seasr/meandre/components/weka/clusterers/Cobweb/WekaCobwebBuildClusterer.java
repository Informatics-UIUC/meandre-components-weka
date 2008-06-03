
package org.seasr.meandre.components.weka.clusterers.Cobweb;


import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import weka.core.Instances;
import weka.clusterers.Clusterer;
import weka.clusterers.Cobweb;
import weka.clusterers.ClusterEvaluation;
import org.seasr.meandre.components.weka.WekaConstants;
import org.meandre.core.ComponentContextProperties;

@Component(creator="Mary Pietrowicz", description="Builds Weka Cobweb Model",
		tags="weka cobweb model", name="WekaCobwebBuildClusterer")
public class WekaCobwebBuildClusterer implements ExecutableComponent
{
	//INPUT
	@ComponentInput(description="The training instances", name=WekaConstants.INSTANCES)
	final String DATA_INPUT_1 = WekaConstants.INSTANCES;

	//PROPERTY
	@ComponentProperty(description = "Acuity",
			name = "acuity", defaultValue = "1.0")
	final String PROPERTY1 = WekaConstants.ACUITY;
	
	@ComponentProperty(description="Cutoff",
			name="cutoff", defaultValue = "0.0028209479177387815")
	final String PROPERTY2 = WekaConstants.CUTOFF;
	
	@ComponentProperty(description = "Debug output? (Y/N)",
			name = "debug", defaultValue = "N")
	final static String PROPERTY3 = WekaConstants.DEBUG;
	
	@ComponentProperty(description="Set save instance data? (Y/N)",
			name="save_instance", defaultValue="N")
	final static String PROPERTY4 = WekaConstants.SAVE_INSTANCE;
	
	private boolean debug = false;
	private boolean save_instance_data = false;
	
	//OUTPUT
	@ComponentOutput(description="The generated model", name=WekaConstants.MODEL)
	final String DATA_OUTPUT_1=WekaConstants.MODEL;
	
	public boolean getDebug()
	{ return debug; }

	public void dispose(ComponentContextProperties ccp)
	{
	   System.out.println("Disposing WekaCobwebBuildClusterer...");
	}

	/*
	 * Builds a clusterer model.
	 *
	 * Inputs:
	 * instances:  The data set to use to build the model.
	 *
	 * Properties:
	 *
     *
     * Outputs:
     * model:  The cobweb model.
     * eval_results: The model evaluation results, in string form.
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
		System.out.println("Firing WekaCobwebBuildClusterer...");
		try
		{
			Instances instances = (Instances)(cc.getDataComponentFromInput(DATA_INPUT_1));
			Cobweb clusterer = new Cobweb();
			String acuity = cc.getProperty(PROPERTY1);
			String cutoff = cc.getProperty(PROPERTY2);
			String debug_str = cc.getProperty(PROPERTY3);
			String save_instance_data_str = cc.getProperty(PROPERTY4);
			if (save_instance_data_str.compareToIgnoreCase("Y") == 0)
			{
				save_instance_data = true;
			}		
			
			if (debug_str.compareToIgnoreCase("Y") == 0)
			{
				debug = true;
				System.out.println("Set acuity to: "+acuity);
				System.out.println("Set cutoff to: "+cutoff);
				System.out.println("Set save instance data is: "+save_instance_data);
			}
			
			clusterer.setCutoff( Double.parseDouble(cutoff) );
			clusterer.setAcuity( Double.parseDouble(acuity) );
			clusterer.setSaveInstanceData(save_instance_data);
			clusterer.buildClusterer(instances);

			cc.pushDataComponentToOutput(DATA_OUTPUT_1, clusterer);
		}
		catch (ComponentContextException ex1)
		{
			ex1.printStackTrace();
			throw new ComponentContextException("Error in WekaCobwebBuildClusterer: "+ex1.getMessage());
		}
		catch (Throwable th)
		{
			// Classify everything else as ComponentExecutionException
			th.printStackTrace();
			throw new ComponentExecutionException("Error in WekaCobwebBuildClusterer: "+th.getMessage());
		}
	}

	public void initialize(ComponentContextProperties ccp)
	{
		System.out.println("Initializing WekaCobwebBuildClusterer...");
	}
}

