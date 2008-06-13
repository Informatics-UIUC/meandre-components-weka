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

package org.seasr.meandre.components.weka.io;

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
import weka.core.Instance;
import weka.core.Attribute;

import java.util.Enumeration;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * <p>
 * Title: Save Instances
 * </p>
 *
 * <p>
 * Description: This executable component saves Instances in file.
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
           description="Take weka.core.Instances as input. Instances's internal information about " +
           "weka.core.Instance and weka.core.Attribute are saved in a local file. The local file " +
           "begins with a series of names followed by values. The local file name is specified through " +
           "property settting",
           name="SaveInstancesToFile",
           tags="io, file, save, instances")

public class SaveInstancesToFile implements ExecutableComponent {
    @ComponentInput(description="Read weka.core.Instances as input.",
                    name= "instances")
    final static String DATA_INPUT = "instances";

    @ComponentOutput(description="Output the local file name where the instances is stored.",
                     name="message")
    final static String DATA_OUTPUT = "message";

    @ComponentProperty(defaultValue="null",
                       description="Import local file name where you want to save the instnaces.",
                       name="message")
    final static String DATA_PROPERTY = "message";

    /** store file name */
    String message = null;
    /** store output handle */
    PrintWriter out = null;

    /**
     * Called when a flow is started.
     *
     * @param ccp ComponentContextProperties
     */
    public void initialize(ComponentContextProperties ccp) {
    }

    /**
     * When ready for execution.
     *
     * @param cc ComponentContext
     * @throws ComponentExecutionException
     * @throws ComponentContextException
     */
    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {
        try {
            Instances instances = (Instances)(cc.getDataComponentFromInput(DATA_INPUT));
            int nr_instances = instances.numInstances();
            int nr_attributes = instances.numAttributes();

            message = cc.getProperty(DATA_PROPERTY);
            if(message == null)
                throw new org.meandre.core.ComponentExecutionException();

            out = new PrintWriter(new BufferedWriter(new FileWriter(message)));
            if(out == null)
                throw new org.meandre.core.ComponentExecutionException();

            Enumeration all_attributes = instances.enumerateAttributes(),
                        all_instances  = instances.enumerateInstances();

            int nr = 0;
            StringBuffer buffer = new StringBuffer();
            while (all_attributes.hasMoreElements()) {
                Attribute single_attribute = (Attribute) all_attributes.
                                             nextElement();
                buffer.append(single_attribute.name());
                if (++nr < nr_attributes)
                    buffer.append(",");
            }
            buffer.append("\n");
            out.print(buffer.toString());

            while (all_instances.hasMoreElements()) {
                Instance instance = (Instance) all_instances.nextElement();
                out.println(instance.toString());
            }

            out.flush();
            out.close();

            cc.pushDataComponentToOutput(DATA_OUTPUT, message);
        } catch (Exception e1) {
            throw new ComponentExecutionException(e1);
        }
   }

   /**
    * Called at the end of an execution flow.
    *
    * @param ccp ComponentContextProperties
    */
   public void dispose(ComponentContextProperties ccp) {
       // TODO Auto-generated method stub
   }
}
