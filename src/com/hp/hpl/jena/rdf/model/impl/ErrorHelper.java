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
 * ErrorHelper.java
 *
 * Created on 09 August 2000, 09:44
 */

package com.hp.hpl.jena.rdf.model.impl;

import java.io.PrintStream;

/** An internal class not normally of interest to developers.
 * @author  bwm
 * @version  Release='$Name$' Revision='$Revision$' Date='$Date$'
 */
public class ErrorHelper extends Object {

    protected static PrintStream logStream = System.out;

    public static void logInternalError(String className, int errorCode) {
        logStream.println("RDF internal error " + className + " " + errorCode);
    }

    public static void logInternalError(
        String className,
        int errorCode,
        Exception e) {
        logStream.println(
            "RDF internal error "
                + className
                + " "
                + errorCode
                + " "
                + e.toString());
        e.printStackTrace(logStream);
    }

    protected static void logWarning(String message) {
        logStream.println("RDF Warning: " + message);
    }
}
