/*
 *  (c)     Copyright 2000, 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id$
 */

package com.hp.hpl.jena.xmloutput.impl;

import com.hp.hpl.jena.xmloutput.RDFXMLWriterI;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.apache.xerces.util.XMLChar;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileUtils;
//import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RSS;
import com.hp.hpl.jena.vocabulary.VCARD;
import com.hp.hpl.jena.vocabulary.RDFSyntax;
import com.hp.hpl.jena.vocabulary.DAML_OIL;

import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.rdf.arp.URI;
import com.hp.hpl.jena.rdf.arp.MalformedURIException;
import com.hp.hpl.jena.rdf.arp.ARP;

import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;

import java.io.*;
import java.util.*;
import org.apache.xerces.util.EncodingMap;
import org.apache.log4j.Logger;

/** 
 * This is not part of the public API.
 * Base class for XML serializers.
 * All methods with side-effects should be synchronized in this class and its
 * subclasses. (i. e. XMLWriters assume that the world is not changing around
 * them while they are writing).
 * 
 * Functionality:
 * 
 * <ul>
 * <li>setProperty etc
 * <li>namespace prefixes
 * <li>xmlbase
 * <li>relative URIs
 * <li>encoding issues
 * <li>anonymous node presentation
 * <li>errorHandler
 * </ul>
 *
 * @author  jjc
 * @version   Release='$Name$' Revision='$Revision$' Date='$Date$'
 */
abstract public class BaseXMLWriter implements RDFXMLWriterI {
	/** log4j logger */
	protected static Logger logger = Logger.getLogger(BaseXMLWriter.class);
	static {
		ARP.initEncoding();
	}
    String attributeQuoteChar ="\"";
    String q(String s) {
        return attributeQuoteChar +s + attributeQuoteChar;
    }
    String qq(String s) {
        return q(Util.substituteStandardEntities(s));
    }
	private Relation nameSpaces = new Relation();
	private Map ns;
	static private Set badRDF = new HashSet();
	int count;
	static String RDFNS = RDF.getURI();
	static private Perl5Matcher matcher = new Perl5Matcher();
	static private Pattern jenaNamespace;
	static {
		try {
			jenaNamespace =
				new Perl5Compiler().compile("j\\.([1-9][0-9]*|cook\\.up)");
		} catch (MalformedPatternException e) {
		}
		badRDF.add("RDF");
		badRDF.add("Description");
		badRDF.add("li");
		badRDF.add("about");
		badRDF.add("aboutEach");
		badRDF.add("aboutEachPrefix");
		badRDF.add("ID");
		badRDF.add("nodeID");
		badRDF.add("parseType");
		badRDF.add("datatype");
		badRDF.add("bagID");
		badRDF.add("resource");
	}

	String xmlBase = null;
	boolean longId = false;
	boolean allowBadURIs = false;
	int tab = 2;
	int width = 60;

	HashMap anonMap = new HashMap();
	int anonCount = 0;
	static private RDFDefaultErrorHandler defaultErrorHandler =
		new RDFDefaultErrorHandler();
	RDFErrorHandler errorHandler = defaultErrorHandler;

	//Map nameSpacePrefices = new HashMap();
	Boolean showXmlDeclaration = null;

	void setupMaps() {
		nameSpaces.set11(RDF.getURI(), "rdf");
		nameSpaces.set11(RDFS.getURI(), "rdfs");
		nameSpaces.set11(DC.getURI(), "dc");
		nameSpaces.set11(RSS.getURI(), "rss");
		nameSpaces.set11("http://www.daml.org/2001/03/daml+oil.daml#", "daml");
		nameSpaces.set11(VCARD.getURI(), "vcard");
		nameSpaces.set11("http://www.w3.org/2002/07/owl#", "owl");
	}
	/*
	 * There are two sorts of id's for anonymous resources.  Short id's are the
	 * default, but require a mapping table.  The mapping table means that
	 * serializing a large model could run out of memory.  Long id's require no
	 * mapping table, but are less readable.
	 */

	String anonId(Resource r)  {
		if (longId) {
			return longAnonId(r);
		} else {
			return shortAnonId(r);
		}
	}

	/*
	 * A shortAnonId is computed by maintaining a mapping table from the internal
	 * id's of anon resources.  The short id is the index into the table of the
	 * internal id.
	 */
	private String shortAnonId(Resource r)  {
		String result = (String) anonMap.get(r.getId());
		if (result == null) {
			result = "A" + Integer.toString(anonCount++);
			anonMap.put(r.getId(), result);
		}
		return result;
	}

	/*
	 * A longAnonId is the internal id of the anon resource expressed as a
	 * character string.
	 *
	 * This code makes no assumptions about the characters used in the
	 * implementation of an anon id.  It checks if they are valid namechar
	 * characters and escapes the id if not.
	 */

	private String longAnonId(Resource r)  {
		String rid = r.getId().toString();
		if (XMLChar.isValidNCName(rid)) {
			//System.err.println("OK: "+rid);
			return rid;
		} else {
			return escapedId(rid);
		}
	}
	private Set namespacesNeeded;
	void addNameSpace(String uri) {
		namespacesNeeded.add(uri);
	}
	void workOutNamespaces() {
		if (ns != null)
			return;
		ns = new HashMap();
		Set used = new HashSet(); // prefixes used.
		Iterator it = namespacesNeeded.iterator();

		// Each uri may be set as a system property,
		// if so use those.
		while (it.hasNext()) {
			String uri = (String) it.next();
			String val = Util.getProperty(RDFWriter.NSPREFIXPROPBASE + uri);
			if (val != null && checkPrefix(val) && !used.contains(val)) {
				ns.put(uri, val);
				used.add(val);
			}
		}

		it = namespacesNeeded.iterator();

		// Repeat for those that are not system properties.
		while (it.hasNext()) {
			String uri = (String) it.next();
			if (ns.containsKey(uri))
				continue;
			String val = null;
			Set s = nameSpaces.forward(uri);
			if (s != null) {
				Iterator it2 = s.iterator();
				if (it2.hasNext())
					val = (String) it2.next();
				if (used.contains(val))
					val = null;
			}
			if (val == null) {
                // just in case the prefix has already been used, look for a free one.
                // (the usual source of such prefixes is reading in a model we wrote out earlier)
				do { val = "j." + (count++); } while (used.contains( val ));
			}
			ns.put(uri, val);

			used.add(val);
		}
	}
	private void addNameSpaces(Model model)  {
		NsIterator nsIter = model.listNameSpaces();
		String uri;
		while (nsIter.hasNext()) {
			this.addNameSpace(nsIter.nextNs());
		}
	}

	String xmlnsDecl() {
		workOutNamespaces();
		StringBuffer rslt = new StringBuffer();
		Iterator it = ns.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry) it.next();
			rslt.append("\n    xmlns");
			String prefix = (String) ent.getValue();
			String uri = (String) ent.getKey();
			if (!allowBadURIs)
				try {
					new URI(uri);
				} catch (MalformedURIException e) {
					throw new BadURIException( "", e );
				}
			if (prefix.length() > 0) {
				rslt.append(":" + prefix);
			}
			rslt.append("=" + qq(uri));

		}
		return rslt.toString();
	}

	boolean isDefaultNamespace(String uri) {
		return "".equals(ns.get(uri));
	}
	static final private int FAST = 1;
	static final private int START = 2;
	static final private int END = 3;
	static final private int ATTR = 4;
	static final private int FASTATTR = 5;
	String rdfEl(String local) {
		return tag(RDFNS, local, FAST, true);
	}
	String startElementTag(String uri, String local) {
		return tag(uri, local, START, false);
	}
	String startElementTag(String uriref) {
		return splitTag(uriref, START);

	}
	String attributeTag(String uriref) {
		return splitTag(uriref, ATTR);

	}
	String attributeTag(String uri, String local) {
		return tag(uri, local, ATTR, false);

	}
	String rdfAt(String local) {
		return tag(RDFNS, local, FASTATTR, true);

	}
	String endElementTag(String uri, String local) {
		return tag(uri, local, END, false);
	}
	String endElementTag(String uriref) {
		return splitTag(uriref, END);
	}
	String splitTag(String uriref, int type) {
		int split = Util.splitNamespace(uriref);
		if (split == uriref.length())
			throw new InvalidPropertyURIException( uriref );
		return tag(
			uriref.substring(0, split),
			uriref.substring(split),
			type,
			true);
	}
	static public boolean dbg = false;
    
	String tag(String uri, String local, int type, boolean localIsQname) {
		if (dbg)
			System.err.println(uri + " - " + local);
		String prefix = (String) ns.get(uri);
		if (type != FAST && type != FASTATTR) {
			if ((!localIsQname) && !XMLChar.isValidNCName(local))
				return splitTag(uri + local, type);
			if (uri.equals(RDFNS)) {
				// Description, ID, nodeID, about, aboutEach, aboutEachPrefix, li
				// bagID parseType resource datatype RDF
				if (badRDF.contains(local)) {
					logger.warn(
						"The URI rdf:"
							+ local
							+ " cannot be serialized in RDF/XML.");
					throw new InvalidPropertyURIException( "rdf:" + local );
				}
			}
		}
		boolean cookUp = false;
		if (prefix == null) {
			if (!allowBadURIs)
				try {
					new URI(uri);
				} catch (MalformedURIException e) {
					throw new JenaException(e);
				}
			logger.warn(
				"Internal error: unexpected QName URI: <"
					+ uri
					+ ">.  Fixing up with j.cook.up code.",
				new BrokenException( "unexpected QName URI " + uri ));
			cookUp = true;
		} else if (prefix.length() == 0) {
			if (type == ATTR || type == FASTATTR)
				cookUp = true;
			else
				return local;
		}
		if (cookUp) {
			prefix = "j.cook.up";
			switch (type) {
				case FASTATTR :
				case ATTR :
					return "xmlns:"
						+ prefix
						+ "="
						+ qq(uri)
						+ " "
						+ prefix
						+ ":"
						+ local;
				case START :
					return prefix
						+ ":"
						+ local
						+ " xmlns:"
						+ prefix
						+ "="
						+ qq(uri);
				case END :
					break;
				case FAST :
					logger.fatal("Unreachable code - reached.");
					throw new BrokenException( "cookup reached final FAST" );
			}
		}
		return prefix + ":" + local;
	}

	/*
	String transNS(Property p) {
	    return nsPrefix(p.getNameSpace()) + ":" + p.getLocalName();
	}
	
	String nsPrefix(String ns) {
	    return nsMap.getProperty(ns);
	}
	*/

	public BaseXMLWriter() {
		setupMaps();
		// nsMap will be set up when writing
	}

	/** Write out an XML serialization of a model.
	 * @param model the model to be serialized
	 * @param out the OutputStream to receive the serialization
	 * @param base The URL at which the file will be placed.
	 * @throws IOException if an io error occurs
	 */
	final public void write(Model model, OutputStream out, String base)
		 {
		write(model, FileUtils.asUTF8(out), base);
	}
    
    private void primeNamespace( Model model )
        {
        Map m = model.getNsPrefixMap();
        Iterator it  = m.entrySet().iterator();
        while (it.hasNext())
            {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            String already = this.getPrefixFor( value );
            if (already == null) this.setNsPrefix( key, value );
            }
        }
        
	/** Serialize Model <code>model</code> to Writer <code>out</out>.
	 * @param out The Writer to which the serialization should
	 * be sent.
	 * @param model The model to be written.
	 * @param base the base URI for relative URI calculations.  <code>
	 * null</code> means use only absolute URI's.
	 */
	final synchronized public void write(Model baseModel, Writer out, String base)
		 {
        primeNamespace( baseModel );
		//ns = new HashMap();
        Model model = ModelFactory.withHiddenStatements( baseModel );
		this.namespacesNeeded = new HashSet();
		ns = null;
		count = 0;
		addNameSpace(RDF.getURI());
		addNameSpaces(model);
		PrintWriter pw;
		if (out instanceof PrintWriter) {
			pw = (PrintWriter) out;
		} else {
			pw = new PrintWriter(out);
		}

		if (!Boolean.FALSE.equals(showXmlDeclaration)) {
			String decl = null;
			if (out instanceof OutputStreamWriter) {
				String javaEnc = ((OutputStreamWriter) out).getEncoding();
				// System.err.println(javaEnc);
				if (!(javaEnc.equals("UTF8") || javaEnc.equals("UTF-16"))) {
					//		System.out.println(javaEnc);
					String xEnc = EncodingMap.getJava2IANAMapping(javaEnc);
					if (xEnc == null) {
                        logger.warn("IANA name for Java encoding: "+javaEnc+" is not known. \n"+
                        "   Not including any encoding declaration in the RDF/XML output.\n" +
                        "   It is better to use a FileOutputStream, in place of a FileWriter.");
                    } else {
					   decl = "<?xml version="+q("1.0")+" encoding=" + q(xEnc) + "?>";
                    }
				}
			}
			if (decl == null && showXmlDeclaration != null)
				decl = "<?xml version="+q("1.0")+"?>";
			if (decl != null) {
				pw.println(decl);
			}
		}
		try {
			if (xmlBase == null) {

				baseURI =
					(base == null || base.length() == 0) ? null : new URI(base);
				writeBody(model, pw, base, false);
			} else {
				baseURI = xmlBase.length() == 0 ? null : new URI(xmlBase);
				writeBody(model, pw, xmlBase, true);
			}
		} catch (MalformedURIException e) {
			throw new BadURIException( "", e);
		}
		pw.flush();
	}
	private URI baseURI;
	private boolean checkPrefix(String prefix) {
		if (prefix.equals(""))
			return true;
		if (prefix.toLowerCase().startsWith("xml"))
			logger.warn(
				"Namespace prefix '" + prefix + "' is reserved by XML.");
		else if (!XMLChar.isValidNCName(prefix))
			logger.warn("'" + prefix + "' is not a legal namespace prefix.");
		else if (matcher.matches(prefix, jenaNamespace))
			logger.warn(
				"Namespace prefix '" + prefix + "' is reserved by Jena.");
		else
			return true;
		return false;

	}
	final synchronized public void setNsPrefix(String prefix, String ns) {
		if (checkPrefix(prefix)) {
			nameSpaces.set11(ns, prefix);
		}
	}
    
    final public String getPrefixFor( String uri )
        {
        Set s = nameSpaces.backward( uri );
        if (s != null && s.size() == 1) return (String) s.iterator().next();
        return null; 
        }

	String relativize(String uri) {
		try {
			if (relativeFlags != 0 && baseURI != null)
				return baseURI.relativize(uri, relativeFlags);
			else if (!allowBadURIs)
				new URI(uri);
		} catch (MalformedURIException e) {
			throw new JenaException(e);
		}
		return uri;
	}

	abstract void writeBody(
		Model mdl,
		PrintWriter pw,
		String baseURI,
		boolean inclXMLBase);

	/** Set an error handler.
	 * @param errHandler The new error handler to be used, or null for the
	 * default handler.
	 * @return the old error handler
	 */
	synchronized public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
		// null means no user defined error handler.
		// We implement this using defaultErrorHandler,
		// but hide this fact from the user.
		RDFErrorHandler rslt = errorHandler;
		if (rslt == defaultErrorHandler)
			rslt = null;
		errorHandler = errHandler == null ? defaultErrorHandler : errHandler;
		return rslt;
	}


	static private final char ESCAPE = 'X';
    
	static private String escapedId(String id) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < id.length(); i++) {
			char ch = id.charAt(i);
			if (ch != ESCAPE
				&& (i == 0 ? XMLChar.isNCNameStart(ch) : XMLChar.isNCName(ch))) {
				result.append( ch );
			} else {
				escape( result, ch );
			}
		}
		return result.toString();
	}

    static final char [] hexchar = "0123456789abcdef".toCharArray();
                
	static private void escape( StringBuffer sb, char ch) {
		sb.append( ESCAPE );
		int charcode = ch;
		do {
			sb.append( hexchar[charcode & 15] );
			charcode = charcode >> 4;
		} while (charcode != 0);
		sb.append( ESCAPE );
	}

	
	final synchronized public Object setProperty(
		String propName,
		Object propValue)
		 {
		if (propName.equalsIgnoreCase("showXmlDeclaration")) {
			String oldValue;
			if (showXmlDeclaration == null)
				oldValue = null;
			else
				oldValue = showXmlDeclaration.toString();
			if (propValue == null)
				showXmlDeclaration = null;
			else if (propValue instanceof Boolean)
				showXmlDeclaration = (Boolean) propValue;
			else if (propValue instanceof String) {
				String propValueStr = (String) propValue;
				if (propValueStr.equalsIgnoreCase("default")) {
					showXmlDeclaration = null;
				}
				if (propValueStr.equalsIgnoreCase("true"))
					showXmlDeclaration = Boolean.TRUE;
				else if (propValueStr.equalsIgnoreCase("false"))
					showXmlDeclaration = Boolean.FALSE;
				else
					// Also overloading the error condition.
					throw new BadBooleanException( propValueStr );
			}
			return oldValue;
		} else if (propName.equalsIgnoreCase("xmlbase")) {
			String result = xmlBase;
			xmlBase = (String) propValue;
			return result;
		} else if (propName.equalsIgnoreCase("tab")) {
			Integer result = new Integer(tab);
			if (propValue instanceof Integer) {
				tab = ((Integer) propValue).intValue();
			} else {
				try {
					tab = Integer.parseInt((String) propValue);
				} catch (Exception e) {
					logger.warn(
						"Bad value for tab: '"
							+ propValue
							+ "' ["
							+ e.getMessage()
							+ "]");
				}
			}
			return result;
		} else if (propName.equalsIgnoreCase("width")) {
			Integer result = new Integer(width);
			if (propValue instanceof Integer) {
				width = ((Integer) propValue).intValue();
			} else {
				try {
					width = Integer.parseInt((String) propValue);
				} catch (Exception e) {
					logger.warn(
						"Bad value for width: '"
							+ propValue
							+ "' ["
							+ e.getMessage()
							+ "]");
				}
			}
			return result;
		} else if (propName.equalsIgnoreCase("longid")) {
			Boolean result = new Boolean(longId);
			longId = toboolean(propValue);
			return result;
		} else if (propName.equalsIgnoreCase("attributeQuoteChar")) {
			String result = attributeQuoteChar;
            if ( "\"".equals(propValue) || "'".equals(propValue) )
              attributeQuoteChar = (String)propValue;
            else 
              logger.warn("attributeQutpeChar must be either \"\\\"\" or \', not \""+propValue+"\"" );
			return result;
		} else if (propName.equalsIgnoreCase("allowBadURIs")) {
			Boolean result = new Boolean(allowBadURIs);
			allowBadURIs = toboolean(propValue);
			return result;
		} else if (propName.equalsIgnoreCase("prettyTypes")) {
			return setTypes((Resource[]) propValue);
		} else if (propName.equalsIgnoreCase("relativeURIs")) {
			int old = relativeFlags;
			relativeFlags = URI.str2flags((String) propValue);
			return URI.flags2str(old);
		} else if (propName.equalsIgnoreCase("blockRules")) {
			return setBlockRules(propValue);
		} else {
			logger.warn("Unsupported property: " + propName);
			return null;
		}
	}
	static private boolean toboolean(Object o) {
		if (o instanceof Boolean)
			return ((Boolean) o).booleanValue();
		else
			return Boolean.valueOf((String) o).booleanValue();
	}

	Resource[] setTypes(Resource x[]) {
		logger.warn(
			"prettyTypes is not a property on the Basic RDF/XML writer.");
		return null;
	}
	private Resource blockedRules[] = new Resource[0];
	Resource[] setBlockRules(Object o) {
		Resource rslt[] = blockedRules;
		unblockAll();
		if (o instanceof Resource[]) {
			blockedRules = (Resource[]) o;
		} else {
			StringTokenizer tkn = new StringTokenizer((String) o, ", ");
			Vector v = new Vector();
			while (tkn.hasMoreElements()) {
				String frag = tkn.nextToken();
				//  System.err.println("Blocking " + frag);
				if (frag.equals("daml:collection"))
					v.add(DAML_OIL.collection);
				else
					v.add(new ResourceImpl(RDFSyntax.getURI() + frag));
			}

			blockedRules = new Resource[v.size()];
			v.copyInto(blockedRules);
		}
		for (int i = 0; i < blockedRules.length; i++)
			blockRule(blockedRules[i]);
		return rslt;
	}
	abstract void unblockAll();
	abstract void blockRule(Resource r);
	/*
	private boolean sameDocument = true;
	private boolean network = false;
	private boolean absolute = true;
	private boolean relative = true;
	private boolean parent = true;
	private boolean grandparent = false;
	*/
	private int relativeFlags =
		URI.SAMEDOCUMENT | URI.ABSOLUTE | URI.RELATIVE | URI.PARENT;

}

/*
	(c) Copyright 2000, 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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