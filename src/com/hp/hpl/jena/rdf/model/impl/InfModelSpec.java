/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.WrappedReasonerFactory;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    A ModelSpec for InfModels. The description of an InfModel is the description of a 
    ModelMaker [for the base graph] plus the JenaModelSpec.reasonsWith property to give the
    Resource who's URI identifies the reasoner to use [as per the ReasonerRegistry].
    
 	@author kers
*/
public class InfModelSpec extends ModelSpecImpl
    {
    /**
        The Resource who's URI identifies the reasoner to use.
    */
    protected Resource reasonerResource;
    protected Resource reasonerRoot;
    protected ReasonerFactory factory;
    
    /**
        Initialise an InfModelSpec using the ModelMaker specification and the value of
        the JenaModelSpec.reasoner property.
    */
    public InfModelSpec( Resource root, Model description )
        {
        super( root, description );
        Statement st = description.getRequiredProperty( null, JenaModelSpec.reasoner );
        Statement xx = description.listStatements( root, JenaModelSpec.reasonsWith, (RDFNode) null ).nextStatement();
        reasonerRoot = st.getSubject();
        Resource yy = xx.getResource();
        reasonerResource = st.getResource();
        factory = getReasonerFactory( st.getSubject(), description );
        }   

    /**
        Answer an InfModel that does the reasoning as defined by the reasoner URI over
        a new graph that is created by the ModelMaker.
        
        @return a new InfModel reasoning over a new base graph.
    */
    public Model doCreateModel()
        { return createModel( maker.getGraphMaker().createGraph() ); }
        
    /**
        Answer a new InfModel based over the graph named in the underlying ModelMaker. 
     	@see com.hp.hpl.jena.rdf.model.ModelSpec#createModelOver(java.lang.String)
     */
    public Model implementCreateModelOver( String name )
        { return createModel( maker.getGraphMaker().createGraph( name ) ); }
        
    /**
        Answer an InfModel based on the given Graph which does reasoning as required
        by this Specs reasonerResource.
        
     	@param base the base graph that the inference is done over
     	@return an inference model that does this spec's reasoning over the base
     */
    protected Model createModel( Graph base )
        {
        Reasoner reasoner = factory.create( null ); 
        return new InfModelImpl( reasoner.bind( base ) );     
        }
    
    /**
        Answer the maker property needed by descriptions.
        @return JenaModelSpec.maker
    */
    public Property getMakerProperty()
        { return JenaModelSpec.maker; }
    
    /**
        Add this ModelMaker and Reasoner description to the supplied model under the
        given name, and answer the descrption model.
        
        @param desc the model to augment with this description
        @param self the resource to use as our name
        @return desc, for cascading
    */
    public Model addDescription( Model desc, Resource self )
        {
        super.addDescription( desc, self );
        Resource r = reasonerRoot; 
        desc.add( self, JenaModelSpec.reasonsWith, r );
        desc.add( r, JenaModelSpec.reasoner, reasonerResource );
        new ModelExtract( notJMS ) .extractInto( desc, r, description );
        return desc;    
        }
    
    private final static TripleBoundary notJMSTriple = new TripleBoundary()
        { public boolean stopAt( Triple t ) { return !t.getPredicate().getNameSpace().equals( JenaModelSpec.baseURI ); }};
    
    private static final StatementBoundary notJMS = new StatementBoundary()
        {
        public boolean stopAt( Statement s ) { return notJMSTriple.stopAt( s.asTriple() ); }
        public TripleBoundary asTripleBoundary( Model ignored ) { return notJMSTriple; }
        };

    /**
         Answer a ReasonerFactory described by the properties of the resource
         <code>R</code> in the model <code>rs</code>. Will throw 
         NoReasonerSuppliedException if no jms:reasoner is supplied, or
         NoSuchReasonerException if the reasoner value isn't known to
         ReasonerRegistry. If any <code>ruleSetURL</code>s are supplied, the
         reasoner factory must be a RuleReasonerFactory, and is wrapped so that
         the supplied rules are specific to this Factory.
    */
    public static ReasonerFactory getReasonerFactory( Resource R, Model rs )
        {
        StmtIterator r = rs.listStatements( R, JenaModelSpec.reasoner, (RDFNode) null );
        if (r.hasNext() == false) throw new NoReasonerSuppliedException();
        Resource rr = r.nextStatement().getResource();
        String rrs = rr.getURI();
        ReasonerFactory rf = ReasonerRegistry.theRegistry().getFactory( rrs );
        if (rf == null) throw new NoSuchReasonerException( rrs );
        return new WrappedReasonerFactory( rf, ((Resource) R.inModel( rs )) );
        }
    }


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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