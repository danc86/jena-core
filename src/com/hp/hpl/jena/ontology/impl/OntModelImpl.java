/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            22 Feb 2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.enhanced.*;

import java.io.*;
import java.util.*;



/**
 * <p>
 * Implementation of a model that can process general ontologies in OWL,
 * DAML and similar languages.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public class OntModelImpl
    extends ModelCom
    implements OntModel
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The document manager this ontology model is using to load imports */
    protected OntDocumentManager m_docMgr;
    
    /** The graph factory this ontology model is using to build the union graph of imports */
    protected GraphMaker m_graphFactory;
    
    /** The language profile that defines what we can do in this ontology */
    protected Profile m_profile;
    
    /** List of URI strings of documents that have been imported into this one */
    protected Set m_imported = new HashSet();
    
    /** Query that will access nodes with types whose type is Class */
    protected BindingQueryPlan m_individualsQuery;
    
    /** Query that will access nodes with types whose type is Class */
    protected List m_individualsQueryAlias = null;
    
    /** Mode switch for strict checking mode */
    protected boolean m_strictMode = true;
    
    
    // Constructors
    //////////////////////////////////


    /**
     * <p>
     * Construct a new ontology model, using the given model as a base.  The document manager
     * will be used to build the imports closure of the model if its policy permits.
     * </p>
     * 
     * @param languageURI Denotes the language profile that this ontology is using
     * @param model The base model that may contain existing statements for the ontology
     * @param docMgr The ontology document manager to use when building the imports closure
     * @param gf The graph factory to use when building the union of the imported graphs
     */
    public OntModelImpl( String languageURI, Model model, OntDocumentManager docMgr, GraphMaker gf ) {
        // all ontologies are defined to be union graphs, to allow us to add the imports to the union
        super( new OntologyGraph(), BuiltinPersonalities.model );
        
        Profile lang = ProfileRegistry.getInstance().getProfile( languageURI );
        if (lang == null) {
            // can't proceed after this
            throw new OntologyException( "Ontology model could not locate language profile for ontology language " + languageURI );
        }
        
        // add the base graph to the union first, so that it will receive updates
        getUnionGraph().addGraph( model.getGraph() );
        
        // record pointers to the helpers we're using
        m_profile = lang;
        m_docMgr = docMgr;
        m_graphFactory = gf;
        
        // cache the query plan for individuals
        m_individualsQuery = queryXTypeOfType( lang.CLASS() );
        
        // cache the query plan for individuals using class alias (if defined)
        if (lang.hasAliasFor( lang.CLASS() )) {
            m_individualsQueryAlias = new ArrayList();
            
            for (Iterator j = lang.listAliasesFor( lang.CLASS() );  j.hasNext(); ) {
                // add to the list of alternates for this query
                m_individualsQueryAlias.add( queryXTypeOfType( (Resource) j.next() ) );
            }
        }        
        
        // load the imports closure, according to the policies in my document manager
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
        ((OntologyGraph) graph).bind();
    }
    
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer a reference to the document manager that this model is using to manage
     * ontology &lt;-&gt; mappings, and to load the imports closure. <strong>Note</strong>
     * by default, an ontology model is constructed with a reference to the shared,
     * global document manager.  Thus changing the settings via this model's document
     * manager may affect other models also using the same instance.
     * </p>
     * @return A reference to this model's document manager
     */
    public OntDocumentManager getDocumentManager() {
        return m_docMgr;
    }
    
    
    /**
     * <p>
     * Answer an iterator that ranges over the ontology resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type Ontology</code> or equivalent. These resources
     * typically contain metadata about the ontology document that contains them. 
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model, see
     * {@link Profile#ONTOLOGY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over ontology resources. 
     */
    public Iterator listOntologies() {
        checkProfileEntry( getProfile().ONTOLOGY(), "ONTOLOGY" );
        return findByTypeAs( getProfile().ONTOLOGY(), Ontology.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type Property</code> or equivalent.  An <code>OntProperty</code>
     * is equivalent to an <code>rdfs:Property</code> in a normal RDF graph; this type is
     * provided as a common super-type for the more specific {@link ObjectProperty} and
     * {@link DatatypeProperty} property types.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over property resources. 
     */
    public Iterator listOntProperties() {
        return findByTypeAs( RDF.Property, OntProperty.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the object property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type ObjectProperty</code> or equivalent.  An object
     * property is a property that is defined in the ontology language semantics as a 
     * one whose range comprises individuals (rather than datatyped literals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#OBJECT_PROPERTY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over object property resources. 
     */
    public Iterator listObjectProperties() {
        checkProfileEntry( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY" );
        return findByTypeAs( getProfile().OBJECT_PROPERTY(), ObjectProperty.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the datatype property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type DatatypeProperty</code> or equivalent.  An datatype
     * property is a property that is defined in the ontology language semantics as a 
     * one whose range comprises datatyped literals (rather than individuals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#DATATYPE_PROPERTY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over datatype property resources. 
     */
    public Iterator listDatatypeProperties() {
        checkProfileEntry( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY" );
        return findByTypeAs( getProfile().DATATYPE_PROPERTY(), DatatypeProperty.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the individual resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type</code> corresponding to a class defined
     * in the ontology.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over Individuals. 
     */
    public Iterator listIndividuals() {
        return queryFor( m_individualsQuery, m_individualsQueryAlias, Individual.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over all of the various forms of class description resource 
     * in this model.  Class descriptions include {@link #listEnumeratedClasses enumerated}
     * classes, {@link #listUnionClasses union} classes, {@link #listComplementClasses complement}
     * classes, {@link #listIntersectionClasses intersection} classes, {@link #listClasses named}
     * classes and {@link #listRestrictions property restrictions}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over class description resources. 
     */
    public Iterator listClasses() {
        return findByTypeAs( getProfile().getClassDescriptionTypes(), OntClass.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the enumerated class class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>oneOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over enumerated class resources. 
     * @see Profile#ONE_OF
     */
    public Iterator listEnumeratedClasses()  {
        checkProfileEntry( getProfile().ONE_OF(), "ONE_OF" );
        return findByDefiningPropertyAs( getProfile().ONE_OF(), EnumeratedClass.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the union class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>unionOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over union class resources. 
     * @see Profile#UNION_OF
     */
    public Iterator listUnionClasses() {
        checkProfileEntry( getProfile().UNION_OF(), "UNION_OF" );
        return findByDefiningPropertyAs( getProfile().UNION_OF(), UnionClass.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the complement class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>complementOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over complement class resources. 
     * @see Profile#COMPLEMENT_OF
     */
    public Iterator listComplementClasses() {
        checkProfileEntry( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF" );
        return findByDefiningPropertyAs( getProfile().COMPLEMENT_OF(), ComplementClass.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the intersection class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>intersectionOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over complement class resources. 
     * @see Profile#INTERSECTION_OF
     */
    public Iterator listIntersectionClasses() {
        checkProfileEntry( getProfile().INTERSECTION_OF(), "INTERSECTION_OF" );
        return findByDefiningPropertyAs( getProfile().INTERSECTION_OF(), IntersectionClass.class );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the named class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Class</code> (or equivalent) and a node URI. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over named class resources. 
     */
    public Iterator listNamedClasses() {
        return ((ExtendedIterator) listClasses()).filterDrop(
            new Filter() {
                public boolean accept( Object x ) {
                    return ((Resource) x).isAnon();
                }
            }
        );
    }
    

    /**
     * <p>
     * Answer an iterator that ranges over the property restriction class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Restriction</code> (or equivalent). 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over restriction class resources. 
     * @see Profile#RESTRICTION
     */
    public Iterator listRestrictions() {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        return findByTypeAs( getProfile().RESTRICTION(), Restriction.class );
    }


    /**
     * <p>
     * Answer an iterator that ranges over the nodes that denote pair-wise disjointness between
     * sets of classes.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over AllDifferent nodes. 
     */
    public Iterator listAllDifferent() {
        checkProfileEntry( getProfile().ALL_DIFFERENT(), "ALL_DIFFERENT" );
        return findByTypeAs( getProfile().ALL_DIFFERENT(), AllDifferent.class );
    }
    
   
    /**
     * <p>
     * Answer an iterator that ranges over the properties in this model that are declared
     * to be annotation properties. Not all supported languages define annotation properties
     * (the category of annotation properties is chiefly an OWL innovation). 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over annotation properties. 
     * @see Profile#getAnnotationProperties()
     */
    public Iterator listAnnotationProperties() {
        checkProfileEntry( getProfile().ANNOTATION_PROPERTY(), "ANNOTATION_PROPERTY" );
        Resource r = (Resource) getProfile().ANNOTATION_PROPERTY();
        
        if (r == null) {
            return new ArrayList().iterator(); 
        }
        else {
            return findByType( r )
                   .andThen( WrappedIterator.create( getProfile().getAnnotationProperties() ) )
                   .mapWith( new SubjectNodeAs( AnnotationProperty.class ) );
        }
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an ontology description node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the ontology node. Conventionally, this corresponds to the base URI
     * of the document itself.
     * @return An Ontology resource.
     */
    public Ontology createOntology( String uri ) {
        checkProfileEntry( getProfile().ONTOLOGY(), "ONTOLOGY" );
        return (Ontology) createOntResource( Ontology.class, getProfile().ONTOLOGY(), uri );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an Indvidual node in this model. A new anonymous resource
     * will be created in the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param cls Resource representing the ontology class to which the individual belongs
     * @return A new anoymous Individual of the given class.
     */
    public Individual createIndividual( Resource cls ) {
        return (Individual) createOntResource( Individual.class, cls, null );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an Individual node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param cls Resource representing the ontology class to which the individual belongs
     * @param uri The uri for the individual, or null for an anonymous individual.
     * @return An Individual resource.
     */
    public Individual createIndividual( Resource cls, String uri ) {
        return (Individual) createOntResource( Individual.class, cls, uri );
    }
    
   
    /**
     * <p>
     * Answer a resource representing an object property in this model, 
     * and that is not a functional property. 
     * </p>
     * 
     * @param uri The uri for the object property. May not be null.
     * @return An ObjectProperty resource.
     * @see #createObjectProperty( String, boolean )
     */
    public ObjectProperty createObjectProperty( String uri ) {
        return createObjectProperty( uri, false );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an object property in this model.  An object property
     * is defined to have a range of individuals, rather than datatypes. 
     * If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the object property. May not be null.
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return An ObjectProperty resource, optionally also functional.
     */
    public ObjectProperty createObjectProperty( String uri, boolean functional ) {
        checkProfileEntry( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY" );
        ObjectProperty p = (ObjectProperty) createOntResource( ObjectProperty.class, getProfile().OBJECT_PROPERTY(), uri );

        if (functional) {
            checkProfileEntry( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
            p.addProperty( RDF.type, getProfile().FUNCTIONAL_PROPERTY() );
        }
        
        return p;
    }
    
   
    /**
     * <p>
     * Answer a resource that represents datatype property in this model, and that is
     * not a functional property.
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @return A DatatypeProperty resource.
     * @see #createDatatypeProperty( String, boolean )
     */
    public DatatypeProperty createDatatypeProperty( String uri ) {
        return createDatatypeProperty( uri, false );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents datatype property in this model. A datatype property
     * is defined to have a range that is a concrete datatype, rather than an individual. 
     * If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return A DatatypeProperty resource.
     */
    public DatatypeProperty createDatatypeProperty( String uri, boolean functional ) {
        checkProfileEntry( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY" );
        DatatypeProperty p = (DatatypeProperty) createOntResource( DatatypeProperty.class, getProfile().DATATYPE_PROPERTY(), uri );

        if (functional) {
            checkProfileEntry( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
            p.addProperty( RDF.type, getProfile().FUNCTIONAL_PROPERTY() );
        }
     
        return p;   
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an annotation property in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the annotation property.
     * @return An AnnotationProperty resource.
     */
    public AnnotationProperty createAnnotationProperty( String uri ) {
        checkProfileEntry( getProfile().ANNOTATION_PROPERTY(), "ANNOTATION_PROPERTY" );
        return (AnnotationProperty) createOntResource( AnnotationProperty.class, getProfile().ANNOTATION_PROPERTY(), uri );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an axiom in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param cls The class of axiom (e&#046;g&#046; <code>owl:AllDifferent</code>).
     * @param uri The uri for the axiom, or null for an anonymous axiom.
     * @return An Axiom resource.
     */
    public Axiom createAxiom( Resource cls, String uri ) {
        return (Axiom) createOntResource( Axiom.class, cls, uri );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an anonymous class description in this model. A new
     * anonymous resource of <code>rdf:type C</code>, where C is the class type from the
     * language profile.
     * </p>
     * 
     * @return An anonymous Class resource.
     */
    public OntClass createClass() {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        return (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), null );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents a class description node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the class node, or null for an anonymous class.
     * @return A Class resource.
     */
    public OntClass createClass( String uri ) {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        return (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), uri );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an anonymous property restriction in this model. A new
     * anonymous resource of <code>rdf:type R</code>, where R is the restriction type from the
     * language profile.
     * </p>
     * 
     * @return An anonymous Restriction resource.
     */
    public Restriction createRestriction() {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        return (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), null );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the restriction node, or null for an anonymous restriction.
     * @return A Restriction resource.
     */
    public Restriction createRestriction( String uri ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        return (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
    }
    
   
    /**
     * <p>
     * Answer a new, anonymous node representing the fact that a given set of classes are all
     * pair-wise distinct.  <code>AllDifferent</code> is a feature of OWL only, and is something
     * of an anomoly in that it exists only to give a place to anchor the <code>distinctMembers</code>
     * property, which is the actual expression of the fact. 
     * </p>
     * 
     * @return A new AllDifferent resource
     */
    public AllDifferent createAllDifferent() {
        return createAllDifferent( null );
    }
    
    
    /**
     * <p>
     * Answer a new, anonymous node representing the fact that a given set of classes are all
     * pair-wise distinct.  <code>AllDifferent</code> is a feature of OWL only, and is something
     * of an anomoly in that it exists only to give a place to anchor the <code>distinctMembers</code>
     * property, which is the actual expression of the fact. 
     * </p>
     * @param differentMembers A list of the class expressions that denote a set of mutually disjoint classes
     * @return A new AllDifferent resource
     */
    public AllDifferent createAllDifferent( OntList differentMembers ) {
        checkProfileEntry( getProfile().ALL_DIFFERENT(), "ALL_DIFFERENT" );
        AllDifferent ad = (AllDifferent) createOntResource( AllDifferent.class, getProfile().ALL_DIFFERENT(), null );
        
        if (differentMembers != null) {
            ad.p_distinctMembers().add( differentMembers );
        }
        
        return ad;
    }
    
    
    /**
     * <p>Answer a new empty list</p>
     * @return An RDF-encoded list of no elements
     */
    public OntList createList() {
        Resource list = getResource( getProfile().NIL().getURI() );
        list.addProperty( RDF.type, getProfile().LIST() );
        
        return (OntList) list.as( OntList.class );
    }
    
    
    /**
     * <p>Answer a new list containing the resources from the given iterator, in order.</p>
     * @param members An iterator, each value of which is expected to be an RDFNode.
     * @return An RDF-encoded list of the elements of the iterator
     */
    public OntList createList( Iterator members ) {
        OntList list = createList();
        
        while (members != null && members.hasNext()) {
            list = list.add( (RDFNode) members.next() );
        }
        
        return list;
    }
    
    
    /**
     * <p>Answer a new list containing the RDF nodes from the given array, in order</p>
     * @param members An array of RDFNodes that will be the members of the list
     * @return An RDF-encoded list 
     */
    public OntList createList( RDFNode[] members ) {
        return createList( Arrays.asList( members ).iterator() );
    }
    
    
    /**
     * <p>
     * Answer a resource that represents a generic ontology node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * <p>
     * This is a generic method for creating any known ontology value.  The selector that determines
     * which resource to create is the same as as the argument to the {@link RDFNode#as as()} 
     * method: the Java class object of the desired abstraction.  For example, to create an
     * ontology class via this mechanism, use:
     * <code><pre>
     *     OntClass c = (OntClass) myModel.createOntResource( OntClass.class, null,
     *                                                        "http://example.org/ex#Parrot" );
     * </pre></code>
     * </p>
     * 
     * @param javaClass The Java class object that represents the ontology abstraction to create
     * @param rdfType Optional resource denoting the ontology class to which an individual or 
     * axiom belongs, if that is the type of resource being created.
     * @param uri The uri for the ontology resource, or null for an anonymous resource.
     * @return An ontology resource, of the type specified by the <code>javaClass</code>
     */
    public OntResource createOntResource( Class javaClass, Resource rdfType, String uri ) {
        return (OntResource) getResourceWithType( uri, rdfType ).as( javaClass );
    }
    
    
    /**
     * <p>
     * Answer the language profile (for example, OWL or DAML+OIL) that this model is 
     * working to.
     * </p>
     *  
     * @return A language profile
     */
    public Profile getProfile() {
        return m_profile;
    }


    /**
     * <p>
     * Answer true if this model has had the given URI document imported into it. This is
     * important to know since an import only occurs once, and we also want to be able to
     * detect cycles of imports.
     * </p> 
     *
     * @param uri An ontology URI 
     * @return True if the document corresponding to the URI has been successfully loaded
     * into this model
     */
    public boolean hasLoadedImport( String uri ) {
        return m_imported.contains( uri );
    }
    
    
    /**
     * <p>
     * Record that this model has now imported the document with the given
     * URI, so that it will not be re-imported in the future.
     * </p>
     * 
     * @param uri A document URI that has now been imported into the model.
     */
    public void addLoadedImport( String uri ) {
        m_imported.add( uri );
    }
    
    
    /**
     * <p>
     * Answer a list of the imported URI's in this ontology model. Detection of <code>imports</code>
     * statments will be according to the local language profile
     * </p>
     * 
     * @return The imported ontology URI's as a list. Note that since the underlying graph is
     * not ordered, the order of values in the list in successive calls to this method is
     * not guaranteed to be preserved.
     */
    public List listImportedOntologyURIs() {
        List imports = new ArrayList();
        
        // list the ontology nodes
        for (StmtIterator i = listStatements( null, RDF.type, m_profile.ONTOLOGY() );  i.hasNext(); ) {
            Resource ontology = i.nextStatement().getSubject();
            
            for (StmtIterator j = ontology.listProperties( m_profile.IMPORTS() ); j.hasNext();  ) {
                // add the imported URI to the list
                imports.add( j.nextStatement().getResource().getURI() );
            }
        }
        
        return imports;
    }
    
    
    /**
     * <p>
     * Answer the graph factory associated with this model (used for constructing the
     * constituent graphs of the imports closure).
     * </p>
     * 
     * @return The local graph factory
     */
    public GraphMaker getGraphFactory() {
        return m_graphFactory;
    }
    
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param uri URI to read from, may be mapped to a local source by the document manager
     */
    public Model read( String uri ) {
        super.read( m_docMgr.getCacheURL( uri ) );
        
        // cache this model against the public uri (if caching enabled)
        m_docMgr.addGraph( uri, getGraph() );
        
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
        ((OntologyGraph) graph).bind();
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input reader
     * @param base The base URI
     */
    public Model read(Reader reader, String base) {
        super.read( reader, base );
        
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
        ((OntologyGraph) graph).bind();
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input stream
     * @param base The base URI
     */
    public Model read(InputStream reader, String base) {
        super.read( reader, base );
        m_docMgr.loadImports( this, new OntReadState( null, this ) );
        ((OntologyGraph) graph).bind();
        return this;
    } 
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param uri URI to read from, may be mapped to a local source by the document manager
     * @param lang The source syntax
     */
    public Model read(String uri, String syntax) {
        super.read( m_docMgr.getCacheURL( uri ), syntax );
                
        // cache this model against the public uri (if caching enabled)
        m_docMgr.addGraph( uri, getGraph() );

        m_docMgr.loadImports( this, new OntReadState( syntax, this ) );
        ((OntologyGraph) graph).bind();
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input reader
     * @param base The base URI
     * @param lang The source syntax
     */
    public Model read(Reader reader, String base, String syntax) {
        super.read( reader, base, syntax );
        
        m_docMgr.loadImports( this, new OntReadState( syntax, this ) );
        ((OntologyGraph) graph).bind();
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input stream
     * @param base The base URI
     * @param lang The source syntax
     */
    public Model read(InputStream reader, String base, String syntax) {
        super.read( reader, base, syntax );
        
        m_docMgr.loadImports( this, new OntReadState( syntax, this ) );
        ((OntologyGraph) graph).bind();
        return this;
    }
    
    
    /**
     * <p>
     * Read operation that is invoked while loading the imports closure of an
     * ontology model.  Not normally invoked by user code.
     * </p>
     *
     * @param uri The URI to load from 
     * @param readState The read-state for this operation
     */
    public void read( String uri, OntReadState readState ) {
        super.read( m_docMgr.getCacheURL( uri ), readState.getSyntax() );
        
        // cache this model against the public uri (if caching enabled)
        m_docMgr.addGraph( uri, getGraph() );
        m_docMgr.loadImports( this, readState );
    }
    
      
    /**
     * <p>
     * Answer the sub-graphs of this model. A sub-graph is defined as a graph that
     * is used to contain the triples from an imported document.
     * </p>
     * 
     * @return A list of sub graphs for this ontology model
     */
    public List getSubGraphs() {
        return ((MultiUnion) getGraph()).getSubGraphs();
    }
    
    
    /**
     * <p>
     * Answer the base-graph of this model. The base-graph is the graph that
     * contains the triples read from the source document for this ontology.
     * </p>
     * 
     * @return The base-graph for this ontology model
     */
    public Graph getBaseGraph() {
        return ((OntologyGraph) getGraph()).getUnion().getBaseGraph();
    }
    
    
    /**
     * <p>
     * Answer the base model of this model. The base model is the model wrapping
     * the graph that contains the triples read from the source document for this 
     * ontology.  It is therefore the model that will be updated if statements are
     * added to a model that is built from a union of documents (via the 
     * <code>imports</code> statements in the source document).
     * </p>
     * 
     * @return The base model for this ontology model
     */
    public Model getBaseModel() {
        return ModelFactory.createModelForGraph( getBaseGraph() );
    }
    
    
    /**
     * <p>
     * Add the given graph as one of the sub-graphs of this ontology union graph
     * </p>
     *
     * @param graph A sub-graph to add 
     */
    public void addSubGraph( Graph graph ) {
        getUnionGraph().addGraph( graph );
    }
    
    
    /**
     * <p>
     * Answer true if this model is currently in <i>strict checking mode</i>. Strict
     * mode means
     * that converting a common resource to a particular language element, such as
     * an ontology class, will be subject to some simple syntactic-level checks for
     * appropriateness. 
     * </p>
     * 
     * @return True if in strict checking mode
     */
    public boolean strictMode() {
        return m_strictMode;
    }
    
    
    /**
     * <p>
     * Set the checking mode to strict or non-strict.
     * </p>
     * 
     * @param strict
     * @see #strictMode()
     */
    public void setStrictMode( boolean strict ) {
        m_strictMode = strict;
    }


    // Internal implementation methods
    //////////////////////////////////

// jjc - want access for Syntax checker ....
    public MultiUnion getUnionGraph() {
        return ((OntologyGraph) graph).getUnion();
    }
    
    
    /**
     * <p>
     * Answer an iterator over all of the resources that have 
     * <code>rdf:type</code> type.  No alias processing.
     * </p>
     * 
     * @param type The resource that is the value of <code>rdf:type</code> we
     * want to match
     * @return An iterator over all triples <code>_x rdf:type type</code>
     */
    protected ExtendedIterator findByType( Resource type ) {
        return getGraph().find( null, RDF.type.asNode(), type.asNode() );
    }
    

    /**
     * <p>
     * Answer an iterator over all of the resources that have 
     * <code>rdf:type type</code>, or optionally, one of the alternative types.
     * </p>
     * 
     * @param type The resource that is the value of <code>rdf:type</code> we
     * want to match
     * @param types An iterator over alternative types to search for, or null
     * @return An iterator over all triples <code>_x rdf:type t</code> where t
     * is <code>type</code> or one of the values from <code>types</code>.
     */
    protected ExtendedIterator findByType( Resource type, Iterator types ) {
        ExtendedIterator i = findByType( type );
        
        // compose onto i the find iterators for the aliases
        if (types != null) {
            while (types.hasNext()) {
                i = i.andThen( findByType( (Resource) types.next() ) );
            }
        }
        
        return i;
    }
    

    /**
     * <p>
     * Answer an iterator over all of the resources that have 
     * <code>rdf:type type</code>, or optionally, one of its aliases.
     * </p>
     * 
     * @param type The resource that is the value of <code>rdf:type</code> we
     * want to match
     * @param aliased If true, check for aliases for <code>type</code> in the profile.
     * @return An iterator over all triples <code>_x rdf:type type</code>
     */
    protected ExtendedIterator findByType( Resource type, boolean aliases ) {
        return findByType( type, aliases ? getProfile().listAliasesFor( type ) : null );
    }
    

    /**
     * <p>
     * Answer an iterator over all of the resources that have 
     * <code>rdf:type type</code>, or optionally, one of the alternative types, 
     * and present the results <code>as()</code> the given class.
     * </p>
     * 
     * @param type The resource that is the value of <code>rdf:type</code> we
     * want to match
     * @param types An iterator over alternative types to search for, or null
     * @param asKey The value to use to present the polymorphic results
     * @return An iterator over all triples <code>_x rdf:type type</code>
     */
    protected ExtendedIterator findByTypeAs( Resource type, Iterator types, Class asKey ) {
        return findByType( type, types ).mapWith( new SubjectNodeAs( asKey ) );
    }
    
    
    /**
     * <p>
     * Answer an iterator over all of the resources that has an 
     * <code>rdf:type</code> from the types iterator, 
     * and present the results <code>as()</code> the given class.
     * </p>
     * 
     * @param types An iterator over types to search for.  An exception will
     * be raised if this iterator does not have at least one next() element.
     * @param asKey The value to use to present the polymorphic results
     * @return An iterator over all triples <code>_x rdf:type type</code>
     */
    protected ExtendedIterator findByTypeAs( Iterator types, Class asKey ) {
        return findByTypeAs( (Resource) types.next(), types, asKey );
    }
    
    
    /**
     * <p>
     * Answer an iterator over resources with the given rdf:type; for each value
     * in the iterator, ensure that is is presented <code>as()</code> the
     * polymorphic object denoted by the given class key.  Will process aliases.
     * </p>
     * 
     * @param type The rdf:type to search for
     * @param asKey The key to pass to as() on the subject nodes
     * @return An iterator over subjects with the given type, presenting as
     * the given polymorphic class.
     */
    protected ExtendedIterator findByTypeAs( Resource type, Class asKey ) {
        return findByType( type, true ).mapWith( new SubjectNodeAs( asKey ) );
    }
    
    
    /**
     * <p>
     * Answer the iterator over the resources from the graph that satisfy the given
     * query, followed by the answers to the alternative queries (if specified). A
     * typical scenario is that the main query gets resources of a given class (say,
     * <code>rdfs:Class</code>), while the altQueries query for aliases for that
     * type (such as <code>daml:Class</code>).
     * </p>
     * 
     * @param query A query to run against the model
     * @param altQueries An optional list of subsidiary queries to chain on to the first 
     * @return ExtendedIterator An iterator over the (assumed single) results of 
     * executing the queries.
     */
    protected ExtendedIterator queryFor( BindingQueryPlan query, List altQueries, Class asKey ) {
        GetBinding firstBinding  = new GetBinding( 0 );
        
        // get the results from the main query
        ExtendedIterator mainQuery = query.executeBindings().mapWith( firstBinding );
        
        // now add the alias queries, if defined
        if (altQueries != null) {
            for (Iterator i = altQueries.iterator();  i.hasNext();  ) {
                ExtendedIterator aliasQuery = ((BindingQueryPlan) i.next()).executeBindings().mapWith( firstBinding );
                mainQuery = mainQuery.andThen( aliasQuery );
            }
        }
        
        // map each answer value to the appropriate ehnanced node
        return mainQuery.mapWith( new SubjectNodeAs( asKey ) );
    }
    
    
    /**
     * <p>
     * Answer a binding query that will search for 'an X that has an 
     * rdf:type whose rdf:type is C' for some given resource C.
     * </p>
     * 
     * @param type The type of the type of the resources we're searching for
     * @return BindingQueryPlan A binding query for the X resources.
     */
    protected BindingQueryPlan queryXTypeOfType( Resource type ) {
        Query q = new Query();
        q.addMatch( Query.X, RDF.type.asNode(), Query.Y );
        q.addMatch( Query.Y, RDF.type.asNode(), type.asNode() );
        
        return queryHandler().prepareBindings( q, new Node[] {Query.X} );
    }
    
    
    /**
     * <p>
     * Answer an iterator over nodes that have p as a subject
     * </p>
     * 
     * @param p A property
     * @return ExtendedIterator over subjects of p.
     */
    protected ExtendedIterator findByDefiningProperty( Property p ) {
        return getGraph().find( null, p.getNode(), null );
    }
    
    
    /**
     * <p>
     * Answer an iterator over nodes that have p as a subject, presented as 
     * polymorphic enh resources of the given facet.
     * </p>
     * 
     * @param p A property
     * @param asKey A facet type
     * @return ExtendedIterator over subjects of p, presented as the facet.
     */
    protected ExtendedIterator findByDefiningPropertyAs( Property p, Class asKey ) {
        return findByDefiningProperty( p ).mapWith( new SubjectNodeAs( asKey ) );
    }
    
    
    /**
     * <p>
     * Answer the resource with the given uri and that has the given rdf:type -
     * creating the resource if necessary.
     * </p>
     * 
     * @param uri The uri to use, or null
     * @param rdfType The resource to assert as the rdf:type
     * @return A new or existing Resource
     */
    protected Resource getResourceWithType( String uri, Resource rdfType ) {
        return getResource( uri ).addProperty( RDF.type, rdfType );
    }
    
    
    /**
     * <p>Throw an OntologyException if the term is not in language profile</p>
     * 
     * @param profileTerm The entry from the profile
     * @param desc A label for the profile term
     * @exception OntologyException if profileTerm is null.
     */
    protected void checkProfileEntry( Object profileTerm, String desc ) {
        if (profileTerm == null) {
            // not in the profile
            throw new OntologyException( "Ontology term " + desc + " is not defined in the language profile for " + getProfile().getLabel() );
        }
    }
    
    
    /**
     * <p>Check that every member of the given list has the given rdf:type, and throw an exception if not.</p>
     * @param list The list to be checked
     * @param rdfType The rdf:type value to check for
     * @exception LanguageConsistencyException if any member of the list does not have <code>rdf:type <i>rdfType</i></code>
     */
    protected void checkListMembersRdfType( OntList list, Resource rdfType ) {
        if (strictMode() && ! ((Boolean) list.reduce( new RdfTypeTestFn( rdfType), Boolean.TRUE )).booleanValue()) {
            // not all of the members of the list are of the given type
            throw new LanguageConsistencyException( "The members of the given list are expected to be of rdf:type " + rdfType.toString() );
        }
    }

    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Map triple subjects or single nodes to subject enh nodes, presented as() the given class */
    protected class SubjectNodeAs implements Map1 
    {
        protected Class m_asKey;
        
        protected SubjectNodeAs( Class asKey ) { m_asKey = asKey; }
        
        public Object map1( Object x ) {
            Node n = (x instanceof Triple) 
                         ? ((Triple) x).getSubject() 
                         : ((x instanceof EnhNode) ? ((EnhNode) x).asNode() :  (Node) x);
            return getNodeAs( n, m_asKey );
        }
    }
    
    /** Project out the first element of a list of bindings */
    protected class GetBinding implements Map1
    {
        protected int m_index;
        protected GetBinding( int index ) { m_index = index; }
        public Object map1( Object x )    { return ((List) x).get( m_index );  }
    }
    
    /** Function to test the rdf type of a list */
    protected class RdfTypeTestFn implements OntList.ReduceFn
    {
        protected Resource m_type;
        protected RdfTypeTestFn( Resource type ) { m_type = type; }
        public Object reduce( RDFNode node, Object accumulator ) {
            Boolean acc = (Boolean) accumulator;
            if (acc.booleanValue()) {
                // true so far
                Resource r = (Resource) node;
                return new Boolean( r.hasProperty( RDF.type, m_type ) );
            }
            else {
                return acc;
            }
        }
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
