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
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.meandre.core.*;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Hashtable;
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
        name = "Sparse results to instances",
        tags = "instances transformation"
)
public class SparseResultsToInstances implements ExecutableComponent {

    @ComponentInput(description = "The result set to transform", name = "result-set")
    final static String DATA_INPUT_RESULT_SET = "result-set";

    @ComponentOutput(description = "The resulting sparse instance set", name = "instances")
    final static String DATA_OUTPUT_INSTANCES = "instances";

    private static int ROW_ID_COLUMN = 0;
    private static int COL_NAME_COLUMN = 1;
    private static int VALUE_COLUMN = 2;
    private static int LABEL_COLUMN = 3;

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
        //ResultSet rs = (ResultSet)cc.getDataComponentFromInput(DATA_INPUT_RESULT_SET);

        String sServer = "http://mensa.ncsa.uiuc.edu:8890/sparql";
        String sQuery = "SELECT ?docID ?pos ?wpcount ?sex " +
                        "WHERE {" +
                        "   GRAPH <TheModel2> {" +
                        "       ?docref <http://2node.org/model#documentName> ?docID ." +
                        "       ?docref <http://2node.org/model#authorSex> ?sex ." +
                        "       ?docref <http://2node.org/model#words> ?wordsref ." +
                        "       ?wordsref <http://2node.org/model/words/word_pos> ?wordref." +
                        "       ?wordref <http://2node.org/model/words/word_pos#pos> ?pos." +
                        "       ?wordref <http://2node.org/model/words/word_pos#count> ?wpcount ." +
                        "   }" +
                        "} " +
                        "order by ?docID  ?pos ?wpcount ?sex ";

        // First component
        ResultSet rs = null;
        try {
            URL url = new URL(sServer + "?query=" + URLEncoder.encode(sQuery, "UTF8") + "&format=xml");

            StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            InputStream is = url.openStream();
            while (is.available() > 0)
                sb.append((char) is.read());

            rs = ResultSetFactory.fromXML(url.openStream());
        }
        catch (Exception e) {
            throw new ComponentExecutionException(e);
        }

        List lst = rs.getResultVars();
        int iNumColumns = lst.size();

        // Parse the information
        String[] saAttName = new String[iNumColumns];
        List<RDFNode> lsta[] = new List[iNumColumns];
        for (int i = 0, iMax = lst.size(); i < iMax; i++) {
            saAttName[i] = lst.get(i).toString();
            lsta[i] = new LinkedList<RDFNode>();
        }

        int iNumIns = 0;
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            for (int i = 0, iMax = iNumColumns; i < iMax; i++) {
                RDFNode obj = qs.get(saAttName[i]);
                lsta[i].add(obj);
            }
            iNumIns++;
        }

        // Collect the different labels
        HashSet<String> hsLabels = new HashSet<String>();
        for (RDFNode rdfn : lsta[LABEL_COLUMN]) {
            String s = rdfn.toString();
            s = typeTrimer(s);
            hsLabels.add(s);
        }

        // Collect the different column names
        HashSet<String> hsColNames = new HashSet<String>();
        for (RDFNode rdfn : lsta[COL_NAME_COLUMN]) {
            String s = rdfn.toString();
            s = typeTrimer(s);
            hsColNames.add(s);
        }

        int iNumAtt = hsColNames.size() + 1;

        // Create the attribute instance set
        FastVector fvAtt = new FastVector(iNumAtt);
        Hashtable<String, Integer> htNameToAtt = new Hashtable<String, Integer>();
        int iCnt = 0;
        for (String sColName : hsColNames) {
            Attribute att = new Attribute(sColName);
            fvAtt.addElement(att);
            htNameToAtt.put(sColName, iCnt++);
        }

        FastVector fvLab = new FastVector(hsLabels.size());
        //for ( String sLabel:hsLabels )
        //	fvLab.addElement(sLabel);
        fvLab.addElement("m");
        fvLab.addElement("f");

        Attribute attClass = new Attribute(saAttName[LABEL_COLUMN], fvLab);
        fvAtt.addElement(attClass);

        System.out.println("" + iNumAtt + "," + iNumIns + "." + fvAtt.size());

        Instances ins = new Instances("Transformed query results", fvAtt, iNumIns);

        String sCurrentDocID = "";
        SparseInstance si = null;
        for (int j = 0; j < iNumIns; j++) {
            String sDocID = typeTrimer(lsta[ROW_ID_COLUMN].get(j).toString());
            String sColName = typeTrimer(lsta[COL_NAME_COLUMN].get(j).toString());
            String sColVal = typeTrimer(lsta[VALUE_COLUMN].get(j).toString());
            String sLabel = typeTrimer(lsta[LABEL_COLUMN].get(j).toString());

            if (sDocID.equals(sCurrentDocID)) {
                // Still the same instance
                int iAtt = htNameToAtt.get(sColName);
                si.setValue(iAtt, Double.parseDouble(sColVal));
            }
            else {
                if (si != null) {
                    si.setValue(fvAtt.size() - 1, sLabel);
                    ins.add(si);
                }
                // New instance
                sCurrentDocID = sDocID;
                si = new SparseInstance(iNumAtt);
                si.setDataset(ins);
                int iAtt = htNameToAtt.get(sColName);
                si.setValue(iAtt, Double.parseDouble(sColVal));
                si.setValue(fvAtt.size() - 1, sLabel);

            }
        }
        ins.add(si);
        ins.setClassIndex(fvAtt.size() - 1);

        System.out.println(ins.toSummaryString());

        cc.pushDataComponentToOutput(DATA_OUTPUT_INSTANCES, ins);
    }

    /**
     * This method is called when the Menadre Flow execution is completed.
     */
    public void dispose(ComponentContextProperties ccp) {

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] sa) throws IOException {
        String sServer = "http://mensa.ncsa.uiuc.edu:8890/sparql";
        String sQuery = "SELECT ?docID ?pos ?wpcount ?sex " +
                        "WHERE {" +
                        "   GRAPH <TheModel2> {" +
                        "       ?docref <http://2node.org/model#documentName> ?docID ." +
                        "       ?docref <http://2node.org/model#authorSex> ?sex ." +
                        "       ?docref <http://2node.org/model#words> ?wordsref ." +
                        "       ?wordsref <http://2node.org/model/words/word_pos> ?wordref." +
                        "       ?wordref <http://2node.org/model/words/word_pos#pos> ?pos." +
                        "       ?wordref <http://2node.org/model/words/word_pos#count> ?wpcount ." +
                        "   }" +
                        "} " +
                        "order by ?docID  ?pos ?wpcount ?sex " +
                        "limit 1000";

        // First component
        URL url = new URL(sServer + "?query=" + URLEncoder.encode(sQuery, "UTF8") + "&format=xml");

        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        InputStream is = url.openStream();
        while (is.available() > 0)
            sb.append((char) is.read());

        ResultSet rs = ResultSetFactory.fromXML(url.openStream());

        List lst = rs.getResultVars();
        int iNumColumns = lst.size();

        // Parse the information
        String[] saAttName = new String[iNumColumns];
        List<RDFNode> lsta[] = new List[iNumColumns];
        for (int i = 0, iMax = lst.size(); i < iMax; i++) {
            saAttName[i] = lst.get(i).toString();
            lsta[i] = new LinkedList<RDFNode>();
        }

        int iNumIns = 0;
        while (rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            for (int i = 0, iMax = iNumColumns; i < iMax; i++) {
                RDFNode obj = qs.get(saAttName[i]);
                lsta[i].add(obj);
            }
            iNumIns++;
        }

        // Collect the different labels
        HashSet<String> hsLabels = new HashSet<String>();
        for (RDFNode rdfn : lsta[LABEL_COLUMN]) {
            String s = rdfn.toString();
            s = typeTrimer(s);
            hsLabels.add(s);
        }

        // Collect the different column names
        HashSet<String> hsColNames = new HashSet<String>();
        for (RDFNode rdfn : lsta[COL_NAME_COLUMN]) {
            String s = rdfn.toString();
            s = typeTrimer(s);
            hsColNames.add(s);
        }

        int iNumAtt = hsColNames.size() + 1;

        // Create the attribute instance set
        FastVector fvAtt = new FastVector(iNumAtt);
        Hashtable<String, Integer> htNameToAtt = new Hashtable<String, Integer>();
        int iCnt = 0;
        for (String sColName : hsColNames) {
            Attribute att = new Attribute(sColName);
            fvAtt.addElement(att);
            htNameToAtt.put(sColName, iCnt++);
        }

        FastVector fvLab = new FastVector(hsLabels.size());
        //for ( String sLabel:hsLabels )
        //	fvLab.addElement(sLabel);
        fvLab.addElement("m");
        fvLab.addElement("f");

        Attribute attClass = new Attribute(saAttName[LABEL_COLUMN], fvLab);
        fvAtt.addElement(attClass);

        System.out.println("" + iNumAtt + "," + iNumIns + "." + fvAtt.size());

        Instances ins = new Instances("Transformed query results", fvAtt, iNumIns);

        String sCurrentDocID = "";
        SparseInstance si = null;
        for (int j = 0; j < iNumIns; j++) {
            String sDocID = typeTrimer(lsta[ROW_ID_COLUMN].get(j).toString());
            String sColName = typeTrimer(lsta[COL_NAME_COLUMN].get(j).toString());
            String sColVal = typeTrimer(lsta[VALUE_COLUMN].get(j).toString());
            String sLabel = typeTrimer(lsta[LABEL_COLUMN].get(j).toString());

            if (sDocID.equals(sCurrentDocID)) {
                // Still the same instance
                int iAtt = htNameToAtt.get(sColName);
                si.setValue(iAtt, Double.parseDouble(sColVal));
            }
            else {
                if (si != null) {
                    si.setValue(fvAtt.size() - 1, sLabel);
                    ins.add(si);
                }
                // New instance
                sCurrentDocID = sDocID;
                si = new SparseInstance(iNumAtt);
                si.setDataset(ins);
                int iAtt = htNameToAtt.get(sColName);
                si.setValue(iAtt, Double.parseDouble(sColVal));
                si.setValue(fvAtt.size() - 1, sLabel);

            }
        }
        ins.add(si);
        ins.setClassIndex(fvAtt.size() - 1);

        System.out.println(ins);
        System.out.println(ins.toSummaryString());

    }

    /**
     * Trime the XSD type out of the way
     *
     * @param s The string
     * @return The processed string
     */
    private static String typeTrimer(String s) {
        int iPos = s.indexOf("^");
        if (iPos > 0)
            s = s.substring(0, iPos);
        return s;
    }
}
