/**
 *
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
 *
 */

package org.seasr.meandre.components.weka.viz;

import gnu.formj.html.*;
import gnu.formj.dhtml.*;
import gnu.formj.html.style.Style;
import gnu.formj.html.table.Table;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ExecutableComponent;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;

import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * <p>
 * Title: Weka Instances Visialoization.
 * </p>
 *
 * <p>
 * Description: This executable component visualizes Weka instances in table form.
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
 * @author Lily Dong
 * @version 1.0
 */

@Component(creator="Lily Dong",
           description="Visualize weka.core.Instances in table form. The table is filled with the information extracted " +
           "from instances. The first line of table displays the names taken out from weka.core.Aattribut and the remaining lines display " +
           "the values taken out from weka.core.Instance. " +
           "The order of display is based to their occurrances in the instances.",
           name="WebVizInstancesTable",
           tags="instances, visualization")

public class WebVizInstancesTable implements ExecutableComponent, WebUIFragmentCallback {
    @ComponentInput(description="Read weka.core.Instances as input.",
                    name= "instances")
    final static String DATA_INPUT = "instances";

    /** If A is added, status is true, otherwise status is false */
    private static boolean status = false;

    /** The blocking semaphore */
    private Semaphore sem = new Semaphore(1,true);

    /** The message to print */
    private Instances instances = null;

    /** The instance ID */
    private String sInstanceID = null;

    /** This method gets call when a request with no parameters is made to a
     * component webui fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem arised during execution and something went wrong
     */
    public void emptyRequest(HttpServletResponse response) throws
            WebUIException {
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
        int nr_instances = instances.numInstances();
        //System.out.println("\nthe number of instances  = " + nr_instances);
        int nr_attributes = instances.numAttributes();
        //System.out.println("\nthe number of attributes = " + nr_attributes);

        Enumeration all_attributes = instances.enumerateAttributes(),
                    all_instances  = instances.enumerateInstances();

        Instance instance = (Instance)all_instances.nextElement();
        int nr_instance_attributes = instance.numAttributes();

        int max = Math.max(nr_attributes, nr_instance_attributes);

        Style s = new Style();
        s.getBox().setBorderStyle("solid");
        s.getBox().setBorder("1px");
        s.getColor().setBackground("#DDDDDD");
        s.getBox().setPadding("1px");
        s.getFont().setFontFamily("Verdana");
        s.getFont().setFontSize("12px");

        Table table = new Table(1+nr_instances, max);
        table.setStyle(s);
        table.box.setBorderStyle("solid");
        table.box.setBorder("1px");

        int nr = 0;
        while(all_attributes.hasMoreElements()) {
            Attribute single_attribute = (Attribute)all_attributes.nextElement();
            table.addNext(single_attribute.name());
            nr++;
            //System.out.print(single_attribute.name() + "\t");
        }
        //System.out.println();
        if(nr < max) //make up spaces
            for(int i=nr; i<max; i++)
                table.addNext("");
        while(all_instances.hasMoreElements()) {
            nr = 0;
            StringTokenizer tokenizer = new StringTokenizer(instance.toString(), ",");
            while(tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                //System.out.print(token + "\t");
                if (token.compareTo("?") != 0) //? means empty
                    table.addNext(token);
                else
                    table.addNext("");
                nr++;
            }
            if(nr < max) //make up spaces
                for(int i=nr; i<max; i++)
                    table.addNext("");
            instance = (Instance)all_instances.nextElement();
            //System.out.println();
        }

        // The message stuff
        Window wmfMessage = new Window("Instances view", "400", "400");
        wmfMessage.setContent(table); //new Label("<pre>"+instances.toString()+"</pre>"));
        wmfMessage.setMovable(true);
        wmfMessage.setMaximizable(true);
        wmfMessage.setMinimizable(true);

        // Done link
        A linkDone = new A();
        linkDone.setHref("/" + sInstanceID + "?done=true");
        linkDone.setContent(new Label("Done with the instances visualization"));
        linkDone.setTitle("Done with the instances visualization");
        linkDone.setToolTip("Done with the instances visualization");

        Div div = new Div();
        if(!status) {
            status = true;
            div.add(linkDone);
        }
        //div.add(table);

        div.add(wmfMessage);

        String s2 = div.toString();

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
    public void handle(HttpServletRequest request, HttpServletResponse response) throws
            WebUIException {
        String sDone = request.getParameter("done");
        if (sDone != null) {
            sem.release();
        } else
            emptyRequest(response);
    }

    /**
     * Call at the end of an execution flow.
     */
    public void initialize(ComponentContextProperties ccp) {
    }

    /** When ready for execution.
     *
     * @param cc The component context
     * @throws ComponentExecutionException An exeception occurred during execution
     * @throws ComponentContextException Illigal access to context
     */
    public void execute(ComponentContext cc) throws ComponentExecutionException,
            ComponentContextException {
        try {
            //System.out.println("Firing the web ui component");
            instances  = (Instances) cc.getDataComponentFromInput(DATA_INPUT);
            sInstanceID = cc.getExecutionInstanceID();
            sem.acquire();
            //System.out.println(">>>Rendering...");
            cc.startWebUIFragment(this);
            //System.out.println(">>>STARTED");
            sem.acquire();
            //System.out.println(">>>Done");
            cc.stopWebUIFragment(this);
        } catch (Exception e) {
            throw new ComponentExecutionException(e);
        }
    }

    /**
     * Called when a flow is started.
     */
    public void dispose(ComponentContextProperties ccp) {
    }
}
