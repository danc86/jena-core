/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            16-Jun-2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;

// Imports
///////////////
import java.io.*;
import java.util.*;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.impl.OntClassImpl;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.vocabulary.OWL;

import junit.framework.*;

/**
 * <p>
 * Unit tests that are derived from user bug reports
 * </p>
 * 
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >
 *         email</a>)
 * @version CVS $Id: TestBugReports.java,v 1.23 2003/11/20 17:53:10
 *          ian_dickinson Exp $
 */
public class TestBugReports extends TestCase {
    // Constants
    //////////////////////////////////

    public static String NS = "http://example.org/test#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    public TestBugReports(String name) {
        super(name);
    }

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }
    
    
    /** Bug report by Danah Nada - listIndividuals returning too many results */
    public void test_dn_0() {
        OntModel schema = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM_RULES_INF, null );
        
        schema.read( "file:doc/inference/data/owlDemoSchema.xml", null );

        int count = 0;
        for (Iterator i = schema.listIndividuals(); i.hasNext(); ) {
            //Resource r = (Resource) i.next();
            i.next();
            count++;
            /* Debugging * /
            for (StmtIterator j = r.listProperties(RDF.type); j.hasNext(); ) {
                System.out.println( "ind - " + r + " rdf:type = " + j.nextStatement().getObject() );
            }
            System.out.println("----------"); /**/
        }
        
        assertEquals( "Expecting 6 individuals", 6, count );
    }
    
    
    /* Bug report by Danah Nada - duplicate elements in property domain */
    public void test_dn_01() {
        // direct reading for the model method 1
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
        m0.read( "file:testing/ontology/bugs/test_hk_07B.owl" );

        OntProperty p0 = m0.getOntProperty( "file:testing/ontology/bugs/test_hk_07B.owl#PropB" );
        int count = 0;
        for (Iterator i = p0.listDomain(); i.hasNext();) {
            count++;
            i.next();
        }
        assertEquals( 3, count );

        // repeat test - thus using previously cached model for import

        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
        m1.read( "file:testing/ontology/bugs/test_hk_07B.owl" );

        OntProperty p1 = m1.getOntProperty( "file:testing/ontology/bugs/test_hk_07B.owl#PropB" );
        count = 0;
        for (Iterator i = p1.listDomain(); i.hasNext();) {
            count++;
            i.next();
        }
        assertEquals( 3, count );
    }
    
    /** Bug report by Danah Nada - cannot remove import */
    public void test_dn_02() {
        OntModel mymod = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
        mymod.read( "file:testing/ontology/testImport3/a.owl" );
        
        assertEquals( "Graph count..", 2, mymod.getSubGraphs().size() );
        
        for (Iterator it = mymod.listImportedModels(); it.hasNext();) {
                mymod.removeSubModel( (Model) it.next() );
        }
        
        assertEquals( "Graph count..", 0, mymod.getSubGraphs().size() );
    }
    
    
    /**
     * Bug report by Mariano Rico Almod�var [Mariano.Rico@uam.es] on June 16th.
     * Said to raise exception.
     */
    public void test_mra_01() {
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM, null, null);
        String myDicURI = "http://somewhere/myDictionaries/1.0#";
        String damlURI = "http://www.daml.org/2001/03/daml+oil#";
        m.setNsPrefix("DAML", damlURI);

        String c1_uri = myDicURI + "C1";
        OntClass c1 = m.createClass(c1_uri);

        DatatypeProperty p1 = m.createDatatypeProperty(myDicURI + "P1");
        p1.setDomain(c1);

        ByteArrayOutputStream strOut = new ByteArrayOutputStream();

        m.write(strOut, "RDF/XML-ABBREV", myDicURI);
        //m.write(System.out,"RDF/XML-ABBREV", myDicURI);

    }

    /**
     * Bug report from Holger Knublauch on July 25th 2003. Cannot convert
     * owl:Class to an OntClass
     */
    public void test_hk_01() {
        // synthesise a mini-document
        String base = "http://jena.hpl.hp.com/test#";
        String doc =
            "<rdf:RDF"
                + "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
                + "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "  <owl:Ontology rdf:about=\"\">"
                + "    <owl:imports rdf:resource=\"http://www.w3.org/2002/07/owl\" />"
                + "  </owl:Ontology>"
                + "</rdf:RDF>";

        // read in the base ontology, which includes the owl language
        // definition
        // note OWL_MEM => no reasoner is used
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.getDocumentManager().setMetadataSearchPath( "file:etc/ont-policy-test.rdf", true );
        m.read(new ByteArrayInputStream(doc.getBytes()), base);

        // we need a resource corresponding to OWL Class but in m
        Resource owlClassRes = m.getResource(OWL.Class.getURI());

        // now can we see this as an OntClass?
        OntClass c = (OntClass) owlClassRes.as(OntClass.class);
        assertNotNull("OntClass c should not be null", c);

        //(OntClass) (ontModel.getProfile().CLASS()).as(OntClass.class);

    }

    /**
     * Bug report from Hoger Knublauch on Aug 19th 2003. NPE when setting all
     * distinct members
     */
    public void test_hk_02() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(null);
        OntModel ontModel = ModelFactory.createOntologyModel(spec, null); // ProfileRegistry.OWL_LANG);
        ontModel.createAllDifferent();
        assertTrue(ontModel.listAllDifferent().hasNext());
        AllDifferent allDifferent = (AllDifferent) ontModel.listAllDifferent().next();
        //allDifferent.setDistinct(ontModel.createList());
        assertFalse(allDifferent.listDistinctMembers().hasNext());
    }

    /** Bug report from Holger Knublauch on Aug 19th, 2003. Initialisation error */
    public void test_hk_03() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(null);
        OntModel ontModel = ModelFactory.createOntologyModel(spec, null);
        OntProperty property = ontModel.createObjectProperty("http://www.aldi.de#property");
        /* MinCardinalityRestriction testClass = */
        ontModel.createMinCardinalityRestriction(null, property, 42);

    }

    /**
     * Bug report from Holger Knublauch on Aug 19th, 2003. Document manager alt
     * mechanism breaks relative name translation
     */
    public void test_hk_04() {
        OntModel m = ModelFactory.createOntologyModel();
        m.getDocumentManager().addAltEntry(
            "http://jena.hpl.hp.com/testing/ontology/relativenames",
            "file:testing/ontology/relativenames.rdf");

        m.read("http://jena.hpl.hp.com/testing/ontology/relativenames");
        assertTrue(
            "#A should be a class",
            m.getResource("http://jena.hpl.hp.com/testing/ontology/relativenames#A").canAs(OntClass.class));
        assertFalse(
            "file: #A should not be a class",
            m.getResource("file:testing/ontology/relativenames.rdf#A").canAs(OntClass.class));
    }

    /** Bug report from Holger Knublach: not all elements of a union are removed */
    public void test_hk_05() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setReasoner(null);
        OntModel ontModel = ModelFactory.createOntologyModel(spec, null);
        String ns = "http://foo.bar/fu#";
        OntClass a = ontModel.createClass(ns + "A");
        OntClass b = ontModel.createClass(ns + "B");

        int oldCount = getStatementCount(ontModel);

        RDFList members = ontModel.createList(new RDFNode[] { a, b });
        IntersectionClass intersectionClass = ontModel.createIntersectionClass(null, members);
        intersectionClass.remove();

        assertEquals("Before and after statement counts are different", oldCount, getStatementCount(ontModel));
    }

    /**
     * Bug report from Holger Knublach: moving between ontology models - comes
     * down to a test for a resource being in the base model
     */
    public void test_hk_06() throws Exception {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        ontModel.read("file:testing/ontology/bugs/test_hk_06/a.owl");

        String NSa = "http://jena.hpl.hp.com/2003/03/testont/a#";
        String NSb = "http://jena.hpl.hp.com/2003/03/testont/b#";

        OntClass A = ontModel.getOntClass(NSa + "A");
        assertTrue("class A should be in the base model", ontModel.isInBaseModel(A));

        OntClass B = ontModel.getOntClass(NSb + "B");
        assertFalse("class B should not be in the base model", ontModel.isInBaseModel(B));

        assertTrue(
            "A rdf:type owl:Class should be in the base model",
            ontModel.isInBaseModel(ontModel.createStatement(A, RDF.type, OWL.Class)));
        assertFalse(
            "B rdf:type owl:Class should not be in the base model",
            ontModel.isInBaseModel(ontModel.createStatement(B, RDF.type, OWL.Class)));
    }

    /**
     * Bug report by federico.carbone@bt.com, 30-July-2003. A literal can be
     * turned into an individual.
     */
    public void test_fc_01() {
        OntModel m = ModelFactory.createOntologyModel();

        ObjectProperty p = m.createObjectProperty(NS + "p");
        Restriction r = m.createRestriction(p);
        HasValueRestriction hv = r.convertToHasValueRestriction(m.createLiteral(1));

        RDFNode n = hv.getHasValue();
        assertFalse("Should not be able to convert literal to individual", n.canAs(Individual.class));
    }

    /**
     * Bug report by Christoph Kunze (Christoph.Kunz@iao.fhg.de). 18/Aug/03 No
     * transaction support in ontmodel.
     */
    public void test_ck_01() {
        Graph g = new GraphMem() {
            TransactionHandler m_t = new MockTransactionHandler();
            public TransactionHandler getTransactionHandler() {
                return m_t;
            }
        };
        Model m0 = ModelFactory.createModelForGraph(g);
        OntModel m1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, m0);

        assertFalse(
            "Transaction not started yet",
            ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction);
        m1.begin();
        assertTrue(
            "Transaction started",
            ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction);
        m1.abort();
        assertFalse(
            "Transaction aborted",
            ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction);
        assertTrue("Transaction aborted", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_aborted);
        m1.begin();
        assertTrue(
            "Transaction started",
            ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction);
        m1.commit();
        assertFalse(
            "Transaction committed",
            ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction);
        assertTrue(
            "Transaction committed",
            ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_committed);
    }

    /**
     * Bug report by Christoph Kunz, 26/Aug/03. CCE when creating a statement
     * from a vocabulary
     *  
     */
    public void test_ck_02() {
        OntModel vocabModel = ModelFactory.createOntologyModel();
        ObjectProperty p = vocabModel.createObjectProperty("p");
        OntClass A = vocabModel.createClass("A");

        OntModel workModel = ModelFactory.createOntologyModel();
        Individual sub = workModel.createIndividual("uri1", A);
        Individual obj = workModel.createIndividual("uri2", A);
        workModel.createStatement(sub, p, obj);
    }

    /**
     * Bug report from Christoph Kunz - reification problems and
     * UnsupportedOperationException
     */
    public void test_ck_03() {
        // part A - surprising reification
        OntModel model1 = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM, null);
        OntModel model2 = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM_RULE_INF, null);

        Individual sub = model1.createIndividual("http://mytest#i1", model1.getProfile().CLASS());
        OntProperty pred = model1.createOntProperty("http://mytest#");
        Individual obj = model1.createIndividual("http://mytest#i2", model1.getProfile().CLASS());
        OntProperty probabilityP = model1.createOntProperty("http://mytest#prob");

        Statement st = model1.createStatement(sub, pred, obj);
        model1.add(st);
        st.createReifiedStatement().addProperty(probabilityP, 0.9);
        assertTrue("st should be reified", st.isReified());

        Statement st2 = model2.createStatement(sub, pred, obj);
        model2.add(st2);
        st2.createReifiedStatement().addProperty(probabilityP, 0.3);
        assertTrue("st2 should be reified", st2.isReified());

        sub.addProperty(probabilityP, 0.3);
        sub.removeAll(probabilityP).addProperty(probabilityP, 0.3); //!!!
                                                                    // exception

        // Part B - exception in remove All
        Individual sub2 = model2.createIndividual("http://mytest#i1", model1.getProfile().CLASS());

        sub.addProperty(probabilityP, 0.3);
        sub.removeAll(probabilityP); //!!! exception

        sub2.addProperty(probabilityP, 0.3);
        sub2.removeAll(probabilityP); //!!! exception

    }

    /**
     * Bug report by sjooseng [sjooseng@hotmail.com]. CCE in listOneOf in
     * Enumerated Class with DAML profile.
     */
    public void test_sjooseng_01() {
        String source =
            "<rdf:RDF xmlns:daml='http://www.daml.org/2001/03/daml+oil#'"
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' >"
                + "    <daml:Class rdf:about='http://localhost:8080/kc2c#C1'>"
                + "        <daml:subClassOf>"
                + "            <daml:Restriction>"
                + "                <daml:onProperty rdf:resource='http://localhost:8080/kc2c#p1'/>"
                + "                <daml:hasClass>"
                + "                    <daml:Class>"
                + "                        <daml:oneOf rdf:parseType=\"daml:collection\">"
                + "                            <daml:Thing rdf:about='http://localhost:8080/kc2c#i1'/>"
                + "                            <daml:Thing rdf:about='http://localhost:8080/kc2c#i2'/>"
                + "                        </daml:oneOf>"
                + "                    </daml:Class>"
                + "                </daml:hasClass>"
                + "            </daml:Restriction>"
                + "        </daml:subClassOf>"
                + "    </daml:Class>"
                + "    <daml:ObjectProperty rdf:about='http://localhost:8080/kc2c#p1'>"
                + "        <rdfs:label>p1</rdfs:label>"
                + "    </daml:ObjectProperty>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(ProfileRegistry.DAML_LANG);
        m.read(new ByteArrayInputStream(source.getBytes()), "http://localhost:8080/kc2c");

        OntClass kc1 = m.getOntClass("http://localhost:8080/kc2c#C1");

        boolean found = false;

        Iterator it = kc1.listSuperClasses(false);
        while (it.hasNext()) {
            OntClass oc = (OntClass) it.next();
            if (oc.isRestriction()) {
                Restriction r = oc.asRestriction();
                if (r.isSomeValuesFromRestriction()) {
                    SomeValuesFromRestriction sr = r.asSomeValuesFromRestriction();
                    OntClass sc = (OntClass) sr.getSomeValuesFrom();
                    if (sc.isEnumeratedClass()) {
                        EnumeratedClass ec = sc.asEnumeratedClass();
                        assertEquals("Enumeration size should be 2", 2, ec.getOneOf().size());
                        found = true;
                    }
                }
            }
        }

        assertTrue(found);
    }

    /**
     * Problem reported by Andy Seaborne - combine abox and tbox in RDFS with
     * ontmodel
     */
    public void test_afs_01() {
        String sourceT =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
                + "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "    <owl:Class rdf:about='http://example.org/foo#A'>"
                + "   </owl:Class>"
                + "</rdf:RDF>";

        String sourceA =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' "
                + "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "    <rdf:Description rdf:about='http://example.org/foo#x'>"
                + "    <rdf:type rdf:resource='http://example.org/foo#A' />"
                + "   </rdf:Description>"
                + "</rdf:RDF>";

        Model tBox = ModelFactory.createDefaultModel();
        tBox.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");

        Model aBox = ModelFactory.createDefaultModel();
        aBox.read(new ByteArrayInputStream(sourceA.getBytes()), "http://example.org/foo");

        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(tBox);

        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM_RULE_INF);
        spec.setReasoner(reasoner);

        OntModel m = ModelFactory.createOntologyModel(spec, aBox);

        List inds = new ArrayList();
        for (Iterator i = m.listIndividuals(); i.hasNext();) {
            inds.add(i.next());
        }

        assertTrue("x should be an individual", inds.contains(m.getResource("http://example.org/foo#x")));

    }

    /**
     * Bug report by Thorsten Ottmann [Thorsten.Ottmann@rwth-aachen.de] -
     * problem accessing elements of DAML list
     */
    public void test_to_01() {
        String sourceT =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
                + "    xmlns:daml='http://www.daml.org/2001/03/daml+oil#'>"
                + "  <daml:Class rdf:about='http://example.org/foo#A'>"
                + "    <daml:intersectionOf rdf:parseType=\"daml:collection\">"
                + "       <daml:Class rdf:ID=\"B\" />"
                + "       <daml:Class rdf:ID=\"C\" />"
                + "    </daml:intersectionOf>"
                + "  </daml:Class>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.DAML_MEM, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");

        OntClass A = m.getOntClass("http://example.org/foo#A");
        assertNotNull(A);

        IntersectionClass iA = A.asIntersectionClass();
        assertNotNull(iA);

        RDFList intersection = iA.getOperands();
        assertNotNull(intersection);

        assertEquals(2, intersection.size());
        assertTrue(intersection.contains(m.getOntClass("http://example.org/foo#B")));
        assertTrue(intersection.contains(m.getOntClass("http://example.org/foo#C")));
    }

    /**
     * Bug report by Thorsten Liebig [liebig@informatik.uni-ulm.de] -
     * SymmetricProperty etc not visible in list ont properties
     */
    public void test_tl_01() {
        String sourceT =
            "<rdf:RDF "
                + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
                + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\">"
                + "   <owl:SymmetricProperty rdf:about='http://example.org/foo#p1'>"
                + "   </owl:SymmetricProperty>"
                + "   <owl:TransitiveProperty rdf:about='http://example.org/foo#p2'>"
                + "   </owl:TransitiveProperty>"
                + "   <owl:InverseFunctionalProperty rdf:about='http://example.org/foo#p3'>"
                + "   </owl:InverseFunctionalProperty>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");

        boolean foundP1 = false;
        boolean foundP2 = false;
        boolean foundP3 = false;

        // iterator of properties should include p1-3
        for (Iterator i = m.listOntProperties(); i.hasNext();) {
            Resource r = (Resource) i.next();
            foundP1 = foundP1 || r.getURI().equals("http://example.org/foo#p1");
            foundP2 = foundP2 || r.getURI().equals("http://example.org/foo#p2");
            foundP3 = foundP3 || r.getURI().equals("http://example.org/foo#p3");
        }

        assertTrue("p1 not listed", foundP1);
        assertTrue("p2 not listed", foundP2);
        assertTrue("p3 not listed", foundP3);

        foundP1 = false;
        foundP2 = false;
        foundP3 = false;

        // iterator of object properties should include p1-3
        for (Iterator i = m.listObjectProperties(); i.hasNext();) {
            Resource r = (Resource) i.next();
            foundP1 = foundP1 || r.getURI().equals("http://example.org/foo#p1");
            foundP2 = foundP2 || r.getURI().equals("http://example.org/foo#p2");
            foundP3 = foundP3 || r.getURI().equals("http://example.org/foo#p3");
        }

        assertTrue("p1 not listed", foundP1);
        assertTrue("p2 not listed", foundP2);
        assertTrue("p3 not listed", foundP3);
    }

    /** Bug report by Dave Reynolds - SF bug report 810492 */
    public void test_der_01() {
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF, null);
        Resource a = m.createResource("http://example.org#A");
        Resource b = m.createResource("http://example.org#B");
        OntClass A = new OntClassImpl(a.getNode(), (EnhGraph) m) {
            protected boolean hasSuperClassDirect(Resource cls) {
                throw new RuntimeException("did not find direct reasoner");
            }
        };

        // will throw an exception if the wrong code path is taken
        A.hasSuperClass(b, true);
    }

    /**
     * Bug report by Ivan Ferrari (ivan_ferrari_75 [ivan_ferrari_75@yahoo.it]) -
     * duplicate nodes in output
     */
    public void test_if_01() {
        //create a new default model
        OntModel m = ModelFactory.createOntologyModel();

        m.getDocumentManager().addAltEntry(
            "http://www.w3.org/2001/sw/WebOnt/guide-src/wine",
            "file:testing/reasoners/bugs/wine.owl");
        m.getDocumentManager().addAltEntry(
            "http://www.w3.org/2001/sw/WebOnt/guide-src/food",
            "file:testing/reasoners/bugs/food.owl");

        // note: due to bug in the Wine example, we have to manually read the
        // imported food document
        m.getDocumentManager().setProcessImports(false);
        m.read("http://www.w3.org/2001/sw/WebOnt/guide-src/wine");
        m.getDocumentManager().setProcessImports(true);
        m.getDocumentManager().loadImport(m, "http://www.w3.org/2001/sw/WebOnt/guide-src/food");

        OntClass ontclass = m.getOntClass("http://www.w3.org/2001/sw/WebOnt/guide-src/wine#Wine");

        int nNamed = 0;
        int nRestriction = 0;
        int nAnon = 0;

        for (ExtendedIterator iter2 = ontclass.listSuperClasses(true); iter2.hasNext();) {
            OntClass ontsuperclass = (OntClass) iter2.next();

            //this is to view different anonymous IDs
            if (!ontsuperclass.isAnon()) {
                nNamed++;
            }
            else if (ontsuperclass.canAs(Restriction.class)) {
                ontsuperclass.asRestriction();
                nRestriction++;
            }
            else {
                //System.out.println("anon. super: " + ontsuperclass.getId());
                nAnon++;
            }
        }

        assertEquals("Should be two named super classes ", 2, nNamed);
        assertEquals("Should be nine named super classes ", 9, nRestriction);
        assertEquals("Should be no named super classes ", 0, nAnon);
    }

    /** Bug report by Lawrence Tay - missing datatype property */
    public void test_lt_01() {
        OntModel m = ModelFactory.createOntologyModel();

        DatatypeProperty p = m.createDatatypeProperty(NS + "p");
        OntClass c = m.createClass(NS + "A");

        Individual i = m.createIndividual(NS + "i", c);
        i.addProperty(p, "testData");

        int count = 0;

        for (Iterator j = i.listPropertyValues(p); j.hasNext();) {
            //System.err.println("Individual i has p value: " + j.next());
            j.next();
            count++;
        }

        assertEquals("i should have one property", 1, count);
    }


    /** Bug report by David Kensche [david.kensche@post.rwth-aachen.de] - NPE in listDeclaredProperties */
    public void test_dk_01() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/bugs/test_dk_01.xml" );
        
        String NS = "http://localhost:8080/Repository/QueryAgent/UserOntology/qgen-example-1#";
        String[] classes = new String[] {NS+"C1", NS+"C3", NS+"C2"};
        
        for (int i = 0; i < classes.length; i++) {
            OntClass c = m.getOntClass( classes[i] );
            for (Iterator j = c.listDeclaredProperties(); j.hasNext(); j.next() );
        }
    }
    
    /** Bug report by Paulo Pinheiro da Silva [pp@ksl.stanford.edu] - exception while accessing PropertyAccessor.getDAMLValue */
    public void test_ppds_01() {
        DAMLModel m = ModelFactory.createDAMLModel();
        DAMLClass c = m.createDAMLClass( NS + "C" );
        DAMLInstance x = m.createDAMLInstance( c, NS + "x" );
        DAMLProperty p = m.createDAMLProperty( NS + "p" );
        
        x.addProperty( p, "(s (s 0))" );
        
        PropertyAccessor a = x.accessProperty( p );
        assertNull( "Property accessor value should be null", a.getDAMLValue() );
    }
    
    /** Bug report by anon at SourceForge - Bug ID 887409 */
    public void test_anon_0() {
        String NS = "http://example.org/foo#";
        String sourceT =
            "<rdf:RDF "
            + "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
            + "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
            + "    xmlns:ex='http://example.org/foo#'"
            + "    xmlns:owl='http://www.w3.org/2002/07/owl#'>"
            + "   <owl:ObjectProperty rdf:about='http://example.org/foo#p' />"
            + "   <owl:Class rdf:about='http://example.org/foo#A' />"
            + "   <ex:A rdf:about='http://example.org/foo#x' />"
            + "   <owl:Class rdf:about='http://example.org/foo#B'>"
            + "     <owl:equivalentClass>"
            + "      <owl:Restriction>" 
            + "        <owl:onProperty rdf:resource='http://example.org/foo#p' />" 
            + "        <owl:hasValue rdf:resource='http://example.org/foo#x' />" 
            + "      </owl:Restriction>"
            + "     </owl:equivalentClass>"
            + "   </owl:Class>"
            + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "http://example.org/foo");
        
        OntClass B = m.getOntClass( NS + "B");
        Restriction r = B.getEquivalentClass().asRestriction();
        HasValueRestriction hvr = r.asHasValueRestriction();
        RDFNode n = hvr.getHasValue();
        
        assertTrue( "Should be an individual", n instanceof Individual );
    }
    
    /** Bug report by Zhao Jun [jeff@seu.edu.cn] - throws no such element exception */
    public void test_zj_0() {
        String NS = "file:/C:/orel/orel0_5.owl#";
        String sourceT =
            "<rdf:RDF " +
            "    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'" +
            "    xmlns:ex='http://example.org/foo#'" +
            "    xmlns:owl='http://www.w3.org/2002/07/owl#'" +
            "      xmlns:orel='file:/C:/orel/orel0_5.owl#'" +
            "      xml:base='file:/C:/orel/orel0_5.owl#'" +
            "      xmlns='file:/C:/orel/orel0_5.owl#'>" +
            " <owl:ObjectProperty rdf:ID='hasAgent' />" +
            " <owl:ObjectProperty rdf:ID='hasResource' />" +
            " <owl:Class rdf:ID='MyPlay'>" +
            "    <rdfs:subClassOf>" +
            "      <owl:Restriction>" +
            "        <owl:onProperty rdf:resource='file:/C:/orel/orel0_5.owl#hasResource'/>" +
            "        <owl:hasValue>" +
            "          <orel:Resource rdf:ID='myResource'>" +
            "            <orel:resourceURI>http://mp3.com/newcd/sample.mp3</orel:resourceURI>" +
            "          </orel:Resource>" +
            "        </owl:hasValue>" +
            "      </owl:Restriction>" +
            "    </rdfs:subClassOf>" +
            "    <rdfs:subClassOf rdf:resource='http://www.w3.org/2002/07/owl#Thing'/>" +
            "    <rdfs:subClassOf>" +
            "      <owl:Restriction>" +
            "        <owl:onProperty rdf:resource='file:/C:/orel/orel0_5.owl#hasAgent'/>" +
            "        <owl:hasValue>" +
            "          <orel:Agent rdf:ID='myAgent'>" +
            "            <orel:agentPK>123456789</orel:agentPK>" +
            "          </orel:Agent>" +
            "        </owl:hasValue>" +
            "      </owl:Restriction>" +
            "    </rdfs:subClassOf>" +
            "    <rdfs:subClassOf rdf:resource='file:/C:/orel/orel0_5.owl#Play'/>" +
            "  </owl:Class>" +
            "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        m.read(new ByteArrayInputStream(sourceT.getBytes()), "file:/C:/orel/orel0_5.owl");
        
        OntClass myPlay = m.getOntClass( NS + "MyPlay");
        for (Iterator i = myPlay.listDeclaredProperties(); i.hasNext(); ) {
            //System.err.println( "prop " + i.next() );
            i.next();
        }
    }
    
    /* Bug reprort by E. Johnson ejohnson@carolina.rr.com - ill formed list in writer */
    public void test_ej_01() {
        String BASE  = "http://jena.hpl.hp.com/testing/ontology";
        String NS = BASE + "#";

        DAMLModel m = ModelFactory.createDAMLModel();
        DAMLClass A = m.createDAMLClass(NS + "A");
        DAMLClass B = m.createDAMLClass(NS + "B");
        DAMLClass C = m.createDAMLClass(NS + "C");
        DAMLList l = m.createDAMLList(new RDFNode[] {A, B, C});

        assertTrue( l.isValid() );
        
        Model baseModel = m.getBaseModel();
        RDFWriter writer = baseModel.getWriter("RDF/XML-ABBREV");
        
        // will generate warnings, so suppress until Jeremy has fixed
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //writer.write(baseModel, out, BASE );
    }

    /** Bug report by Harry Chen - closed exception when reading many models */
    public void test_hc_01() 
        throws Exception 
    {
        for (int i = 0; i < 5; i++) {

            OntModel m = ModelFactory.createOntologyModel();

            FileInputStream ifs = new FileInputStream("testing/ontology/relativenames.rdf");

            //System.out.println("Start reading...");
            m.read(ifs, "http://example.org/foo");
            //System.out.println("Done reading...");

            ifs.close();
            //System.out.println("Closed ifs");
            m.close();
            //System.out.println("Closed model");
        }
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    private int getStatementCount(OntModel ontModel) {
        int count = 0;
        for (Iterator it = ontModel.listStatements(); it.hasNext(); it.next()) {
            count++;
        }
        return count;
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    class MockTransactionHandler extends SimpleTransactionHandler {
        boolean m_inTransaction = false;
        boolean m_aborted = false;
        boolean m_committed = false;

        public void begin() {
            m_inTransaction = true;
        }
        public void abort() {
            m_inTransaction = false;
            m_aborted = true;
        }
        public void commit() {
            m_inTransaction = false;
            m_committed = true;
        }
    }
}

/*
 * (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
