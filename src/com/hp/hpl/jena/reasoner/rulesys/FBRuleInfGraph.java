/******************************************************************
 * File:        FBRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  28-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

//import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;

import org.apache.log4j.Logger;

/**
 * An inference graph that uses a mixture of forward and backward
 * chaining rules. The forward rules can create direct deductions from
 * the source data and schema and can also create backward rules. A
 * query is answered by consulting the union of the raw data, the forward
 * derived results and any relevant backward rules (whose answers are tabled
 * for future reference).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class FBRuleInfGraph  extends BasicForwardRuleInfGraph implements BackwardRuleInfGraphI {
    
    /** Single context for the reasoner, used when passing information to builtins */
    protected BBRuleContext context;
     
    /** A finder that searches across the data, schema, axioms and forward deductions*/
    protected Finder dataFind;
    
    /** The core backward rule engine which includes all the memoized results */
    protected BRuleEngine bEngine;
    
    /** The original rule set as supplied */
    protected List rules;
    
    /** A temporary list of prototypes that should be checked during f->b transistion */
    protected List prototypes = new ArrayList();
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(FBRuleInfGraph.class);

//  =======================================================================
//  Constructors

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param schema the (optional) schema graph to be included
     */
    public FBRuleInfGraph(Reasoner reasoner, Graph schema) {
        super(reasoner, schema);
        bEngine = new BRuleEngine(this);
    }

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     */
    public FBRuleInfGraph(Reasoner reasoner, List rules, Graph schema) {
        super(reasoner, rules, schema);
        this.rules = rules;
        bEngine = new BRuleEngine(this);
    }

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     * @param data the data graph to be processed
     */
    public FBRuleInfGraph(Reasoner reasoner, List rules, Graph schema, Graph data) {
        super(reasoner, rules, schema, data);
        this.rules = rules;        
        bEngine = new BRuleEngine(this);
    }
    
//  =======================================================================
//   Interface between infGraph and the goal processing machinery

    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
     */
    public ExtendedIterator findDataMatches(Node subject, Node predicate, Node object) {
        return dataFind.find(new TriplePattern(subject, predicate, object));
    }
    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
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
    
    /**
     * Adds a new Backward rule as a rusult of a forward rule process. Only some
     * infgraphs support this.
     */
    public void addBRule(Rule brule) {
        logger.debug("Adding rule " + brule);
        bEngine.addRule(brule);
        bEngine.reset();
    }
    
    /**
     * Adds a set of new Backward rules
     */
    public void addBRules(List rules) {
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule rule = (Rule)i.next();
            logger.debug("Adding rule " + rule);
            bEngine.addRule(rule);
        }
        bEngine.reset();
    }
    
    /**
     * Return an ordered list of all registered backward rules. Includes those
     * generated by forward productions.
     */
    public List getBRules() {
        return bEngine.getAllRules();
    }
    
    /**
     * Return the originally supplied set of rules, may be a mix of forward
     * and backward rules.
     */
    public List getRules() {
        return rules;
    }
    
    /**
     * Return a compiled representation of all the registered
     * forward rules.
     */
    private FRuleEngine.RuleStore getForwardRuleStore() {
        return engine.getRuleStore();
    }
    
    /**
     * Record a class prototype which should be checked for subtypes
     * during the f->b transition phase.
     */
    public void schedulePrototypeCheck(Node[] prototypeSpec) {
//        System.out.println("Scheduling prototype check on " + PrintUtil.print(prototypeSpec[1]));
        prototypes.add(prototypeSpec);
    }
    
    /**
     * Execute the prototype checks.
     */
    public void processPrototypeChecks() {
        BFRuleContext context = new BFRuleContext(this);
        for (Iterator i = prototypes.iterator(); i.hasNext(); ) {
            Node[] prototypeSpec = (Node[])i.next();
            Node prototype = prototypeSpec[0];
            Node type = prototypeSpec[1];
            for (Iterator tyi = find(prototype, RDF.type.asNode(), null); tyi.hasNext(); ) {
                Node newTy = ((Triple)tyi.next()).getObject();
                Triple sct = new Triple(type, TransitiveReasoner.subClassOf, newTy);
//                System.out.println("Adding prototype derivation: " + PrintUtil.print(sct));
                context.addTriple(sct);
            }
        }
        engine.addSet(context);
        prototypes.clear();
    }
    
//  =======================================================================
//  Core inf graph methods
    
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
            isPrepared = true;
            // initilize the deductions graph
            fdeductions = new FGraph( new GraphMem() );
            dataFind = (fdata == null || fdata.getGraph() == null) ? fdeductions :  FinderUtil.cascade(fdeductions, fdata);
            boolean rulesLoaded = false;
            if (schemaGraph != null) {
                Graph rawPreload = ((InfGraph)schemaGraph).getRawGraph();
                if (rawPreload != null) {
                    dataFind = FinderUtil.cascade(dataFind, new FGraph(rawPreload));
                }
                rulesLoaded = preloadDeductions(schemaGraph);
            }
            if (rulesLoaded) {
                engine.fastInit(); 
            } else {
                // No preload so do the rule separation
                addBRules(extractPureBackwardRules(rules));
                engine.init(true);
            }
            // Prepare the context for builtins run in backwards engine
            context = new BBRuleContext(this, dataFind);
            // Process any scheduled prototype checks
            // Prototype processing is currently disabled until we can support concurrently updatable graphs
            // processPrototypeChecks();
        }
    }
    
    /**
     * Cause the inference graph to reconsult the underlying graph to take
     * into account changes. Normally changes are made through the InfGraph's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfGraph's back" and this forces a full reconsult of
     * the changed data. 
     */
    public void rebind() {
        if (bEngine != null) bEngine.reset();
        isPrepared = false;
    }
    
    /**
     * Create a compiled representation of a list of rules.
     * @param rules a list of Rule objects
     * @return a datastructure containing precompiled representations suitable
     * for initializing FBRuleInfGraphs
     */
    public static RuleStore compile(List rules) {
        FRuleEngine.RuleStore fRules = FRuleEngine.compile(rules, true);
        List bRules = extractPureBackwardRules(rules);
        return new RuleStore(rules, fRules, bRules);
    }

    /**
     * Attach a compiled rule set to this inference graph.
     * @param rulestore a compiled set of rules.
     */
    public void setRuleStore(RuleStore ruleStore) {
        this.rules = ruleStore.rawRules;
        addBRules(ruleStore.bRules);
        engine.setRuleStore(ruleStore.fRuleStore);
    }
    
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Logger at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        super.setTraceOn(state);
        bEngine.setTraceOn(state);
    }

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
        engine.setDerivationLogging(recordDerivations);
        bEngine.setDerivationLogging(recordDerivations);
        if (recordDerivations) {
            derivations = new OneToManyMap();
        } else {
            derivations = null;
        }
    }
   
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized
     */
    public long getNRulesFired() {
        return engine.getNRulesFired() + bEngine.getNRulesFired();
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
        
        if (continuation == null) {
            return WrappedIterator.create( new TopGoalIterator(bEngine, pattern) );
        } else {
            return WrappedIterator.create( new TopGoalIterator(bEngine, pattern) )
                            .andThen(continuation.find(pattern));
        }
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
        bEngine.reset();
        isPrepared = false;
    }

//  =======================================================================
//  Helper methods

    /**
     * Scan the initial rule set and pick out all the backward-only rules with non-null bodies,
     * and transfer these rules to the backward engine. 
     */
    private static List extractPureBackwardRules(List rules) {
        List bRules = new ArrayList();
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule r = (Rule)i.next();
            if (r.isBackward() && r.bodyLength() > 0) {
                bRules.add(r);
            }
        }
        return bRules;
    }

    /**
     * Adds a set of precomputed triples to the deductions store. These do not, themselves,
     * fire any rules but provide additional axioms that might enable future rule
     * firing when real data is added. Used to implement bindSchema processing
     * in the parent Reasoner.
     * @return true if the preload was able to load rules as well
     */
    protected boolean preloadDeductions(Graph preloadIn) {
        Graph d = fdeductions.getGraph();
        FBRuleInfGraph preload = (FBRuleInfGraph)preloadIn;
        // If the rule set is the same we can reuse those as well
        if (preload.rules == rules) {
            // Load raw deductions
            for (Iterator i = preload.getDeductionsGraph().find(null, null, null); i.hasNext(); ) {
                d.add((Triple)i.next());
            }
            // Load backward rules
            addBRules(preload.getBRules());
            // Load forward rules
            engine.setRuleStore(preload.getForwardRuleStore());
            // Add access to raw data
            return true;
        } else {
            return false;
        }
    }
    
//  =======================================================================
//   Inner classes

    /**
     * Structure used to wrap up pre-processed/compiled rule sets.
     */
    public static class RuleStore {
        
        /** The raw rules */
        protected List rawRules;
        
        /** The indexed store used by the forward chainer */
        protected FRuleEngine.RuleStore fRuleStore;
        
        /** The separated backward rules */
        protected List bRules;
        
        /** 
         * Constructor.
         */
        public RuleStore(List rawRules, FRuleEngine.RuleStore fRuleStore, List bRules) {
            this.rawRules = rawRules;
            this.fRuleStore = fRuleStore;
            this.bRules = bRules;
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