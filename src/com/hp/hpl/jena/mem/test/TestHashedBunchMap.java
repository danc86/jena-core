/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id$
*/

package com.hp.hpl.jena.mem.test;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestHashedBunchMap extends ModelTestBase
    {
    public TestHashedBunchMap( String name )
        { super( name ); }
    
    public void testSize()
        {
        HashCommon b = new HashedBunchMap();
        }

    }

