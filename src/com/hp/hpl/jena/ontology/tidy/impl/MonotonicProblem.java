/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.impl;

import com.hp.hpl.jena.ontology.tidy.ConflictingTriplesProblem;

/**
  This class has additional methods 
  over its superclass
  which are not part of the API
 * 
 *  * @author Jeremy J. Carroll
 *
 */
public abstract class MonotonicProblem extends ConflictingTriplesProblem {
   public String getMessage() {
       return msg;
   }
   private final String msg;
   public MonotonicProblem(String msg) {
       this.msg = msg;
   }
   
   private MonotonicProblem next;
   MonotonicProblem nextProblem() {
       return next;
   }
  void addNext(MonotonicProblem t){
       if (next==null)
           next = t;
       else
           next.addNext(t);
   }
}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 */
 
