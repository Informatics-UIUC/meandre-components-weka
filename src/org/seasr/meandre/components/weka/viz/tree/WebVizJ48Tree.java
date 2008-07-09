/**
 * University of Illinois/NCSA
 * Open Source License
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.  
 * All rights reserved.
 * 
 * Developed by: 
 * 
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 * 
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions: 
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers. 
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the 
 *    documentation and/or other materials provided with the distribution. 
 * 
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */ 

package org.seasr.meandre.components.weka.viz.tree;

import gnu.formj.dhtml.Window;
import gnu.formj.html.A;
import gnu.formj.html.Div;
import gnu.formj.html.Label;
import gnu.formj.html.style.Style;
import gnu.formj.html.table.Table;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.meandre.core.ComponentContextProperties;

import weka.classifiers.trees.J48;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;

/** A demo of a web UI callback.
 *
 * @author Xavier llor&agrave
 * Modified by Lily Dong;
 *
 */
@Component(creator="Xavier llor&agrave",
           description="visualization of J48 Tree using dhtml",
           name="WebVizJ48Tree",
           tags="visualization")

public class WebVizJ48Tree implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentInput(description="input data",
                    name= "tree")
    final static String DATA_INPUT = "tree";


	/** The blocking semaphore */
	private Semaphore sem = new Semaphore(1,true);

	/** The message to print */
	private J48 j48Tree = null;

	/** The instance ID */
	private String sInstanceID = null;

	/** This method gets call when a request with no parameters is made to a
	 * component webui fragment.
	 *
	 * @param response The response object
	 * @throws WebUIException Some problem arised during execution and something went wrong
	 */
	public void emptyRequest(HttpServletResponse response)
	throws WebUIException {
		try {
			response.getWriter().println(getViz());
		} catch (IOException e) {
			throw new WebUIException(e);
		}
	}

	/** A simple message.
	 *
	 * @return The html containing the page
	 */
	private String getViz() {

		// The message stuff
		Window wmfMessage = new Window("Learned tree","400","400");
		wmfMessage.setContent(new Label("<pre>"+j48Tree.toString()+"</pre>"));
		wmfMessage.setMovable(true);
		wmfMessage.setMaximizable(true);
		wmfMessage.setMinimizable(true);

		// Done link
		A linkDone = new A();
		linkDone.setHref("/"+sInstanceID +"?done=true");
		linkDone.setContent(new Label("Done with the tree visualization!"));
		linkDone.setTitle("Done with the tree visualization!");
		linkDone.setToolTip("Done with the tree visualization!");

		Div div = new Div();
		div.add(linkDone);
		div.add(wmfMessage);

                String s2 = div.toString();

                //replace the relative path with the absolute path
                s2 = s2.replaceAll("script/", "http://norma.ncsa.uiuc.edu/public-dav/Meandre/javascript/formj/script/");
                s2 = s2.replaceAll("theme/", "http://norma.ncsa.uiuc.edu/public-dav/Meandre/javascript/formj/theme/");

		return s2; //div.toString();
	}

	/** This method gets called when a call with parameters is done to a given component
	 * webUI fragment
	 *
	 * @param target The target path
	 * @param request The request object
	 * @param response The response object
	 * @throws WebUIException A problem arised during the call back
	 */
	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws WebUIException {
		String sDone = request.getParameter("done");
		if ( sDone!=null ) {
			sem.release();
		}
		else
			emptyRequest(response);
	}

	/** Call at the end of an execution flow.
	 *
	 *
	 */
	public void dispose(ComponentContextProperties ccp) {

	}

	/** When ready for execution.
	 *
	 * @param cc The component context
	 * @throws ComponentExecutionException An exeception occurred during execution
	 * @throws ComponentContextException Illigal access to context
	 */
	public void execute(ComponentContext cc) throws ComponentExecutionException, ComponentContextException {
		try {
			System.out.println("Firing the web ui component");
			j48Tree  = (J48) cc.getDataComponentFromInput("tree");
			sInstanceID = cc.getExecutionInstanceID();
			sem.acquire();
			System.out.println(">>>Rendering...");
			cc.startWebUIFragment(this);
			System.out.println(">>>STARTED");
			sem.acquire();
			System.out.println(">>>Done");
			cc.stopWebUIFragment(this);
		}
		catch ( Exception e ) {
			throw new ComponentExecutionException(e);
		}

	}

	/** Called when a flow is started.
	 *
	 */
	public void initialize(ComponentContextProperties cpp) {

	}

}
