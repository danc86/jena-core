/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            26 Jan 2001
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     Preview-release $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright Hewlett-Packard Company 2001
 * All rights reserved.
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
 *
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
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import com.hp.hpl.jena.ontology.daml.LiteralAccessor;
import com.hp.hpl.jena.ontology.daml.DAMLCommon;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.util.Log;



/**
 * Encapsulates the standard methods of modifying a property on a DAML object, where
 * the value of the property is an RDF literal (as opposed to another DAML value,
 * see {@link com.hp.hpl.jena.ontology.daml.PropertyAccessor PropertyAccessor}.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id$
 */
public class LiteralAccessorImpl
    extends PropertyAccessorImpl
    implements LiteralAccessor
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * Construct a new accessor for literal values of the given property.
     *
     * @param property The property that this accessor works on
     * @param val The DAML value that has this property
     */
    public LiteralAccessorImpl( Property property, DAMLCommon val ) {
        super( property, val );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Answer an iteration over the literal values that this property has in the
     * RDF model.
     *
     * @return An iteration over the values of the encapsulated property. Each
     *         member of the iteration will be an RDF literal.
     */
    public NodeIterator getValues() {
        return m_val.getPropertyValues( getProperty() );
    }


    /**
     * Answer the a value of the encapsulated property. If it has no values, answer
     * null. If it has one value, answer that value. Otherwise, answer an undetermined
     * member of the set of values.
     *
     * @return A value for the encapsulated property, or null
     *         if the property has no value.
     */
    public Literal getValue() {
        try {
            NodeIterator i = getValues();
            return (i == null  ||  !i.hasNext()) ? null : ((Literal) i.nextNode());
        }
        catch (JenaException e) {
            Log.severe( "RDF exception when getting literal values: " + e, e );
            throw new RuntimeException( "RDF exception when getting literal values: " + e );
        }
    }


    /**
     * Add a value to the encapsulated property.
     *
     * @param value The value to be added, as a string.
     */
    public void addValue( String value ) {
        try {
            addValue( m_val.getModel().createLiteral( value ) );
        }
        catch (JenaException e) {
            Log.severe( "Saw RDF Exception while creating literal: " + e, e );
            throw new RuntimeException( "Saw RDF Exception while creating literal: " + e );
        }
    }


    /**
     * Add a value to the encapsulated property.
     *
     * @param value The value to be added, as an RDF literal.
     */
    public void addValue( Literal value ) {
        try {
            m_val.addProperty( getProperty(), value );
        }
        catch (JenaException e) {
            Log.severe( "RDF exception " + e, e );
        }
    }


    /**
     * Remove a value from the encapsulated property.
     *
     * @param value The value to be removed, as a string.
     */
    public void removeValue( String value ) {
        try {
            removeValue( m_val.getModel().createLiteral( value ) );
        }
        catch (JenaException e) {
            Log.severe( "Saw RDF Exception while creating literal: " + e, e );
            throw new RuntimeException( "Saw RDF Exception while creating literal: " + e );
        }
    }


    /**
     * Remove a value from the encapsulated property.
     *
     * @param value The value to be removed, as a literal.
     */
    public void removeValue( Literal value ) {
        m_val.removeProperty( getProperty(), value );
    }


    /**
     * Answer true if the encapsulated property has the given value as one of its
     * values.
     *
     * @param value A DAML value to test for
     * @return True if the RDF model contains a statement giving a value for
     *         the encapsulated property matching the given value.
     */
    public boolean hasValue( String value ) {
        try {
            return hasValue( m_val.getModel().createLiteral( value ) );
        }
        catch (JenaException e) {
            Log.severe( "Saw RDF Exception while creating literal: " + e, e );
            throw new RuntimeException( "Saw RDF Exception while creating literal: " + e );
        }
    }


    /**
     * Answer true if the encapsulated property has the given value as one of its
     * values.
     *
     * @param value A DAML value to test for
     * @return True if the RDF model contains a statement giving a value for
     *         the encapsulated property matching the given value.
     */
    public boolean hasValue( Literal value ) {
        try {
            return m_val.hasProperty( getProperty(), value );
        }
        catch (JenaException e) {
            Log.severe( "RDF Exception " + e, e );
            throw new RuntimeException( "RDF Exception " + e );
        }
    }



    // Internal implementation methods
    //////////////////////////////////



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
