/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 
 * * $Id$
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * S.java
 *
 * Created on July 15, 2001, 7:13 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.util.XMLChar;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTag;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagCodes;
import com.hp.hpl.jena.rdf.arp.lang.LanguageTagSyntaxException;

/**
 *
 * @author  jjc
 * 
 */
public class ParserSupport
	implements ARPErrorNumbers,  LanguageTagCodes, Names {
    
    protected void badURI(String uri, URISyntaxException e) throws SAXParseException {
        String msg = e.getMessage();
        if (msg.endsWith(uri)) {
            msg = msg.substring(0,msg.length()-uri.length())+"<"+uri+">";            
        } else {
            msg = "<" + uri + "> " + msg; 
        }
//        URI uri2;
//        uri2.
        warning(WARN_MALFORMED_URI, "Bad URI: " + msg);
    }
    
	protected ParserSupport(XMLHandler arp) {
		this.arp = arp;
	}
    Map idsUsed() {
        return arp.idsUsed;
    }
    protected final XMLHandler arp;
	/**
	 * @param str The fully expanded URI
	 */
	protected void checkIdSymbol(XMLContext ctxt, String str)
		throws SAXParseException {
		if (!arp.ignoring(WARN_REDEFINITION_OF_ID)) {
			Map idsUsedForBase = (Map) idsUsed().get(ctxt.getURI());
			if (idsUsedForBase == null) {
				idsUsedForBase = new HashMap();
				idsUsed().put(ctxt.getURI(), idsUsedForBase);
			}
			Location prev = (Location) idsUsedForBase.get(str);
			if (prev != null) {
				arp.warning(
					WARN_REDEFINITION_OF_ID,
					"Redefinition of ID: " + str);
				arp.warning(
					WARN_REDEFINITION_OF_ID,
					prev,
					"Previous definition of '" + str + "'.");
			} else {
				idsUsedForBase.put(str, arp.location());
			}
		}
		if (!ctxt.isSameAsDocument())
			arp.warning(
				IGN_XMLBASE_SIGNIFICANT,
				"The use of xml:base changes the meaning of ID '"
					+ str
					+ "'.");

		checkXMLName(str);
		checkEncoding(str);
	}
	private void checkXMLName( String str) throws SAXParseException {
		if (!XMLChar.isValidNCName(str)) {
			//   	System.err.println("not name (id): " + str);
			warning(
				WARN_BAD_NAME,
				"Not an XML Name: '" + str + "'");
		}

	}
	protected String checkNodeID(String str) throws SAXParseException {
		if (!XMLChar.isValidNCName(str)) {
			warning(
				WARN_BAD_NAME,
				"Not an XML Name: '" + str + "'");
		}
		return str;
	}
    // TODO: make calls to checkString
	public void checkString(String t) throws SAXParseException {
		if (!CharacterModel.isNormalFormC(t))
			warning(
				WARN_STRING_NOT_NORMAL_FORM_C,
				"String not in Unicode Normal Form C: \"" + t +"\"");
		checkEncoding(t);
		checkComposingChar(t);
	}
    // TODO: make calls to checkComposingChar, when sewing pieces together
	void checkComposingChar(String t) throws SAXParseException {
		if (CharacterModel.startsWithComposingCharacter(t))
			warning(
				WARN_STRING_COMPOSING_CHAR,
				"String is not legal in XML 1.1; starts with composing char: \""
					+ t
					+ "\" (" + ((int)t.charAt(0))+ ")");
	}
    public void checkComposingChar(char ch[], int st, int ln) throws SAXParseException {
        if (ln>0 && CharacterModel.isComposingChar(ch[st]))
            warning(
                WARN_STRING_COMPOSING_CHAR,
                "String is not legal in XML 1.1; starts with composing char: \""
                    + new String(ch,st,ln)
                    + "\" (" + (int)ch[st]+ ")");
    }

	
	public void checkXMLLang(String lang) throws SAXParseException {
		if (lang.equals(""))
			return;
		try {
			LanguageTag tag = new LanguageTag(lang);
			int tagType = tag.tagType();
			if (tagType == LT_ILLEGAL) {
				warning(
					WARN_BAD_XMLLANG,
					tag.errorMessage());
			}
			if ((tagType & LT_UNDETERMINED) == LT_UNDETERMINED) {
				warning(
					WARN_BAD_XMLLANG,
					"Unnecessary use of language tag \"und\" prohibited by RFC3066");
			}
			if ((tagType & LT_IANA_DEPRECATED) == LT_IANA_DEPRECATED) {
				warning(
					WARN_DEPRECATED_XMLLANG,
					"Use of deprecated language tag \"" + lang + "\".");
			}
			if ((tagType & LT_PRIVATE_USE) == LT_PRIVATE_USE) {
				warning(
					IGN_PRIVATE_XMLLANG,
					"Use of (IANA) private language tag \"" + lang + "\".");
			} else if ((tagType & LT_LOCAL_USE) == LT_LOCAL_USE) {
				warning(
					IGN_PRIVATE_XMLLANG,
					"Use of (ISO639-2) local use language tag \""
						+ lang
						+ "\".");
			} else if ((tagType & LT_EXTRA) == LT_EXTRA) {
				warning(
					IGN_PRIVATE_XMLLANG,
					"Use of additional private subtags on language \""
						+ lang
						+ "\".");
			}
		} catch (LanguageTagSyntaxException e) {
			warning(
				WARN_MALFORMED_XMLLANG,
				e.getMessage());
		}
	}

//	void createTriple(ARPResource r, Token p, Object v, String reify)
//		throws ParseException {
//		switch (p.kind) {
//			case E_OTHER :
//			case E_RDF_N :
//				r.setPredicateObject(
//					((ARPQname) p).asURIReference(arp),
//					v,
//					reify);
//				break;
//			case E_LI :
//				r.setLiObject(v, reify);
//				break;
//			default :
//				throw new RuntimeException("Assertion failure in ParserSupport.createTriple");
//		}
//	}

    // TODO: typed literal creation ...
//	ARPDatatypeLiteral createDatatypeLiteral(
//		URIReference dtURI,
//		ARPString dtLex) {
//		return new ARPDatatypeLiteral(dtLex, dtURI);
//	}
	public void checkEncoding(String s) throws SAXParseException {
		if (arp.encodingProblems) {
			for (int i = s.length() - 1; i >= 0; i--) {
				if (s.charAt(i) > 127)
					warning(
						ERR_ENCODING_MISMATCH,
						"Encoding error with non-ascii characters.");
			}
		}
	}
	/*
	   private Map checkNameSpace(StringBuffer b,ARPQname qn,Map ns) {
	       String q = qn.qName;
	       int colon = q.indexOf(':');
	       String prefix = colon==-1?"":q.substring(0,colon);
	       String old = (String)ns.get(prefix);
	       if ( old == null || !old.equals(qn.nameSpace) ) {
	           Map rslt = new HashMap(ns);
	           rslt.put(prefix,qn.nameSpace);
	           if ( prefix.length() == 0 ) {
	               // MUST use \" as delimiter refer to RFC 2396, \' may appear in uri.
	               b.append(" xmlns=\"" + qn.nameSpace + "\"");
	           } else {
	               // MUST use \" as delimiter refer to RFC 2396, \' may appear in uri.
	               b.append(" xmlns:"+prefix+"=\"" + qn.nameSpace + "\"");
	           }
	           return rslt;
	       } else {
	           return ns;
	       }
	   }
	   */
//	private void useNameSpace(Map ns, ARPQname qn) {
//		useNameSpace(ns, qn.prefix(), qn.nameSpace);
//	}
//	private void useNameSpace(Map ns, String prefix, String uri) {
//		ns.put(prefix, uri);
//	}
//	void startLitElement(StringBuffer b, Token t, Map ns) {
//		ARPQname qn = (ARPQname) t;
//		b.append("<" + qn.qName);
//		useNameSpace(ns, qn);
//		return;
//	}
//	private void checkNamespace(Map allNs, String prefix, String uri, Token t)
//		throws ParseException {
//		checkNamespaceURI(uri, t);
//		String ns = (String) allNs.get(prefix);
//		if (ns == null || !ns.equals(uri)) {
//			//	System.err.println(prefix);
//			//	System.err.println(uri);
//			//	System.err.println(ns);
//			//	System.err.println(t);
//			//	Iterator it = allNs.entrySet().iterator();
//			//	while ( it.hasNext() ) {
//			//		Map.Entry e = (Map.Entry)it.next();
//			//		System.out.println(e.getKey().toString() + " = " +
//			//	  e.getValue().toString());
//			//	}
//			arp.parseWarning(
//				ERR_INTERNAL_ERROR,
//				t.location,
//				"Internal namespaces error, please report to jjc@hpl.hp.com.");
//
//		}
//	}
	/**
	 * @param buf Add namespace attrs and then attrs to this buf.
	 * @param attrs The attributes on this element.
	 * @param visiblyUsed The visibly used namespaces on this element.
	 * @param ns The namespaces declared within the parent element
	 *            of the resulting XML Literal
	 * @param allNs The namespaces as in the input document.
	 */
//	Map litAttributes(
//		StringBuffer buf,
//		SortedMap attrs,
//		SortedMap visiblyUsed,
//		Map ns,
//		Map allNs,
//		Token t)
//		throws ParseException {
//		boolean nsIsNew = false;
//		Iterator it = visiblyUsed.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry entry = (Map.Entry) it.next();
//			String prefix = (String) entry.getKey();
//			String uri = (String) entry.getValue();
//			checkNamespace(allNs, prefix, uri, t);
//			if (uri.equals(ns.get(prefix)))
//				continue;
//			if (!nsIsNew) {
//				ns = new HashMap(ns);
//				nsIsNew = true;
//			}
//			ns.put(prefix, uri);
//			String attr = prefix.equals("") ? "xmlns" : "xmlns:" + prefix;
//			buf.append(" " + attr + "=\"" + encodeAttributeText(uri) + "\"");
//		}
//		it = attrs.values().iterator();
//		while (it.hasNext()) {
//			buf.append((String) it.next());
//		}
//		return ns;
//	}
//	Map litNamespace(Token prefix, Token uri, Map ns, Map used) {
//		String urins = ((StrToken) uri).value;
//		String prefixS = ((StrToken) prefix).value;
//		// useNameSpace(used,prefixS,urins); reagle-01 reagle-02
//		Map rslt = new HashMap(ns);
//		rslt.put(prefixS, urins);
//		return rslt;
//	}
//	String litAttrName(Token attr, Map visiblyUsed) {
//		ARPQname qn = (ARPQname) attr;
//		if (!qn.prefix().equals("")) {
//			useNameSpace(visiblyUsed, qn);
//		}
//		return qn.qName;
//	}
//	String litAttribute(Token attr, Token val) {
//		ARPQname qn = (ARPQname) attr;
//		return " "
//			+ qn.qName
//			+ "=\""
//			+ encodeAttributeText(((StrToken) val).value)
//			+ "\"";
//	}
//	void litComment(StringBuffer b, Token comment) {
//		b.append("<!--" + ((StrToken) comment).value + "-->");
//	}
//	void litProcessingInstruction(StringBuffer b, Token pi) {
//		b.append("<?" + ((StrToken) pi).value + "?>");
//	}
//	void endLitElement(StringBuffer b, Token t) {
//		String q = ((ARPQname) t).qName;
//		b.append("</" + q + ">");
//	}
/*
	Map litAttrName(StringBuffer b,Token t,Map ns) {
	    ARPQname qn = (ARPQname)t;
	    Map rslt = checkNameSpace(b,qn,ns);
	    b.append(" " + qn.qName );
	    return ns;
	}
	void litAttrValue(StringBuffer b,Token t) {
	    b.append("=\"" + encodeAttr(((StrToken)t).value) + "\"");
	}
	*/
//	void litText(StringBuffer b, Token t) {
//		b.append(encodeTextNode(((StrToken) t).value));
//	}
	/*
	    private void checkXMLLiteralNameSpace(String uri,String raw) {
	        if ( !uri.equals("") ) {
	            int colon = raw.indexOf(':');
	            String prefix = colon==-1?null:raw.substring(0,colon);
	            String oldUri = (String)xmlLiteralNameSpaces.get(prefix);
	            if (oldUri!=null && oldUri.equals(uri))
	                return;
	            thisDepthXMLLiteralNameSpaces.add(new String[]{prefix,oldUri});
	            xmlLiteralNameSpaces.put(prefix,uri);
	            if ( prefix == null ) {
	                xmlLiteralValue.append(" xmlns");
	            } else {
	                xmlLiteralValue.append(" xmlns:");
	                xmlLiteralValue.append(prefix);
	            }
	            xmlLiteralValue.append("='");
	            xmlLiteralValue.append(encodeAttr(uri));
	            xmlLiteralValue.append('\'');
	        }
	    }
	*/

	// http://www.w3.org/TR/2001/REC-xml-c14n-20010315#ProcessingModel
	/* The string value of the node is modified by replacing all 
	 * ampersands (&) with &amp;, all open angle brackets (<) with 
	 * &lt;, all quotation mark characters with &quot;, and the 
	 * whitespace characters #x9, #xA, and #xD, with character references. 
	 * The character references are written in uppercase hexadecimal 
	 * with no leading zeroes (for example, #xD is represented by the 
	 * character reference &#xD;). 
	 */

	/**
     * whether this is a warning or an error is determined later.
     * @param i
     * @param msg
     */
    protected void warning(int i, String msg) throws SAXParseException {
        arp.warning(i,msg);
    }
    protected boolean isWhite(char ch[], int st, int ln) {
        for (int i=0;i<ln;i++)
            if (! isWhite(ch[st+i]) )
                return false;
         return true;
    }
    protected boolean isWhite(StringBuffer buf) {
        for (int i=buf.length()-1;i>=0;i--)
           if (! isWhite(buf.charAt(i)) )
               return false;
        return true;
    }
    private boolean isWhite(char c) {
        switch (c) {
        case '\n' :
        case '\r' :
        case '\t' :
        case ' ' :
            return true;
        default :
            return false;
    }
    }
    protected void triple(ANode a, ANode b, ANode c) {
        arp.triple(a,b,c);
    }
//    static private String encodeAttributeText(String s) {
//		StringBuffer rslt = null;
//		String replace;
//		char ch;
//		for (int i = 0; i < s.length(); i++) {
//			ch = s.charAt(i);
//			switch (ch) {
//				case '&' :
//					replace = "&amp;";
//					break;
//				case '<' :
//					replace = "&lt;";
//					break;
//				case '"' :
//					replace = "&quot;";
//					break;
//				case 9 :
//					replace = "&#x9;";
//					break;
//				case 0xA :
//					replace = "&#xA;";
//					break;
//				case 0xD :
//					replace = "&#xD;";
//					break;
//				default :
//					replace = null;
//			}
//			if (replace != null) {
//				if (rslt == null) {
//					rslt = new StringBuffer();
//					rslt.append(s.substring(0, i));
//				}
//				rslt.append(replace);
//			} else if (rslt != null) {
//				rslt.append(ch);
//			}
//		}
//		return rslt == null ? s : rslt.toString();
//	}
	// http://www.w3.org/TR/2001/REC-xml-c14n-20010315#ProcessingModel
	/** except all ampersands are replaced by &amp;, all open angle
	  brackets () are replaced by &lt;, all closing angle brackets 
	  (>) are replaced by &gt;, and all #xD characters are replaced 
	  by &#xD;.  
	 */

//	static private String encodeTextNode(String s) {
//		StringBuffer rslt = null;
//		String replace;
//		char ch;
//		for (int i = 0; i < s.length(); i++) {
//			ch = s.charAt(i);
//			switch (ch) {
//				case '&' :
//					replace = "&amp;";
//					break;
//				case '<' :
//					replace = "&lt;";
//					break;
//				case '>' :
//					replace = "&gt;";
//					break;
//				case 0xD :
//					replace = "&#xD;";
//					break;
//				default :
//					replace = null;
//			}
//			if (replace != null) {
//				if (rslt == null) {
//					rslt = new StringBuffer();
//					rslt.append(s.substring(0, i));
//				}
//				rslt.append(replace);
//			} else if (rslt != null) {
//				rslt.append(ch);
//			}
//		}
//		return rslt == null ? s : rslt.toString();
//	}

}
