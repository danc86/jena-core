/******************************************************************
 * File:        TestRDFSReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

//import com.hp.hpl.jena.reasoner.rdfsReasoner1.*;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RDFSFBRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.io.*;
import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the set of admissable RDFS reasoners.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class TestRDFSReasoners extends ReasonerTestBase {
    
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    protected static Logger logger = LoggerFactory.getLogger(TestReasoners.class);

    /**
     * Boilerplate for junit
     */ 
    public TestRDFSReasoners( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        try {
            // FB reasoner doesn't support validation so the full set of wg tests are
            // commented out            
//            constructRDFWGtests(suite, RDFSFBRuleReasonerFactory.theInstance(), null);
            constructQuerytests(suite, "rdfs/manifest-nodirect-noresource.rdf", RDFSFBRuleReasonerFactory.theInstance(), null);
            
            Resource config = newResource().addProperty(ReasonerVocabulary.PROPenableCMPScan, "true" ); // TODO make boolean value work
//            config.addProperty(ReasonerVocabulary.PROPtraceOn, true);
            constructRDFWGtests(suite, RDFSRuleReasonerFactory.theInstance(), null);
            constructQuerytests(suite, "rdfs/manifest-standard.rdf", RDFSRuleReasonerFactory.theInstance(), config);
            
            suite.addTest(new TestRDFSMisc(RDFSRuleReasonerFactory.theInstance(), null));

            Resource configFull = newResource().addProperty(ReasonerVocabulary.PROPsetRDFSLevel, ReasonerVocabulary.RDFS_FULL);
            constructQuerytests(suite, "rdfs/manifest.rdf", RDFSRuleReasonerFactory.theInstance(), configFull);
            
            // This test was needed for the brief time the rdfs12 rules might have been in the standard
            // That's no longer true but left comment out because we might want them for OWL someday
//            constructQuerytests(suite, "rdfs/manifest-rdfs12.rdf", RDFSRuleReasonerFactory.theInstance(), configFull);

            Resource configSimple = newResource().addProperty(ReasonerVocabulary.PROPsetRDFSLevel, ReasonerVocabulary.RDFS_SIMPLE);
            constructQuerytests(suite, "rdfs/manifest-simple.rdf", RDFSRuleReasonerFactory.theInstance(), configSimple);

            // Single test case used in debugging, subsumed by above
//            constructSingleQuerytests(suite, 
//                                      "rdfs/manifest.rdf", 
//                                      "http://www.hpl.hp.com/semweb/2003/query_tester/rdfs/test13", 
//                                      RDFSRuleReasonerFactory.theInstance(), 
//                                      configFull);
            
        } catch (IOException e) {
            // failed to even built the test harness
            logger.error("Failed to construct RDFS test harness", e);
        }
        return suite;
    }  
    
    /**
     * Build a single named query test
     */
    private static void constructSingleQuerytests(TestSuite suite, String manifest, String test, ReasonerFactory rf, Resource config) throws IOException {
        ReasonerTester tester = new ReasonerTester(manifest);
        Reasoner r = rf.create(config);
        suite.addTest(new TestReasonerFromManifest(tester, test, r));
    }
    
    /**
     * Build the query tests for the given reasoner.
     */
    private static void constructQuerytests(TestSuite suite, String manifest, ReasonerFactory rf, Resource config) throws IOException {
        ReasonerTester tester = new ReasonerTester(manifest);
        Reasoner r = rf.create(config);
        for (Iterator<String> i = tester.listTests().iterator(); i.hasNext(); ) {
            String test = i.next();
            suite.addTest(new TestReasonerFromManifest(tester, test, r));
        }
    }
    
    /**
     * Build the working group tests for the given reasoner.
     */
    private static void constructRDFWGtests(TestSuite suite, ReasonerFactory rf, Resource config) throws IOException {
        WGReasonerTester tester = new WGReasonerTester("Manifest.rdf");
        for (Iterator<String> i = tester.listTests().iterator(); i.hasNext(); ) {
            String test = i.next();
            suite.addTest(new TestReasonerWG(tester, test, rf, config));
        }
    }
        
    
    /**
     * Build the query tests for the given reasoner.
     */
    public static void constructQuerytests(TestSuite suite, String manifest, Reasoner reasoner) throws IOException {
        ReasonerTester tester = new ReasonerTester(manifest);
        for (Iterator<String> i = tester.listTests().iterator(); i.hasNext(); ) {
            String test = i.next();
            suite.addTest(new TestReasonerFromManifest(tester, test, reasoner));
        }
    }
    
    /**
     * Inner class defining a test framework for invoking a single locally
     * defined query-over-inference test.
     */
    static class TestReasonerFromManifest extends TestCase {
        
        /** The tester which already has the test manifest loaded */
        ReasonerTester tester;
        
        /** The name of the specific test to run */
        String test;
        
        /** The factory for the reasoner type under test */
        Reasoner reasoner;
        
        /** Constructor */
        TestReasonerFromManifest(ReasonerTester tester, String test, Reasoner reasoner) {
            super(test);
            this.tester = tester;
            this.test = test;
            this.reasoner = reasoner;
        }
        
    
        /**
         * The test runner
         */
        @Override
        public void runTest() throws IOException {
            tester.runTest(test, reasoner, this);
        }

    }

    /**
     * Inner class defining a test framework for invoking a single 
     * RDFCore working group test.
     */
    static class TestReasonerWG extends TestCase {
        
        /** The tester which already has the test manifest loaded */
        WGReasonerTester tester;
        
        /** The name of the specific test to run */
        String test;
        
        /** The factory for the reasoner type under test */
        ReasonerFactory reasonerFactory;
        
        /** An optional configuration model */
        Resource config;
        
        /** Constructor */
        TestReasonerWG(WGReasonerTester tester, String test, 
                                 ReasonerFactory reasonerFactory, Resource config) {
            super(test);
            this.tester = tester;
            this.test = test;
            this.reasonerFactory = reasonerFactory;
            this.config = config;
        }
        
        /**
         * The test runner
         */
        @Override
        public void runTest() throws IOException {
            tester.runTest(test, reasonerFactory, this, config);
        }

    }
    
    /**
     * Inner class defining the misc extra tests needed to check out a
     * candidate RDFS reasoner.
     */
    static class TestRDFSMisc extends TestCase {
        
        /** The factory for the reasoner type under test */
        ReasonerFactory reasonerFactory;
        
        /** An optional configuration model */
        Resource config;
        
        /** Constructor */
        TestRDFSMisc(ReasonerFactory reasonerFactory, Resource config) {
            super("TestRDFSMisc");
            this.reasonerFactory = reasonerFactory;
            this.config = config;
        }

        /**
         * The test runner
         */
        @Override
        public void runTest() throws IOException {
            ReasonerTester tester = new ReasonerTester("rdfs/manifest.rdf");
            // Test effect of switching off property scan - should break container property test case
            Resource configuration = newResource();
            if (config != null) {
                for (StmtIterator i = config.listProperties(); i.hasNext();) {
                    Statement s = i.nextStatement();
                    configuration.addProperty(s.getPredicate(), s.getObject());
                }
            }
            configuration.addProperty(ReasonerVocabulary.PROPenableCMPScan, "false");
            assertTrue("scanproperties off", 
                        !tester.runTest(NAMESPACE + "rdfs/test17", reasonerFactory, null, configuration));
        
            // Check capabilities description
            Reasoner r = reasonerFactory.create(null);
            assertTrue(r.supportsProperty(RDFS.subClassOf));
            assertTrue(r.supportsProperty(RDFS.domain));
            assertTrue(r.supportsProperty(RDFS.range));

            // Datatype tests
            assertTrue( ! doTestRDFSDTRange("dttest1.nt", reasonerFactory));
            assertTrue( ! doTestRDFSDTRange("dttest2.nt", reasonerFactory));
            assertTrue( doTestRDFSDTRange("dttest3.nt", reasonerFactory));
        }

        /**
         * Helper for dt range testing - loads a file, validates it using RDFS/DT
         * and returns error status of the result
         */
        private boolean doTestRDFSDTRange(String file, ReasonerFactory rf) throws IOException {
            String langType = "RDF/XML";
            if (file.endsWith(".nt")) {
                langType = "N-TRIPLE";
            } else if (file.endsWith("n3")) {
                langType = "N3";
            }
            Model m = ModelFactory.createNonreifyingModel();
            Reader reader = new BufferedReader(new FileReader("testing/reasoners/rdfs/"+file));
            m.read(reader, WGReasonerTester.BASE_URI + file, langType);
            InfGraph g = rf.create(null).bind(m.getGraph());
            ValidityReport report = g.validate();
            if (!report.isValid()) {
                logger.debug("Validation error report:");
                for (Iterator<Report> i = report.getReports(); i.hasNext(); ) {
                    logger.debug(i.next().toString());
                }
            }
            return report.isValid();
        }
          
    }
    
}



/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/