/******************************************************************
 * File:        BasicBackwardRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  03-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.iterator.*;
import org.apache.log4j.Logger;

/**
 * An inference graph that runs a set of rules using a tabled
 * backward chaining interpreter.
 *
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class BasicBackwardRuleInfGraph extends BaseInfGraph {

//=======================================================================
// variables

    /** Set for rules being used */
    protected List rules;
    
    /** Table of derivation records, maps from triple to RuleDerivation */
    protected OneToManyMap derivations;
    
    /** An optional graph of separate schema assertions that should also be processed */
    protected FGraph fschema;
    
    /** Cache of deductions made from the rules */
    protected FGraph fdeductions;
     
    /** A finder that searches across the data, schema and axioms */
    protected Finder dataFind;
    
    /** The core rule engine which includes all the memoized results */
    protected BRuleEngine engine;
    
    /** Single context for the reasoner, used when passing information to builtins */
    protected BBRuleContext context;
    
    /** performance stats - number of rules passing initial trigger */
    int nRulesTriggered = 0;
    
    /** performance stats - number of rules fired */
    long nRulesFired = 0;
    
    /** threshold on the numbers of rule firings allowed in a single operation */
    long nRulesThreshold = DEFAULT_RULES_THRESHOLD;

    /** Flag which, if true, enables tracing of rule actions to logger.info */
    boolean traceOn = false;
        
    /** Default setting for rules threshold */
    public static final long DEFAULT_RULES_THRESHOLD = 500000;
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(BasicBackwardRuleInfGraph.class);
    
//=======================================================================
// Core methods

    /**
     * Constructor. Create a new backward inference graph to process
     * the given data. The parent reasoner supplies the ruleset and
     * any additional schema graph.
     * 
     * @param reasoner the parent reasoner 
     * @param data the data graph to be processed
     * @param ruleStore the indexed set of rules to use
     */
    public BasicBackwardRuleInfGraph(BasicBackwardRuleReasoner reasoner, Graph data, RuleStore ruleStore) {
        super(data, reasoner);
        if (reasoner.schemaGraph != null) {
            fschema = new FGraph(reasoner.schemaGraph);
        }
        
        // Set up the chain of searches for triple matches in the raw data
        rules = reasoner.getRules();
        // Set up the backchaining engine
        engine = new BRuleEngine(this, ruleStore);
    }    
    
    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    public void prepare() {
        if (!isPrepared) {
            fdeductions = new FGraph( new GraphMem() );
            extractAxioms();
            dataFind = fdata;
            if (fdeductions != null) {
                dataFind = FinderUtil.cascade(dataFind, fdeductions);
            }
            if (fschema != null) {
                dataFind = FinderUtil.cascade(dataFind, fschema);
            }
            
            context = new BBRuleContext(this, dataFind);
        }
        
        isPrepared = true;
    }

    /**
     * Replace the underlying data graph for this inference graph and start any
     * inferences over again. This is primarily using in setting up ontology imports
     * processing to allow an imports multiunion graph to be inserted between the
     * inference graph and the raw data, before processing.
     * @param data the new raw data graph
     */
    public void rebind(Graph data) {
        fdata = new FGraph(data);
        engine.reset();
        isPrepared = false;
    }
    
    /**
     * Cause the inference graph to reconsult the underlying graph to take
     * into account changes. Normally changes are made through the InfGraph's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfGraph's back" and this forces a full reconsult of
     * the changed data. 
     */
    public void rebind() {
        engine.reset();
        isPrepared = false;
    }
   
    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        if (!isPrepared) prepare();
        return WrappedIterator.create(
             new TopGoalIterator( engine.findGoal(pattern) )
        );
    }
   
    /** 
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     */
    public ExtendedIterator find(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), null);
    }

    /**
     * Basic pattern lookup interface.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        return findWithContinuation(pattern, null);
    }

    /**
     * Flush out all cached results. Future queries have to start from scratch.
     */
    public void reset() {
        engine.reset();
        isPrepared = false;
    }
    
//=======================================================================
// support for proof traces

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
        if (recordDerivations) {
            derivations = new OneToManyMap();
        } else {
            derivations = null;
        }
    }
    
    /**
     * Return the derivation of at triple.
     * The derivation is a List of DerivationRecords
     */
    public Iterator getDerivation(Triple t) {
        return derivations.getAll(t);
    }
    
    /**
     * Change the threshold on the number of rule firings 
     * allowed during a single operation.
     * @param threshold the new cutoff on the number rules firings per external op
     */
    public void setRuleThreshold(long threshold) {
        nRulesThreshold = threshold;
    }
    
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Logger at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        traceOn = state;
        engine.setTraceOn(state);
    }
    
    /**
     * Return true if tracing is switched on
     */
    public boolean isTraceOn() {
        return traceOn;
    }
    
    /**
     * Dump an a summary of the goal table state to stdout.
     * Just debugging, do not use for real.
     */
    public void dump() {
        engine.dump();        
    }
    
//  =======================================================================
//   Interface between infGraph and the goal processing machinery

    /**
     * Match a pattern just against the stored data (raw data, schema,
     * axioms) but no derivation.
     */
    public ExtendedIterator findDataMatches(TriplePattern pattern) {
        return dataFind.find(pattern);
    }
            
    /**
     * Process a call to a builtin predicate
     * @param clause the Functor representing the call
     * @param env the BindingEnvironment for this call
     * @param rule the rule which is invoking this call
     * @return true if the predicate succeeds
     */
    public boolean processBuiltin(Object clause, Rule rule, BindingEnvironment env) {
        if (clause instanceof Functor) {
            context.setEnv(env);
            context.setRule(rule);
            return((Functor)clause).evalAsBodyClause(context);
        } else {
            throw new ReasonerException("Illegal builtin predicate: " + clause + " in rule " + rule);
        }
    }
    
//  =======================================================================
//   Rule engine

    /**
     * Find any axioms (rules with no body) in the rule set and
     * add those to the auxilliary graph to be included in searches.
     */
    protected void extractAxioms() {
        Graph axioms = fdeductions.getGraph();
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule rule = (Rule)i.next();
            if (rule.bodyLength() == 0) {
                // An axiom
                for (int j = 0; j < rule.headLength(); j++) {
                    Object axiom = rule.getHeadElement(j);
                    if (axiom instanceof TriplePattern) {
                        axioms.add(((TriplePattern)axiom).asTriple());
                    }
                }
            }
        }
    }
    
//  =======================================================================
//   Inner classes

    /**
     * Top level result iterator. Pumps the top level GoalState until
     * it hits fail or the agenda is empty and it hits suspend.
     */
    class TopGoalIterator implements ClosableIterator {
        
        /** The GoalState which is traversing the top level derivation tree */
        GoalState goalState;
        
        /** The next result to be returned, or null if we have finished */
        Object lookAhead;
        
        /**
         * Constructor. Wraps a top level goal state as an iterator
         */
        TopGoalIterator(GoalState goalState) {
            this.goalState = goalState;
            moveForward();
        }
        
        /**
         * Find the next result in the goal state and put it in the
         * lookahead buffer.
         */
        private void moveForward() {
            lookAhead = goalState.next();
            if (lookAhead == StateFlag.SUSPEND) {
                if (engine.next(goalState) != null) {
                    lookAhead = goalState.next();
                } else {
                    lookAhead = null;
                }
            } else if (lookAhead == StateFlag.FAIL) {
                lookAhead = null;
            }
        }
        
        /**
         * @see com.hp.hpl.jena.util.iterator.ClosableIterator#close()
         */
        public void close() {
            goalState.close();
        }
    
        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return (lookAhead != null);
        }
    
        /**
         * @see java.util.Iterator#next()
         */
        public Object next() {
            Object result = lookAhead;
            moveForward();
            return result;
        }
    
        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
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