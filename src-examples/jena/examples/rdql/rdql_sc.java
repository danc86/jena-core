/*
 * (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
 * [See end of file]
 */
package jena.examples.rdql ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdql.* ;

import java.util.* ;

public class rdql_sc
{
    static public void main(String[] argv)
    {
        try {
            Model model = ModelFactory.createDefaultModel() ;
            model.read("file:hierarchy.nt", "N-TRIPLE") ;

            String queryString = 
                "SELECT * " +
                "WHERE (?x, <rdfs:subClassOf>, ?y) "+
                "USING "+
                "     ex   FOR <http://hpl.hp.com/semweb/JenaTutorial/RDQL/schema#> ,"+
                "     rdf  FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ," +
                "     rdfs FOR <http://www.w3.org/2000/01/rdf-schema#>" 
            ;

            Query query = new Query(queryString) ;
            query.setSource(model);
            QueryExecution qe = new QueryEngine(query) ;

            QueryResults results = qe.exec() ;
            for ( Iterator iter = results ; iter.hasNext() ; )
            {
                ResultBinding res = (ResultBinding)iter.next() ;
                String x = res.get("x").toString() ;
                String y = res.get("y").toString() ;

                x = x.substring(x.indexOf('#')+1) ;
                y = y.substring(y.indexOf('#')+1) ;

                System.out.println(x+" subClassOf "+y) ;
            }
            // Always close result iterator - frees work space.
            results.close() ;
            
        } catch (Exception ex)
        {
            System.err.println("Exception: "+ex) ;
            ex.printStackTrace(System.err) ;
        }
    }
}

/*
 * (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
 * All rights reserved.
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