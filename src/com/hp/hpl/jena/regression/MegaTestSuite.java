/*
 *  (c) Copyright Hewlett-Packard Company 2001
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id$
 */

/*
 * MegaTestSuite.java
 *
 * Created on September 18, 2001, 7:41 PM
 */

package com.hp.hpl.jena.regression;

import junit.framework.TestSuite;

/**
 * All developers should edit this file to add their tests.
 * Please try to name your tests and test suites appropriately.
 * Note, it is better to name your test suites on creation
 * rather than in this file.
 * @author  jjc
 */
public class MegaTestSuite extends TestSuite {

    /** Creates new MegaTestSuite */
    static public TestSuite suite() {
        return new MegaTestSuite();
    }
    private MegaTestSuite() {
        super("Jena");
        addTest("GraphTestBase", com.hp.hpl.jena.graph.GraphTestBase.suite());
        /* redundant ? */
        addTest("basic Node tests", com.hp.hpl.jena.graph.test.TestNode.suite());
        /* redundant ? */
        addTest("basic Graph tests", com.hp.hpl.jena.graph.test.TestGraph.suite());
        addTest(
            "Graph test suite",
            com.hp.hpl.jena.graph.test.TestPackage.suite());
        addTest(
            "Memory Model",
            com.hp.hpl.jena.mem.TestSuiteRegression.suite());
//         addTest(
//             "graph.query.QueryTest",
//             com.hp.hpl.jena.graph.query.QueryTest.suite());
/*        addTest(
            "inference test",
            com.hp.hpl.jena.inference.InferenceTestSuite.suite());
            */
        if (false)
            addTest("DAML", com.hp.hpl.jena.ontology.daml.test.DAMLTest.suite());
        else
            System.err.println("WARNING: DAML tests suppressed for the moment");
    }
    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}
