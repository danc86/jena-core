/******************************************************************
 * File:        BuiltinException.java
 * Created by:  Dave Reynolds
 * Created on:  11-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

/**
 * Exceptions thrown by runtime errors in exceuting rule system
 * builtin operations.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class BuiltinException extends RuntimeException {

    /**
     * Constructor.
     * @param builtin the invoking builtin
     * @param context the invoking rule context
     * @param message a text explanation of the error
     */
    public BuiltinException(Builtin builtin, RuleContext context, String message) {
        super("Error in clause of rule (" + context.getRule().toShortString() + ") "
                                         + builtin.getName() + ": " + message);
    }
}
