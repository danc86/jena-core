/******************************************************************
 * File:        Drop.java
 * Created by:  Dave Reynolds
 * Created on:  26-Jul-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: Drop.java,v 1.1 2005-07-26 16:21:20 der Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinException;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.Util;

/**
 * A variant of the "remove" builtin that will delete matched triples
 * from the graph but will not trigger further rule processing for
 * the removed triples. This makes it seriously non-monotonic but
 * useful for rewrite rules.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $
 */

public class Drop  extends BaseBuiltin  {
    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    public String getName() {
        return "drop";
    }    
    
    /**
     * This method is invoked when the builtin is called in a rule head.
     * Such a use is only valid in a forward rule.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     */
    public void headAction(Node[] args, int length, RuleContext context) {
        boolean ok = false;
        InfGraph inf = context.getGraph();
        Graph raw = inf.getRawGraph();
        Graph deductions = inf.getDeductionsGraph();
        for (int i = 0; i < length; i++) {
            Node clauseN = getArg(i, args, context);
            if (Util.isNumeric(clauseN)) {
                int clauseIndex = Util.getIntValue(clauseN);
                Object clause = context.getRule().getBodyElement(clauseIndex);
                if (clause instanceof TriplePattern) {
                    Triple t = context.getEnv().instantiate((TriplePattern)clause);
                    raw.delete(t);
                    deductions.delete(t);
                } else {
                    throw new BuiltinException(this, context, "illegal triple to remove non-triple clause");
                }
            } else {
                throw new BuiltinException(this, context, "illegal arg to remove (" + clauseN + "), must be an integer");
            }
        }
    }    
}

/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
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