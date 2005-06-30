/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 * RDFNode.java
 *
 * Created on 25 July 2000, 13:13
 */

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.FrontsNode;

/** 
     An RDF Resource or an RDF Literal.

    <p><CODE>RDFNode</CODE> represents the methods which RDF Resources and RDF
    Literals have in common.</p>
    <p>Chris added the _as_ method to allow RDFNodes to participate in polymorphic
    conversions.
    @author bwm
*/
public interface RDFNode extends FrontsNode
    {
    /** 
        Return a String representation of the node.  The form of the string 
        depends on the type of the node.
    */
    public String toString();
    
    /** 
        Answer true iff this RDFNode is an anonynous resource. Useful for
        one-off tests: see also visitWith() for making literal/anon/URI choices.
    */
    public boolean isAnon();
    
    /** 
        Answer true iff this RDFNode is a literal resource. Useful for
        one-off tests: see also visitWith() for making literal/anon/URI choices.
    */
    public boolean isLiteral();
    
    /** 
        Answer true iff this RDFNode is an anonynous resource. Useful for
        one-off tests: see also visitWith() for making literal/anon/URI choices.
    */
    public boolean isURIResource();
    
    /**
        Answer true iff this RDFNode is a URI resource or an anonynous
        resource (ie is not a literal). Useful for one-off tests: see also 
        visitWith() for making literal/anon/URI choices.
    */
    public boolean isResource();
    
    /**
        RDFNodes can be converted to different implementation types. Convert
        this RDFNode to a type supporting the <code>view</code>interface. The 
        resulting RDFNode should be an instance of <code>view</code> and should 
        have any internal invariants as specified.
    <p>
        If the RDFNode cannot be converted, an exception is thrown.
    */
    public RDFNode as( Class view );
    
    /**
        return true iff this RDFNode can be viewed as a _view_.
    */
    public boolean canAs( Class view );
    
    /**
        returns a .equals() version of this node, except that its in the model m.
        
        @param m a model to move the node to
        @return this, if it's already in m (or no model), a copy in m otherwise
    */
    public RDFNode inModel( Model m );
    
    /**
        Apply the appropriate method of the visitor to this node's content and
        return the result.
        
        @param rv an RDFVisitor with a method for URI/blank/literal nodes
        @return the result returned by the selected method
    */
    public Object visitWith( RDFVisitor rv );
    }
