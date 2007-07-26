/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id$
*/

package com.hp.hpl.jena.mem.test;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestHashedBunchMap extends ModelTestBase
    { // TODO should extend this a lot
    public TestHashedBunchMap( String name )
        { super( name ); }
    
    public void testSize()
        {
        HashCommon b = new HashedBunchMap();
        }

    public void testClearSetsSizeToZero()
        {
        TripleBunch a = new ArrayBunch();
        HashedBunchMap b = new HashedBunchMap();
        b.clear();
        assertEquals( 0, b.size() );
        b.put( "key",  a );
        assertEquals( 1, b.size() );
        b.clear();
        assertEquals( 0, b.size() );
        }
    }

