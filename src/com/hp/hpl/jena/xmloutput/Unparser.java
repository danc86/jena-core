/*
 *  (c)     Copyright Hewlett-Packard Company 2000, 2001, 2002
 *   All rights reserved.
 * [See end of file]
 *  $Id$
 */

package com.hp.hpl.jena.xmloutput;

/*
 * @author Jeremy Carroll
 *
 * Want TODO List
 * - easy efficiency gains in listSubjects() and modelListSubjects()
 *   by removing those subjects that we have already considered.

 *
 *Don't want TODO List - BagIDs - not interesting to me. - Set Default language
 *during first pass.
 *
 *
 *Notes on ID and BagID:
 *Our preferences are follows:
 * for a Stating with an explicit local ID we avoid explicitly
 * constructing the reification, and try and use rule 6.12 with an idAttr.
 * If the Stating is anonymous (e.g. from a BagID use) or non-local then
 * we construct the reification explicitly. If and when we implemnet BagID
 * then anonymous Statings should be deferred in preference to a use of the
 * BagID construct, where plausible.
 *
 *
 * Notes:
 * The following rules are not supported by the current Jena RDF parser:
 *   6.8
 *
 *
 * [6.1] RDF            ::= ['<rdf:RDF>'] obj* ['</rdf:RDF>']
 * [6.2] obj            ::= description | container
 * [6.3] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
 *                        | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
 *                                       propertyElt* '</rdf:Description>'
 *                        | typedNode
 [6.4] container      ::= sequence | bag | alternative
 [6.5] idAboutAttr    ::= idAttr | aboutAttr | aboutEachAttr
 [6.6] idAttr         ::= ' ID="' IDsymbol '"'
 [6.7] aboutAttr      ::= ' about="' URI-reference '"'
[6.8] aboutEachAttr  ::= ' aboutEach="' URI-reference '"'
                        | ' aboutEachPrefix="' string '"'
 [6.9] bagIdAttr      ::= ' bagID="' IDsymbol '"'
 [6.10] propAttr       ::= typeAttr
                          | propName '="' string '"' (with embedded quotes escaped)
 [6.11] typeAttr       ::= ' type="' URI-reference '"'
 [6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
                         | '<' propName idAttr? parseLiteral '>'
                               literal '</' propName '>'
                         | '<' propName idAttr? parseResource '>'
                               propertyElt* '</' propName '>'
                         | '<' propName idRefAttr? bagIdAttr? propAttr* '/>'

 [daml.1 - 6.12 cont.]   | '<' propName idAttr? parseDamlCollection '>'
                            obj*
                            '</' propName '>'
 [daml.2] parseDamlCollection ::= ' parseType="daml:collection"'

 [6.13] typedNode      ::= '<' typeName idAboutAttr? bagIdAttr? propAttr* '/>'
                          | '<' typeName idAboutAttr? bagIdAttr? propAttr* '>'
 *                                           propertyElt* '</' typeName '>'
 * [6.14] propName       ::= Qname
 * [6.15] typeName       ::= Qname
 * [6.16] idRefAttr      ::= idAttr | resourceAttr
 * [6.17] value          ::= obj | string
 * [6.18] resourceAttr   ::= ' resource="' URI-reference '"'
 * [6.19] Qname          ::= [ NSprefix ':' ] name
 * [6.20] URI-reference  ::= string, interpreted per [URI]
 * [6.21] IDsymbol       ::= (any legal XML name symbol)
 * [6.22] name           ::= (any legal XML name symbol)
 * [6.23] NSprefix       ::= (any legal XML namespace prefix)
 * [6.24] string         ::= (any XML text, with "<", ">", and "&" escaped)
 * [6.25] sequence       ::= '<rdf:Seq' idAttr? '>' member* '</rdf:Seq>'
 * | '<rdf:Seq' idAttr? memberAttr* '/>'
 * [6.26] bag            ::= '<rdf:Bag' idAttr? '>' member* '</rdf:Bag>'
 * | '<rdf:Bag' idAttr? memberAttr* '/>'
 * [6.27] alternative    ::= '<rdf:Alt' idAttr? '>' member+ '</rdf:Alt>'
 *                                | '<rdf:Alt' idAttr? memberAttr? '/>'
 * [6.28] member         ::= referencedItem | inlineItem
 * [6.29] referencedItem ::= '<rdf:li' resourceAttr '/>'
 * [6.30] inlineItem     ::= '<rdf:li' '>' value </rdf:li>'
 *                         | '<rdf:li' parseLiteral '>' literal </rdf:li>'
 *                         | '<rdf:li' parseResource '>' propertyElt* </rdf:li>'
 * [6.31] memberAttr     ::= ' rdf:_n="' string '"' (where n is an integer)
 * [6.32] parseLiteral   ::= ' parseType="Literal"'
 * [6.33] parseResource  ::= ' parseType="Resource"'
 * [6.34] literal        ::= (any well-formed XML)
 *
 */
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.Util;
import org.apache.xerces.util.XMLChar;

import java.util.*;
import java.io.*;

/** An Unparser will output a model in the abbreviated syntax.
 ** @version  Release='$Name$' Revision='$Revision$' Date='$Date$'

 */
class Unparser {
	static private Property LI = new PropertyImpl(RDF.getURI(), "li");
	static private Property DESCRIPTION = new PropertyImpl(RDF.getURI(), "Description");
	/** Creates an Unparser for the specified model.
	 * The localName is the URI (typical URL) intended for
	 * the output file. No trailing "#" should be used.
	 * This will control the use of <I>ID</I> or <I>about</I> or <I>resource</I>
	 * on various rules.
	 * @param localName The intended URI of the output file. No trailing "#".
	 * @param m The model.
	 * @param w The output.
	 * @throws RDFException -
	 */
	Unparser(Abbreviated parent, String localName, Model m, PrintWriter w)
		throws RDFException {
		this.localName = localName;
		prettyWriter = parent;
		out = w;
		model = m;
		addTypeNameSpaces();
		objectTable = new HashMap();
		StmtIterator ss = m.listStatements();
		try {
			while (ss.hasNext()) {
				Statement s = ss.next();
				RDFNode rn = s.getObject();
				if (rn instanceof Resource) {
					increaseObjectCount((Resource) rn);
				}
			}
		} finally {
			ss.close();
		}
		/*
		try {
		    allReifiedStatements = new HashSet();
		    ss = m.listReifiedStatements();
		    while (ss.hasNext())
		        allReifiedStatements.add(ss.next());
		}
		finally {
		    ss.close();
		}
		 */

		// Well I think reification in Jena isn't working ....
		// Here's my code.
		try {
			res2statement = new HashMap();
			statement2res = new HashMap();
			ResIterator reified = new FilterResIterator(new Filter() {
				public boolean accept(Object o) {
					Resource r = (Resource) o;
					try {
						return r.hasProperty(RDF.subject)
							&& r.hasProperty(RDF.object)
							&& r.hasProperty(RDF.predicate);
					} catch (RDFException e) {
						throw new RuntimeRDFException(e);
					}
				}
			}, model.listSubjectsWithProperty(RDF.type, RDF.Statement));
			while (reified.hasNext()) {
				Resource r = reified.next();
				try {
					/**
					 *  This block of code assumes that really we
					 *  are dealing with a reification.
					 *  We may, on the contrary, be dealing with a random
					 *  collection of triples that do not make sense.
					 */
					Statement subj = r.getProperty(RDF.subject);
					Statement pred = r.getProperty(RDF.predicate);
					Statement obj = r.getProperty(RDF.object);
					RDFNode nobj = obj.getObject();
					Resource rsubj = (Resource) subj.getObject();
					Resource rpred = (Resource) pred.getObject();

					Property ppred =
						model.createProperty(((Resource) rpred).getURI());

					Statement statement =
						model.createStatement((Resource) rsubj, ppred, nobj);
					res2statement.put(r, statement);
					statement2res.put(statement, r);
				} catch (Exception ignored) {
				}
			}
		} finally {
			ss.close();
		}

	}
	/** Should be called exactly once for each Unparser.
	 * Calling it a second time will have undesired results.
	 * @throws RDFException -
	 */
	void write() throws RDFException {
		prettyWriter.workOutNamespaces();
		wRDF();
		/*
		System.out.print("Coverage = ");
		for (int i=0;i<codeCoverage.length;i++)
		    System.out.print(" c[" + i + "] = " + codeCoverage[i]+ ";");
		System.out.println();
		 **/
	}
	/** Suggest nice prefixes for some of the namespaces.
	 * These will only be used if the namespaces occur in the model.
	 * The suggestions are passed in as an array of pairs { "key", "URI" }.
	 * @param pairs The suggested prefixes as an array of pairs { {"key", "uri" }}.
	 * /
	void useNameSpaceDecl(String[][] pairs) {
		useNameSpaceDecl(new ArrayMap(pairs));
	}
	*/
	/** Suggest nice prefixes for some of the namespaces.
	 * These will only be used if the namespaces occur in the model.
	 * The suggestions are passed in as a <CODE>Map</CODE> from key <CODE>String</CODE>s to URI <CODE>String</CODE>s.
	 * @param nameSpaceDecl The suggested prefixes of the form [key: prefix; value: uri].
	 * /
	void useNameSpaceDeclx(Map nameSpaceDecl) {
		// Paranoia.
		nameSpaceDecl.remove("rdf");
		nameSpaceDecl.remove("RDF");
		nameSpaceDecl.remove("xml");
		nameSpaceDecl.remove("XML");
		// An issue is that some of the prefixes already chosen
		// in nameSpaces may conflict with those in nameSpaceDecl.
		Iterator it = nameSpaceDecl.entrySet().iterator();
		Set problems = new HashSet();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String uri = (String) pair.getValue();
			if (nameSpaces.forward(uri) != null || problems.contains(uri)) {
				String prefix = (String) pair.getKey();
				Set before = nameSpaces.backward(prefix);
				if (before != null) {
					problems.addAll(before);
				}
				nameSpaces.set11(uri, prefix);
				problems.remove(uri);
			}
		}
		it = problems.iterator();
		int genSym = 1;
		while (it.hasNext()) {
			String uri = (String) it.next();
			String prefix;
			do {
				prefix = "RDFNsId" + genSym++;
			} while (nameSpaces.backward(prefix) != null);
			nameSpaces.set(uri, prefix);
		}
	}
	*/
	/** Set a list of types of objects that will be expanded at the
	 *  top-level of the file.
	 *  @param types An array of rdf:Class'es.
	 *
	 */
	void setTopLevelTypes(Resource types[]) {
		pleasingTypes = types;
		pleasingTypeSet = new HashSet(Arrays.asList(types));
	}

	private String xmlBase;
	void setXMLBase(String b) {
		xmlBase = b;
	}
	/*
		private String xmlDeclaration = null;
		void setDeclaration(String b) {
			xmlDeclaration = b;
		}
	*/
	/* THE MORE INTERESTING MEMBER VARIABLES.
	 * Note there are others scattered throughout the file,
	 * but those are only used by one or two methods.
	 */

	final private static String rdfns = RDF.type.getNameSpace();
	final private static Integer one = new Integer(1);

	//private Relation nameSpaces;
	private String localName;
	private Map objectTable; // This is a map from Resource to Integer
	// which indicates how many times each resource
	// occurs as an object of a triple.
	private Model model;
	private PrintWriter out;
	private Set doing = new HashSet(); // Some of the resources that
	// are currently being written.
	private Set doneSet = new HashSet(); // The triples that have been output.
	private Set haveReified = new HashSet(); // Those local resources that are
	// the id's of a reification, used to ensure that anonymous
	// resources are made non-anonymous when reified in certain ways.

	private Resource pleasingTypes[] = null;
	private Set pleasingTypeSet = new HashSet();

	final private Abbreviated prettyWriter;

	private boolean avoidExplicitReification = true;
	// We set this to false as we start giving up on elegance.

	// Reification stuff.

	Map res2statement;
	Map statement2res;

	/* The top-down recursive descent unparser.
	 * The methods starting in w all refer to one of the rules
	 * of the grammar, which they implement.
	 * boolean valued rules first check whether they are applicable
	 * and return false if not. Otherwise they create appropriate output
	 * (using recursive descent) and return true. Note all necessary
	   checks are made before any output or any recursive descent.
	 * The void w- methods just implement the rule, which typically does not
	 * involve any choice.
	 */
	/*
	[6.1] RDF            ::= ['<rdf:RDF>'] obj* ['</rdf:RDF>']
	 */
	private void wRDF() throws RDFException {
		tab();
		/*
		if (xmlDeclaration != null) {
			print(xmlDeclaration);
			tab();
		}
		*/
		print("<");
		print(prettyWriter.rdfEl("RDF"));
		indentPlus();
		printNameSpaceDefn();
		if (xmlBase != null) {
			localName = xmlBase;
			tab();
			print("xml:base=" + quote(xmlBase));
		}
		print(">");
		wObjStar();
		indentMinus();
		tab();
		print("</");
		print(prettyWriter.rdfEl("RDF"));
		print(">");
		tab();
	}
	/**
	 *  All subjects get listed, for top level use only.
	 */
	private void wObjStar() throws RDFException {
		try {
			Iterator rs = listSubjects();
			while (rs.hasNext()) {
				Resource r = (Resource) rs.next();
				increaseObjectCount(r);
				// This forces us to not be anonymous unless
				// we are never an object. See isGenuineAnon().
				wObj(r, true);
			}
			closeAllResIterators();
		} catch (RuntimeRDFException rdfE) {
			throw rdfE.getUnderlyingException();
		}
	}
	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	                       | '<' propName idAttr? parseLiteral '>'
	                             literal '</' propName '>'
	                       | '<' propName idAttr? parseResource '>'
	                             propertyElt* '</' propName '>'
	                       | '<' propName idRefAttr? bagIdAttr? propAttr* '/>'
	[daml.1 - 6.12 cont.]   | '<' propName idAttr? parseDamlCollection '>'
	                          obj*
	                          '</' propName '>'
	[daml.2] parseDamlCollection ::= ' parseType="daml:collection"'
	
	For daml collections we prefer the special syntax otherwise:
	We prefer choice 4 where possible, except in the case where the
	statement is reified and the object is not anonymous in which case
	we use one of the others (e.g. choice 1).
	For embedded XML choice 2 is obligatory.
	For untyped, anonymous resource valued items choice 3 is used.
	Choice 1 is the fall back.
	 */
	private boolean wPropertyElt(WType wt, Property prop, Statement s, RDFNode val)
		throws RDFException {
		return wPropertyEltCompact(wt, prop, s, val) || // choice 4
		wPropertyEltDamlCollection(wt,prop, s, val) || // choice daml.1
		wPropertyEltLiteral(wt,prop, s, val) || // choice 2
		wPropertyEltResource(wt,prop, s, val) || // choice 3
        wPropertyEltDatatype(wt,prop,s,val) ||
		wPropertyEltValue(wt,prop, s, val); // choice 1.
	}
	/* [6.12.4] propertyElt    ::= '<' propName idRefAttr? bagIdAttr? propAttr* '/>'
	 */
	private boolean wPropertyEltCompact(WType wt,
		Property prop,
		Statement s,
		RDFNode val)
		throws RDFException {
		// Conditions
		if (!(val instanceof Resource))
			return false;
		Resource r = (Resource) val;
		if (!(allPropsAreAttr(r) || doing.contains(r)))
			return false;
		// '<' propName '/>'   is 6.12.1 rather than 6.12.4
		// and it becomes an empty string value.
		// Whether this is a mistake or not is debatable.
		// We avoid the construction.
		if ((!hasProperties(r)) && isGenuineAnon(r))
			return false;
		// Write out
		done(s);
		tab();
		print("<");
		wt.wTypeStart(prop);
		indentPlus();
		wIdRefAttrOpt(s, r);
		if (!doing.contains(r)) {
			wBagIdAttrOpt(r);
			wPropAttrAll(r);
		} else if (isGenuineAnon(r)) {
			error("Genuine anon resource in cycle?");
		}
		indentMinus();
		print("/>");
		return true;
	}
	/*
	[6.12.2] propertyElt    ::=  '<' propName idAttr? parseLiteral '>'
	                             literal '</' propName '>'
	 */
	private boolean wPropertyEltLiteral(WType wt,Property prop, Statement s, RDFNode r)
		throws RDFException {
		if (!((r instanceof Literal) && ((Literal) r).getWellFormed())) {
			return false;
		}
		// print out.
		done(s);
		tab();
		print("<");
		wt.wTypeStart(prop);
		wIdAttrReified(s);
		wParseLiteral();
		print(">");
		print(r.toString());
		print("</");
		wt.wTypeEnd(prop);
		print(">");
		return true;
	}
    private boolean wPropertyEltDatatype(WType wt,Property prop, Statement s, RDFNode r)
        throws RDFException {
        if (!((r instanceof Literal) && ((Literal) r).getDatatypeURI()!=null)) {
            return false;
        }
        // print out.
        done(s);
        tab();
        print("<");
        wt.wTypeStart(prop);
        wIdAttrReified(s);
        wDatatype(((Literal) r).getDatatypeURI());
        print(">");
        print(((Literal) r).getLexicalForm());
        print("</");
        wt.wTypeEnd(prop);
        print(">");
        return true;
    }
	/*
	[6.12.3] propertyElt    ::=  '<' propName idAttr? parseResource '>'
	                             propertyElt* '</' propName '>'
	 */
	private boolean wPropertyEltResource(WType wt,Property prop, Statement s, RDFNode r)
		throws RDFException {
		if (r instanceof Literal)
			return false;
		Resource res = (Resource) r;
		if (!isGenuineAnon(res))
			return false;
		if (getType(res) != null)
			return false; // preferred typed node construction.
		// print out.
		done(s);
		tab();
		print("<");
		wt.wTypeStart(prop);
		indentPlus();
		wIdAttrReified(s);
		wParseResource();
		print(">");
		wPropertyEltStar(res);
		indentMinus();
		tab();
		print("</");
		wt.wTypeEnd(prop);
		print(">");
		return true;
	}
	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	 */
	private boolean wPropertyEltValue(WType wt,Property prop, Statement s, RDFNode r)
		throws RDFException {
		return wPropertyEltValueString(wt,prop, s, r)
			|| wPropertyEltValueObj(wt,prop, s, r);
	}
	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	 */
	private boolean wPropertyEltValueString(WType wt,
		Property prop,
		Statement s,
		RDFNode r)
		throws RDFException {
		if (r instanceof Literal) {
			done(s);
			Literal lt = (Literal) r;
			String lang = lt.getLanguage();
			tab();
			print("<");
			wt.wTypeStart(prop);
			wIdAttrReified(s);
			if (lang != null && lang.length() > 0)
				print(" xml:lang=" + quote(lang));
			print(">");
			wValueString(lt);
			print("</");
			wt.wTypeEnd(prop);
			print(">");
			return true;
		} else {
			return false;
		}
	}
	/*
	[6.17.2] value          ::=  string
	 */
	private void wValueString(Literal lt) throws RDFException {
		String val = lt.getString();
		// Personally (jjc), I don't believe this is sufficient.
		// I have cribbed it from the basicWriter (RDFWriter).
		// I get the impression that the String processing rules
		// are a minefield. I might be wrong the XML
		// reference doesn't make it seem so hard ...
		//
		print(Util.substituteStandardEntities(val));
	}

	/*
	[6.12] propertyElt    ::= '<' propName idAttr? '>' value '</' propName '>'
	[6.17.1] value          ::= obj
	 */
	private boolean wPropertyEltValueObj(WType wt,Property prop, Statement s, RDFNode r)
		throws RDFException {
		if (r instanceof Resource) {
			Resource res = (Resource) r;
			done(s);
			tab();
			print("<");
			wt.wTypeStart(prop);
			wIdAttrReified(s);
			print(">");
			tab();
			indentPlus();
			wObj(res, false);
			indentMinus();
			tab();
			print("</");
			wt.wTypeEnd(prop);
			print(">");
			return true;
		} else {
			return false;
		}
	}

	/*
	[daml.1 - 6.12 cont.]   | '<' propName idAttr? parseDamlCollection '>'
	                          obj*
	                          '</' propName '>'
	 */
	private boolean wPropertyEltDamlCollection(WType wt,
		Property prop,
		Statement s,
		RDFNode r)
		throws RDFException {
		boolean daml = true;
		Statement list[][] = getDamlList(r);
		if (list == null) {
			daml = false;
			list = getRDFList(r);
		}
		if (list == null)
			return false;
		// print out.
		done(s);
		// record all done's first - they may impact the
		// way we print the values.
		for (int i = 0; i < list.length; i++) {
			done(list[i][0]);
			done(list[i][1]);
			done(list[i][2]);
		}
		tab();
		print("<");
		wt.wTypeStart(prop);
		indentPlus();
		wIdAttrReified(s);
		if (daml)
			wParseDamlCollection();
		else
			wParseCollection();

		print(">");
		for (int i = 0; i < list.length; i++) {
			wObj((Resource) list[i][0].getObject(), false);
		}
		indentMinus();
		tab();
		print("</");
		wt.wTypeEnd(prop);
		print(">");
		return true;
	}
	// propAttr* with no left over statements.
	private void wPropAttrAll(Resource r) throws RDFException {
		wPropAttrSome(r);
		if (hasProperties(r))
			error("Bad call to wPropAttrAll");
	}
	// propAttr* possibly with left over statements.
	private void wPropAttrSome(Resource r) throws RDFException {
		StmtIterator ss = listProperties(r);
		try {
			Set seen = new HashSet();
			while (ss.hasNext()) {
				Statement s = ss.next();
				RDFNode val = s.getObject();
				if (canBeAttribute(s, seen)) {
					done(s);
					wPropAttr(s.getPredicate(), s.getObject());
				}
			}
		} finally {
			ss.close();
		}
	}
	/*
	[6.2] obj            ::= description | container
	[6.3] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
	                              propertyElt* '</rdf:Description>'
	                       | typedNode
	[6.4] container      ::= sequence | bag | alternative
	We use:
	[6.2a] obj            ::= description | container  | typedNode
	[6.3a] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
	                              propertyElt* '</rdf:Description>'
	 *
	 * This method has got somewhat messy. If we are not at the topLevel we
	 * may choose to not expand a node but just use a
	 *  typedNode ::= '<' typeName idAboutAttr '/>'
	 * rule.
	 * This rules also applies to Bags that we feel unconfortable with,
	 * such as a Bag arising from a BagId rule that we don't handle properly.
	 *
	
	 */
	private boolean wObj(Resource r, boolean topLevel) throws RDFException {
		try {
			doing.add(r);
			Statement typeStatement = getType(r);
			if (typeStatement != null) {
				Resource t = typeStatement.getResource();
				if (!topLevel) {
					if (pleasingTypeSet.contains(t) && (!isGenuineAnon(r))) {
						return wTypedNodeNoProperties(r);
					}
				}
				return wTypedNode(r) || wDescription(r);
			} else
				return wDescription(r);
		} finally {
			doing.remove(r);
		}
	}
	abstract private class WType {
		abstract void wTypeStart(Resource uri);
		abstract void wTypeEnd(Resource uri);
	}
	static private int RDF_HASH = RDF.getURI().length();
	private WType wdesc = new WType() {
		void wTypeStart(Resource u) {
			print(prettyWriter.rdfEl(u.getURI().substring(RDF_HASH)));
		}
		void wTypeEnd(Resource u) {
			print(prettyWriter.rdfEl(u.getURI().substring(RDF_HASH)));
		}
	};
	private WType wtype = new WType() {
		void wTypeStart(Resource u) {
			print(prettyWriter.startElementTag(u.getURI()));
		}
		void wTypeEnd(Resource u) {
			print(prettyWriter.endElementTag(u.getURI()));
		}
	};

	/*
	[6.3a] description    ::= '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<rdf:Description' idAboutAttr? bagIdAttr? propAttr* '>'
	                              propertyElt* '</rdf:Description>'
	 */
	private boolean wDescription(Resource r) throws RDFException {
		return wTypedNodeOrDescription(wdesc,DESCRIPTION, r);
	}
	/*
	[6.13] typedNode      ::= '<' typeName idAboutAttr? bagIdAttr? propAttr* '/>'
	                       | '<' typeName idAboutAttr? bagIdAttr? propAttr* '>'
	                             propertyElt* '</' typeName '>'
	 */
	private boolean wTypedNode(Resource r) throws RDFException {
		Statement st = getType(r);
		if (st == null)
			return false;
		Resource type = st.getResource();
		done(st);
		return wTypedNodeOrDescription(wtype,type, r);
	}
	private boolean wTypedNodeOrDescription(
		WType wt,
		Resource ty,
		Resource r)
		throws RDFException {
		// preparation - look for the li's.
		Vector found = new Vector();
		StmtIterator ss = listProperties(r);
		try {
			int greatest = 0;
			while (ss.hasNext()) {
				Statement s = ss.next();
				int ix = s.getPredicate().getOrdinal();
				if (ix != 0) {
					if (ix > greatest) {
						found.setSize(ix);
					}
					found.set(ix - 1, s);
				}
			}
		} finally {
			ss.close();
		}
		int last = found.indexOf(null);
		List li = last == -1 ? found : found.subList(0, last);

		return wTypedNodeOrDescriptionCompact(wt, ty, r, li)
			|| wTypedNodeOrDescriptionLong(wt, ty, r, li);
	}
	/* [6.13.1] typedNode      ::= '<' typeName idAboutAttr? bagIdAttr? propAttr* '/>'
	 */
	private boolean wTypedNodeOrDescriptionCompact(
	WType wt,
	Resource ty,
		Resource r,
		List li)
		throws RDFException {
		// Conditions
		if ((!li.isEmpty()) || !allPropsAreAttr(r))
			return false;
		// Write out
		tab();
		print("<");
		wt.wTypeStart(ty);
		indentPlus();
		wIdAboutAttrOpt(r);
		wBagIdAttrOpt(r);
		wPropAttrAll(r);
		print("/>");
		indentMinus();
		return true;
	}
	/* [6.13.1] typedNode      ::= '<' typeName idAboutAttr  '/>'
	 */
	private boolean wTypedNodeNoProperties(Resource r) throws RDFException {
		// Conditions
		if (isGenuineAnon(r))
			return false;
		Statement st = getType(r);
		if (st == null)
			return false;
		Resource type = st.getResource();
		done(st);
		// Write out
		tab();
		print("<");
		wtype.wTypeStart(type);
		indentPlus();
		if (hasProperties(r))
			wAboutAttr(r);
		else
			wIdAboutAttrOpt(r);
		print("/>");
		indentMinus();
		return true;
	}
	/*
	[6.13.2] typedNode      ::=  '<' typeName idAboutAttr? bagIdAttr? propAttr* '>'
	                             propertyElt* '</' typeName '>'
	 */
	private boolean wTypedNodeOrDescriptionLong(
	
	WType wt,
	Resource ty,
		Resource r,
		List li)
		throws RDFException {
		Iterator it = li.iterator();
		while (it.hasNext()) {
			done((Statement) it.next());
		}

		tab();
		print("<");
		wt.wTypeStart(ty);
		indentPlus();
		wIdAboutAttrOpt(r);
		wBagIdAttrOpt(r);
		wPropAttrSome(r);
		print(">");
		wLiEltStar(li.iterator());
		wPropertyEltStar(r);
		indentMinus();
		tab();
		print("</");
		wt.wTypeEnd(ty);
		print(">");
		return true;
	}
	private void wPropertyEltStar(Resource r) throws RDFException {
		StmtIterator ss = this.listProperties(r);
		try {
			while (ss.hasNext()) {
				Statement s = ss.next();
				wPropertyElt(wtype,s.getPredicate(), s, s.getObject());
			}
		} finally {
			ss.close();
		}

	}
	private void wLiEltStar(Iterator ss) throws RDFException {
		while (ss.hasNext()) {
			Statement s = (Statement) ss.next();
			wPropertyElt(wdesc, LI, s, s.getObject());
		}
	}
	/*
	[6.5] idAboutAttr    ::= idAttr | aboutAttr | aboutEachAttr
	we use
	[6.5a] idAboutAttr    ::= idAttr | aboutAttr
	 */
	private Set idDone = new HashSet();
	private boolean wIdAboutAttrOpt(Resource r) throws RDFException {
		return wIdAttrOpt(r) || wNodeIDAttr(r) || wAboutAttr(r);
	}
	/**
	 * Returns false if the resource is not genuinely anonymous and cannot
	 * be referred to using an ID.
	 * [6.6] idAttr         ::= ' ID="' IDsymbol '"'
	 */
	private boolean wIdAttrOpt(Resource r) throws RDFException {
		if (isGenuineAnon(r))
			return true; // We have output resource (with nothing).
		if (r.isAnon())
			return false;
		if (isLocalReference(r)) {
			// Try and use the reification rules if they apply.
			// Issue: aren't we just about to list those statements explicitly.
			if (wantReification(r))
				return false;
			// Can be an ID if not already output.
			if (idDone.contains(r)) {
				return false; // We have already output this one.
			} else {
				idDone.add(r);
				print(" ");
				printRdfAt("ID");
				print("=");
				print(quote(getLocalName(r)));
				return true;
			}
		} else {
			return false;
		}
	}
	/*
	[6.7] aboutAttr      ::= ' about="' URI-reference '"'
	 */
	private boolean wAboutAttr(Resource r) throws RDFException {
		print(" ");
		printRdfAt("about");
		print("=");
		wURIreference(r);
		return true;
	}
	private void wURIreference(Resource r) throws RDFException {
		//if (isLocalReference(r)) {
		//	print(quote("#" + getLocalName(r)));
		//} else 
		if (r.getURI().equals(this.localName)) {
			print("''"); // Used particularly in DAML files.
		} else {
			print(quote(r.getURI()));
		}
	}
	/*
	[6.16] idRefAttr      ::= idAttr | resourceAttr
	 */
	private void wIdRefAttrOpt(Statement s, Resource r) throws RDFException {
		if (wantReification(s)) {
			if (!isGenuineAnon(r))
				error("Bad use of wIdRefAttrOpt rule - want both ID and resource");
			wIdAttrReified(s);
		} else if (!isGenuineAnon(r)) {
			wResourceNodeIDAttr(r);
		}
	}
	/*
	[6.6] idAttr         ::= ' ID="' IDsymbol '"'
	 */
	private void wIdAttrReified(Statement s) throws RDFException {
		if (wantReification(s)) {
			Statement reify[] = reification(s);
			Resource res = (Resource) statement2res.get(s);
			idDone.add(res);
			int i;
			for (i = 0; i < reify.length; i++)
				done(reify[i]);
			print(" ");
			printRdfAt("ID");
			print("=");
			print(quote(getLocalName(res)));
			haveReified.add(res);
		}
	}

	/*
	[6.18] resourceAttr   ::= ' resource="' URI-reference '"'
	 */
	private boolean wResourceNodeIDAttr(Resource r) throws RDFException {
		return wNodeIDAttr(r) || wResourceAttr(r);
	}
	/*
	 nodeIDAttr   ::= ' rdf:nodeID="' URI-reference '"'
	 */
	private boolean wNodeIDAttr(Resource r) throws RDFException {
		if (!r.isAnon())
			return false;
		print(" ");
		printRdfAt("nodeID");
		print("='");
		print(prettyWriter.anonId(r));
		print("'");
		return true;
	}
	/*
	[6.18] resourceAttr   ::= ' resource="' URI-reference '"'
	 */
	private boolean wResourceAttr(Resource r) throws RDFException {
		if (r.isAnon())
			return false;
		print(" ");
		printRdfAt("resource");
		print("=");
		wURIreference(r);
		return true;
	}

	// TODO
	private void wBagIdAttrOpt(Resource r) {
		// return;
	}
	int codeCoverage[] = new int[8];
	/*
	 [6.15] typeName       ::= Qname
	 * /
	private void wTypeName(Resource r) throws RDFException {
		wQname(r);
	}
	
	/*
	 [6.19] Qname          ::= [ NSprefix ':' ] name
	 */
	private void wQnameStart(String ns, String local) throws RDFException {
		print(prettyWriter.startElementTag(ns, local));
	}
	private void wQnameEnd(String ns, String local) throws RDFException {
		print(prettyWriter.endElementTag(ns, local));
	}

	/*
	 [6.14] propName       ::= Qname
	private void wPropName(Statement s) throws RDFException {
		wQname(s.getPredicate());
	}
	 * /
	private void wQNameStartx(Resource p) throws RDFException {
		print(prettyWriter.startElementTag(p.getURI()));
	}
	private void wQNameEndx(Resource p) throws RDFException {
		print(prettyWriter.endElementTag(p.getURI()));
	}*/
	private void wQNameAttr(Property p) throws RDFException {
		print(prettyWriter.attributeTag(p.getURI()));
	}
	private void printRdfAt(String s) {
		print(prettyWriter.rdfAt(s));
	}
	/*
	[6.10] propAttr       ::= typeAttr
	                       | propName '="' string '"' (with embedded quotes escaped)
	[6.11] typeAttr       ::= ' type="' URI-reference '"'
	 */
	private void wPropAttr(Property p, RDFNode n) throws RDFException {
		tab();
		if (p.equals(RDF.type))
			wTypeAttr((Resource) n);
		else
			wPropAttrString(p, (Literal) n);
	}
	private void wTypeAttr(Resource r) throws RDFException {
		print(" ");
		printRdfAt("type");
		print("=");
		print(quote(r.getURI()));
	}
	private void wPropAttrString(Property p, Literal l) throws RDFException {
		print(" ");
		wQNameAttr(p);
		print("=" + quote(l.getString()));
	}

	/*
	[daml.2] parseDamlCollection ::= ' parseType="daml:collection"'
	 */
	private void wParseDamlCollection() throws RDFException {
		print(" ");
		printRdfAt("parseType");
		print("='daml:collection'");
	}
	/*
	[List.2] parseCollection ::= ' parseType="Collection"'
	 */
	private void wParseCollection() throws RDFException {
		print(" ");
		printRdfAt("parseType");
		print("='Collection'");
	}
	/*
	[6.32] parseLiteral   ::= ' parseType="Literal"'
	 */
	private void wParseLiteral() throws RDFException {
		print(" ");
		printRdfAt("parseType");
		print("='Literal'");
	}
        private void wDatatype(String dtURI) throws RDFException {
            print(" ");
            printRdfAt("datatype");
            print("=");
            print(quote(dtURI));
        }
	/*
	[6.33] parseResource  ::= ' parseType="Resource"'
	 */
	private void wParseResource() throws RDFException {
		print(" ");
		printRdfAt("parseType");
		print("='Resource'");
	}

	private void printNameSpaceDefn() {
		print(prettyWriter.xmlnsDecl());
		/*
		Iterator it = nameSpaces.iterator();
		Map.Entry ns;.
		while (it.hasNext()) {
			ns = (Map.Entry) it.next();
			tab();
			print("xmlns:" + ns.getValue() + "=" + quote((String) ns.getKey()));
		}
		*/
	}

	/****************************************************************************
	 *  Utility routines ...
	 *
	 ***************************************************************************/

	/***
	 * Output and indentation.
	 ***/
	private int indentLevel = 0;
	private int currentColumn = 0;
	static private String filler(int lgth) {
		char rslt[] = new char[lgth];
		Arrays.fill(rslt, ' ');
		return new String(rslt);
	}
	private void tab() {
		int desiredColumn = 4 * indentLevel;
		if ((desiredColumn == 0 && currentColumn == 0)
			|| desiredColumn > currentColumn) {
			String spaces = filler(desiredColumn - currentColumn);
			out.print(spaces);
		} else {
			out.println();
			out.print(filler(desiredColumn));
		}
		currentColumn = desiredColumn;
	}

	/**
	 *  Quote str with either ' or " quotes to be in attribute position
	 *  in XML.
	    The real rules are found at http://www.w3.org/TR/REC-xml#AVNormalize
	 */
	private String quote(String str) {
		return "'" + Util.substituteStandardEntities(str) + "'";
	}
	/**
	 *  Indentation screws up if there is a tab character in s.
	 *  We do not check this.
	 */
	private void print(String s) {
		out.print(s);
		int ix = s.lastIndexOf('\n');
		if (ix == -1)
			currentColumn += s.length();
		else
			currentColumn = s.length() - ix - 1;
	}
	private void indentPlus() {
		indentLevel++;
	}
	private void indentMinus() {
		indentLevel--;
	}

	/* Unexpected error.
	 */
	private void error(String msg) {
		RuntimeException e =
			new RuntimeException("Internal error in Unparser: " + msg);
		this.prettyWriter.fatalError(e);
		throw e; // Just in case.
	}
	/**
	 * Name space stuff.
	 **/
	private static Map specialPrefixes = new HashMap();
	static {
		specialPrefixes.put(RDF.getURI(), "rdf");
		specialPrefixes.put(RDFS.getURI(), "rdfs");
		specialPrefixes.put(RSS.getURI(), "rss");
		//    specialPrefixes.put("http://www.daml.org/2000/12/daml+oil#","daml");
	}
	/**
	 *  This looks up a URI in my list of favorite prefixes.
	 *  This is not an invertible function, so another stage of
	 *  reasoning is needed to make a bijective function for the
	 *  name space declaration.
	 */
	static private String specialPrefix(String uri) {
		String rslt = (String) specialPrefixes.get(uri);
		if (rslt == null
			&& uri.startsWith("http://www.daml.org/")
			&& uri.endsWith("daml+oil#"))
			return "daml";
		return rslt;
	}
	/*
	private int genSym;
	
	private void addNameSpace(String uri, Relation nsv) {
		String prefix = specialPrefix(uri);
		if (nsv.backward(prefix) != null) {
			prefix = null;
		}
		if (prefix == null) {
			if (nsv.forward(uri) != null)
				return; // Already in there.
			prefix = "RDFNsId" + genSym++;
		}
		nsv.set(uri, prefix);
	}
	
	*/
	private void addTypeNameSpaces() {
		NodeIterator nn = model.listObjectsOfProperty(RDF.type);
		try {
			while (nn.hasNext()) {
				RDFNode obj = nn.next();
				int split = isOKType(obj);
				if (split != -1)
					prettyWriter.addNameSpace(
						((Resource) obj).getURI().substring(0, split));
			}
		} finally {
			nn.close();
		}
	}
	/*
	private String nameSpaceAbbreviation(String ns) {
		Set rslt = (Set) nameSpaces.forward(ns);
		if (rslt == null || rslt.isEmpty()) {
			/*
			new Exception("foo").printStackTrace();
			System.err.println("Please e-mail the above stack trace to jjc@hplb.hpl.hp.com");
			System.err.println("There may be a heisenbug related to this condition.");
			System.err.println("It is hard to reproduce.");
			* /
			error("Name space failure: " + ns);
		}
		return (String) rslt.iterator().next();
	}
	*/
	private String getNameSpace(Resource r) {
		if (r.isAnon()) {
			Log.severe(
				"Internal error - giving up",
				"Unparser",
				"getNameSpace");
			throw new RuntimeException("Internal error");
		} else {
			String uri = r.getURI();
			int split = Util.splitNamespace(uri);
			return uri.substring(0, split);
		}
	}

	/**
	 *  Local and/or anonymous resources.
	 **/
	private boolean isGenuineAnon(Resource r) {
		if (!r.isAnon())
			return false;
		Integer v = (Integer) objectTable.get(r);
		return v == null || (v.intValue() <= 1 && (!haveReified.contains(r)));
	}

	private boolean isLocalReference(Statement s) {
		return false;
	}

	private boolean isLocalReference(Resource r) throws RDFException {
		return (!r.isAnon())
			&& getNameSpace(r).equals(localName + "#")
			&& XMLChar.isValidNCName(getLocalName(r));
	}
	/*
	 * Utility for turning an integer into an alphabetic string.
	 */
	private static String getSuffix(int suffixId) {
		if (suffixId == 0)
			return "";
		else {
			suffixId--;
			int more = (suffixId / 26);

			return getSuffix(more)
				+ new Character((char) ('a' + suffixId % 26));
		}
	}
	//private Map localNameMap = new HashMap();
	//private int localId = 1;
	private String getLocalName(Resource r) throws RDFException {
		if (r.isAnon()) {
			Log.severe(
				"Internal error - giving up",
				"Unparser",
				"getLocalName");
			throw new RuntimeException("Internal error");
		} else {
			String uri = r.getURI();
			int split = Util.splitNamespace(uri);
			return uri.substring(split);
		}
	}
	/**
	 *   objectTable initialization.
	 */

	private void increaseObjectCount(Resource r) {
		Integer cnt = (Integer) objectTable.get(r);
		if (cnt == null) {
			cnt = one;
		} else {
			cnt = new Integer(cnt.intValue() + 1);
		}
		objectTable.put(r, cnt);
	}

	/***
	 *  Reification support.
	 ****/
	/*
	  Is the use of ID in rule [6.12] to create a reification helpful or not?
	 */
	private boolean wantReification(Statement s) throws RDFException {
		return wantReification(s, (Resource) statement2res.get(s));
	}
	private boolean wantReification(Resource res) throws RDFException {
		return wantReification((Statement) res2statement.get(res), res);
	}
	private boolean wantReification(Statement s, Resource ref)
		throws RDFException {
		if (s == null || ref == null || ref.isAnon())
			return false;
		if (!(isLocalReference(ref) && isLocalReference(s)))
			return false;
		Statement reify[] = reification(s);
		int i;
		for (i = 0; i < reify.length; i++)
			if (doneSet.contains(reify[i]) || (!model.contains(reify[i])))
				return false; // Some of reification already done.
		return true; // Reification rule helps.
	}
	private Statement[] reification(Statement s) throws RDFException {
		Model m = s.getModel();
		Resource r = (Resource) statement2res.get(s);
		return new Statement[] {
			m.createStatement(r, RDF.type, RDF.Statement),
			m.createStatement(r, RDF.subject, s.getSubject()),
			m.createStatement(r, RDF.predicate, s.getPredicate()),
			m.createStatement(r, RDF.object, s.getObject())};
	}

	private boolean hasProperties(Resource r) throws RDFException {
		StmtIterator ss = listProperties(r);
		if (avoidExplicitReification
			&& //  ( r instanceof Statement ) &&
		 (!r.isAnon())
			&& isLocalReference(r)
			&& res2statement.containsKey(r)) {
			ss = new FilterStmtIterator(new Filter() {
				public boolean accept(Object o) {
					Statement s = (Statement) o;
					Property p = s.getPredicate();
					String local = p.getLocalName();
					return (!p.getNameSpace().equals(rdfns))
						|| !((RDF.type.equals(p)
							&& s.getObject().equals(RDF.Statement))
							|| RDF.object.equals(p)
							|| RDF.predicate.equals(p)
							|| RDF.subject.equals(p));
				}
			}, ss);
		}
		try {
			return ss.hasNext();
		} finally {
			ss.close();
		}
	}
	private StmtIterator listProperties(Resource r) throws RDFException {
		return new FilterStmtIterator(new Filter() {
			public boolean accept(Object o) {
				return !doneSet.contains(o);
			}
		}, r.listProperties());
	}
	// Good type statement, or simple string valued statement with no langID
	// See http://www.w3.org/TR/REC-xml#AVNormalize
	private boolean canBeAttribute(Statement s, Set seen) throws RDFException {
		Property p = s.getPredicate();
		// Check seen first.
		if (seen.contains(p)) // We can't use the same attribute
			// twice in one rule.
			return false;
		seen.add(p);

		if (p.equals(RDF.type)) {
			// If we have a model in which a type is given
			// as a string, then we avoid the attribute rule 6.10 which is
			// ambiguous with 6.11.
			RDFNode n = s.getObject();
			return (n instanceof Resource) && !((Resource) n).isAnon();
		}

		if (s.getObject() instanceof Literal) {
			Literal l = s.getLiteral();
            if (l.getDatatypeURI()!=null)
                return false;
			if (l.getLanguage().equals("")) {
				String str = l.getString();
				if (str.length() < 40) {
					char buf[] = str.toCharArray();
					for (int i = 0; i < buf.length; i++) {
						// See http://www.w3.org/TR/REC-xml#AVNormalize
						if (buf[i] <= ' ')
							return false;
						/*  NOT NECESSARY:
						else
						    // See http://www.w3.org/TR/REC-xml#NT-AttValue
						    // It's very liberal, since we will escape
						    // the following problem characters
						    // we can ignore them.
						    switch (buf[i]) {
						        case '"':
						        case '\'':
						        case '&':
						        case '<':
						        case '>':
						            return false;
						    }
						*/
					}
					return !wantReification(s);
				}
			}
		}
		return false;
	}
	private boolean allPropsAreAttr(Resource r) throws RDFException {
		StmtIterator ss = listProperties(r);
		Set seen = new HashSet();
		try {
			while (ss.hasNext()) {
				Statement s = ss.next();
				if (!canBeAttribute(s, seen))
					return false;
			}
		} finally {
			ss.close();
		}
		return true;
	}
	private void done(Statement s) {
		doneSet.add(s);
		// return false;
	}
	/**  If r represent a daml:collection return a 2D array of its statements.
	 *  For each member there are three statements the first gives the DAML.first
	 *  statement, the second the DAML.rest statement and the third the RDF.type
	 *  statement.
	 *  @return null on failure or the elements of the collection.
	 *
	 */
	private Statement[][] getDamlList(RDFNode r) {
		return getList(r, DAML.List, DAML.first, DAML.rest, DAML.nil);
	}
	private Statement[][] getRDFList(RDFNode r) {
		return getList(r, RDF.List, RDF.first, RDF.rest, RDF.nil);
	}

	private Statement[][] getList(
		RDFNode r,
		Resource list,
		Property first,
		Property rest,
		Resource nil) {
		Vector rslt = new Vector();
		Set seen = new HashSet();
		RDFNode next = r;
		// We walk down the list and check each member.
		while (!next.equals(nil)) {
			Statement elt[] = new Statement[3];
			if (next instanceof Literal)
				return null;
			Resource res = (Resource) next;
			// We cannot label the nodes in the daml:collection construction.
			if (!isGenuineAnon(res))
				return null;
			// The occurs check - cyclic loop rather than a list.
			if (seen.contains(next))
				return null;
			seen.add(next);

			// We must have exactly three properties.
			StmtIterator ss = res.listProperties();
			try {
				while (ss.hasNext()) {
					Statement s = ss.next();
					Property p = s.getPredicate();
					int ix;
					RDFNode obj = s.getObject();
					if (doneSet.contains(s))
						return null;
					if (!(obj instanceof Resource)) {
						return null;
					}
					if (p.equals(RDF.type)) {
						ix = 2;
						if (!obj.equals(list))
							return null;
					} else if (p.equals(first)) {
						ix = 0;
					} else if (p.equals(rest)) {
						ix = 1;
						next = obj;
					} else {
						return null;
					}
					if (elt[ix] != null)
						return null;
					elt[ix] = s;
				}
			} finally {
				ss.close();
			}
			for (int i = 0; i < 3; i++)
				if (elt[i] == null) // didn't have the three required elements.
					return null;
			rslt.add(elt);
		}
		if (rslt.size() == 0)
			return null;
		Statement array[][] = new Statement[rslt.size()][];
		rslt.copyInto(array);
		return array;
	}
	/**
	 * @return A statement that is suitable for a typed node construction or null.
	 */
	private Statement getType(Resource r) throws RDFException {
		Statement rslt;
		try {
			if (r instanceof Statement) {
				rslt = ((Statement) r).getStatementProperty(RDF.type);
				if (rslt == null || (!rslt.getObject().equals(RDF.Statement)))
					error("Statement type problem");
			} else {
				rslt = r.getProperty(RDF.type);
			}
		} catch (RDFException rdfe) {
			if (rdfe.getErrorCode() == RDFException.PROPERTYNOTFOUND) {
				if (r instanceof Statement)
					error("Statement type problem");
				rslt = null;
			} else
				throw rdfe;
		}
		if (rslt == null || isOKType(rslt.getObject()) == -1)
			return null;

		return rslt;
	}

	/** @param n The value of some rdf:type (precondition).
	 * @return The split point or -1.
	 */

	private int isOKType(RDFNode n) {

		if (!(n instanceof Resource))
			return -1;
		if (((Resource) n).isAnon())
			return -1;
		// Only allow resources with namespace and fragment ID
		String uri = ((Resource) n).getURI();

		int split = Util.splitNamespace(uri);
		if (split == 0 || split == uri.length())
			return -1;

		return split;
	}

	/**
	 *  The order of outputting the resources.
	 *  This all supports wObjStar.
	 **/
	private Set infinite;
	private void findInfiniteCycles() throws RDFException {
		// find all statements that haven't been done.
		StmtIterator ss = model.listStatements();
		Relation relation = new Relation();
		try {
			while (ss.hasNext()) {
				Statement s = ss.next();
				if (!doneSet.contains(s)) {
					RDFNode rn = s.getObject();
					if (rn instanceof Resource) {
						relation.set(s.getSubject(), rn);
					}
				}
			}
		} finally {
			ss.close();
		}
		relation.transitiveClosure();
		infinite = relation.getDiagonal();
	}
	/**
	 * This class is an iterator over the set infinite, but
	 * we wait until it is used before instantiating the
	 * underlying iterator.
	 */
	private Iterator allInfiniteLeft() {
		return new LateBindingIterator() {
			public Iterator create() {
				return infinite.iterator();
			}
		};
	}

	private Iterator pleasingTypeIterator() throws RDFException {
		if (pleasingTypes == null)
			return new NullIterator();
		Map buckets = new HashMap();
		Set bucketArray[] = new Set[pleasingTypes.length];
		// Set up buckets and bucketArray. Each is a collection
		// of the same buckets, one ordered, the other hashed.
		for (int i = 0; i < pleasingTypes.length; i++) {
			bucketArray[i] = new HashSet();
			buckets.put(pleasingTypes[i], bucketArray[i]);
		}

		ResIterator rs = model.listSubjects();
		try {
			while (rs.hasNext()) {
				Resource r = rs.next();
				Statement s = getType(r);
				if (s != null) {
					Set bucket = (Set) buckets.get(s.getObject());
					if (bucket != null) {
						if (isGenuineAnon(r)) {
							Integer v = (Integer) objectTable.get(r);
							if (v != null && v.intValue() == 1)
								continue;
						}
						bucket.add(r);
					}
				}
			}
		} finally {
			rs.close();
		}

		// Now all the pleasing resources are in the buckets.
		// Add all their iterators togethor:

		return new IteratorIterator(new Map1Iterator(new Map1() {
			public Object map1(Object bkt) {
				return ((Set) bkt).iterator();
			}
		}, new ArrayIterator(bucketArray)));

	}
	/**
	 *  listSubjects  - generates a list of subjects for the wObjStar rule.
	 *  We wish to order these elegantly.
	 *  The current implementation goes for:
	 * <ul>
	 * <li> The current file - mainly intended for good DAML.
	 * <li> Subjects that are not objects of anything.
	 * <li> At these stage we evaluate a dependency graph of the remaining resources.
	 * <li>non-anonymous resources that are the object of more than one
	 * rule that are in infinite cycles.
	 * <li> any non genuinely anonymous resources that are in infinite cycles
	 * <li>any other resource in an infinite cyle
	 * <li>any other resource.
	 *</ul>
	 *
	 *
	 * At the end, we need to close any underlying ResIterators from the model,
	 * however to avoid complications in much of this code we use general
	 * java.util.Iterator-s. We hence use a wrapper around a ResIterator to
	 * allow us to manage the closing issue.
	 */
	private Iterator listSubjects() throws RDFException {
		//  The current file - mainly intended for good DAML.
		Iterator currentFile =
			new ArrayIterator(
				new Resource[] { model.createResource(this.localName)});
		// The pleasing types
		Iterator pleasing = pleasingTypeIterator();

		Iterator fakeStopPleasing = new NullIterator() {
			public boolean hasNext() {
				pleasingTypeSet = new HashSet();
				return false;
			}
		};

		// Subjects that are not objects of anything.
		Iterator nonObjects = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				return !objectTable.containsKey(o);
			}
		}, modelListSubjects());
		// At these stage we evaluate a dependency graph of the remaining resources.
		// This is stuck in the master iterator so that it's hasNext is called
		// at an appropriate time (after the earlier stages, before the later stages).
		// We use this to trigger the dependency graph evalaution.
		Iterator fakeLazyEvaluator = new NullIterator() {
			public boolean hasNext() {
					// Evalaute dependency graph.
	try {
					findInfiniteCycles();
				} catch (RDFException e) {
					throw new RuntimeRDFException(e);
				}
				return false;
			}
		};
		// non-anonymous resources that are the object of more than one
		// triple that are in infinite cycles.
		Iterator firstChoiceCyclic = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				Resource r = (Resource) o;
				codeCoverage[4]++;
				if (r.isAnon())
					return false;
				Integer cnt = (Integer) objectTable.get(r);
				if (cnt == null || cnt.intValue() <= 1)
					return false;
				return true;
			}
		}, this.allInfiniteLeft());
		// any non genuinely anonymous resources that are in infinite cycles
		Iterator nonAnonInfinite = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				codeCoverage[5]++;
				Resource r = (Resource) o;
				return !isGenuineAnon(r);
			}
		}, allInfiniteLeft());
		// any other resource in an infinite cyle
		Iterator infinite = allInfiniteLeft();
		Iterator anotherFake = new NullIterator() {
			public boolean hasNext() {
				avoidExplicitReification = false;
				return false;
			}
		};
		Iterator reifications = new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				codeCoverage[6]++;
				return o instanceof Statement;
			}
		}, allInfiniteLeft());
		// any other resource.
		Iterator backStop = modelListSubjects();

		Iterator all[] =
			new Iterator[] {
				currentFile,
				pleasing,
				fakeStopPleasing,
				nonObjects,
				fakeLazyEvaluator,
				firstChoiceCyclic,
				nonAnonInfinite,
				infinite,
				anotherFake,
				reifications,
				new NullIterator() {
					public boolean hasNext() { if (
						modelListSubjects()
						.hasNext())
						codeCoverage[7]++;
					return false;
				}
			}, backStop };
		Iterator allAsOne = new IteratorIterator(new ArrayIterator(all));

		// Filter for those that still have something to list.
		return new FilterIterator(new Filter() {
			public boolean accept(Object o) {
				try {
					return hasProperties((Resource) o);
				} catch (RDFException e) {
					throw new RuntimeRDFException(e);
				}
			}
		}, allAsOne);
	}
	private Set openResIterators = new HashSet();
	private synchronized void close(ResIterator resIt) throws RDFException {
		resIt.close();
		openResIterators.remove(resIt);
	}
	private synchronized void closeAllResIterators() throws RDFException {
		Iterator members = openResIterators.iterator();
		while (members.hasNext()) {
			((ResIterator) members.next()).close();
		}
		openResIterators = new HashSet();
	}
	private class SubjectIterator implements Iterator {
		private ResIterator resIt;
		private boolean dead = false;
		SubjectIterator(ResIterator resItx) {
			resIt = resItx;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
		public Object next() {
			if (dead)
				throw new NoSuchElementException();
			try {
				return resIt.next();
			} catch (RDFException ee) {
				throw new RuntimeRDFException(ee);
			}
		}
		public boolean hasNext() {
			if (dead)
				return false;
			boolean rslt;
			try {
				rslt = resIt.hasNext();
				if (!rslt) {
					dead = true;
					close(resIt);
				}
				return rslt;
			} catch (RDFException ee) {
				throw new RuntimeRDFException(ee);
			}
		}
	}
	private Iterator modelListSubjects() {
		try {
			ResIterator resIt = model.listSubjects();
			openResIterators.add(resIt);
			return new SubjectIterator(resIt);
		} catch (RDFException ee) {
			throw new RuntimeRDFException(ee);
		}
	}
	/** TESTING
	static public void main(String args[]) throws RDFException, IOException{
	    //Model mdl  = new com.hp.hpl.jena.mem.ModelMem();
	   // new Regression().test21(new ModelMem(),new ModelMem());
	
	    for (int i=0; i< 4; i++) {
	        System.out.print(i);
	        for (int j=0; j<5;j++) {
	            new Regression().test21(new ModelMem(),new ModelMem());
	            System.out.print("+"); System.out.flush();
	        }
	        System.out.println();
	    }
	
	    System.out.println("Done");
	}
	 */

}

/*
    (c) Copyright Hewlett-Packard Company 200,2001, 2002
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