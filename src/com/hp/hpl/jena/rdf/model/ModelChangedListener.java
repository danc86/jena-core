/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.rdf.model;

/**
    The interface for classes that listen for model-changed events
 	@author kers (design by andy & the team)
*/
public interface ModelChangedListener
    {
    /**
        Method to call when a single statement has been added to the attached model.
        @param s the statement that has been presented for addition.
    */
    void addedStatement( Statement s );
    
    /**
        Method to call when an array of statements has been added to the attached model.
        NOTE. This array need not be == to the array added using Model::add(Statement[]).
        
        @param statements the array of added statements
    */
    void addedStatements( Statement [] statements );
    
    /**
        Method to call when a single statement has been removed from the attached model.
        @param s the statement that has been presented for removal.
    */
    void removedStatement( Statement s );
    
    /**
        Method to call when an array of statements has been removed from the attached 
        model. NOTE. This array need not be == to the array added using 
        Model::remove(Statement[]).
        
        @param statements the array of removed statements
    */    
    void removedStatements( Statement [] statements );
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
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