/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id$
*/
package com.hp.hpl.jena.graph.query.regexptrees;

import java.util.*;

/**
     Parse Perl5 patterns into RegexpTree structures, or throw an exception for
     cases that haven't been implemented.
     
 	@author hedgehog
*/
public class PerlPatternParser
    {
    /**
         The string being parsed, as supplied to the constructor(s).
    */
    final protected String toParse;
    
    /**
         The index into the string of the next undealt-with character, ie, it starts at 0.
    */
    protected int pointer;
    
    /**
         The length of the string to parse, used as a limit.
    */
    protected int limit;
    
    /**
         The generator for the RegexpTree nodes to be used in the parse.
    */
    protected RegexpTreeGenerator generator;
    
    /**
         Initialise this parser with the string to parse and with the default
         generator (SimpleGenerator).
    */
    public PerlPatternParser( String toParse )
        { this( toParse, new SimpleGenerator() ); }
    
    /**
         Initialise this parser with the string to parse and with the generator to
         use for node construction.
    */
    public PerlPatternParser( String toParse, RegexpTreeGenerator gen )
        { this.toParse = toParse; 
        this.limit = toParse.length();
        this.generator = gen; }
    
    /**
        Answer the result of parsing the given string as a sequence of alternatives.
    */
    public static RegexpTree parse( String string )
        { return new PerlPatternParser( string ) .parseAlts(); }
    
    /**
        Answer the result of parsing the given string as a sequence of alternatives,
        using the supplied generator for the pattern nodes.
    */    
    public static RegexpTree parse( String string, RegexpTreeGenerator gen )
        { return new PerlPatternParser( string, gen ) .parseAlts(); }
    
    /**
         Exception thrown if a syntax error is detected. Further details are in the
         error message - it doesn't seem worth worrying about having different
         classes for different errors. Possibly this should be a non-static class so
         that it can get at the current context?
    */
    public static class SyntaxException extends RuntimeException
        {
        public SyntaxException( String message )
            { super( message ); }
        }
    
    /**
         Answer the string that this parser is parsing.
    */
    public String getString()
        { return toParse; }
    
    /**
         Answer the current index into the parse string.
    */
    public int getPointer()
        { return pointer; }
    
    /**
         Parse a single atom and return the tree for it, advancing the pointer. This
         does not deal with quantifiers, for which see parseQuantifier. Unmatched
         right parentheses, unexpected (hence unbound) quantifiers, and those things
         that aren't implemented, throw exceptions. An empty atom is permitted
         (at the end of a string or before a |).
    */
    public RegexpTree parseAtom()
        {
        if (pointer < limit)
            {
            char ch = toParse.charAt( pointer++ );
            switch (ch)
                {
                case '.':   return generator.getAnySingle();
                case '^':   return generator.getStartOfLine();
                case '$':   return generator.getEndOfLine();
                case '|':   pointer -= 1; return generator.getNothing();
                case ')':   throw new PerlPatternParser.SyntaxException( "unmatched bracket " + ch );
                case '(':   throw new PerlPatternParser.SyntaxException( "can't do (E) yet" );
                case '[':   throw new PerlPatternParser.SyntaxException( "can't do [C] yet" );
                case '\\':  throw new PerlPatternParser.SyntaxException( "can't do \\ yet" );
                case '*':
                case '+':
                case '?':
                case '{': throw new PerlPatternParser.SyntaxException( "unbound quantifier " + ch );
                case ']':
                case '}':
                default: return generator.getText( ch );       
                }
            }
        return generator.getNothing();
        }
    
    /**
         Parse any quantifier and answer the quantified version of the argument
         tree <code>d</code>. TODO: handle non-greedy quantifiers. (These will
         currently generate syntax errors when their flagging ? is encountered by
         parseAtom.)
    */
    public RegexpTree parseQuantifier( RegexpTree d )
        {
        if (pointer < limit)
            {
            char ch = toParse.charAt( pointer );
            switch (ch)
                {
                case '*':
                    pointer += 1;
                    return generator.getZeroOrMore( d );
                    
                case '+':
                    pointer += 1;
                    return generator.getOneOrMore( d );
                    
                case '?':
                    pointer += 1;
                    return generator.getOptional( d );
                    
                case '{':
                    throw new SyntaxException( "numeric quantifiers not done yet" );
                }
            }
        return d;
        }
    
    /**
         Parse an element (an atom and any following quantifier) and answer the
         possibly-quantified tree.
    */
    public RegexpTree parseElement()
        { return parseQuantifier( parseAtom() ); }

    /**
    	Parse a sequence of elements [possibly-quantified atoms] and answer the
        sequence (singular sequences may be reduced to its single element).
    */
    public RegexpTree parseSeq()
        {
        List operands = new ArrayList();
        while (true)
            {
            RegexpTree next = parseElement();
            if (next.equals( generator.getNothing() ) ) break;
            operands.add( next );
            }
        return generator.getSequence( operands );
        }

    /**
         Parse an alternation of sequences and answer an alternative tree (or the
         single component if there is just one alternative).
    */
    public RegexpTree parseAlts()
        {
        List operands = new ArrayList();
        while (true)
            {
            operands.add( parseSeq() );
            if (pointer < limit && toParse.charAt( pointer ) == '|') pointer += 1;
            else break;
            }
        return generator.getAlternatives( operands );
        }
    }

/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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