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

package org.seasr.meandre.components.weka.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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

/** 
 * <p>
 * Title: WekaLoadInstances
 * </p>
 * 
 * <p>
 * Description:  This component takes a filename/URL as input and outputs it 
 * as a Weka Instances object.
 * </p>
 * 
 *  <p>
 * Copyright: Copyright (c) 2008
 * </p>
 *
 * @author Loretta Auvil
 * @author Boris Capitanu
 * @author Mary Pietrowicz
 */


@Component(
		name="WekaLoadInstances",
		tags="weka instances",
		creator="Loretta Auvil",
		description="This component takes a filename as input and "+
		            "loads it into the Weka instances object."
		)
public class WekaLoadInstances implements ExecutableComponent {

	@ComponentInput(
	description = "Input URL to the object containing the instance data.", 
	name = "inputURL")
	public final static String DATA_INPUT_INPUTURL = "inputURL";

	@ComponentOutput(
	description = "Weka instances object.", 
	name = "instances")
	public final static String DATA_OUTPUT_INSTANCES = "instances";

	@ComponentOutput(
			description = "Just the filename of the file.", 
			name = "filename")
			public final static String DATA_OUTPUT_FILENAME = "filename";

	@ComponentProperty(
	description = "Print instances? (Y/N)",
	name = "printInstances", 
	defaultValue = "N")
	public final static String DATA_PROPERTY_PRINTINSTANCES = "printInstances";

	/* The logger object to use for output. */
	private static PrintStream logger = null;
	
 
	/** 
	 * This method is called when the Meandre Flow execution begins.
     **/	
	public void initialize(ComponentContextProperties ccp) 
	{
		logger = ccp.getOutputConsole();
		logger.println("Initializing WekaLoadInstances...");
	}

    /** 
    * This method is called when the Meandre Flow executes the component.
    * It reads in instance data from a source, creates a Weka instances
    * object, and outputs the instances.
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
		String fileName = null;
    	
		try {
			url = new URL(inputURL);
		}
		catch (MalformedURLException e) {}

		// Check if the URL references a CSV file
		if (url != null) {
			if (inputURL.toLowerCase().endsWith(".csv")) 
			{
				logger.println("WekaLoadInstances: Detected a URL CSV file");

				// Get the file name referenced by the URL
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
		    	catch (IOException e) 
		    	{
		    		logger.println("WekaLoadInstances:  Cannot create temporary file. "+e.getMessage());
		    		e.printStackTrace();
		    	}

		    	logger.println("WekaLoadInstances: Copying data from '" + inputURL +
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
				logger.println("\nDataset:\n"+instances);
			}

			context.pushDataComponentToOutput(DATA_OUTPUT_INSTANCES, instances);
			context.pushDataComponentToOutput(DATA_OUTPUT_FILENAME, fileName);
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
    	catch (IOException e) 
    	{
    		logger.println("WekaLoadInstances: Cannot copy data from URL! \n"+e.getMessage());
    	}
    	finally {
    		try {
    			if (urlStream != null) urlStream.close();
    			if (outStream != null) outStream.close();
    		}
    		catch (IOException e) {}
    	}
	}

	/** 
	 * This method is invoked when the Meandre Flow execution ends.
     **/
	public void dispose(ComponentContextProperties ccp) 
	{
	   logger.println("Disposing WekaLoadInstances...");
	}
}

