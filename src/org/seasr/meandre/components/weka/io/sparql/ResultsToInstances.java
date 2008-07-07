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
package org.seasr.meandre.components.weka.io.sparql;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.meandre.core.*;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Converts a result sets into a sparse instance set where the last one is the label (nominal) and the attributes are doubles
 *
 * @author Xavier Llora
 */

@Component(
        creator = "Xavier Llora",
        description = "Converts a result sets into a sparse instance set where the last one " +
                      "is the label (nominal) and the attributes are doubles",
        name = "Results to instances",
        tags = "instances transformation"
)
public class ResultsToInstances implements ExecutableComponent {

    @ComponentInput(description = "The result set to transform", name = "result-set")
    final static String DATA_INPUT_RESULT_SET = "result-set";

    @ComponentOutput(description = "The resulting sparse instance set", name = "instances")
    final static String DATA_OUTPUT_INSTANCES = "instances";

    /**
     * This method is invoked when the Meandre Flow is being prepared for
     * getting run.
     */
    public void initialize(ComponentContextProperties ccp) {

    }

    /**
     * Converts a result sets into a sparse instance set where the last one is the label (nominal) and the attributes are doubles
     *
     * @throws ComponentExecutionException If a fatal condition arises during
     *                                     the execution of a component, a ComponentExecutionException
     *                                     should be thrown to signal termination of execution required.
     * @throws ComponentContextException   A violation of the component context
     *                                     access was detected
     */
    @SuppressWarnings("unchecked")
    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {
        ResultSet rs = (ResultSet) cc.getDataComponentFromInput(DATA_INPUT_RESULT_SET);

        List lst = rs.getResultVars();
        int iNumAtts = lst.size();

        // Parse the information
        String[] saAttName = new String[iNumAtts];
        List<RDFNode> lsta[] = new List[iNumAtts];
        for (int i = 0, iMax = lst.size(); i < iMax; i++) {
            saAttName[i] = lst.get(i).toString();
            lsta[i] = new LinkedList<RDFNode>();
        }

        int iNumIns = 0;
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            for (int i = 0, iMax = iNumAtts; i < iMax; i++) {
                RDFNode obj = qs.get(saAttName[i]);
                lsta[i].add(obj);
            }
            iNumIns++;
        }

        // Collect the differnent labels
        HashSet<String> hs = new HashSet<String>();
        for (RDFNode rdfn : lsta[iNumAtts - 1]) {
            String s = rdfn.toString();
            int iPos = s.indexOf("^");
            if (iPos > 0)
                s = s.substring(0, iPos);
            hs.add(s);
        }

        // Create the instance set
        FastVector fvAtt = new FastVector(iNumAtts);

        for (int i = 0, iMax = lst.size() - 1; i < iMax; i++)
            fvAtt.addElement(new Attribute(saAttName[i]));

        FastVector fvLab = new FastVector(hs.size());
        for (String sLabel : hs)
            fvLab.addElement(sLabel);

        fvAtt.addElement(new Attribute(saAttName[saAttName.length - 1], fvLab));

        Instances ins = new Instances("Transformed query results", fvAtt, iNumIns);

        for (int j = 0; j < iNumIns; j++) {
            SparseInstance si = new SparseInstance(iNumAtts);
            si.setDataset(ins);
            for (int i = 0; i < iNumAtts - 1; i++) {
                // Hack for the data ingestion bug
                String s = lsta[i].get(j).toString();
                int iPos = s.indexOf("^");
                if (iPos > 0)
                    s = s.substring(0, iPos);
                // Conversion to double
                si.setValue(i, Double.parseDouble(s));
            }
            String sClass = lsta[iNumAtts - 1].get(j).toString();
            int iPos = sClass.indexOf("^");
            if (iPos > 0)
                sClass = sClass.substring(0, iPos);
            si.setValue(iNumAtts - 1, sClass);
            ins.add(si);
        }
        ins.setClassIndex(iNumAtts - 1);

        cc.pushDataComponentToOutput(DATA_OUTPUT_INSTANCES, ins);
    }

    /**
     * This method is called when the Menadre Flow execution is completed.
     */
    public void dispose(ComponentContextProperties ccp) {

    }
}
