/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.impl.DAMLModelImpl;
import com.hp.hpl.jena.ontology.impl.OntModelImpl;

/**
    ModelFactory provides methods for creating standard kinds of Model. 
    (ModelFactoryBase is helper functions for it).
*/

public class ModelFactory extends ModelFactoryBase
{
    /**
        No-one can make instances of this.
    */
    private ModelFactory()
        {}
        
    /**
        The standard reification style; quadlets contribute to reified statements,
        and are visible to listStatements().
    */
    public static final Reifier.Style Standard = Reifier.Standard;
    
    /**
        The convenient reification style; quadlets contribute to reified statements,
        but are invisible to listStatements().
    */
    public static final Reifier.Style Convenient = Reifier.Convenient;
    
    /**
        The minimal reification style; quadlets do not contribute to reified statements,
        and are visible to listStatements(). 
    */
    public static final Reifier.Style Minimal = Reifier.Minimal;
    
    /**
        Answer a ModelSpec which can create models to the specifcations in the RDF
        description.
    */
    public static ModelSpec createSpec( Model desc )
        { return ModelSpecImpl.create( desc ); }
        
    /**
        Answer a fresh Model created according to the ModelSpec argument.
    */
    public static Model createModel( ModelSpec desc )
        { return desc.createModel(); }
    
    /** 
        Answer a fresh Model with the default specification and Standard reification style
        [reification triples contribute to ReifiedStatements, and are visible to listStatements,
        etc]. 
    */
    public static Model createDefaultModel()
        { return createDefaultModel( Standard ); }
               
    /** 
        Answer a new memory-based model with the given reification style
    */
    public static Model createDefaultModel( Reifier.Style style )
        { return new ModelCom( new GraphMem( style ) ); }   

    /**
        Answer a read-only Model with all the statements of this Model and any
        statements "hidden" by reification. That model is dynamic, ie
        any changes this model will be reflected that one.
    */
    public static Model withHiddenStatements( Model m )
        { return ModelReifier.withHiddenStatements( m ); }
        
    /**
        construct a new memory-based model that does not capture reification triples
        (but still handles reifyAs() and .as(ReifiedStatement).
    */
    public static Model createNonreifyingModel()
        { return new ModelCom( new GraphMem( Minimal ) );}
        
    /** 
     * Answer a model that encapsulates the given graph.
     * @param g A graph structure
     * @return A model presenting an API view of graph g
     */
    public static Model createModelForGraph( Graph g ) {
        return new ModelCom( g ); 
    }
    
    /**
        Answer a ModelMaker that constructs memory-based Models that
        are backed by files in the root directory. The Model is loaded from the
        file when it is opened, and when the Model is closed it is written back.
        The model is given the Standard reification style.
        
        @param root the name of the directory in which the backing files are held
        @return a ModelMaker linked to the files in the root
    */
    public static ModelMaker createFileModelMaker( String root )
        { return createFileModelMaker( root, Standard ); }
    
    /**
        Answer a ModelMaker that constructs memory-based Models that
        are backed by files in the root directory. The Model is loaded from the
        file when it is opened, and when the Model is closed it is written back.
        
        @param root the name of the directory in which the backing files are held
        @param style the desired reification style
        @return a ModelMaker linked to the files in the root
    */
    public static ModelMaker createFileModelMaker( String root, Reifier.Style style )
        { return new ModelMakerImpl( new FileGraphMaker( root, style ) ); }
        
    /**
        Answer a ModelMaker that constructs memory-based Models that do
        not persist past JVM termination. The model has the Standard reification
        style.
        
        @return a ModelMaker that constructs memory-based models
    */
    public static ModelMaker createMemModelMaker()
        { return createMemModelMaker( Standard ); }
        
    /**
        Answer a ModelMaker that constructs memory-based Models that do
        not persist past JVM termination, with the given reification style.
        
        @param style the reification style for the model
        @return a ModelMaker that constructs memory-based models
    */
      public static ModelMaker createMemModelMaker( Reifier.Style style )
        { return new ModelMakerImpl( new SimpleGraphMaker( style ) ); }
        
    /**
        Answer a ModelMaker that accesses database-backed Models on
        the database at the other end of the connection c with the usual
        Standard reification style.
        
        @param c a connection to the database holding the models
        @return a ModelMaker whose Models are held in the database at c
    */
    public static ModelMaker createModelRDBMaker( IDBConnection c )
        { return createModelRDBMaker( c, Standard ); }
        
    /**
        Answer a ModelMaker that accesses database-backed Models on
        the database at the other end of the connection c with the given
        reification style.
        
        @param c a connection to the database holding the models
        @param style the desired reification style
        @return a ModelMaker whose Models are held in the database at c
    */        
    public static ModelMaker createModelRDBMaker
        ( IDBConnection c, Reifier.Style style )
        { return new ModelMakerImpl( new GraphRDBMaker( c, style ) ); }
        
        
    /**
        Answer a plain IDBConnection to a database with the given URL, with
        the given user having the given password. For more complex ways of
        forming a connection, see the DBConnection documentation.
        
        @param url the URL of the database
        @param user the user name to use to access the database
        @param password the password to use. WARNING: open text.
        @param dbtype the databate type: currently, "Oracle" or "MySQL".
        @return the connection
        @exception quite possibly
    */
    public static IDBConnection createSimpleRDBConnection
        ( String url, String user, String password, String dbType )
        { return new DBConnection( url, user, password, dbType ); }
        
    /**
        Answer a plain IDBConnection to a database, with the arguments implicitly
        supplied by system properties:
    <p>    
        The database URL - jena.db.url
        <br>The user - jena.db.user, or fails back to "test"
        <br>The password - jena.db.password, or fails back to ""
        <br>The db type - jena.db.type, or guessed from the URL
    */
    public static IDBConnection createSimpleRDBConnection()
        { 
        return createSimpleRDBConnection
            ( guessDBURL(), guessDBUser(), guessDBPassword(), guessDBType() );
        }
               
    /**
     * Return a Model through which all the RDFS entailments 
     * derivable from the given model are accessible. Some work is done
     * when the inferenced model is created but each query will also trigger some
     * additional inference work.
     * 
     * @param model the Model containing both instance data and schema assertions to be inferenced over
     */
    public static InfModel createRDFSModel(Model model) {
         Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
         InfGraph graph     = reasoner.bind(model.getGraph());
         return new InfModelImpl(graph);
    }
               
    /**
     * Return a Model through which all the RDFS entailments 
     * derivable from the given data and schema models are accessible. 
     * There is no strict requirement to separate schema and instance data between the two
     * arguments.
     * 
     * @param model a Model containing instance data assertions 
     * @param schema a Model containing RDFS schema data
     */
    public static InfModel createRDFSModel(Model schema, Model model) {
         Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
         InfGraph graph     = reasoner.bindSchema(schema.getGraph()).bind(model.getGraph());
         return new InfModelImpl(graph);
    }
    
    /**
     * Build an inferred model by attaching the given RDF model to the given reasoner.
     * 
     * @param reasoner the reasoner to use to process the data
     * @param model the Model containing both instance data and schema assertions to be inferenced over
     */
    public static InfModel createInfModel(Reasoner reasoner, Model model) {
         InfGraph graph     = reasoner.bind(model.getGraph());
         return new InfModelImpl(graph);
    }
               
    /**
     * Build an inferred model by attaching the given RDF model to the given reasoner.
     * 
     * @param reasoner the reasoner to use to process the data
     * @param model a Model containing instance data assertions 
     * @param schema a Model containing RDFS schema data
     */
    public static InfModel createInfModel(Reasoner reasoner, Model schema, Model model) {
         InfGraph graph     = reasoner.bindSchema(schema.getGraph()).bind(model.getGraph());
         return new InfModelImpl(graph);
    }
    

    /**
     * <p>
     * Answer a new ontology model which will process in-memory models of
     * ontologies expressed the default ontology language (OWL).
     * The default document manager
     * will be used to load the ontology's included documents.
     * </p>
     * 
     * @return A new ontology model
     * @see OntModelSpec
     */
    public static OntModel createOntologyModel() {
        return createOntologyModel( ProfileRegistry.OWL_LANG );
    }
    
    
    /**
     * <p>
     * Answer a new ontology model which will process in-memory models of
     * ontologies in the given language.
     * The default document manager
     * will be used to load the ontology's included documents.
     * </p>
     * 
     * @param languageURI The URI specifying the ontology language we want to process
     * @return A new ontology model
     * @see OntModelSpec
     */
    public static OntModel createOntologyModel( String languageURI ) {
        return createOntologyModel( OntModelSpec.getDefaultSpec( languageURI ), null );
    }
    
    
    /**
     * <p>
     * Answer a new ontology model which will process in-memory models of
     * ontologies expressed the default ontology language (OWL).
     * The default document manager
     * will be used to load the ontology's included documents.
     * </p>
     * 
     * @param spec An ontology model specification that defines the language and reasoner to use
     * @param maker A model maker that is used to get the initial store for the ontology (unless
     * the base model is given),
     * and create addtional stores for the models in the imports closure
     * @param base The base model, which contains the contents of the ontology to be processed
     * @return A new ontology model
     * @see OntModelSpec
     */
    public static OntModel createOntologyModel( OntModelSpec spec, ModelMaker maker, Model base ) {
        OntModelSpec _spec = new OntModelSpec( spec );
        _spec.setModelMaker( maker );
        
        return createOntologyModel( _spec, base );
    }
    
    
    /**
     * <p>
     * Answer a new ontology model, constructed according to the given ontology model specification, 
     * and starting with the ontology data in the given model.
     * </p>
     * 
     * @param spec An ontology model specification object, that will be used to construct the ontology
     * model with different options of ontology language, reasoner, document manager and storage model
     * @param base An existing model to treat as an ontology model, or null.
     * @return A new ontology model
     * @see OntModelSpec
     */
    public static OntModel createOntologyModel( OntModelSpec spec, Model base ) {
        return new OntModelImpl( spec, base );
    }
    
    
    /**
     * <p>Answer a model for processing DAML+OIL, using the legacy Jena1 DAML API.  Users are encouraged
     * to switch from the DAML-specific API to the new generic ontology API 
     * (see {@link #createOntologyModel(OntModelSpec, Model)}).  The continuation of the DAML-specific
     * API in Jena is not assured beyond Jena version 2.</p>
     * 
     * @return A model for in-memory processing of DAML objects.
     */
    public static DAMLModel createDAMLModel() {
        return new DAMLModelImpl( OntModelSpec.getDefaultSpec( ProfileRegistry.DAML_LANG ), null );
    }
}
    

/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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