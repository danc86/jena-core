/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            25-Mar-2003
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
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;


/**
 * <p>
 * Abstract base class to provide shared implementation for implementations of ontology
 * resources.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id$
 */
public class OntResourceImpl
    extends ResourceImpl
    implements OntResource 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////
    
    /**
     * A factory for generating OntResource facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new OntResourceImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to OntResource");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an OntResource facet if it is a uri or bnode
            return node.isURI() || node.isBlank();
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an ontology resource represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public OntResourceImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the ontology language profile that governs the ontology model to which
     * this ontology resource is attached.  
     * </p>
     * 
     * @return The language profile for this ontology resource
     */
    public Profile getProfile() {
        return ((OntModel) getModel()).getProfile();
    }


    // sameAs
    
    /**
     * <p>Assert equivalence between the given resource and this resource. Any existing 
     * statements for <code>sameAs</code> will be removed.</p>
     * @param res The resource that is declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public void setSameAs( Resource res ) {
        setPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public void addSameAs( Resource res ) {
        addPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    /**
     * <p>Answer a resource that is declared to be the same as this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public OntResource getSameAs() {
        return objectAsResource( getProfile().SAME_AS(), "SAME_AS" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to be the same as
     * this resource. Each elemeent of the iterator will be an {@link #OntResource}.</p>
     * @return An iterator over the resources equivalent to this resource.
     * @exception OntProfileException If the {@link Profile#SAME_AS()} property is not supported in the current language profile.   
     */ 
    public Iterator listSameAs() {
        return listAs( getProfile().SAME_AS(), "SAME_AS", OntResource.class );
    }

    /**
     * <p>Answer true if this resource is the same as the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared the same via a <code>sameAs</code> statement.
     */
    public boolean isSameAs( Resource res ) {
        return hasPropertyValue( getProfile().SAME_AS(), "SAME_AS", res );
    }

    // differentFrom
    
    /**
     * <p>Assert that the given resource and this resource are distinct. Any existing 
     * statements for <code>differentFrom</code> will be removed.</p>
     * @param res The resource that is declared to be distinct from this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public void setDifferentFrom( Resource res ) {
        setPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    /**
     * <p>Add a resource that is declared to be equivalent to this resource.</p>
     * @param res A resource that declared to be the same as this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public void addDifferentFrom( Resource res ) {
        addPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }

    /**
     * <p>Answer a resource that is declared to be distinct from this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that declared to be different from this resource
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public OntResource getDifferentFrom() {
        return objectAsResource( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to be different from
     * this resource. Each elemeent of the iterator will be an {@link #OntResource}.</p>
     * @return An iterator over the resources different from this resource.
     * @exception OntProfileException If the {@link Profile#DIFFERENT_FROM()} property is not supported in the current language profile.   
     */ 
    public Iterator listDifferentFrom() {
        return listAs( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", OntResource.class );
    }

    /**
     * <p>Answer true if this resource is different from the given resource.</p>
     * @param res A resource to test against
     * @return True if the resources are declared to be distinct via a <code>differentFrom</code> statement.
     */
    public boolean isDifferentFrom( Resource res ) {
        return hasPropertyValue( getProfile().DIFFERENT_FROM(), "DIFFERENT_FROM", res );
    }
    
    // version info

    /**
     * <p>Assert that the given string is the value of the version info for this resource. Any existing 
     * statements for <code>versionInfo</code> will be removed.</p>
     * @param info The version information for this resource
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public void setVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        removeAll( getProfile().VERSION_INFO() );
        addVersionInfo( info );
    }

    /**
     * <p>Add the given version information to this resource.</p>
     * @param info A version information string for this resource 
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public void addVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        addProperty( getProfile().VERSION_INFO(), getModel().createTypedLiteral( info, null, XSDDatatype.XSDstring ) );
    }

    /**
     * <p>Answer the version information string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return A version info string
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public String getVersionInfo() {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        return getProperty( getProfile().VERSION_INFO() ).getString();
    }

    /**
     * <p>Answer an iterator over all of the version info strings for this resource.</p>
     * @return An iterator over the version info strings for this resource.
     * @exception OntProfileException If the {@link Profile#VERSION_INFO()} property is not supported in the current language profile.   
     */ 
    public Iterator listVersionInfo() {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        return WrappedIterator.create( listProperties( getProfile().VERSION_INFO() ) )
               .mapWith( new ObjectAsStringMapper() );
    }

    /**
     * <p>Answer true if this resource has the given version information</p>
     * @param info Version information to test for
     * @return True if this resource has <code>info</code> as version information.
     */
    public boolean hasVersionInfo( String info ) {
        checkProfile( getProfile().VERSION_INFO(), "VERSION_INFO" );
        return hasProperty( getProfile().VERSION_INFO(), info );
    }
    
    // label
    
    /**
     * <p>Assert that the given string is the value of the label for this resource. Any existing 
     * statements for <code>label</code> will be removed.</p>
     * @param label The label for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void setLabel( String label, String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        removeAll( getProfile().LABEL() );
        addLabel( label, lang );
    }

    /**
     * <p>Add the given label to this resource.</p>
     * @param label A label string for this resource
     * @param lang The language attribute for this label (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void addLabel( String label, String lang ) {
        addLabel( getModel().createTypedLiteral( label, lang, XSDDatatype.XSDstring ) );
    }

    /**
     * <p>Add the given label to this resource.</p>
     * @param label The literal label
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public void addLabel( Literal label ) {
        addPropertyValue( getProfile().LABEL(), "LABEL", label );
    }

    /**
     * <p>Answer the label string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired label (EN, FR, etc) or null for don't care. Will 
     * attempt to retreive the most specific label matching the given language</p>
     * @return A label string matching the given language, or null if there is no matching label.
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public String getLabel( String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        if (lang == null) {
            // don't care which language version we get
            return getProperty( getProfile().LABEL() ).getString();
        }
        else {
            // search for the best match for the specified language
            return selectLang( listProperties( getProfile().LABEL() ), lang );
        }
    }

    /**
     * <p>Answer an iterator over all of the label literals for this resource.</p>
     * @param lang The language to restrict any label values to, or null to select all languages
     * @return An iterator over RDF {@link Literal}'s.
     * @exception OntProfileException If the {@link Profile#LABEL()} property is not supported in the current language profile.   
     */ 
    public Iterator listLabels( String lang ) {
        checkProfile( getProfile().LABEL(), "LABEL" );
        return WrappedIterator.create( listProperties( getProfile().LABEL() ) )
               .filterKeep( new LangTagFilter( lang ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource has the given label</p>
     * @param label The label to test for
     * @param lang The optional language tag, or null for don't care.
     * @return True if this resource has <code>label</code> as a label.
     */
    public boolean hasLabel( String label, String lang ) {
        return hasLabel( getModel().createTypedLiteral( label, lang, XSDDatatype.XSDstring ) );
    }
    
    /**
     * <p>Answer true if this resource has the given label</p>
     * @param label The label to test for
     * @return True if this resource has <code>label</code> as a label.
     */
    public boolean hasLabel( Literal label ) {
        boolean found = false;
        
        Iterator i = listLabels( label.getLanguage() );
        while (!found && i.hasNext()) {
            found = label.equals( i.next() );
        }
        
        if (i instanceof ClosableIterator) {
            ((ClosableIterator) i).close();
        } 
        return found;
    }
    
    // comment

    /**
     * <p>Assert that the given string is the comment on this resource. Any existing 
     * statements for <code>comment</code> will be removed.</p>
     * @param comment The comment for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void setComment( String comment, String lang ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        removeAll( getProfile().COMMENT() );
        addComment( comment, lang );
    }

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment A comment string for this resource
     * @param lang The language attribute for this comment (EN, FR, etc) or null if not specified. 
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void addComment( String comment, String lang ) {
        addComment( getModel().createTypedLiteral( comment, lang, XSDDatatype.XSDstring ) );
    }

    /**
     * <p>Add the given comment to this resource.</p>
     * @param comment The literal comment
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public void addComment( Literal comment ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        addProperty( getProfile().COMMENT(), comment );
    }

    /**
     * <p>Answer the comment string for this object. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @param lang The language attribute for the desired comment (EN, FR, etc) or null for don't care. Will 
     * attempt to retreive the most specific comment matching the given language</p>
     * @return A comment string matching the given language, or null if there is no matching comment.
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public String getComment( String lang ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        if (lang == null) {
            // don't care which language version we get
            return getProperty( getProfile().COMMENT() ).getString();
        }
        else {
            // search for the best match for the specified language
            return selectLang( listProperties( getProfile().COMMENT() ), lang );
        }
    }

    /**
     * <p>Answer an iterator over all of the comment literals for this resource.</p>
     * @return An iterator over RDF {@link Literal}'s.
     * @exception OntProfileException If the {@link Profile#COMMENT()} property is not supported in the current language profile.   
     */ 
    public Iterator listComments( String lang ) {
        checkProfile( getProfile().COMMENT(), "COMMENT" );
        return WrappedIterator.create( listProperties( getProfile().COMMENT() ) )
               .filterKeep( new LangTagFilter( lang ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource has the given comment.</p>
     * @param comment The comment to test for
     * @param lang The optional language tag, or null for don't care.
     * @return True if this resource has <code>comment</code> as a comment.
     */
    public boolean hasComment( String comment, String lang ) {
        return hasComment( getModel().createTypedLiteral( comment, lang, XSDDatatype.XSDstring ) );
    }
    
    /**
     * <p>Answer true if this resource has the given comment.</p>
     * @param comment The comment to test for
     * @return True if this resource has <code>comment</code> as a comment.
     */
    public boolean hasComment( Literal comment ) {
        boolean found = false;
        
        Iterator i = listComments( comment.getLanguage() );
        while (!found && i.hasNext()) {
            found = comment.equals( i.next() );
        }
        
        if (i instanceof ClosableIterator) {
            ((ClosableIterator) i).close();
        } 
        return found;
    }
    
    
    // seeAlso
    
    /**
     * <p>Assert that the given resource provides additional information about the definition of this resource</p>
     * @param res A resource that can provide additional information about this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public void setSeeAlso( Resource res ) {
        setPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    /**
     * <p>Add a resource that is declared to provided additional information about the definition of this resource</p>
     * @param res A resource that provides extra information on this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public void addSeeAlso( Resource res ) {
        addPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }

    /**
     * <p>Answer a resource that provides additional information about this resource. If more than one such resource
     * is defined, make an arbitrary choice.</p>
     * @return res A resource that provides additional information about this resource
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public Resource getSeeAlso() {
        return objectAsResource( getProfile().SEE_ALSO(), "SEE_ALSO" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to provide addition
     * information about this resource.</p>
     * @return An iterator over the resources providing additional definition on this resource.
     * @exception OntProfileException If the {@link Profile#SEE_ALSO()} property is not supported in the current language profile.   
     */ 
    public Iterator listSeeAlso() {
        checkProfile( getProfile().SEE_ALSO(), "SEE_ALSO" );
        return WrappedIterator.create( listProperties( getProfile().SEE_ALSO() ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource has the given resource as a source of additional information.</p>
     * @param res A resource to test against
     * @return True if the <code>res</code> provides more information on this resource.
     */
    public boolean hasSeeAlso( Resource res ) {
        return hasPropertyValue( getProfile().SEE_ALSO(), "SEE_ALSO", res );
    }
    
    // is defined by
    
    /**
     * <p>Assert that the given resource provides a source of definitions about this resource. Any existing 
     * statements for <code>isDefinedBy</code> will be removed.</p>
     * @param res The resource that is declared to be a definition of this resource.
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public void setIsDefinedBy( Resource res ) {
        setPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }

    /**
     * <p>Add a resource that is declared to provide a definition of this resource.</p>
     * @param res A defining resource 
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public void addIsDefinedBy( Resource res ) {
        addPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }

    /**
     * <p>Answer a resource that is declared to provide a definition of this resource. If there is
     * more than one such resource, an arbitrary selection is made.</p>
     * @return res An ont resource that is declared to provide a definition of this resource
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public Resource getIsDefinedBy() {
        return objectAsResource( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY" );
    }

    /**
     * <p>Answer an iterator over all of the resources that are declared to define
     * this resource. </p>
     * @return An iterator over the resources defining this resource.
     * @exception OntProfileException If the {@link Profile#IS_DEFINED_BY()} property is not supported in the current language profile.   
     */ 
    public Iterator listIsDefinedBy() {
        checkProfile( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY" );
        return WrappedIterator.create( listProperties( getProfile().IS_DEFINED_BY() ) )
               .mapWith( new ObjectMapper() );
    }

    /**
     * <p>Answer true if this resource is defined by the given resource.</p>
     * @param res A resource to test against
     * @return True if <code>res</code> defines this resource.
     */
    public boolean isDefinedBy( Resource res ) {
        return hasPropertyValue( getProfile().IS_DEFINED_BY(), "IS_DEFINED_BY", res );
    }
    

    /**
     * <p>Answer the cardinality of the given property on this resource. The cardinality
     * is the number of distinct values there are for the property.</p>
     * @param p A property
     * @return The cardinality for the property <code>p</code> on this resource, as an
     * integer greater than or equal to zero.
     */
    public int getCardinality( Property p ) {
        int n = 0;
        for (Iterator i = listProperties( p );  i.hasNext(); n++) {
            i.next(); 
        }
        
        return n;
    }
    
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the given
     * property of any ontology value. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @param p A property
     * @param name The name of the property, so that an appropriate message can be printed if not in the profile
     * @return An abstract accessor for the property p
     */
    public PathSet accessor( Property p, String name ) {
        return asPathSet( p, name );
    }
    
    
    /**
     * <p>
     * Set the value of the given property of this ontology resource to the given
     * value, encoded as an RDFNode.  Maintains the invariant that there is
     * at most one value of the property for a given resource, so existing
     * property values are first removed.  To add multiple properties, use
     * {@link #addProperty( Property, RDFNode ) addProperty}.
     * </p>
     * 
     * @param property The property to update
     * @param value The new value of the property as an RDFNode, or null to
     *              effectively remove this property.
     */
    public void setPropertyValue( Property property, RDFNode value ) {
        // if there is an existing property, remove it
        removeAll( property );

        // now set the new value
        addProperty( property, value );
    }


    /**
     * <p>
     * Remove any values for a given property from this resource.
     * </p>
     *
     * @param property The RDF resource that defines the property to be removed
     */
    public void removeAll( Property property ) {
        for (StmtIterator i = listProperties( property );  i.hasNext();  ) {
            i.next();
            i.remove();
        }
    }


    /**
     * <p>Set the RDF type property for this node in the underlying model, replacing any
     * existing <code>rdf:type</code> property.  
     * To add a second or subsequent type statement to a resource,
     * use {@link #setRDFType( Resource, boolean ) setRDFType( Resource, false ) }.
     * </p>
     * 
     * @param ontClass The RDF resource denoting the new value for the rdf:type property,
     *                 which will replace any existing type property.
     */
    public void setRDFType( Resource ontClass ) {
        setRDFType( ontClass, true );
    }


    /**
     * <p>
     * Add an RDF type property for this node in the underlying model. If the replace flag
     * is true, this type will replace any current type property for the node. Otherwise,
     * the type will be in addition to any existing type property.
     * </p>
     * 
     * @param ontClass The RDF resource denoting the class that will be the value 
     * for a new <code>rdf:type</code> property.
     * @param replace  If true, the given class will replace any existing 
     * <code>rdf:type</code> property for this
     *                 value, otherwise it will be added as an extra type statement.
     */
    public void setRDFType( Resource ontClass, boolean replace ) {
        // first remove any existing values, if required
        if (replace) {
            removeAll( RDF.type );
            
            Property typeAlias = (Property) getProfile().getAliasFor( RDF.type );
            if (typeAlias != null) {
                removeAll( typeAlias );
            }
        }
        
        
        addProperty( RDF.type, ontClass );
    }


    /**
     * <p>
     * Answer true if this DAML value is a member of the class denoted by the given URI.
     * </p>
     *
     * @param classURI String denoting the URI of the class to test against
     * @return True if it can be shown that this DAML value is a member of the class, via
     *         <code>rdf:type</code>.
     */
    public boolean hasRDFType( String classURI ) {
        return hasRDFType( getModel().getResource( classURI ) );
    }


    /**
     * <p>
     * Answer true if this ontology value is a member of the class denoted by the
     * given class resource.
     * </p>
     * 
     * @param ontClass Denotes a class to which this value may belong
     * @return True if <code><i>this</i> rdf:type <i>ontClass</i></code> is
     * a valid entailment in the model.
     */
    public boolean hasRDFType( Resource ontClass ) {
        return getModel().listStatements( this, RDF.type, ontClass ).hasNext() ||
               (getProfile().hasAliasFor( RDF.type ) && 
                getModel().listStatements( this, (Property) getProfile().getAliasFor( RDF.type), ontClass ).hasNext() );
    }


    /**
     * <p>
     * Answer an iterator over all of the RDF types to which this class belongs.
     * </p>
     *
     * @param closed TODO Not used in the current implementation  - fix
     * @return an iterator over the set of this ressource's classes
     */
    public Iterator getRDFTypes( boolean closed ) {
        Map1 mObject = new Map1() {  public Object map1( Object x ) { return ((Statement) x).getObject();  } };
        
        // make sure that we have an extneded iterator
        Iterator i = listProperties( RDF.type );
        ExtendedIterator ei = (i instanceof ExtendedIterator) ? (ExtendedIterator) i : WrappedIterator.create( i );
        
        // aliases to cope with?
        if (getProfile().hasAliasFor( RDF.type )) {
            ei = ei.andThen( WrappedIterator.create( listProperties( (Property) getProfile().getAliasFor( RDF.type ) ) ) );
        }
        
        // we only want the objects of the statements, and we only want one of each
        return new UniqueExtendedIterator( ei.mapWith( mObject ) );
    }

    
    /** 
     * <p>Answer a view of this resource as an annotation property</p>
     * @return This resource, but viewed as an AnnotationProperty
     * @exception ConversionException if the resource cannot be converted to an annotation property
     */
    public AnnotationProperty asAnnotationProperty() {
        return (AnnotationProperty) as( AnnotationProperty.class );
    }
    
    /** 
     * <p>Answer a view of this resource as a list </p>
     * @return This resource, but viewed as an OntList
     * @exception ConversionException if the resource cannot be converted to a list
     */
    public OntList asList() {
        return (OntList) as( OntList.class );
    }
    
    /** 
     * <p>Answer a view of this resource as a property</p>
     * @return This resource, but viewed as an OntProperty
     * @exception ConversionException if the resource cannot be converted to a property
     */
    public OntProperty asProperty() {
        return (OntProperty) as( OntProperty.class );
    }
    
    /** 
     * <p>Answer a view of this resource as an individual</p>
     * @return This resource, but viewed as an Individual
     * @exception ConversionException if the resource cannot be converted to an individual
     */
    public Individual asIndividual() {
        return (Individual) as( Individual.class );
    }
    
    /** 
     * <p>Answer a view of this resource as a class</p>
     * @return This resource, but viewed as an OntClass
     * @exception ConversionException if the resource cannot be converted to a class
     */
    public OntClass asClass() {
        return (OntClass) as( OntClass.class );
    }
    
    /** 
     * <p>Answer a view of this resource as an ontology description node</p>
     * @return This resource, but viewed as an Ontology
     * @exception ConversionException if the resource cannot be converted to an ontology description node
     */
    public Ontology asOntology() {
        return (Ontology) as( Ontology.class );
    }
    
    /** 
     * <p>Answer a view of this resource as an 'all different' declaration</p>
     * @return This resource, but viewed as an AllDifferent node
     * @exception ConversionException if the resource cannot be converted to an all different declaration
     */
    public AllDifferent asAllDifferent() {
        return (AllDifferent) as( AllDifferent.class );
    }
    


    // Internal implementation methods
    //////////////////////////////////


    protected PathSet asPathSet( Property p, String name ) {
        if (p == null) {
            throw new ProfileException( name, getProfile() );
        }
        else {
            return new PathSet( this, PathFactory.unit( p ) );
        }
    }
    
    /** 
     * Throw an exception if a term is not in the profile
     * @param term The term being checked
     * @param name The name of the term
     * @exception ProfileException if term is null (indicating it is not in the profile) 
     **/
    protected void checkProfile( Object term, String name ) {
        if (term == null) {
            throw new ProfileException( name, getProfile() );
        }
    }
    
    
    /**
     * <p>Answer the literal with the language tag that best matches the required language</p>
     * @param stmts A StmtIterator over the candidates
     * @param lang The language we're searching for, assumed non-null.
     * @return The literal value that best matches the given language tag, or null if there are no matches
     */
    protected String selectLang( StmtIterator stmts, String lang ) {
        String found = null;
        
        while (stmts.hasNext()) {
            RDFNode n = stmts.nextStatement().getObject();
            
            if (n instanceof Literal) {
                Literal l = (Literal) n; 
                String lLang = l.getLanguage();
                
                // is this a better match?
                if (lang.equalsIgnoreCase( lLang )) {
                    // exact match
                    found = l.getString();
                    break;
                }
                else if (lang.equalsIgnoreCase( lLang.substring( 0, 2 ) )) {
                    // partial match - want EN, found EN-GB
                    // keep searching in case there's a better
                    found = l.getString();
                }
                else if (found == null && lLang == null) {
                    // found a string with no (i.e. default) language - keep this unless we've got something better
                    found = l.getString();
                }
            }
        }
        
        stmts.close();
        return found;
    }
    
    /** Answer true if the desired lang tag matches the target lang tag */
    protected boolean langTagMatch( String desired, String target ) {
        return (desired == null) ||
               (desired.equalsIgnoreCase( target )) ||
               (target.length() > desired.length() && desired.equalsIgnoreCase( target.substring( desired.length() ) ));
    }
    
    /** Answer the object of a statement with the given property, .as() the given class */
    protected Object objectAs( Property p, String name, Class asClass ) {
        checkProfile( p, name );
        return getProperty( p ).getObject().as( asClass );
    }

    
    /** Answer the object of a statement with the given property, .as() an OntResource */
    protected OntResource objectAsResource( Property p, String name ) {
        return (OntResource) objectAs( p, name, OntResource.class );
    }

    
    /** Answer the object of a statement with the given property, .as() an OntProperty */
    protected OntProperty objectAsProperty( Property p, String name ) {
        return (OntProperty) objectAs( p, name, OntProperty.class );
    }

    
    /** Answer an iterator for the given property, whose values are .as() some class */
    protected Iterator listAs( Property p, String name, Class cls ) {
        checkProfile( p, name );
        return WrappedIterator.create( listProperties( p ) ).mapWith( new ObjectAsMapper( cls ) );
    }

    
    /** Add the property value, checking that it is supported in the profile */
    protected void addPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );
        addProperty( p, value );
    }
    
    /** Set the property value, checking that it is supported in the profile */
    protected void setPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );
        removeAll( p );
        addProperty( p, value );
    }

    /** Answer true if the given property is defined in the profile, and has the given value */
    protected boolean hasPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );
        return hasProperty( p, value );
    }
    
    /** Add the given value to a list which is the value of the given property */
    protected void addListPropertyValue( Property p, String name, RDFNode value ) {
        checkProfile( p, name );
        
        // get the list value
        if (hasProperty( p )) {
            RDFNode cur = getProperty( p ).getObject();
            if (!cur.canAs( OntList.class )) {
                throw new OntologyException( "Tried to add a value to a list-valued property " + p + 
                                             " but the current value is not a list: " + cur ); 
            }
            
            OntList values = (OntList) cur.as( OntList.class );
        
            // now add our value to the list
            if (!values.contains( value )){
                OntList newValues = values.add( value );
                
                // if the previous values was nil, the return value will be a new list
                if (newValues != values) {
                    removeAll( p );
                    addProperty( p, newValues );
                }
            }
        }
        else {
            // create a new list to hold the only value we know so far
            addProperty( p, ((OntModel) getModel()).createList( new RDFNode[] {value} ) );
        }
    }
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Implementation of Map1 that performs as( Class ) for a given class */
    protected class AsMapper
        implements Map1
    {
        private Class m_as;
        protected AsMapper( Class as ) { m_as = as; }
        public Object map1( Object x ) { return (x instanceof Resource) ? ((Resource) x).as( m_as ) : x; }
    }
    
    /** Implementation of Map1 that performs as( Class ) for a given class, on the subject of a statement */
    protected class SubjectAsMapper
        implements Map1
    {
        private Class m_as;
        protected SubjectAsMapper( Class as ) { m_as = as; }
        public Object map1( Object x ) { return (x instanceof Statement) ? ((Statement) x).getSubject().as( m_as ) : x; }
    }
    
    /** Implementation of Map1 that performs as( Class ) for a given class, on the object of a statement */
    protected class ObjectAsMapper
        implements Map1
    {
        private Class m_as;
        protected ObjectAsMapper( Class as ) { m_as = as; }
        public Object map1( Object x ) { return (x instanceof Statement) ? ((Statement) x).getObject().as( m_as ) : x; }
    }
    
    /** Implementation of Map1 that performs getString on the object of a statement */
    protected class ObjectAsStringMapper
        implements Map1
    {
        public Object map1( Object x ) { return (x instanceof Statement) ? ((Statement) x).getString() : x; }
    }
    
    /** Implementation of Map1 that returns the object of a statement */
    protected class ObjectMapper
        implements Map1
    {
        public Object map1( Object x ) { return (x instanceof Statement) ? ((Statement) x).getObject() : x; }
    }
    
    /** Filter for matching language tags on literals */
    protected class LangTagFilter 
        implements Filter
    {
        protected String m_lang;
        protected LangTagFilter( String lang ) { m_lang = lang; }
        public boolean accept( Object x ) {
            if (x instanceof Literal) {
                return langTagMatch( m_lang, ((Literal) x).getLanguage() );
            }
            else if (x instanceof Statement) {
                // we assume for a statement that we're filtering on the object of the statement
                return accept( ((Statement) x).getObject() );
            }
            else {
                return false;
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
