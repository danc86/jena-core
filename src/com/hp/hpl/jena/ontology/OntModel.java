/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.graph.query.BindingQueryPlan;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.io.*;
import java.util.*;



/**
 * <p>
 * An enhanced view of a Jena model that is known to contain ontology
 * data, under a given ontology {@link Profile vocabulary} (such as OWL).
 * This class does not by itself compute the deductive extension of the graph
 * under the semantic rules of the language.  Instead, we wrap an underlying
 * model with this ontology interface, that presents a convenience syntax for accessing
 * the language elements. Depending on the inference capability of the underlying model,
 * the OntModel will appear to contain more or less triples. For example, if
 * this class is used to wrap a plain memory or database model, only the
 * relationships asserted by the document will be reported through this
 * convenience API. Alternatively, if the OntModel wraps an OWL inferencing model,
 * the inferred triples from the extension will be reported as well. For
 * example, assume the following ontology fragment: 
 * <code><pre>
 *      :A rdf:type owl:Class .
 *      :B rdf:type owl:Class ; rdfs:subClassOf :A .
 *      :widget rdf:type :B .
 * </pre></code>
 * In a non-inferencing model, the <code>rdf:type</code> of the widget will be
 * reported as class <code>:B</code> only.  In a model that can process the OWL
 * semantics, the widget's types will include <code>:B</code>, <code>:A</code>,
 * and <code>owl:Thing</code>.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public interface OntModel
    extends Model
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

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
    public ExtendedIterator listOntologies();
    

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
    public ExtendedIterator listOntProperties();
    

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
    public ExtendedIterator listObjectProperties();
    

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
    public ExtendedIterator listDatatypeProperties();
    

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
     * @return An iterator over individual resources. 
     */
    public ExtendedIterator listIndividuals();
    

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
    public ExtendedIterator listClasses();
    

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
    public ExtendedIterator listEnumeratedClasses();
    

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
    public ExtendedIterator listUnionClasses();
    

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
    public ExtendedIterator listComplementClasses();
    

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
    public ExtendedIterator listIntersectionClasses();
    

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
    public ExtendedIterator listNamedClasses();
    

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
    public ExtendedIterator listRestrictions();
    
    
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
    public ExtendedIterator listAnnotationProperties();
    
   
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
    public ExtendedIterator listAllDifferent();
    
   
    /**
     * <p>
     * Answer a resource that represents an ontology description node in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an Ontology, return the
     * Ontology facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the ontology node. Conventionally, this corresponds to the base URI
     * of the document itself.
     * @return An Ontology resource or null.
     */
    public Ontology getOntology( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an Individual node in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an Individual, return the
     * Individual facet, otherwise return null. 
     * </p>
     * 
     * @param uri The URI for the requried individual 
     * @return An Individual resource or null.
     */
    public Individual getIndividual( String uri );
    
   
    /**
     * <p>
     * Answer a resource representing an generic property in this model. If a property
     * with the given uri exists in the model, return the
     * OntProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the property.
     * @return An OntProperty resource or null.
     */
    public OntProperty getOntProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource representing an object property in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an ObjectProperty, return the
     * ObjectProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the object property. May not be null.
     * @return An ObjectProperty resource or null.
     */
    public ObjectProperty getObjectProperty( String uri );
    
   
    /**
     * <p>Answer a resource representing a transitive property. If a resource
     * with the given uri exists in the model, and can be viewed as a TransitiveProperty, return the
     * TransitiveProperty facet, otherwise return null. </p>
     * @param uri The uri for the property. May not be null.
     * @return A TransitiveProperty resource or null
     */
    public TransitiveProperty getTransitiveProperty( String uri );
    
    
    /**
     * <p>Answer a resource representing a symmetric property. If a resource
     * with the given uri exists in the model, and can be viewed as a SymmetricProperty, return the
     * SymmetricProperty facet, otherwise return null. </p>
     * @param uri The uri for the property. May not be null.
     * @return A SymmetricProperty resource or null
     */
    public SymmetricProperty getSymmetricProperty( String uri );   
    
    
    /**
     * <p>Answer a resource representing an inverse functional property. If a resource
     * with the given uri exists in the model, and can be viewed as a InverseFunctionalProperty, return the
     * InverseFunctionalProperty facet, otherwise return null. </p>
     * @param uri The uri for the property. May not be null.
     * @return An InverseFunctionalProperty resource or null
     */
    public InverseFunctionalProperty getInverseFunctionalProperty( String uri );
    
    
    /**
     * <p>
     * Answer a resource that represents datatype property in this model. . If a resource
     * with the given uri exists in the model, and can be viewed as a DatatypeProperty, return the
     * DatatypeProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @return A DatatypeProperty resource or null
     */
    public DatatypeProperty getDatatypeProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an annotation property in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an AnnotationProperty, return the
     * AnnotationProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the annotation property. May not be null.
     * @return An AnnotationProperty resource or null
     */
    public AnnotationProperty getAnnotationProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents a class description node in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an OntClass, return the
     * OntClass facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the class node, or null for an anonymous class.
     * @return An OntClass resource or null.
     */
    public OntClass getOntClass( String uri );
    
   
    /**
     * <p>Answer a resource representing the class that is the complement of another class. If a resource
     * with the given uri exists in the model, and can be viewed as a ComplementClass, return the
     * ComplementClass facet, otherwise return null. </p>
     * @param uri The URI of the new complement class.
     * @return A complement class or null
     */
    public ComplementClass getComplementClass( String uri );
    
   
    /**
     * <p>Answer a resource representing the class that is the enumeration of a list of individuals. If a resource
     * with the given uri exists in the model, and can be viewed as an EnumeratedClass, return the
     * EnumeratedClass facet, otherwise return null. </p>
     * @param uri The URI of the new enumeration class.
     * @return An enumeration class or null
     */
    public EnumeratedClass getEnumeratedClass( String uri );
    
   
    /**
     * <p>Answer a resource representing the class that is the union of a list of class desctiptions. If a resource
     * with the given uri exists in the model, and can be viewed as a UnionClass, return the
     * UnionClass facet, otherwise return null. </p>
     * @param uri The URI of the new union class.
     * @return A union class description or null
     */
    public UnionClass getUnionClass( String uri );
    
   
    /**
     * <p>Answer a resource representing the class that is the intersection of a list of class descriptions. If a resource
     * with the given uri exists in the model, and can be viewed as a IntersectionClass, return the
     * IntersectionClass facet, otherwise return null. </p>
     * @param uri The URI of the new intersection class.
     * @return An intersection class description or null
     */
    public IntersectionClass getIntersectionClass( String uri );


    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given uri exists in the model, and can be viewed as a Restriction, return the
     * Restriction facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the restriction node.
     * @return A Restriction resource or null
     */
    public Restriction getRestriction( String uri );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have the given
     * resource as the value of the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a HasValueRestriction, return the
     * HasValueRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a has-value restriction or null
     */
    public HasValueRestriction getHasValueRestriction( String uri );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * one property with a value belonging to the given class. If a resource
     * with the given uri exists in the model, and can be viewed as a SomeValuesFromRestriction, return the
     * SomeValuesFromRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a some-values-from restriction, or null
     */
    public SomeValuesFromRestriction getSomeValuesFromRestriction( String uri );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals for which all values
     * of the given property belong to the given class. If a resource
     * with the given uri exists in the model, and can be viewed as an AllValuesFromResriction, return the
     * AllValuesFromRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing an all-values-from restriction or null
     */
    public AllValuesFromRestriction getAllValuesFromRestriction( String uri );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have exactly
     * the given number of values for the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a CardinalityRestriction, return the
     * CardinalityRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a has-value restriction, or null
     */
    public CardinalityRestriction getCardinalityRestriction( String uri );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * the given number of values for the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a MinCardinalityRestriction, return the
     * MinCardinalityRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a min-cardinality restriction, or null
     */
    public MinCardinalityRestriction getMinCardinalityRestriction( String uri );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at most
     * the given number of values for the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a MaxCardinalityRestriction, return the
     * MaxCardinalityRestriction facet, otherwise return null.</p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a mas-cardinality restriction, or null
     */
    public MaxCardinalityRestriction getMaxCardinalityRestriction( String uri );


    /**
     * <p>
     * Answer a resource that represents an ontology description node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the ontology node. Conventionally, this corresponds to the base URI
     * of the document itself.
     * @return An Ontology resource.
     */
    public Ontology createOntology( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an Indvidual node in this model. A new anonymous resource
     * will be created in the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param cls Resource representing the ontology class to which the individual belongs
     * @return A new anoymous Individual of the given class.
     */
    public Individual createIndividual( Resource cls );
    
   
    /**
     * <p>
     * Answer a resource that represents an Individual node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param cls Resource representing the ontology class to which the individual belongs
     * @param uri The uri for the individual, or null for an anonymous individual.
     * @return An Individual resource.
     */
    public Individual createIndividual( String uri, Resource cls );
    
   
    /**
     * <p>
     * Answer a resource representing an generic property in this model.  Effectively
     * this method is an alias for {@link #createProperty( String )}, except that
     * the return type is {@link OntProperty}, which allow more convenient access to
     * a property's position in the property hierarchy, domain, range, etc.
     * </p>
     * 
     * @param uri The uri for the property. May not be null.
     * @return An OntProperty resource.
     */
    public OntProperty createOntProperty( String uri );
    
   
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
    public ObjectProperty createObjectProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an object property in this model.  An object property
     * is defined to have a range of individuals, rather than datatypes. 
     * If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the object property. May not be null.
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return An ObjectProperty resource, optionally also functional.
     */
    public ObjectProperty createObjectProperty( String uri, boolean functional );
    
    
    /**
     * <p>Answer a resource representing a transitive property</p>
     * @param uri The uri for the property. May not be null.
     * @return An TransitiveProperty resource
     * @see #createTransitiveProperty( String, boolean )
     */
    public TransitiveProperty createTransitiveProperty( String uri );
    
    
    /**
     * <p>Answer a resource representing a transitive property, which is optionally
     * also functional. <strong>Note:</strong> although it is permitted in OWL full
     * to have functional transitive properties, it makes the language undecideable.
     * Functional transitive properties are not permitted in OWL Lite or OWL DL.</p>
     * @param uri The uri for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An TransitiveProperty resource, optionally also functional.
     */
    public TransitiveProperty createTransitiveProperty( String uri, boolean functional );

    
    /**
     * <p>Answer a resource representing a symmetric property</p>
     * @param uri The uri for the property. May not be null.
     * @return An SymmetricProperty resource
     * @see #createSymmetricProperty( String, boolean )
     */
    public SymmetricProperty createSymmetricProperty( String uri );   
    
    
    /**
     * <p>Answer a resource representing a symmetric property, which is optionally
     * also functional.</p>
     * @param uri The uri for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An SymmetricProperty resource, optionally also functional.
     */
    public SymmetricProperty createSymmetricProperty( String uri, boolean functional );   

    
    /**
     * <p>Answer a resource representing an inverse functional property</p>
     * @param uri The uri for the property. May not be null.
     * @return An InverseFunctionalProperty resource
     * @see #createInverseFunctionalProperty( String, boolean )
     */
    public InverseFunctionalProperty createInverseFunctionalProperty( String uri );
    
    
    /**
     * <p>Answer a resource representing an inverse functional property, which is optionally
     * also functional.</p>
     * @param uri The uri for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An InverseFunctionalProperty resource, optionally also functional.
     */
    public InverseFunctionalProperty createInverseFunctionalProperty( String uri, boolean functional );
    
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
    public DatatypeProperty createDatatypeProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents datatype property in this model. A datatype property
     * is defined to have a range that is a concrete datatype, rather than an individual. 
     * If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return A DatatypeProperty resource.
     */
    public DatatypeProperty createDatatypeProperty( String uri, boolean functional );
    
   
    /**
     * <p>
     * Answer a resource that represents an annotation property in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the annotation property. May not be null.
     * @return An AnnotationProperty resource.
     */
    public AnnotationProperty createAnnotationProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an anonymous class description in this model. A new
     * anonymous resource of <code>rdf:type C</code>, where C is the class type from the
     * language profile.
     * </p>
     * 
     * @return An anonymous Class resource.
     */
    public OntClass createClass();
    
   
    /**
     * <p>
     * Answer a resource that represents a class description node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the class node, or null for an anonymous class.
     * @return A Class resource.
     */
    public OntClass createClass( String uri );
    
   
    /**
     * <p>Answer a resource representing the class that is the complement of the given argument class</p>
     * @param uri The URI of the new complement class, or null for an anonymous class description.
     * @param cls Resource denoting the class that the new class is a complement of
     * @return A complement class
     */
    public ComplementClass createComplementClass( String uri, Resource cls );
    
   
    /**
     * <p>Answer a resource representing the class that is the enumeration of the given list of individuals</p>
     * @param uri The URI of the new enumeration class, or null for an anonymous class description.
     * @param members An optional list of resources denoting the individuals in the enumeration, or null.
     * @return An enumeration class
     */
    public EnumeratedClass createEnumeratedClass( String uri, RDFList members );
    
   
    /**
     * <p>Answer a resource representing the class that is the union of the given list of class desctiptions</p>
     * @param uri The URI of the new union class, or null for an anonymous class description.
     * @param members A list of resources denoting the classes that comprise the union
     * @return A union class description
     */
    public UnionClass createUnionClass( String uri, RDFList members );
    
   
    /**
     * <p>Answer a resource representing the class that is the intersection of the given list of class descriptions.</p>
     * @param uri The URI of the new intersection class, or null for an anonymous class description.
     * @param members A list of resources denoting the classes that comprise the intersection
     * @return An intersection class description
     */
    public IntersectionClass createIntersectionClass( String uri, RDFList members );


    /**
     * <p>
     * Answer a resource that represents an anonymous property restriction in this model. A new
     * anonymous resource of <code>rdf:type R</code>, where R is the restriction type from the
     * language profile.
     * </p>
     * @param p The property that is restricted by this restriction, or null to omit from the restriction
     * @return An anonymous Restriction resource.
     */
    public Restriction createRestriction( Property p );
    
   
    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the restriction node, or null for an anonymous restriction.
     * @param p The property that is restricted by this restriction, or null to omit from the restriction
     * @return A Restriction resource.
     */
    public Restriction createRestriction( String uri, Property p );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have the given
     * resource as the value of the given property</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param value The value of the property, as a resource or RDF literal
     * @return A new resource representing a has-value restriction
     */
    public HasValueRestriction createHasValueRestriction( String uri, Property prop, RDFNode value );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * one property with a value belonging to the given class</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cls The class to which at least one value of the property belongs
     * @return A new resource representing a some-values-from restriction
     */
    public SomeValuesFromRestriction createSomeValuesFromRestriction( String uri, Property prop, Resource cls );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals for which all values
     * of the given property belong to the given class</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cls The class to which any value of the property belongs
     * @return A new resource representing an all-values-from restriction
     */
    public AllValuesFromRestriction createAllValuesFromRestriction( String uri, Property prop, Resource cls );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have exactly
     * the given number of values for the given property.</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The exact cardinality of the property
     * @return A new resource representing a has-value restriction
     */
    public CardinalityRestriction createCardinalityRestriction( String uri, Property prop, int cardinality );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * the given number of values for the given property.</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The minimum cardinality of the property
     * @return A new resource representing a min-cardinality restriction
     */
    public MinCardinalityRestriction createMinCardinalityRestriction( String uri, Property prop, int cardinality );
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at most
     * the given number of values for the given property.</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The maximum cardinality of the property
     * @return A new resource representing a mas-cardinality restriction
     */
    public MaxCardinalityRestriction createMaxCardinalityRestriction( String uri, Property prop, int cardinality );


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
    public AllDifferent createAllDifferent();
    
    
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
    public AllDifferent createAllDifferent( RDFList differentMembers );
    
    
    /**
     * <p>
     * Answer a resource that represents a generic ontology node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-model of the ontology model. 
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
    public OntResource createOntResource( Class javaClass, Resource rdfType, String uri );

    
    /**
     * <p>
     * Answer a list of the imported URI's in this ontology model. Detection of <code>imports</code>
     * statments will be according to the local language profile.  Note that, in order to allow this
     * method to be called during the imports closure process, we <b>only query the base model</b>,
     * thus side-stepping the any attached reasoner.
     * </p>
     * 
     * @return The imported ontology URI's as a list. Note that since the underlying graph is
     * not ordered, the order of values in the list in successive calls to this method is
     * not guaranteed to be preserved.
     */
    public List listImportedOntologyURIs();
    
    
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
    public boolean hasLoadedImport( String uri );
    
    
    /**
     * <p>
     * Record that this model has now imported the document with the given
     * URI, so that it will not be re-imported in the future.
     * </p>
     * 
     * @param uri A document URI that has now been imported into the model.
     */
    public void addLoadedImport( String uri );
    
    
    /**
     * <p>
     * Answer the language profile (for example, OWL or DAML+OIL) that this model is 
     * working to.
     * </p>
     *  
     * @return A language profile
     */
    public Profile getProfile();
    
    
    /**
     * <p>
     * Answer the model maker associated with this model (used for constructing the
     * constituent models of the imports closure).
     * </p>
     * 
     * @return The local model maker
     */
    public ModelMaker getModelMaker();
    
    
    /**
     * <p>
     * Answer the sub-graphs of this model. A sub-graph is defined as a graph that
     * is used to contain the triples from an imported document.
     * </p>
     * 
     * @return A list of graphs that are contained in this ontology model
     */
    public List getSubGraphs();
    
    
    /**
     * <p>
     * Answer the base model of this model. The base model is the model 
     * that contains the triples read from the source document for this 
     * ontology.  It is therefore this base model that will be updated if statements are
     * added to a model that is built from a union of documents (via the 
     * <code>imports</code> statements in the source document).
     * </p>
     * 
     * @return The base model for this ontology model
     */
    public Model getBaseModel();
    
    
    /**
     * <p>
     * Add the given model as one of the sub-models of the enclosed ontology union model.    Will 
     * cause the associated infererence engine (if any) to update, so this may be
     * an expensive operation in some cases. 
     * </p>
     *
     * @param model A sub-model to add 
     * @see #addSubModel( Model, boolean )
     */
    public void addSubModel( Model model );
    
    
    /**
     * <p>
     * Add the given model as one of the sub-models of the enclosed ontology union model.
     * </p>
     *
     * @param model A sub-model to add
     * @param rebind If true, rebind any associated inferencing engine to the new data (which
     * may be an expensive operation) 
     */
    public void addSubModel( Model model, boolean rebind );
    
    
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
    public boolean strictMode();
    
    
    /**
     * <p>
     * Set the checking mode to strict or non-strict.
     * </p>
     * 
     * @param strict
     * @see #strictMode()
     */
    public void setStrictMode( boolean strict );
    
    
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
    public OntDocumentManager getDocumentManager();
    
    
    /**
     * <p>Answer the ontology model specification that was used to construct this model</p>
     * @return An ont model spec instance.
     */
    public OntModelSpec getSpecification();
    
    
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
    public ExtendedIterator queryFor( BindingQueryPlan query, List altQueries, Class asKey );

    // output operations
    
    /** 
     * <p>Write the model as an XML document.
     * It is often better to use an OutputStream rather than a Writer, since this
     * will avoid character encoding errors.  
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write 
     * all triples, including imported data and inferred triples, use 
     * {@link #writeAll( Writer, String, String ) writeAll }.
     * </p>
     * 
     * @param writer A writer to which the XML will be written
     * @return this model
     */
    public Model write( Writer writer ) ;
    
    /** 
     * <p>Write a serialized represention of a model in a specified language.
     * It is often better to use an OutputStream rather than a Writer, since this
     * will avoid character encoding errors.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write 
     * all triples, including imported data and inferred triples, use 
     * {@link #writeAll( Writer, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code> is "RDF/XML".</p>
     * @param writer The output writer
     * @param lang The output language
     * @return this model
     */
    public Model write( Writer writer, String lang ) ;

    /** 
     * <p>Write a serialized represention of a model in a specified language.
     * It is often better to use an OutputStream rather than a Writer,
     * since this will avoid character encoding errors.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write 
     * all triples, including imported data and inferred triples, use 
     * {@link #writeAll( Writer, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param writer The output writer
     * @param base The base uri for relative URI calculations.
     * <code>null</code> means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return this model
     */
    public Model write( Writer writer, String lang, String base );

    /** 
     * <p>Write a serialization of this model as an XML document.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write 
     * all triples, including imported data and inferred triples, use 
     * {@link #writeAll( OutputStream, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value is
     * represented by <code>null</code> is "RDF/XML".</p>
     * @param out The output stream to which the XML will be written
     * @return This model
     */
    public Model write( OutputStream out );

    /** 
     * <p>Write a serialized represention of this model in a specified language.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write 
     * all triples, including imported data and inferred triples, use 
     * {@link #writeAll( OutputStream, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param lang The output langauge
     * @return This model
     */
    public Model write( OutputStream out, String lang );

    /** 
     * <p>Write a serialized represention of a model in a specified language.
     * <strong>Note:</strong> This method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).  To write 
     * all triples, including imported data and inferred triples, use 
     * {@link #writeAll( OutputStream, String, String ) writeAll }.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param base The base uri to use when writing relative URI's. <code>null</code>
     * means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return This model
     */
    public Model write( OutputStream out, String lang, String base );

    /** 
     * <p>Write a serialized represention of all of the contents of the model,
     * including inferred statements and statements imported from other
     * documents.  To write only the data asserted in the base model, use
     * {@link #write( Writer, String, String ) write}. 
     * It is often better to use an OutputStream rather than a Writer,
     * since this will avoid character encoding errors.
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param writer The output writer
     * @param base The base uri for relative URI calculations.
     * <code>null</code> means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return This model
     */
    public Model writeAll( Writer writer, String lang, String base );
    
    /** 
     * <p>Write a serialized represention of all of the contents of the model,
     * including inferred statements and statements imported from other
     * documents.  To write only the data asserted in the base model, use
     * {@link #write( OutputStream, String, String ) write}. 
     * </p>
     * <p>The language in which to write the model is specified by the
     * <code>lang</code> argument.  Predefined values are "RDF/XML",
     * "RDF/XML-ABBREV", "N-TRIPLE" and "N3".  The default value,
     * represented by <code>null</code>, is "RDF/XML".</p>
     * @param out The output stream to which the RDF is written
     * @param base The base uri to use when writing relative URI's. <code>null</code>
     * means use only absolute URI's.
     * @param lang The language in which the RDF should be written
     * @return This model
     */
    public Model writeAll( OutputStream out, String lang, String base );

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

