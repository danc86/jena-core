/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id$
*/

package com.hp.hpl.jena.assembler.exceptions;

import com.hp.hpl.jena.rdf.model.Resource;

public class ReasonerClashException extends AssemblerException
    {
    public ReasonerClashException( Resource root )
        { super( root, "root has both reasonerFactory and reasonerURL properties" ); }
    }

