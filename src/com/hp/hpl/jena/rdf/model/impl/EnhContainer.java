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
 * EnhContainer.java
 *
 * Created on 17 August 2000, 09:07
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;

/** A base class for supporting enhanced containers.  An enhanced resource is a
 * resource with extended functionality, provided either by the application or
 * by the implementation.  This class is a base class with container
 * functionality which can be extended by the application.
 *
 * <p>Enhanced resources are implemented using encapsulation.  An enhanced
 *   resource contains an embedded resource which provides the basic
 *   functionality of the resource.  The application code provides any extended
 *   behaviour.  This method was chosen, rather than subclassing, so as not
 *   to constrain the applications need to create its own class hierarchy.</p>
 *
 * <p>This class is intended to be subclassed by the application to provide the
 *   specific enhanced behaviour.  It provides Container funcationality and some
 *   basic housekeeping functions to support the encapsulation.</p>
 *
 * @deprecated ContainerImpl now is enhanced.
 * @author bwm
 * @version  Release='$Name$' Revision='$Revision$' Date='$Date$'
 */
public abstract class EnhContainer extends ContainerImpl {

    
    protected EnhContainer(Container c) {
        super(c, (ModelCom)c.getModel());
    }   
    

}