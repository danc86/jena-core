/******************************************************************
 * File:        TestTransitiveGraphCacheNew.java
 * Created by:  Dave Reynolds
 * Created on:  25-Nov-2004
 * 
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id$
 *****************************************************************/

package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveGraphCacheNew;
import com.hp.hpl.jena.reasoner.TriplePattern;
import  com.hp.hpl.jena.graph.Node;
import  com.hp.hpl.jena.graph.Triple;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A purely temporary test suite just used during development and kept
 * off the main unit test paths.
 *  
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$
 */

public class TestTransitiveGraphCacheNew extends TestCase {
    
    /** The cache under test */
    TransitiveGraphCacheNew cache;
    
    // Dummy predicates and nodes for the graph
    String NS = "urn:x-hp-test:ex/";
    Node directP = Node.createURI(NS+"directSubProperty");
    Node closedP = Node.createURI(NS+"subProperty");
    
    Node a = Node.createURI(NS+"a");
    Node b = Node.createURI(NS+"b");
    Node c = Node.createURI(NS+"c");
    Node d = Node.createURI(NS+"d");
    Node e = Node.createURI(NS+"e");
    Node f = Node.createURI(NS+"f");
    Node g = Node.createURI(NS+"g");
     
    /**
     * Boilerplate for junit
     */ 
    public TestTransitiveGraphCacheNew( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
//        return new TestSuite( TestTransitiveGraphCacheNew.class ); 
        TestSuite suite = new TestSuite();
        suite.addTest( new TestTransitiveGraphCacheNew("testBasicCache"));
        suite.addTest( new TestTransitiveGraphCacheNew("testBug1"));
        suite.addTest( new TestTransitiveGraphCacheNew("testCycle"));
        suite.addTest( new TestTransitiveGraphCacheNew("testCycle2"));
        suite.addTest( new TestTransitiveGraphCacheNew("testCycle3"));
        suite.addTest( new TestTransitiveGraphCacheNew("testDirect"));
        return suite;
    }  

    /**
     * Test the basic functioning a Transitive closure cache.
     * Caches the graph but not the final closure.
     */
    public void testBasicCache() {
        initCache();
        cache.setCaching(false);
        doBasicTest(cache);
    }
    
    /**
     * Test the basic functioning a Transitive closure cache.
     * Caches the graph and any requested closures
     */
    public void testCachingCache() {
        initCache();
        cache.setCaching(true);
        doBasicTest(cache);
    }
    
    /**
     * Test the clone operation
     */
    public void testCloning() {
        initCache();
        TransitiveGraphCacheNew clone = cache.deepCopy();
        // Mess with the original to check cloning
        cache.addRelation(a, d);
        cache.addRelation(g, a);
        doBasicTest(clone);
    }
        
    /**
     * Initialize the cache with some test data
     */
    private void initCache() {
        // Create a graph with reflexive references, cycles, redundant links
        cache = new TransitiveGraphCacheNew(directP, closedP);        
        cache.addRelation(a, b);
        cache.addRelation(b, e);
        cache.addRelation(b, c);
        cache.addRelation(e, f);
        cache.addRelation(c, f);
        cache.addRelation(f, g);
        cache.addRelation(d, c);
        cache.addRelation(d, e);
        cache.addRelation(d, g);  // reduntant two ways
        cache.addRelation(a, e);  // redundant
        cache.addRelation(d, b);  // Makes both earlier d's redundant
        
        cache.addRelation(a, a);
        cache.addRelation(b, b);
        cache.addRelation(c, c);
        cache.addRelation(d, d);
        cache.addRelation(e, e);
        cache.addRelation(f, f);
        cache.addRelation(g, g);
    }
            
    public void doBasicTest(TransitiveGraphCacheNew cache) {
         // Test forward property patterns
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
                new Triple(a, closedP, e),
                new Triple(a, closedP, f),
                new Triple(a, closedP, g)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, g)),
            new Object[] {
                new Triple(a, closedP, g),
            });
            
        // Test backward patterns
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, directP, f)),
            new Object[] {
                new Triple(e, closedP, f),
                new Triple(f, closedP, f),
                new Triple(c, closedP, f)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, closedP, f)),
            new Object[] {
                new Triple(f, closedP, f),
                new Triple(e, closedP, f),
                new Triple(b, closedP, f),
                new Triple(c, closedP, f),
                new Triple(a, closedP, f),
                new Triple(d, closedP, f)
            });
        
        // List all cases
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, directP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(d, closedP, d),
                new Triple(d, closedP, b),
                new Triple(b, closedP, b),
                new Triple(b, closedP, e),
                new Triple(b, closedP, c),
                new Triple(e, closedP, e),
                new Triple(e, closedP, f),
                new Triple(c, closedP, c),
                new Triple(c, closedP, f),
                new Triple(f, closedP, f),
                new Triple(f, closedP, g),
                new Triple(g, closedP, g)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, closedP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
                new Triple(a, closedP, e),
                new Triple(a, closedP, f),
                new Triple(a, closedP, g),
                new Triple(d, closedP, d),
                new Triple(d, closedP, b),
                new Triple(d, closedP, e),
                new Triple(d, closedP, c),
                new Triple(d, closedP, f),
                new Triple(d, closedP, g),
                new Triple(b, closedP, b),
                new Triple(b, closedP, e),
                new Triple(b, closedP, c),
                new Triple(b, closedP, f),
                new Triple(b, closedP, g),
                new Triple(e, closedP, e),
                new Triple(e, closedP, f),
                new Triple(e, closedP, g),
                new Triple(c, closedP, c),
                new Triple(c, closedP, f),
                new Triple(c, closedP, g),
                new Triple(f, closedP, f),
                new Triple(f, closedP, g),
                new Triple(g, closedP, g)
             });
        
        // Add a look in the graph and check the loop from each starting position
        cache.addRelation(g, e);
        
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(e, directP, null)),
                new Object[] {
                    new Triple(e, closedP, e),
                    new Triple(e, closedP, f),
                    new Triple(e, closedP, g)
                });
            TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(f, directP, null)),
                new Object[] {
                    new Triple(f, closedP, f),
                    new Triple(f, closedP, g),
                    new Triple(f, closedP, e)
                });
            TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(g, directP, null)),
                new Object[] {
                    new Triple(g, closedP, g),
                    new Triple(g, closedP, e),
                    new Triple(g, closedP, f)
                });
            TestUtil.assertIteratorValues(this, 
                    cache.find(new TriplePattern(null, directP, e)),
                    new Object[] {
                        new Triple(e, closedP, e),
                        new Triple(f, closedP, e),
                        new Triple(b, closedP, e),
                        new Triple(c, closedP, e),
                        new Triple(g, closedP, e)
                    });
                TestUtil.assertIteratorValues(this, 
                    cache.find(new TriplePattern(null, directP, f)),
                    new Object[] {
                        new Triple(f, closedP, f),
                        new Triple(g, closedP, f),
                        new Triple(b, closedP, f),
                        new Triple(c, closedP, f),
                        new Triple(e, closedP, f)
                    });
                TestUtil.assertIteratorValues(this, 
                    cache.find(new TriplePattern(null, directP, g)),
                    new Object[] {
                        new Triple(g, closedP, g),
                        new Triple(e, closedP, g),
                        new Triple(b, closedP, g),
                        new Triple(c, closedP, g),
                        new Triple(f, closedP, g)
                    });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(g, closedP, null)),
            new Object[] {
                new Triple(g, closedP, g),
                new Triple(g, closedP, e),
                new Triple(g, closedP, f)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(e, closedP, null)),
            new Object[] {
                new Triple(e, closedP, g),
                new Triple(e, closedP, e),
                new Triple(e, closedP, f)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(f, closedP, null)),
            new Object[] {
                new Triple(f, closedP, g),
                new Triple(f, closedP, e),
                new Triple(f, closedP, f)
            });
        /*        
        System.out.println("Add e-f-g-e loop");        
        cache.printAll();
        listFind(cache, e, directP, null);
        listFind(cache, e, closedP, null);
        listFind(cache, f, directP, null);
        listFind(cache, f, closedP, null);
        listFind(cache, g, directP, null);
        listFind(cache, g, closedP, null);        
        */
    }
    
    /**
     * Test a a case where an earlier version had a bug due to removing
     * a link which was required rather than redundant.
     */
    public void testBug1() {
        TransitiveGraphCacheNew cache = new TransitiveGraphCacheNew(directP, closedP);
        cache.addRelation(a, b);  
        cache.addRelation(c, a);        
        cache.addRelation(c, b);        
        cache.addRelation(a, c);     
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
            });
           
    }
        
    /**
     * Test the removeRelation functionality.
     */
    public void testRemove() {
        TransitiveGraphCacheNew cache = new TransitiveGraphCacheNew(directP, closedP);
        cache.addRelation(a, b);
        cache.addRelation(a, c);
        cache.addRelation(b, d);
        cache.addRelation(c, d);
        cache.addRelation(d, e);
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
                new Triple(a, closedP, d),
                new Triple(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(b, closedP, null)),
            new Object[] {
                new Triple(b, closedP, b),
                new Triple(b, closedP, d),
                new Triple(b, closedP, e)
            });
        cache.removeRelation(b, d);
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
                new Triple(a, closedP, d),
                new Triple(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(b, closedP, null)),
            new Object[] {
                new Triple(b, closedP, b),
            });
        cache.removeRelation(a, c);
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(b, closedP, null)),
            new Object[] {
                new Triple(b, closedP, b),
            });
    }
    
    /**
     * Test direct link case with adverse ordering.
     */
    public void testDirect() {
        TransitiveGraphCacheNew cache = new TransitiveGraphCacheNew(directP, closedP);
        cache.addRelation(a, b);
        cache.addRelation(c, d);
        cache.addRelation(a, d);
        cache.addRelation(b, c);
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
            });
    }
    
    /**
     * Test cycle detection.
     */
    public void testCycle() {
        TransitiveGraphCacheNew cache = new TransitiveGraphCacheNew(directP, closedP);
        cache.addRelation(a, b);
        cache.addRelation(b, c);
        cache.addRelation(a, c);
        cache.addRelation(c, b);
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
            });
    }
    
    /**
     * A ring of three cycle
     */
    public void testCycle2() {
        TransitiveGraphCacheNew cache = new TransitiveGraphCacheNew(directP, closedP);
        cache.addRelation(a, b);
        cache.addRelation(a, c);
        cache.addRelation(f, b);
        cache.addRelation(b, g);
        cache.addRelation(b, d);
        cache.addRelation(d, c);
        cache.addRelation(d, e);
        cache.addRelation(c, e);
        cache.addRelation(c, b);
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(c, directP, null)),
                new Object[] {
                    new Triple(c, closedP, e),
                    new Triple(c, closedP, g),
                    new Triple(c, closedP, b),
                    new Triple(c, closedP, d),
                    new Triple(c, closedP, c),
                });
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(null, directP, c)),
                new Object[] {
                    new Triple(a, closedP, c),
                    new Triple(b, closedP, c),
                    new Triple(d, closedP, c),
                    new Triple(f, closedP, c),
                    new Triple(c, closedP, c),
                });
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(f, closedP, null)),
                new Object[] {
                    new Triple(f, closedP, f),
                    new Triple(f, closedP, b),
                    new Triple(f, closedP, c),
                    new Triple(f, closedP, d),
                    new Triple(f, closedP, g),
                    new Triple(f, closedP, e),
                });
    }
    
    /**
     * Two ring-of-three cycles joined at two points
     */
    public void testCycle3() {
        TransitiveGraphCacheNew cache = new TransitiveGraphCacheNew(directP, closedP);
        cache.addRelation(a, b);
        cache.addRelation(b, c);
        cache.addRelation(c, a);
        cache.addRelation(d, e);
        cache.addRelation(e, f);
        cache.addRelation(f, d);
        cache.addRelation(b, d);
        cache.addRelation(f, c);
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(a, directP, null)),
                new Object[] {
                new Triple(a, closedP, a),
                new Triple(a, closedP, b),
                new Triple(a, closedP, c),
                new Triple(a, closedP, d),
                new Triple(a, closedP, e),
                new Triple(a, closedP, f),
                });
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(null, directP, a)),
                new Object[] {
                new Triple(a, closedP, a),
                new Triple(b, closedP, a),
                new Triple(c, closedP, a),
                new Triple(d, closedP, a),
                new Triple(e, closedP, a),
                new Triple(f, closedP, a),
                });
    }

}


/*
    (c) Copyright 2004 Hewlett-Packard Development Company, LP
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
