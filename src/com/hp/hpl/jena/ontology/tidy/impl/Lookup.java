/*
   (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/
package com.hp.hpl.jena.ontology.tidy.impl;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public interface Lookup {
	
	/**
	 * Frees any resources associated with this instance of key,
	 * the return value of qrefine.
	 * @param key
	 */
	public void done(int key);
	
	public abstract int qrefine(int s, int p, int o);
	/**
	  * 
	  * @param refinement The result of {@link #qrefine(int,int,int)}
	  * @param subj The old subcategory for the subject.
	  * @return The new subcategory for the subject.
	  */
	public abstract int subject(int old, int refinement);
	/**
	  * 
	  * @param refinement The result of {@link #qrefine(int,int,int)}
	  * @param prop The old subcategory for the property.
	  * @return The new subcategory for the property.
	  */
	public abstract int prop(int old, int refinement);
	/**
	  * 
	  * @param refinement The result of {@link #qrefine(int,int,int)}
	  * @param obj The old subcategory for the object.
	  * @return The new subcategory for the object.
	  */
	public abstract int object(int old, int refinement);
	/**
	  * 
	  * @param refinement The result of {@link #qrefine(int,int,int)}
	  * @return An integer reflecting an action needed in response to this triple.
	  */
	public abstract int action(int k);
	/**
	 * 
	 * @param refinement The result of {@link #qrefine(int,int,int)}
	 * @return True if this triple is <em>the</em> triple for the blank node object.
	 */
	public abstract boolean tripleForObject(int k);
	public abstract boolean tripleForSubject(int k);
	public abstract boolean removeTriple(int k);
	/**
	 *@param refinement The result of {@link #qrefine(int,int,int)}
	 * @return Is this triple in DL?.
	 */
	public abstract boolean dl(int k);
	/**
	  * @param k
	  * @return
	  */
	public abstract byte allActions(int k);

	/**
	 * Return Failure if meet is bottom
	 * @param c0
	 * @param c1
	 * @return
	 */
	public int meet(int c0, int c1);
	
	/**
	 * Return the cats that can appear as subject of p.
	 * @param p A basic category
	 *
	 */
	public int[] domain(int p);
	/**
	 * Return the cats that can appear as object of p.
	 * @param p A basic category
	 *
	 */
	public int[] range(int p);
}
/*
   (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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