/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.util.* ;
import java.io.* ;
import com.hp.hpl.jena.rdf.model.* ;

/** <p>Takes a QueryResult object and returns formatted (in various ways)
 *  Useful for the scripting interface.
 *  May help for display in other contexts.</p>
 *
 *  <p>Note: this is compute intensive and memory intensive.
 *  It needs to read all the results first (all the results are now in-memory - not kept here)
 *  in order to find things like the maximum length of a column value; then it needs
 *  to pass over the results again, turning them into Strings again, in order to return them.
 *  </p>
 *  <p>We prefer slow and less memory intensive because it is more rebust for scripting.</p>
 *
 *  Don't keep QueryResultsFormatter's around unnecessarily!
 * 
 * @author   Andy Seaborne
 * @version  $Id$
 */

public class QueryResultsFormatter
{
    QueryResults queryResults ;
    QueryResultsMem all = null ;
    int numRows = -2 ;
    int numCols = -2 ;
    int colWidths[] = null ;
    static final String notThere = "<<unset>>" ;

    /** Create a formatter for a QueryResults object */

    public QueryResultsFormatter(QueryResults qresults)
    {
        queryResults = qresults ;
    }

    /** How wide is the result table */
    public int numColumns() { return queryResults.getResultVars().size() ; }

    /** How deep is the result table.  Negative implies unknown */
    public int numRows() { return numRows ; }

    private void colWidths()
    {
    	if ( all == null )
    		all = new QueryResultsMem(queryResults) ;
    		
        numCols = queryResults.getResultVars().size() ;
        numRows = 0 ;
        colWidths = new int[numCols] ;

        // Widths at least that of the variable name.  Assumes we will print col headings.
        for ( int i = 0 ; i < numCols ; i++ )
            colWidths[i] = ((String)queryResults.getResultVars().get(i)).length() ;

        // Preparation pass : find the maximum width for each column
        for ( ; all.hasNext() ; )
        {
            numRows++ ;
            ResultBinding env = (ResultBinding)all.next() ;
            int col = -1 ;
            for ( Iterator iter = queryResults.getResultVars().iterator() ; iter.hasNext() ; )
            {
                col++ ;
                String rVar = (String)iter.next() ;
                Value val = env.getValue(rVar) ;
                String s = (val==null)? notThere : val.asQuotedString() ;
                if ( colWidths[col] < s.length() )
                    colWidths[col] = s.length() ;
            }
        }
        all.reset() ;
    }

    /** Forcefully clearup.
     *  As results might have been read into memory, this operation signals
     *  intermediate data is no longer needed.
     */
    public void close()
    {
        queryResults.close() ;
        queryResults = null ;
        all = null ;
        colWidths = null ;
    }

    // Generalise: there are two algorithms : the one pass and the two pass

    /** Write out a compact form.  This encodes all the information is a vaguely
     *  readable way but is suitable for reading in again.  Used for testing.
     */


	public void dump(PrintWriter pw, boolean format)
	{
		if (queryResults.getResultVars().size() == 0)
		{
			pw.println("# ==== No variables ====");
			pw.flush();
			return;
		}
		else
		{
			boolean first = true;
			pw.println("# Variables:");
			for (Iterator iter = queryResults.getResultVars().iterator(); iter.hasNext();)
			{
				String vName = (String) iter.next();
				pw.print("?" + vName+" ");
				first = false;
			}
			pw.println(".") ;
			pw.println("# Data:");
			pw.flush() ;
		}

		if (format)
			dumpAligned(pw);
		else
			dumpRaw(pw);
	}

    // One pass algorithm
    private void dumpRaw(PrintWriter pw)
    {
        numCols = queryResults.getResultVars().size() ;
        for ( Iterator tableIter = queryResults ; tableIter.hasNext() ; )
        {
            ResultBinding env = (ResultBinding)tableIter.next() ;
            for (Iterator iter = queryResults.getResultVars().iterator() ; iter.hasNext() ; )
            {
                String rVar = (String)iter.next() ;
                Value val = env.getValue(rVar) ;

                /*
            // Doing it this way tests the iterator on ResultBindings
            // but does not print anything for variables not in the query results.
            for ( ResultBinding.ResultBindingIterator iter = env.iterator() ; iter.hasNext() ; )
            {
                iter.next() ;
                String rVar = iter.varName() ;
                if ( ! queryResults.getResultVars().contains(rVar) )
                    continue ;
                Value val = iter.value() ;
                 */
                String s = (val==null)? notThere : val.asQuotedString() ;
                pw.print("?") ;
                pw.print(rVar) ;
                pw.print(" ");
                pw.print(s);
                pw.print(" ");
            }
            pw.println(".") ;
        }
        queryResults.close() ;
    }

    // Dump formated : columns padded for readability.
    // Requires reading all the data into memory - its a two pass algorithm.
    private void dumpAligned(PrintWriter pw)
    {
    	if ( all == null )
    		all = new QueryResultsMem(queryResults) ;
 
        if ( colWidths == null )
            colWidths() ;

        String row[] = new String[numCols] ;
        int lineWidth = 0 ;
        for ( int col = 0 ; col < numCols ; col++ )
        {
            String rVar = (String)queryResults.getResultVars().get(col) ;
            row[col] = rVar ;
            lineWidth += colWidths[col] ;
        }

        for ( Iterator tableIter = all ; tableIter.hasNext() ; )
        {
            ResultBinding env = (ResultBinding)tableIter.next() ;
            for ( int col = 0 ; col < numCols ; col++ )
            {
                StringBuffer sbuff = new StringBuffer(120) ;
                String rVar = (String)queryResults.getResultVars().get(col) ;
                sbuff.append('?') ;
                sbuff.append(rVar) ;
                sbuff.append(' ') ;
                Value val = env.getValue(rVar) ;
                String s = (val==null)? notThere : val.asQuotedString() ;

                int pad = colWidths[col] ;
                sbuff.append(s) ;

                for ( int j = 0 ; j < pad-s.length() ; j++ )
                    sbuff.append(' ') ;
                // Always has a trailing space
                sbuff.append(' ') ;
                pw.print(sbuff) ;
            }
            pw.println(" .") ;
        }
        all.close() ;
        pw.flush() ;
    }

    /** Textual representation : default layout using " | " to separate columns
     *  @param printwriter Output
     */
    public void printAll(PrintWriter pw) { printAll(pw, " | ", null) ; }
    
    /** Textual representation : layout using given separator
     *  @param PrintWriter Output
     *  @param String      Column separator
     */
    public void printAll(PrintWriter pw, String colSep) { printAll(pw, colSep, null) ; }
    
    /** Textual representation : layout using given separator
     *  @param PrintWriter Output
     *  @param String      Column separator
     *  @param String      String to add to end of lines
     */
    public void printAll(PrintWriter pw, String colSep, String lineEnd)
    {
        // Temp move
        if ( queryResults.getResultVars().size() == 0 )
        {
            pw.println("==== No variables ====") ;
            pw.flush() ;
            return ;
        }

     	if ( all == null )
    		all = new QueryResultsMem(queryResults) ;

        if ( colWidths == null )
            colWidths() ;

        String row[] = new String[numCols] ;
        int lineWidth = 0 ;
        for ( int col = 0 ; col < numCols ; col++ )
        {
            String rVar = (String)queryResults.getResultVars().get(col) ;
            row[col] = rVar ;
            lineWidth += colWidths[col] ;
            if ( col > 0 )
                lineWidth += colSep.length() ;
        }
        printRow(pw, row, colSep, lineEnd) ;

        for ( int i = 0 ; i < lineWidth ; i++ )
            pw.print('=') ;
        pw.println() ;

        for ( Iterator tableIter = all ; tableIter.hasNext() ; )
        {
            ResultBinding env = (ResultBinding)tableIter.next() ;
            for ( int col = 0 ; col < numCols ; col++ )
            {
                String rVar = (String)queryResults.getResultVars().get(col) ;
                Value val = env.getValue(rVar) ;
                String s = (val==null)? notThere : val.asQuotedString() ;
                row[col] = s ;
            }
            printRow(pw, row, colSep, lineEnd) ;
        }
        all.reset() ;
        pw.flush() ;
    }


    private void printRow(PrintWriter pw, String[] row, String colSep, String lineEnd)
    {
        if ( row.length != numCols )
            throw new RDQL_InternalErrorException("QueryResultsFormatter.printRow: Row length ("+row.length+") != numCols ("+numCols+")") ;

        for ( int col = 0 ; col < numCols ; col++ )
        {
            String s = row[col] ;
            int pad = colWidths[col] ;
            StringBuffer sbuff = new StringBuffer(120) ;

            if ( col > 0 )
                sbuff.append(colSep) ;

            sbuff.append(s) ;
            for ( int j = 0 ; j < pad-s.length() ; j++ )
                sbuff.append(' ') ;

            pw.print(sbuff) ;
        }
        if ( lineEnd != null )
            pw.print(lineEnd);
        pw.println() ;
    }

    /** HTML representation */

    public void printHTML(PrintWriter pw)
    {
    	if ( all == null )
    	{
    		all = new QueryResultsMem(queryResults) ;
            numRows = all.size() ;
        }

        pw.println("<table>");
        // Column headings
        pw.println("  <tr>");
        for ( int ii = 0 ; ii < queryResults.getResultVars().size() ; ii++ )
        {
            String tmp = (String)queryResults.getResultVars().get(ii) ;
            pw.print("    <th>") ;
            pw.print(tmp) ;
            pw.print("</th>") ;
            pw.println() ;
        }
        pw.println("  </tr>");

        for ( ; all.hasNext() ; )
        {
            pw.println("  <tr>");
            ResultBinding env = (ResultBinding)all.next() ;
            for ( int col = 0 ; col < queryResults.getResultVars().size() ; col++ )
            {
                String rVar = (String)queryResults.getResultVars().get(col) ;
                Value val = env.getValue(rVar) ;
                // Use the unquoted form here - shoudl also XML-escape it.
                String s = (val==null)? notThere : val.toString() ;

                pw.print("    <td>") ;
                pw.print(s) ;
                pw.print("</td>") ;
                pw.println() ;
            }
            pw.println("  </tr>");
        }
        pw.println("</table>");
        pw.flush() ;
        all.reset() ;
    }

    /** This operation faithfully walks the results but does nothing with them.
     *  Used in timing operations.  Be careful that a compiler does
     *  not optimize some or all of it away!
     */

    public void consume()
    {
        //numCols = queryResults.getResultVars().size() ;
        for ( Iterator rowIter = queryResults ; rowIter.hasNext() ; )
        {
            ResultBinding result = (ResultBinding)rowIter.next() ;

            for ( ResultBinding.ResultBindingIterator iter = result.iterator() ; iter.hasNext() ; )
            {
                iter.next() ;
                String rVar = iter.varName() ;
                Value val = iter.value() ;
                //String valStr = (val==null) ? null : val.toString() ;
            }
        }
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
