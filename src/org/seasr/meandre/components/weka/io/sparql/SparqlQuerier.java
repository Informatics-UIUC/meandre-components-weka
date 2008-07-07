package org.seasr.meandre.components.weka.io.sparql;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.meandre.core.*;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This component runs a query against a SPARQL aware web server
 *
 * @author Xavier Llora
 */

@Component(
        creator = "Xavier Llora",
        description = "This component runs a query against a SPARQL aware web server",
        name = "SPARQL Querier",
        tags = "metadata query"
)
public class SparqlQuerier implements ExecutableComponent {

    @ComponentOutput(description = "A JENA result set containing the results of the query executed against the store",
                     name = "result-set")
    final static String DATA_OUTPUT_RESULT_SET = "result-set";

    @ComponentProperty(description = "The SPARQL aware server",
                       name = "server", defaultValue = "http://mensa.ncsa.uiuc.edu:8890/sparql")
    final static String DATA_PROPERTY_SERVER = "server";

    @ComponentProperty(description = "The SPARQL query to run",
                       name = "query",
                       defaultValue = "SELECT ?eos ?ord ?pos " +
                                      "WHERE {GRAPH <ANCF1901> {?s <http://www.tei-c.org/ns/1.0#eos> ?eos . " +
                                      "?s <http://www.tei-c.org/ns/1.0#eos> ?ord . " +
                                      "?s <http://www.tei-c.org/ns/1.0#pos> ?pos}} " +
                                      "LIMIT 100")
    final static String DATA_PROPERTY_QUERY = "query";

    /**
     * This method is invoked when the Meandre Flow is being prepared for
     * getting run.
     */
    public void initialize(ComponentContextProperties ccp) {

    }

    /**
     * This component runs a query against a SPARQL aware web server
     *
     * @throws ComponentExecutionException If a fatal condition arises during
     *                                     the execution of a component, a ComponentExecutionException
     *                                     should be thrown to signal termination of execution required.
     * @throws ComponentContextException   A violation of the component context
     *                                     access was detected
     */
    public void execute(ComponentContext cc)
            throws ComponentExecutionException, ComponentContextException {


        try {
            String sServer = cc.getProperty(DATA_PROPERTY_SERVER);
            String sQuery = URLDecoder.decode(cc.getProperty(DATA_PROPERTY_QUERY), "UTF8");

            // First component
            URL url = new URL(sServer + "?query=" + URLEncoder.encode(sQuery, "UTF8") + "&format=xml");

            StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            InputStream is = url.openStream();
            while (is.available() > 0)
                sb.append((char) is.read());

            ResultSet rs = ResultSetFactory.fromXML(url.openStream());

            cc.pushDataComponentToOutput(DATA_OUTPUT_RESULT_SET, rs);
        }
        catch (IOException ioe) {
            throw new ComponentExecutionException(ioe);
        }


    }

    /**
     * This method is called when the Menadre Flow execution is completed.
     */
    public void dispose(ComponentContextProperties ccp) {

    }

    @SuppressWarnings("unchecked")
    public static void main(String[] sa) {

        try {

            // First component
            String sServer = "http://mensa.ncsa.uiuc.edu:8890/sparql";
            String sQuery = "SELECT ?eos ?ord ?pos WHERE {GRAPH <ANCF1901> {?s <http://www.tei-c.org/ns/1.0#eos> ?eos . ?s <http://www.tei-c.org/ns/1.0#eos> ?ord . ?s <http://www.tei-c.org/ns/1.0#pos> ?pos}} LIMIT 5";

            URL url = new URL(sServer + "?query=" + URLEncoder.encode(sQuery, "UTF8") + "&format=xml");

            StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            InputStream is = url.openStream();
            while (is.available() > 0)
                sb.append((char) is.read());

            ResultSet rs = ResultSetFactory.fromXML(url.openStream());

            // Second component
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

            Instances ins = new Instances(sQuery, fvAtt, 10);

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


            System.out.println(ins);


        }
        catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
