/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 * NodeIteratorImpl.java
 *
 * Created on 07 August 2000, 06:43
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** A NodeIterator implementation.
 *
 * @author  bwm
 * @version   Release='$Name$' Revision='$Revision$' Date='$Date$'
 */
public class NodeIteratorImpl extends Object implements NodeIterator {
    
    Iterator iterator;
    Object   object;

    /** Creates new NodeIteratorImpl */
    public NodeIteratorImpl(Iterator iter, Object object) {
        this.iterator = iter;
        this.object   = object;
    }

    public boolean hasNext() throws RDFException {
        if (iterator != null) {
            return iterator.hasNext();
        } else {
            throw new RDFException(RDFException.ITERATORCLOSED);
        }        
    }
    
    public Object next() throws NoSuchElementException, RDFException {
        if (iterator != null) {
            return iterator.next();
        } else {
            throw new RDFException(RDFException.ITERATORCLOSED);
        }
    }
    
    public RDFNode nextNode() throws NoSuchElementException, RDFException {
        return (RDFNode) iterator.next();
    }
    
    public void remove() throws NoSuchElementException, RDFException {
        throw new RDFException(RDFException.UNSUPPORTEDOPERATION);
    }
    
    public void close() throws RDFException {
        if (iterator instanceof ClosableIterator) {
            ((ClosableIterator) iterator).close();
        }
        iterator = null;
        object   = null;
    }
}