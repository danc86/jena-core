/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            17-Nov-2003
 * Filename           $RCSfile$
 * Revision           $Revision$
 * Release status     $State$
 *
 * Last modified on   $Date$
 *               by   $Author$
 *
 * @copyright@
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////

/**
 * <p>
 * An exception type that wraps a checked exception from the DIG interface as a Jena (runtime)
 * exception.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id$)
 */
public class DIGWrappedException 
    extends DIGReasonerException
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    private Throwable m_ex;
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a DIG exception that wraps a deeper exception from the DIG interface.</p>
     * @param ex An exception or other error to be wrapped
     */
    public DIGWrappedException( Throwable ex ) {
        super( "DIG wrapped exception: " + ex.getMessage() );
        m_ex = ex;
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the exception that this exception is wrapping.</p>
     * @return The underlying, or wrapped, exception
     */
    public Throwable getWrappedException() {
        return m_ex;
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
@footer@
*/
