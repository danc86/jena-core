/******************************************************************
 * File:        Node_RuleVariable.java
 * Created by:  Dave Reynolds
 * Created on:  30-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id$
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * A variation on the normal Node_Variable which support for value bindings.
 * Currently the forward rule system stores the values externally but requires
 * variables to have an offset index in the rule environment vector. The
 * variables can also suport prolog-like reference chains and trails but these
 * are not yet used.
 * <p>
 * Note that this should not be used in a real Triple, in particular
 * it should not end up in a Graph. It is only needed for the rule systems. </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date$
 */
public class Node_RuleVariable extends Node_Variable {
    /** The offset of this variable in the Frule's binding table */
    protected int index;

    /** The value to which this variable is bound, can be another variable,
     *  itself (meaning unbound) or an actual value */
    protected Node value;
    
    /** A flag to indicate the the value is reference (pointer to a var) */
    protected boolean isRef = true;
    
    /** A static wildcard - like Node.ANY but tests equl to other Node_RuleVariables */
    public static final Node_RuleVariable WILD = new Node_RuleVariable("*", -1);
         
    /**
     * Constructor
     * @param label the text label for the variable
     * @param index the calculated index of this variable in the rule
     */
    public Node_RuleVariable(String label, int index) {
        super(new VarLabel(label));
        this.index = index;
        this.value = this;
    }
         
    /**
     * Constructor
     * @param label the text label for the variable
     * @param index the calculated index of this variable in the rule
     */
    private Node_RuleVariable(VarLabel label, int index) {
        super(label);
        this.index = index;
        this.value = this;
    }
    
    /**
     * Returns the varibles index in an frule binding vector.
     * @return int
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Return an indexable object for this Node. This is actually the 
     * rule label. This is weird but needed because equals is (deliberately)
     * perverse on Node_Rulelabel so if we want to put then in a Set or Map
     * we need something with a better equals function.
     */
    public Object getRepresentative() {
        return label;
    }
    
    /**
     * Binds a value to the brule version of the variable. Does not follow
     * any reference trail, assues we have already be derefenced.
     * @param node a concrete Node value or another Node_RuleVariable
     * to alias to
     */
    public void simpleBind(Node node) {
        value = node;
        isRef = node instanceof Node_RuleVariable;
    }
    
    /**
     * Dereference a variable by following the reference chain.
     * @return either a concrete node value or the last variable
     * in the reference chain.
     */
    public Node deref() {
        Node_RuleVariable var = this;
        while (var.isRef) {
            if (var.value == var) {
                return var;
            }
            var = (Node_RuleVariable)var.value;
        }
        return var.value;
    }
    
    /**
     * Set the variable to be unbound (in the brule sense)
     */
    public void unbind() {
        isRef = true;
        value = this;
    }
    
    /**
     * Test if the variable is unbound (in the brule sense).
     */
    public boolean isUnbound() {
        return (isRef && (value == this));
    }
    
    /**
     * Clone the rule variable to allow multiple rule instaces to be active at the same time.
     */
    public Node_RuleVariable cloneNode() {
        return new Node_RuleVariable((VarLabel)label, index);        
    }
    
    /** printable form */        
    public String toString() {
        return ((VarLabel)label).getLabel();
    }
    
    /** Equality override - all rule variables are treated as equal
     *  to support easy variant matching. */
    public boolean equals(Object o) {
        return o instanceof Node_RuleVariable;
    }
        
    /** hash function override - all vars have same hash code to support fast
     *  search of variant tables */
    public int hashCode() {
        return 0xc3a7;
    }

    /**
     * Compare two nodes, taking into account variable indices.
     */
    public static boolean sameNodeAs(Node n, Node m) {
        if (n instanceof Node_RuleVariable) {
            if (m instanceof Node_RuleVariable) {
                return ((Node_RuleVariable)n).getIndex() == ((Node_RuleVariable)m).getIndex();
            } else {
                return false;
            }
        } else {
            return n.sameValueAs(m);
        }
    }
    
    /** Inner class to wrap the label to ensure it is distinct from other usages */
    static class VarLabel {
        
        /** The label being wrapped */
        String label;
        
        VarLabel(String label ) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }

}
