/******************************************************************
 * File:        ReasonerVocabulary.java
 * Created by:  Dave Reynolds
 * Created on:  04-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

/**
 * A collection of RDF terms used in driving or configuring some of the
 * builtin reasoners.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class ReasonerVocabulary {
    
    /** The namespace used for system level descriptive properties of any reasoner */
    public static String JenaReasonerNS = "http://www.hpl.hp.com/semweb/2003/JenaReasoner#";
    
    /** The RDF class to which all Reasoners belong */
    public static Resource ReasonerClass = ResourceFactory.createResource(JenaReasonerNS + "ReasonerClass");
    
    /** Reasoner description property: name of the reasoner */
    public static Property nameP;
    
    /** Reasoner description property: text description of the reasoner */
    public static Property descriptionP;
    
    /** Reasoner description property: version of the reasoner */
    public static Property versionP;
    
    /** Reasoner description property: a schema property supported by the reasoner */
    public static Property supportsP;
    
    /** Reasoner description property: a configuration property supported by the reasoner */
    public static Property configurationP;

    /** The property that represents the direct/minimal version of the subClassOf relationship */
    public static Property directSubClassOf; 

    /** The property that represents the direct/minimal version of the subPropertyOf relationship */
    public static Property directSubPropertyOf; 

    /** Base URI used for configuration properties for rule reasoners */
    public static final String PropURI = "http://www.hpl.hp.com/semweb/2003/RuleReasoner";

    /** Property used to configure the derivation logging behaviour of a reasoner.
     *  Set to "true" to enable logging of derivations. */
    public static Property PROPderivationLogging;

    /** Property used to configure the tracing behaviour of a reasoner.
     *  Set to "true" to enable internal trace message to be sent to Logger.info . */
    public static Property PROPtraceOn;

    /** Property used to set the mode of a generic rule reasoner.
     *  Valid values are the strings "forward", "backward" or "hybrid" */
    public static Property PROPruleMode;
    
    /** Property used to attach a file a rules to a generic rule reasoner.
     *  Value should a URI giving the rule set to use. */
    public static Property PROPruleSet;
    
    /** Property used to switch on/off OWL schema translation on a generic rule reasoner.
     *  Value should be "true" to enable OWL translation */
    public static Property PROPenableOWLTranslation;
    
    /** Property used to switch on/off use of the dedicated subclass/subproperty
     *  caching in a generic rule reasoner. Set to "true" to enable caching. */
    public static Property PROPenableTGCCaching;
    
    /** A namespace used for Rubric specific properties */
    public static final String RBNamespace = "urn:x-hp-jena:rubrik/";
            
//  --------------------------------------------------------------------
// Method versions of key namespaces which are more initializer friendly

    /** Return namespace used for Rubric specific properties */
    public static final String getRBNamespace() {
        return RBNamespace;
    }
    
    /** Return namespace used for system level descriptive properties of any reasoner */
    public static final String getJenaReasonerNS() {
        return JenaReasonerNS;
    }
           
//  --------------------------------------------------------------------
//  Initializers

    static {
        try {
            nameP = ResourceFactory.createProperty(JenaReasonerNS, "name");
            descriptionP = ResourceFactory.createProperty(JenaReasonerNS, "description");
            versionP = ResourceFactory.createProperty(JenaReasonerNS, "version");
            supportsP = ResourceFactory.createProperty(JenaReasonerNS, "supports");
            configurationP = ResourceFactory.createProperty(JenaReasonerNS, "configurationProperty");
            directSubClassOf = ResourceFactory.createProperty(ReasonerRegistry.makeDirect(RDFS.subClassOf.getNode()).getURI());
            directSubPropertyOf = ResourceFactory.createProperty(ReasonerRegistry.makeDirect(RDFS.subPropertyOf.getNode()).getURI());
            PROPderivationLogging  = ResourceFactory.createProperty(PropURI+"#", "derivationLogging");
            PROPtraceOn = ResourceFactory.createProperty(PropURI+"#", "traceOn");
            PROPruleMode = ResourceFactory.createProperty(PropURI+"#", "ruleMode");
            PROPruleSet = ResourceFactory.createProperty(PropURI+"#", "ruleSet");
            PROPenableOWLTranslation= ResourceFactory.createProperty(PropURI+"#", "enableOWLTranslation");
            PROPenableOWLTranslation= ResourceFactory.createProperty(PropURI+"#", "enableTGCCaching");
        } catch (Exception e) {
            System.err.println("Initialization error: " + e);
            e.printStackTrace(System.err);
        }
    }
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