/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.WrappedRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.test.TestSetRules;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/**
    An abstract base class for implementations of ModelSpec. It provides the base 
    functionality of providing a ModelMaker (different sub-classes use this for different
    purposes) and utility methods for reading and creating RDF descriptions. It also
    provides a value table associating freshly-constructed bnodes with arbitrary Java
    values, so program-constructed specifications can pass on database connections,
    actual document managers, and so forth.
    
 	@author kers
*/
public abstract class ModelSpecImpl implements ModelSpec
    {
    /**
        The ModelMaker that may be used by sub-classes.
    */
    protected ModelMaker maker;
    
    /**
        The map which associates bnodes with Java values.
    */        
    private static Map values = new HashMap();
    
    /**
        Initialise this ModelSpec with the supplied non-nullModeMaker.
        
        @param maker the ModelMaker to use, or null to create a fresh one
    */
    public ModelSpecImpl( ModelMaker maker )
        {
        if (maker == null) throw new RuntimeException( "null maker not allowed" );
        this.maker = maker; 
        }
        
    public ModelSpecImpl( Resource root, Model description )
        { 
        this( createMaker( getMaker( root, description ), description ) );
        this.root = root;
        this.description = description; 
        }
    
    protected static final Model emptyModel = ModelFactory.createDefaultModel();
    
    protected Model description = emptyModel;
    
    protected Resource root = ResourceFactory.createResource( "" );
        
    /**
        Answer a Model created according to this ModelSpec; left abstract for subclasses
        to implement.
    */
    public abstract Model createModel();
    
    /**
        Answer a Model created according to this ModelSpec and based on an underlying
        Model with the given name.
         
     	@see com.hp.hpl.jena.rdf.model.ModelSpec#createModelOver(java.lang.String)
     */
    public abstract Model createModelOver( String name );
    
    /**
        Answer the JMS subproperty of JMS.maker that describes the relationship 
        between this specification and its ModelMaker.
        
        @return a sub-property of JMS.maker
    */
    public abstract Property getMakerProperty();
    
    /**
        Answer a ModelSpec created from the RDF model to be found using the URI of
        the (non-bnode) Resource.
        
        see com.hp.hpl.jena.rdf.model.impl.createByRoot
     	@param desc the resource who's URI names the model to read
     	@return a ModelSpec as described by the resource
    */
    public static ModelSpec create( Resource desc )
        { return create( readModel( desc ) ); }
    
    /*
     	Answer a Model, as per the specification of ModelSource; if the name
     	is useful, use it, otherwise don't bother. Default implementation is
     	to return a fresh model.
    */
    public Model openModel( String URI )
        { return ModelFactory.createDefaultModel(); }
    
    /**
     	Answer null, as ModelSpecs as ModelSources don't remember any Models.
     	This is consistent with openModel() always creating a new Model.
    */
    public Model getExistingModel( String URI )
        { return null; }
        
    /**
        Answer a ModelSpec created from the RDF model to be found using the URI of
        the (non-bnode) Resource and starting from the given root.
        
        see com.hp.hpl.jena.rdf.model.impl.createByRoot
        @param root the root of the description within the model
        @param desc the resource who's URI names the model to read
        @return a ModelSpec as described by the resource
    */        
    public static ModelSpec create( Resource root, Resource desc )
        { return create( root, readModel( desc ) ); }
        
    /**
        see com.hp.hpl.jena.rdf.model.impl.createByRoot
        @param desc a model containing a JMS description
        @return a ModelSpec fitting that description
    */
    public static ModelSpec create( Model desc )
        { Model d = withSchema( desc );
        return createByRoot( findRootByType( d, JMS.ModelSpec ), d ); }
        
    /**
        see com.hp.hpl.jena.rdf.model.impl.createByRoot
        @param root theJMS:ModelSpec resource that roots the description
        @param desc a model containing a JMS description
        @return a ModelSpec fitting that description
    */        
    public static ModelSpec create( Resource root, Model desc )
        { return createByRoot( root, withSchema( desc ) ); }
        
    /**
        Answer a new ModelSpec created from the description handing of the root resource.
        The description model must be RDFS-complete.
        
     	@param root theJMS:ModelSpec resource that roots the description
     	@param fullDesc an RDFS-complete model containing a JMS description
     	@return the ModelSpec fitting that description
     */
    public static ModelSpec createByRoot( Resource root, Model fullDesc )
        {
        Resource type = findSpecificType( fullDesc, root, JMS.ModelSpec );
        ModelSpecCreator sc = ModelSpecCreatorRegistry.findCreator( type );
        if (sc == null) throw new BadDescriptionException( "neither ont nor inf nor mem", fullDesc );
        return sc.create( root, fullDesc );    
        }
        
    public static Resource getMaker( Resource root, Model desc )
        {
        StmtIterator it = desc.listStatements( root, JMS.maker, (RDFNode) null );
        if (it.hasNext())
        	return it.nextStatement().getResource();
        else 
            throw new BadDescriptionException( "no jms:maker for " + root, desc );
        }
        
    /**
        Answer the "most specific" type of root in desc which is an instance of type.
        We assume a single inheritance thread starting with that type. 
        
    	@param desc the model the search is conducted in
    	@param root the subject whos type is to be found
    	@param type the base type for the search
    	@return T such that (root type T) and if (root type T') then (T' subclassof T)
    */
    static Resource findSpecificType( Model desc, Resource root, Resource type )
        {
        StmtIterator it = desc.listStatements( root, RDF.type, (RDFNode) null );
        while (it.hasNext())
            {
            Resource candidate = it.nextStatement().getResource();
            if (desc.contains( candidate, RDFS.subClassOf, type )) type = candidate;  
            }
        return type;    
        }
        
    /**
        Answer the ModelMaker that this ModelSpec uses.
        @return the embedded ModelMaker
    */
    public ModelMaker getModelMaker()
        { return maker; }
        
    public Model getDescription() 
        { return getDescription( ResourceFactory.createResource() ); }
        
    public Model getDescription( Resource root ) 
        { return addDescription( ModelFactory.createDefaultModel(), root ); }

    public Model addDescription( Model desc, Resource root )
        {
        Resource makerRoot = desc.createResource();
        desc.add( root, JMS.maker, makerRoot );
        maker.addDescription( desc, makerRoot );
        return desc;
        }
        
    /**
        Answer a version of the given model with RDFS completion of the JMS
        schema applied. 
    */
    public static Model withSchema( Model m )
        { return ModelFactory.createRDFSModel( JMS.schema, m ); }

    /**
        Answer a new bnode Resource associated with the given value. The mapping from
        bnode to value is held in a single static table, and is not intended to hold many
        objects; there is no provision for garbage-collecting them [this might eventually be
        regarded as a bug].
        
        @param value a Java value to be remembered 
        @return a fresh bnode bound to <code>value</code>
    */
    public static Resource createValue( Object value )
        {
        Resource it = ResourceFactory.createResource();
        values.put( it, value );
        return it;    
        }
        
    /**
        Answer the value bound to the supplied bnode, or null if there isn't one or the
        argument isn't a bnode.
        
        @param it the RDF node to be looked up in the <code>createValue</code> table.
        @return the associated value, or null if there isn't one.
    */
    public static Object getValue( RDFNode it )
        { return values.get( it ); }
        
    /**
        Answer the unique subject with the given rdf:type.
        
        @param m the model in which the typed subject is sought
        @param type the RDF type the subject must have
        @return the unique S such that (S rdf:type type)
        @exception BadDescriptionException if there's not exactly one subject
    */        
    public static Resource findRootByType( Model description, Resource type )
        { 
        Model d = withSchema( description );
        ResIterator rs  = d.listSubjectsWithProperty( RDF.type, type );
        if (!rs.hasNext()) throw new BadDescriptionException( "no " + type + " thing found", description );
        Resource result = rs.nextResource();
        if (rs.hasNext()) throw new BadDescriptionException( "ambiguous " + type + " thing found", description );
        return result;
        }
    
    /**
        Answer a ModelMaker that conforms to the supplied description. The Maker
        is found from the ModelMakerCreatorRegistry by looking up the most 
        specific type of the unique object with type JMS.MakerSpec.
        
        @param d the model containing the description
        @return a ModelMaker fitting that description
    */
    public static ModelMaker createMaker( Model description )
        { Model d = withSchema( description );
        return createMakerByRoot( findRootByType( d, JMS.MakerSpec ), d ); }
        
    public static ModelMaker createMaker( Resource root, Model d )
        { return createMakerByRoot( root, withSchema( d ) ); }
        
    public static ModelMaker createMakerByRoot( Resource root, Model fullDesc )
        { Resource type = findSpecificType( fullDesc, root, JMS.MakerSpec );
        ModelMakerCreator mmc = ModelMakerCreatorRegistry.findCreator( type );
        if (mmc == null) throw new RuntimeException( "no maker type" );  
        return mmc.create( fullDesc, root ); }
        
    /**
        Read a model from a given URI.
     	@param source the resource who's URI specifies what to laod
     	@return the model as loaded from the resource URI
     */
    public static Model readModel( Resource source )
        {
        String uri = source.getURI();
        return FileUtils.loadModel( uri );
        }

    protected Model loadFiles(Model m)
        {
        StmtIterator it = description.listStatements( root, JMS.loadWith, (RDFNode) null );
        while (it.hasNext()) FileUtils.loadModel( m, it.nextStatement().getResource().getURI() );
        return m;
        }

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
        StmtIterator r = rs.listStatements( R, JMS.reasoner, (RDFNode) null );
        if (r.hasNext() == false) throw new NoReasonerSuppliedException();
        Resource rr = r.nextStatement().getResource();
        String rrs = rr.getURI();
        ReasonerFactory rf = ReasonerRegistry.theRegistry().getFactory( rrs );
        if (rf == null) throw new NoSuchReasonerException( rrs );
        return loadFactoryRulesets( rf, rs, R );
        }
    
    /**
     	If there are no jms:ruleSet or jms:ruleSetURL properties of <code>R</code>, answer the
     	supplied factory <code>rf</code>. Otherwise, <code>rf</code> must be a RuleReasonerFactory,
     	and it is wrapped up in a WrappedRuleReasonerFactory which is loaded with all the specified
     	rules.
	*/
	private static ReasonerFactory loadFactoryRulesets( ReasonerFactory rf, Model rs, Resource R )
		{
		StmtIterator rulesets = rs.listStatements( R, JMS.ruleSetURL, (RDFNode) null );
		StmtIterator others = rs.listStatements( R, JMS.ruleSet, (RDFNode) null );
		if (rulesets.hasNext() || others.hasNext())
		    {
		    WrappedRuleReasonerFactory f = new WrappedRuleReasonerFactory( (RuleReasonerFactory) rf );
		    while (rulesets.hasNext()) load( f, rulesets.nextStatement().getResource() );
		    while (others.hasNext()) loadNamedRulesets( f, others.nextStatement().getResource() );
		    return f;
		    }
		else
			return rf;
		}

	/**
	 	load into the factory <code>f</code> any rules described by the jms:hasRule and
	 	jms:ruleSetURL properties of <code>ruleSet</code>.
	*/
	protected static void loadNamedRulesets( RuleReasonerFactory f, Resource ruleSet )
        {
        loadRulesetStrings( f, ruleSet );
        loadRulesetURLs( f, ruleSet );
        }
    
    /**
     	load into the factory <code>f</code> the rules given by the literal strings
     	which are the jms:hasRule properties of <code>ruleSet</code>.
	*/
	protected static void loadRulesetStrings( RuleReasonerFactory f, Resource ruleSet )
		{
		StmtIterator it = ruleSet.listProperties( JMS.hasRule );
		while (it.hasNext())
		    f.addRules( Rule.parseRules( it.nextStatement().getString() ) );
		}

	/**
		load into the factory <code>f</code> all the rules which are at the URLs
		specified by the resources-values of the jms:ruleSetURL properties
		of <code>ruleSet</code>.
	*/
	protected static void loadRulesetURLs( RuleReasonerFactory f, Resource ruleSet )
		{
		StmtIterator it = ruleSet.listProperties( JMS.ruleSetURL );
		while (it.hasNext()) load( f, it.nextStatement().getResource() );
		}

	/**
	 	load into the factory <code>f</code> the rules at the URL which is the
	 	URI of the resource <code>u</code>.
    */
    protected static void load( RuleReasonerFactory rf, Resource u )
        { rf.addRules( Rule.rulesFromURL( u.getURI() ) ); }
                
    }

/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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