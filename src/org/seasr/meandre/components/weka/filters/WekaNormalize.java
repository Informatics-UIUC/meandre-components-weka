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
package org.seasr.meandre.components.weka.filters;

import org.meandre.core.*;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

@Component(creator = "Amit Kumar",
           description = "This component takes Weka instances and normalizes the values. " +
                         "Then it outputs the instances.",
           name = "Weka Normalize",
           tags = "weka")
public class WekaNormalize implements ExecutableComponent {

    @ComponentInput(description = "Weka instances", name = "instances")
    final static String DATA_INPUT_INSTANCES = "instances";

    @ComponentOutput(description = "Weka instances", name = "instances")
    final static String DATA_OUTPUT_INSTANCES = "instances";

    public void initialize(ComponentContextProperties cpp) {
    }

    /**
     * Takes instances and normalizes them
     * returns null in case of error
     */
    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {
        // Pull the instances from the input port
        System.out.println(">>>Starting: WekaNormalize");
        Instances instances = (Instances) (cc.getDataComponentFromInput(DATA_INPUT_INSTANCES));
        Instances outInstances = null;
        Normalize normalize = new weka.filters.unsupervised.attribute.Normalize();
        try {
            normalize.setInputFormat(instances);
        }
        catch (Exception e1) {
            // TODO Auto-generated catch block
            System.out.println(">>>ERROR: " + e1.getMessage());
            e1.printStackTrace();
        }

        try {
            outInstances = Filter.useFilter(instances, normalize);

        }
        catch (Exception e) {
            System.out.println(">>>ERROR: " + e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(">>>End: WekaNormalize");

        cc.pushDataComponentToOutput(DATA_OUTPUT_INSTANCES, outInstances);
    }

    public void dispose(ComponentContextProperties ccp) {
    }
}
