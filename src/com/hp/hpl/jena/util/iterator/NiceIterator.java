/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.util.iterator;

import java.util.*;

/**
    NiceIterator is the standard base class implementing ExtendedIterator. It provides
    the static methods for <code>andThen</code>, <code>filterKeep</code> and
    <code>filterDrop</code>; these can be reused from any other class. It defines
    equivalent instance methods for descendants and to satisfy ExtendedIterator.  
	@author kers
*/

public class NiceIterator implements ExtendedIterator
    {
    public NiceIterator()
        { super(); }

    /**
        default close: don't need to do anything.
    */
    public void close()
        { }

    /**
        default hasNext: no elements, return false.
    */
    public boolean hasNext()
        {  return false; }

    protected void ensureHasNext()
        { if (hasNext() == false) throw new NoSuchElementException(); }
    
    /**
        default next: throw an exception.
    */
    public Object next()
        { return noElements( "empty NiceIterator" ); }
        
    /**
        Utility method for this and other (sub)classes: raise the appropriate
        "no more elements" exception. I note that we raised the wrong exception
        in at least one case ...
    
        @param message the string to include in the exception
        @return never - but we have a return type to please the compiler
    */
    protected Object noElements( String message )
        { throw new NoSuchElementException( message ); }
        
    /**
        default remove: we have no elements, so we can't remove any.
    */
    public void remove()
        { 
        throw new UnsupportedOperationException( "remove not supported for this iterator" ); 
        }
    
    /**
         Answer the next object, and remove it.
    */
    public Object removeNext()
        { Object result = next(); remove(); return result; }
        
    /**
        concatenate two closable iterators.
    */
    
    public static ExtendedIterator andThen( final Iterator a, final Iterator b )
        {
        return new NiceIterator()
            {
            private boolean walkingA = true;
            
            public boolean hasNext()
                { return (walkingA && (walkingA = a.hasNext())) || b.hasNext(); }
                
            public Object next()
                { return (walkingA && (walkingA = a.hasNext())) ? a.next() : b.next(); }
                
            public void close()
                {
                close( a );
                close( b );
                }
                
            public void remove()
                { (walkingA ? a : b).remove(); }
            };
        }
        
    /**
        make a new iterator, which is us then the other chap.
    */   
    public ExtendedIterator andThen( ClosableIterator other )
        { return andThen( this, other ); }
        
    /**
        make a new iterator, which is our elements that pass the filter
    */
    public ExtendedIterator filterKeep( Filter f )
        { return new FilterIterator( f, this ); }

    /**
        make a new iterator, which is our elements that pass the filter
    */        
    public ExtendedIterator filterDrop( final Filter f )
        { 
        Filter notF = new Filter() { public boolean accept( Object x ) { return !f.accept( x ); } };
        return new FilterIterator( notF, this ); 
        }
   
    /**
        make a new iterator which is the elementwise _map1_ of the base iterator.
    */     
    public ExtendedIterator mapWith( Map1 map1 )
        { return new Map1Iterator( map1, this ); }

    /**
        If <code>it</code> is a Closableiterator, close it. Abstracts away from
        tests [that were] scattered through the code.
    */
    public static void close( Iterator it )
        { if (it instanceof ClosableIterator) ((ClosableIterator) it).close(); }
   
    static final private NiceIterator emptyInstance = new NiceIterator();
    
    /**
     * An iterator over no elements.
     * @return A class singleton which doesn't iterate.
     */
    static public ExtendedIterator emptyIterator() {
        return emptyInstance;
    }
  
    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
