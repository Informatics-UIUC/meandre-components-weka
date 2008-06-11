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

package org.meandre.components.io;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ExecutableComponent;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;

import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;
import weka.core.converters.ConverterUtils.DataSource;

import ncsa.d2k.modules.core.datatype.table.*;
import ncsa.d2k.modules.core.datatype.table.basic.StringColumn;
import ncsa.d2k.modules.core.datatype.table.basic.FloatColumn;
import ncsa.d2k.modules.core.datatype.table.basic.MutableTableImpl;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * <p>
 * Title: Instances to Table
 * </p>
 *
 * <p>
 * Description: This executable component converts Instances to Table.
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
           description="Convert a weka.core.Instances to a ncsa.d2k.modules.core.datatype.table.Table. " +
           "The attribtes and instnace held by instances are mapped into rows and columns in table. " +
           "The current version works only weka.core.Attribute whose type is string or float. The property " +
           "verbose is used to control whether the converted table is output to the console. " +
           "The value of verbose can be specified through property setting.",
           name="InstancesToTable",
           tags="conversion, instances, table")

public class InstancesToTable implements ExecutableComponent {
    @ComponentInput(description="Read weka.core.Instances as input.",
                    name= "instances")
    public final static String DATA_INPUT = "instances";

    @ComponentOutput(description="Output ncsa.d2k.modules.core.datatype.table.Table converted from weka.core.Instances.",
                     name="table")
    public final static String DATA_OUTPUT = "table";

    @ComponentProperty(defaultValue="true",
                       description="Control whether debugging information is output to the console.",
                       name="verbose")
    final static String DATA_PROPERTY = "verbose";

    private boolean verbose = true;

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
        Logger logger = cc.getLogger();
        try {
            verbose = Boolean.valueOf(cc.getProperty(DATA_PROPERTY));

            Instances instances = (Instances) (cc.getDataComponentFromInput(DATA_INPUT));

            int nr_instances = instances.numInstances();
            if(verbose)
                logger.log(Level.INFO, "the number of instances  = " + nr_instances);

            int nr_attributes = instances.numAttributes();
            if(verbose)
                logger.log(Level.INFO, "the number of attributes = " + nr_attributes);

            Enumeration all_instances = instances.enumerateInstances();

            int[] type = new int[nr_attributes]; //storing attribute type
            Column[] schema = new Column[nr_attributes];

            for (int i = 0; i < nr_attributes; i++) {
                Attribute single_attribute = instances.attribute(i);
                if (single_attribute.type() == 0) //float column
                    type[i] = 0;
                else //otherwise string column
                    type[i] = 1;
            }

            for (int i = 0; i < type.length; i++) {
                if (type[i] == 0)
                    schema[i] = new FloatColumn();
                else {
                    StringColumn sc = new StringColumn();
                    schema[i] = new StringColumn();
                }
            }
            MutableTableImpl table = new MutableTableImpl(schema);

            for (int i = 0; i < nr_attributes; i++) {
                Attribute single_attribute = instances.attribute(i);
                table.setColumnLabel(single_attribute.name(), i);
            }

            table.addRows(nr_instances); //-1 means skipping one line of string string float float... for csv file, but for arff file, one line missing

            int row = 0;
            all_instances.nextElement(); //skip one line of string string float float... for csv file, but for arff file, one line missing
            while (all_instances.hasMoreElements()) {
                int col = 0;
                Instance instance = (Instance) all_instances.nextElement();
                if(verbose)
                    logger.log(Level.INFO, instance.toString());
                    //System.out.println(instance.toString());
                StringTokenizer tokenizer = new StringTokenizer(instance.toString(), ",");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (type[col] == 0) { //float
                        if (token.compareTo("?") != 0) { //? means empty
                            table.setFloat(Float.valueOf(token), row, col);
                        }
                    } else { //string
                        table.setString(token, row, col);
                    }
                    ++col;
                }
                ++row;
            }

            if(verbose) {
                logger.log(Level.INFO, "the number of table columns = " + table.getNumColumns());
                logger.log(Level.INFO, "the number of table rows = " + table.getNumRows());
                //System.out.println("the number of table columns = " + table.getNumColumns());
                //System.out.println("the number of table rows = " + table.getNumRows());
                for (int j = 0; j < table.getNumColumns(); j++) {
                    for (int i = 0; i < table.getNumRows(); i++) {
                        if (table.isColumnNumeric(j))
                            logger.log(Level.INFO, table.getFloat(i, j) +
                                             ((i != table.getNumRows()) ? " " : "\n"));
                            /*System.out.print(table.getFloat(i, j) +
                                             ((i != table.getNumRows()) ? " " : "\n"));*/
                        else
                            logger.log(Level.INFO, table.getString(i, j) +
                                       ((i != table.getNumRows()) ? " " : "\n"));
                            /*System.out.print(table.getString(i, j) +
                                             ((i != table.getNumRows()) ? " " :
                                              "\n"));*/
                    }
                    //System.out.println();
                }
            }

            cc.pushDataComponentToOutput(DATA_OUTPUT, table);
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
