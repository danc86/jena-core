/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/


// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import java.util.*;



/**
 * <p>
 * Maintains a pool of active DIG connections and whether they are allocated or not.
 * Implements Singleton pattern.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id$)
 */
public class DIGConnectionPool {
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /** The singleton instance */
    private static DIGConnectionPool s_instance = new DIGConnectionPool();
    
    
    // Instance variables
    //////////////////////////////////

    /** The adapter pool - unallocated adapters */
    private List m_pool = new ArrayList();
    
    /** The allocated adapter list */
    private List m_allocated = new ArrayList();
    
    
    // Constructors
    //////////////////////////////////

    /** Private constructor to enforce Singleton pattern. */
    private DIGConnectionPool() {}
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the singleton instance of the adapter pool.</p>
     */
    public static DIGConnectionPool getInstance() {
        return s_instance;
    }
    
    
    public DIGConnection allocate() {
        DIGConnection dc = m_pool.isEmpty() ? new DIGConnection() : (DIGConnection) m_pool.remove( 0 );
        m_allocated.add( dc );
            
        return dc;
    }
    
    
    public void release( DIGConnection dc ) {
        m_allocated.remove( dc );
        m_pool.add( dc );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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
