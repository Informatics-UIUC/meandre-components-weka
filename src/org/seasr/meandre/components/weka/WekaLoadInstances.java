package org.seasr.meandre.components.weka;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import org.meandre.core.ComponentContextProperties;

/** This component takes a filename as input and
 *  loads it into the Weka instances object.
 *
 * @author Loretta Auvil
 * @author Boris Capitanu
 */

@Component(
		creator = "Loretta Auvil",
		description = "This component takes a filename as input " +
					  "and loads it into the Weka instances object.",
		name = "WekaLoadInstances",
		tags = "weka")

public class WekaLoadInstances implements ExecutableComponent {

	@ComponentInput(description = "Input URL", name = "inputURL")
	public final static String DATA_INPUT_INPUTURL = "inputURL";

	@ComponentOutput(description = "Weka instances", name = "instances")
	public final static String DATA_OUTPUT_INSTANCES = "instances";

	@ComponentProperty(description = "Print instances? (Y/N)",
			name = "printInstances", defaultValue = "N")
	public final static String DATA_PROPERTY_PRINTINSTANCES = "printInstances";

    /** This method is called when the Meandre Flow execution is completed.
    *
    */
	public void initialize(ComponentContextProperties ccp) {

	}

    /** This method is called when the Meandre Flow executes the component.
    *
    * @param  context The component execution context
    * @throws ComponentExecutionException If a fatal condition arises during
    *         the execution of a component, a ComponentExecutionException
    *         should be thrown to signal termination of execution required.
    * @throws ComponentContextException A violation of the component context
    *         access was detected
    */
	public void execute(ComponentContext context)
		throws ComponentExecutionException, ComponentContextException {

		// Retrieve the input URL passed by the previous component in the flow
		String inputURL = (String)context.getDataComponentFromInput(DATA_INPUT_INPUTURL);

		String strPrintInstances = context.getProperty(DATA_PROPERTY_PRINTINSTANCES);
		Boolean printInstances = strPrintInstances.toUpperCase().equals("Y");

		// Check whether the input in a URL
		URL url = null;
		try {
			url = new URL(inputURL);
		}
		catch (MalformedURLException e) {}

		// Check if the URL references a CSV file
		if (url != null) {
			if (inputURL.toLowerCase().endsWith(".csv")) {
				System.out.println("WekaLoadInstances: Detected a URL CSV file");

				// Get the file name referenced by the URL
		    	String fileName = null;
		    	StringTokenizer st = new StringTokenizer(url.getFile(), File.separator);
		    	while (st.hasMoreTokens())
		    		fileName = st.nextToken();

		    	// Create a temporary file to store the contents of the file referenced in URL
		    	File localTempFile = null;
		    	OutputStream outStream = null;
		    	try {
		    		localTempFile = File.createTempFile(fileName.substring(0, fileName.lastIndexOf('.')), ".csv");
		    		localTempFile.deleteOnExit();

		    		outStream = new FileOutputStream(localTempFile);
		    	}
		    	catch (IOException e) {
		    		System.out.println("WekaLoadInstances: Cannot create temporary file!");
		    		e.printStackTrace();
		    	}

	    		System.out.println("WekaLoadInstances: Copying data from '" + inputURL +
	    				"' to '" + localTempFile.getAbsolutePath() + "'");

	    	   	// Copy the URL stream to the output stream
				copyFromURL(url, outStream);

				// Set the input URL to the temporary file
				inputURL = localTempFile.getAbsolutePath();
			}
		}

		// Read all the instances in the file (ARFF, CSV, XRFF, ...)
		DataSource source;
		try {
			source = new DataSource(inputURL);
			Instances instances = source.getDataSet();

			// Make the last attribute be the class
			instances.setClassIndex(instances.numAttributes() - 1);

			if (printInstances) {
				// Print header and instances.
				System.out.println("\nDataset:\n");
				System.out.println(instances);
			}

			context.pushDataComponentToOutput(DATA_OUTPUT_INSTANCES, instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Copies the data referenced by url to the output stream outStream
	 *
	 * @param url - the URL referencing the data to be copied
	 * @param outStream - the output stream to hold the copied data
	 */
    private void copyFromURL(URL url, OutputStream outStream) {
    	InputStream urlStream = null;

    	try {
    		urlStream = url.openStream();
    		byte[] buffer = new byte[16384];  // TODO: should the buffer be customizable?
    		int len;
    		while ((len = urlStream.read(buffer)) > 0)
    			outStream.write(buffer, 0, len);
    	}
    	catch (IOException e) {
    		System.out.println("WekaLoadInstances: Cannot copy data from URL!");
    		e.printStackTrace();
    	}
    	finally {
    		try {
    			if (urlStream != null) urlStream.close();
    			if (outStream != null) outStream.close();
    		}
    		catch (IOException e) {}
    	}
	}

	/** This method is invoked when the Meandre Flow is being prepared for
     * getting run.
     *
     */
	public void dispose(ComponentContextProperties ccp) {

	}
}

