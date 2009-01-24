/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 *
 * $Id$
 *
 */

package com.hp.hpl.jena.xmloutput.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.util.iterator.IteratorIterator;
import com.hp.hpl.jena.util.iterator.Map1;
import com.hp.hpl.jena.util.iterator.Map1Iterator;

/**
 * A sparse 2 dimensional array of boolean indexed by Object.
 *
 * Complete with transitive closure algorithm.
 * @author jjc
 * @version  Release='$Name$' Revision='$Revision$' Date='$Date$'
 */
class Relation<T> {
    final private Map<T, Set<T>> rows;
    final private Map<T, Set<T>> cols;
    final private Set<T> index;
    /** The empty Relation.
     */
    public Relation() {
        rows = new HashMap<T, Set<T>>();
        cols = new HashMap<T, Set<T>>();
        index = new HashSet<T>();
    }
    /** <code>a</code> is now related to <code>b</code>
     *
     */
    synchronized public void set(T a, T b) {
        index.add(a);
        index.add(b);
        innerAdd(rows, a, b);
        innerAdd(cols, b, a);
    }
    /** Uniquely <code>a</code> is now related to uniquely <code>b</code>.
     *
     *  When this is called any other <code>a</code> related to this <code>b</code> is removed.
     *  When this is called any other <code>b</code> related to this <code>a</code> is removed.
     *
     */
    synchronized public void set11(T a, T b) {
        clearX(a, forward(a));
        clearX(backward(b), b);
        set(a, b);
    }
    /** Uniquely <code>a</code> is now related to <code>b</code>.
     *  Many <code>b</code>'s can be related to each <code>a</code>.
     *  When this is called any other <code>a</code> related to this <code>b</code> is removed.
     */
    synchronized public void set1N(T a, T b) {
        clearX(backward(b), b);
        set(a, b);
    }
    /** <code>a</code> is now related to uniquely <code>b</code>.
     *  Many <code>a</code>'s can be related to each <code>b</code>.
     *  When this is called any other <code>b</code> related to this <code>a</code> is removed.
     */
    synchronized public void setN1(T a, T b) {
        clearX(a, forward(a));
        set(a, b);
    }
    /** <code>a</code> is now related to <code>b</code>
     *
     */
    synchronized public void setNN(T a, T b) {
        set(a, b);
    }
    /** <code>a</code> is now <em>not</em> related to <code>b</code>
     *
     */
    synchronized public void clear(T a, T b) {
        innerClear(rows, a, b);
        innerClear(cols, b, a);
    }
    private void clearX(Set<T> s, T b) {
        if (s == null)
            return;
        Iterator<T> it = s.iterator();
        while (it.hasNext())
            clear(it.next(), b);
    }
    private void clearX(T a, Set<T> s) {
        if (s == null)
            return;
        Iterator<T> it = s.iterator();
        while (it.hasNext())
            clear(a, it.next());
    }
    static private <T> void innerAdd(Map<T, Set<T>> s, T a, T b) {
        Set<T> vals = s.get(a);
        if (vals == null) {
            vals = new HashSet<T>();
            s.put(a, vals);
        }
        vals.add(b);
    }
    static private <T> void innerClear(Map<T, Set<T>> s, T a, T b) {
        Set<T> vals = s.get(a);
        if (vals != null) {
            vals.remove(b);
        }
    }
    /** Is <code>a</code> related to <code>b</code>?
     *
     */
    public boolean get(T a, T b) {
        Set <T> vals = rows.get(a);
        return vals != null && vals.contains(b);
    }
    /**
     * Takes this to its transitive closure.
    See B. Roy. <b>Transitivit� et connexit�.</b> <i>C.R. Acad. Sci.</i> Paris <b>249</b>, 1959 pp 216-218.
    or
    S. Warshall, <b>A theorem on Boolean matrices</b>, <i>Journal of the ACM</i>, <b>9</b>(1), 1962, pp11-12
    
    
     */
    synchronized public void transitiveClosure() {
        Iterator<T> j = index.iterator();
        while (j.hasNext()) {
            Object oj = j.next();
            Set<T> si = cols.get(oj);
            Set<T> sk = rows.get(oj);
            if (si != null && sk != null) {
                Iterator<T> i = si.iterator();
                while (i.hasNext()) {
                    T oi = i.next();
                    if (oi != oj) {
                        Iterator<T> k = sk.iterator();
                        while (k.hasNext()) {
                            T ok = k.next();
                            if (ok != oj)
                                set(oi, ok);
                        }
                    }
                }
            }
        }
    }
    /**
     * The set of <code>a</code> such that <code>a</code> is related to <code>a</code>.
     *
     */
    synchronized public Set<T> getDiagonal() {
        Set<T> rslt = new HashSet<T>();
        Iterator<T> it = index.iterator();
        while (it.hasNext()) {
            T o = it.next();
            if (get(o, o))
                rslt.add(o);
        }
        return rslt;
    }
    /**
     * The set of <code>b</code> such that <code>a</code> is related to <code>b</code>.
     *
     */
    public Set<T> forward(T a) {
        return rows.get(a);
    }
    /**
     * The set of <code>a</code> such that <code>a</code> is related to <code>b</code>.
     *
     */
    public Set<T> backward(T b) {
        return cols.get(b);
    }
    /**
     * An Iterator over the pairs of the Relation.
     * Each pair is returned as a java.util.Map.Entry.
     * The first element is accessed through <code>getKey()</code>,
     * the second through <code>getValue()</code>.
     *@see java.util.Map.Entry
     */
    public Iterator<Map.Entry<T, T>> iterator() {
        return new IteratorIterator(new Map1Iterator(new Map1() {
            // Convert a Map.Entry into an iterator over Map.Entry
            public Object map1(Object o) {
                Map.Entry pair = (Map.Entry) o;
                final Object a = pair.getKey();
                Set bs = (Set) pair.getValue();
                return new Map1Iterator(
                // Converts a b into a Map.Entry pair.
                new Map1() {
                    public Object map1(Object b) {
                        return new PairEntry<Object, Object>(a, b);
                    }
                }, bs.iterator());
            }
        }, rows.entrySet().iterator()));
    }
    
    synchronized public Relation<T> copy() {
        Relation<T> rslt = new Relation<T>();
        Iterator<Map.Entry<T, T>> it = iterator();
        while ( it.hasNext() ) {
            Map.Entry<T, T> e = it.next();
            rslt.set(e.getKey(),e.getValue());
        }
        return rslt;
    }
}
