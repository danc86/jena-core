/*
 (c) Copyright 2003-2005 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id$
 */
package com.hp.hpl.jena.ontology.tidy.impl;

import java.util.*;

import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.ontology.tidy.errors.*;

/**
 * 
 * This class looks at particular triples and tries to work out what went wrong,
 * giving a specific anaylsis.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll </a>
 *  
 */
public class MonotonicErrorAnalyzer implements Constants {
    
    // TODO MAX_SINGLETON_SET in Grammar is wrong.
    
    final static private int BLANK_PROP = 1;

    final static private int LITERAL_PROP = 2;

    final static private int LITERAL_SUBJ = 3;

    final static private int BADID_USE = 4;

    final static private int BUILTIN_NON_PRED = 5;

    final static private int BUILTIN_NON_SUBJ = 6;

    final static private int TYPE_NEEDS_ID = 10;

    final static private int TYPE_NEEDS_BLANK = 11;

    final static private int TYPE_NEEDS_ID_OR_BLANK = 12;

    final static private int TYPE_OF_CLASS_ONLY_ID = 13;

    final static private int TYPE_OF_PROPERTY_ONLY_ID = 14;

    final static private int TYPE_FOR_BUILTIN = 15;

    final static private int ST_DOMAIN_RANGE = 20;

    final static int BAD_DOMAIN = ST_DOMAIN_RANGE + 1;

    final static int BAD_RANGE = ST_DOMAIN_RANGE + 2;

    final static int BAD_DOMAIN_RANGE = ST_DOMAIN_RANGE + 3;

    final static private int DIFFERENT_CATS = 30;

    final static private int DIFFERENT_CATS_S = DIFFERENT_CATS + 1;

    final static private int DIFFERENT_CATS_P = DIFFERENT_CATS + 2;

    final static private int DIFFERENT_CATS_SP = DIFFERENT_CATS + 3;

    final static private int DIFFERENT_CATS_O = DIFFERENT_CATS + 4;

    final static private int DIFFERENT_CATS_SO = DIFFERENT_CATS + 5;

    final static private int DIFFERENT_CATS_PO = DIFFERENT_CATS + 6;

    final static private int DIFFERENT_CATS_SPO = DIFFERENT_CATS + 7;

    final static private int DC_DOM_RANGE = 40;

    final static private int INCOMPATIBLE_S = DC_DOM_RANGE + 1;

    final static private int INCOMPATIBLE_P = DC_DOM_RANGE + 2;

    final static private int INCOMPATIBLE_SP = DC_DOM_RANGE + 3;

    final static private int INCOMPATIBLE_O = DC_DOM_RANGE + 4;

    final static private int INCOMPATIBLE_SO = DC_DOM_RANGE + 5;

    final static private int INCOMPATIBLE_PO = DC_DOM_RANGE + 6;

    final static private int INCOMPATIBLE_SPO = DC_DOM_RANGE + 7;

    static private LookupTable look = (LookupTable) LookupTable.get();

    static private final int SZ = CategorySet.unsorted.size();

    MonotonicProblem rslt;
    
    public MonotonicProblem getErrorCode(int s, int p, int o, int sx, int px, int ox) {
        //  Throws array access error if not 0 <= s,p,o,sx,px,ox < SZ.
        if (look.qrefine(s, p, o) != Failure)
            throw new IllegalArgumentException("triple is not in error");
        rslt = null;
        int key = look.qrefine(sx, px, ox);
        if (key == Failure) {
            return singleTripleError(sx, px, ox);
        }
        int given[] = { s, p, o };
        int general[] = { look.subject(sx, key), look.prop(px, key),
                look.object(ox, key) };
        for (int i = 0; i < 3; i++) {
            if (look.meet(general[i], given[i]) == Failure) {
                IncompatibleUsageProblem r = new IncompatibleUsageProblem(i);
                catMiss(r,general[i], given[i]);
                addResult(r);
            }
        }
        if (rslt == null) {
            return difficultCase(given, general);
        }
        return rslt;

    }

    static private int spo(int f, int old, int key) {
        switch (f) {
        case 0:
            return look.subject(old, key);

        case 1:
            return look.prop(old, key);

        case 2:
            return look.object(old, key);
        }
        throw new BrokenException("Illegal argument to spo: " + f);
    }


    private MonotonicProblem difficultCase(int given[], int general[]) {

        int keyWithIGeneral[] = { look.qrefine(general[0], given[1], given[2]),
                look.qrefine(given[0], general[1], given[2]),
                look.qrefine(given[0], given[1], general[2]) };

        int keyWithIGiven[] = { look.qrefine(given[0], general[1], general[2]),
                look.qrefine(general[0], given[1], general[2]),
                look.qrefine(general[0], general[1], given[2]) };

        int cats[][] = new int[3][];
        int failures = 0;

        for (int i = 0; i < 3; i++)
            cats[i] = nonPseudoCats(given[i]);

        for (int i = 0; i < 3; i++) {
            if (keyWithIGiven[i] == Failure)
                throw new BrokenException("logic error");
        }
        for (int i = 0; i < 3; i++) {
            if (keyWithIGeneral[i] == Failure) {
                // the two that are not i have conflicting given values
                failures |= (1 << i);
                ComplexIncompatibleUsageProblem r = new ComplexIncompatibleUsageProblem(i);
                computeGivenWanted(r,i, general, keyWithIGiven, cats);
                addResult(r);
            }
        }
        if (rslt==null)
            throw new BrokenException("logic error");
            
        return rslt;
    }

    /**
     * 
     * @param unchanged
     *            The other two keys conflict with one another
     * @param general
     *            The categories given by this triple alone
     * @param keyWithIGiven
     *            The result of qrefine with i being given by the other triples,
     *            the other two from this triple alone
     * 
     * @param givenCats
     *            The categories from the other triples
     */
    private static void computeGivenWanted(
            ComplexIncompatibleUsageProblem r,
            int unchanged, int[] general,
            int[] keyWithIGiven, int[][] givenCats) {
        int ix = 0;
        for (int i = 0; i < 3; i++)
            if (i != unchanged) {
                int want = spo(i, general[i], keyWithIGiven[3 - i - unchanged]);
                int wantC[] = nonPseudoCats(want);
                CategorySetNames.symmetricNames(r.wantedGiven(i),wantC, givenCats[i]);

            }

    }

    public static boolean isLiteral(int o) {
        return o == Grammar.literal || o == Grammar.liteInteger
                || o == Grammar.dlInteger || o == Grammar.userTypedLiteral;
    }

    private void addResult(MonotonicProblem p) {
        if (rslt==null)
            rslt = p;
        else
            rslt.addNext(p);
    }
    private MonotonicProblem singleTripleError(int sx, int px, int ox) {
        if (isBlank(px))
            addResult(new BlankPropertyProblem());
        if (isLiteral(px))
            addResult(new LiteralPropertyProblem());
        if (isLiteral(sx))
            addResult(new LiteralSubjectProblem());
        
        if (sx == Grammar.badID)
            addResult(new BadURIProblem("%s"));
        if (px == Grammar.badID)
            addResult(new BadURIProblem("%p"));
        if (ox == Grammar.badID)
            addResult(new BadURIProblem("%o"));
            
        if (rslt != null)
            return rslt;
        
        if (!look.canBeSubj(sx)) {
            addResult(new IllegalSubjectProblem());
        }

        // The intent here is that we are only testing for built-in
        // non-predicates, but a few cases are both 'built-in'
        // and not isBuiltin[], e.g. rdf:Bag, which in much
        // of the code is just a URIref, but can only be a classID
        // and not a propID. The syntactic category given to rdf:Bag
        // initially is { notype, classID } which is not an isBuiltin[] like
        // owlClass.

        if (//isBuiltin[px]&&
        !look.canBeProp(px)) {
                addResult(new IllegalPredicateProblem());
        }
        int sxx[] = nonPseudoCats(sx);
        int oxx[] = nonPseudoCats(ox);
        int pxx[] = nonPseudoCats(px);
        if (!inDomain(pxx, sxx)) {
            DomainRangeProblem r = new IllegalSubjectForPredicateProblem();
            CategorySetNames.symmetricNames(r,sxx, domain(pxx));
            addResult(r);
        }
        if (!inRange(pxx, oxx)) {
            DomainRangeProblem r = new IllegalObjectForPredicateProblem();
            CategorySetNames.symmetricNames(r,oxx, range(pxx));
            addResult(r);
        }
        if (rslt != null)
            return rslt;

        if (px == Grammar.rdftype) {
            return handleRDFType(sx, ox);
        }
        throw new BrokenException("Unreachable code.");
    }

    static private boolean inRange(int px[], int[] ox) {
        for (int i=0; i<px.length;i++)
            if (Q.intersect(ox,look.range(px[i])))
                return true;
            
        return false;
    }

    static private boolean inDomain(int px[], int[] sx) {
        for (int i=0; i<px.length;i++)
            if (Q.intersect(sx,look.domain(px[i])))
                return true;
            
        return false;
    }
    
    static private int[] domain(int p[]){
        if (p.length==1)
            return look.domain(p[0]);
        Set d = new TreeSet();
        for (int i=0;i<p.length;i++) {
            int dd[] = look.domain(p[i]);
            for (int j=0;j<dd.length;j++)
                d.add(new Integer(dd[j]));
        }
        int rslt[] = new int[d.size()];
        Iterator it=d.iterator();
        int ix = 0;
        while (it.hasNext())
            rslt[ix++]=((Integer)it.next()).intValue();
        return rslt;
    }
    static private int[] range(int p[]){
        if (p.length==1)
            return look.range(p[0]);
        Set d = new TreeSet();
        for (int i=0;i<p.length;i++) {
            int dd[] = look.range(p[i]);
            for (int j=0;j<dd.length;j++)
                d.add(new Integer(dd[j]));
        }
        int rslt[] = new int[d.size()];
        Iterator it=d.iterator();
        int ix = 0;
        while (it.hasNext())
            rslt[ix++]=((Integer)it.next()).intValue();
        return rslt;
    }

    /**
     * @param sx
     * @param ox
     */
    private static MonotonicProblem handleRDFType(int sx, int ox) {
        if (maybeBuiltinID(sx))
            return new BuiltinURIGivenTypeProblem();

        if (isPropertyOnly(sx)) {

            switch (ox) {
            case Grammar.rdfsDatatype:
            case Grammar.rdfsClass:
            case Grammar.owlClass:
            case Grammar.owlOntology:
            case Grammar.owlDeprecatedClass:
            case Grammar.owlOntologyProperty: // not permitted!! See
                                              // http://www.w3.org/TR/owl-semantics/mapping.html#separated_vocabulary
                return new PropertyOnlyURIWithOtherTypeProblem();

            }
            if (isUserID(ox) || isBlank(ox))
                return new PropertyOnlyURIWithOtherTypeProblem();
        }
        switch (ox) {
        case Grammar.owlOntology:
            if (isClassOnly(sx))
                return new ClassOnlyURIWithOtherTypeProblem();
            if (!isUserID(sx) && !isBlank(sx))
                return new TypeNeedsBlankOrUserIDSubjectProblem();
            break;
        case Grammar.rdfsDatatype:
        case Grammar.rdfProperty:
        case Grammar.owlAnnotationProperty:
        case Grammar.owlDatatypeProperty:
        case Grammar.owlFunctionalProperty:
        case Grammar.owlInverseFunctionalProperty:
        case Grammar.owlObjectProperty:
        case Grammar.owlOntologyProperty:
        case Grammar.owlSymmetricProperty:
        case Grammar.owlTransitiveProperty:
        case Grammar.owlDeprecatedProperty:
            if (isClassOnly(sx))
                return new ClassOnlyURIWithOtherTypeProblem();
            if (!isUserID(sx))
                return  new TypeNeedsUserIDSubjectProblem();
            break;
        case Grammar.owlDeprecatedClass:
            if (!isUserID(sx))
                return new TypeNeedsUserIDSubjectProblem();
            break;
        case Grammar.rdfList:
        case Grammar.owlAllDifferent:
        case Grammar.owlDataRange:
        case Grammar.owlRestriction:
            if (!isBlank(sx))
                return new TypeNeedsBlankSubjectProblem();
            break;
        default:
            if (isBlank(ox) || isUserID(ox)) {
                if (isClassOnly(sx))
                    return new ClassOnlyURIWithOtherTypeProblem();
            }
        }
        throw new BrokenException("Unreachable code. "
                + CategorySet.catString(sx) + " rdf:type "
                + CategorySet.catString(ox));
    }

    public static boolean maybeBuiltinID(int sx) {
        boolean builtin;
        switch (sx) {
        case Grammar.annotationPropID:// rdfs:seeAlso ??
        case Grammar.datatypeID: //xsd:int
        case Grammar.dataRangeID: //rdfs:Literal
        case Grammar.dataAnnotationPropID: // rdfs:label
        case Grammar.classID: // owl:Thing
        case Grammar.ontologyPropertyID: // owl:priorVersion
            builtin = true;
            break;
        default:
            builtin = false;
            break;
        }
        return builtin;
    }

    public static boolean isClassOnly(int sx) {
        return look.meet(sx, Grammar.classOnly) == sx;
    }

    public static boolean isPropertyOnly(int sx) {
        return look.meet(sx, Grammar.propertyOnly) == sx;
    }
    public static boolean isBlank(int sx) {
        return look.meet(sx, Grammar.blank) == sx;
    }
    public static boolean isUserID(int sx) {
        return look.meet(sx, Grammar.userID) == sx;
    }

    /**
     * categories without pseudo-categories. Note the pseuds (if any) all appear
     * at the beginning, by (a) sorting and (b) arrangement of the grammar.
     */
    static private int[] nonPseudoCats(int c) {
        int cats[] = CategorySet.getSet(c);
        int i;
        for (i = 0; i < cats.length; i++) {
            if (!Grammar.isPseudoCategory(cats[i]))
                break;
        }
        if (i == 0)
            return cats;

        int rslt[] = new int[cats.length - i];
        System.arraycopy(cats, i, rslt, 0, rslt.length);
        return rslt;

    }

    /**
     * precondition !Q.intersect(wantedC,givenC)
     * 
     * @param wantedC
     *            from this triple only
     * @param givenC
     *            from other triples
     * @return
     */
    static private void catMiss(WantedGiven w,int wantedC, int givenC) {
        if (isBlank(wantedC) != isBlank(givenC))
            throw new BrokenException("Logic error");
        CategorySetNames.symmetricNames(w,nonPseudoCats(wantedC),
                nonPseudoCats(givenC));
    }
}
/*
    static private int bad = 0;

    // The number of errors of each type.
    static private int eCnt[] = new int[1000];

    // The first three examples found for each sort.
    static private int eExamples[][][] = new int[1000][3][6];
/*
    static private void allCases(int s, int p, int o) {
        for (int i = 0; i < start[s].length; i++)
            for (int j = 0; j < start[p].length; j++)
                for (int k = 0; k < start[o].length; k++) {
                    bad++;
                    int e = getErrorCode(s, p, o, start[s][i], start[p][j],
                            start[o][k]);
                    int ix = eCnt[e]++;
                    if (ix < 3) {
                        eExamples[e][ix][0] = s;
                        eExamples[e][ix][1] = p;
                        eExamples[e][ix][2] = o;
                        eExamples[e][ix][3] = start[s][i];
                        eExamples[e][ix][4] = start[p][j];
                        eExamples[e][ix][5] = start[o][k];
                    }
                }
    }
    */
/*
    public static void main(String args[]) {

       for (int j = 1; j < SZ; j++) {
            if (Grammar.isPseudoCategory(j))
                continue;
            if (isBlank(j))
                continue;
            if (isLiteral(j))
                continue;
            for (int i = 1; i < SZ; i++) {
                if (Grammar.isPseudoCategory(i))
                    continue;
                if (isLiteral(i))
                    continue;
                for (int k = 1; k < SZ; k++) {
                    if (Grammar.isPseudoCategory(k))
                        continue;
                    if (look.qrefine(i, j, k) == Failure) {
     //                   allCases(i, j, k);

                    }

                }
            }

        }
        System.out.println(bad + " cases considered.");
        dump("Codes:", eCnt);

        for (int i = 0; i < SZ; i++)
            if (diffPreds[i] != 0)
                System.out.println(CategorySet.catString(i) + " "
                        + diffPreds[i]);
        DebugCategorySetNames.anyUsedNames();
    }
*/
/*
 * 
 * 
     static private String fieldName[] = { "subj", "pred", "obj ", "S1  ",
            "P1  ", "01  " };

    static private void dump(String s, int a[]) {
        boolean examples = s.startsWith("Codes");
        Field fld[] = MonotonicErrorAnalyzer.class.getDeclaredFields();
        Field fldsByN[] = new Field[1000];
        if (examples) {
            for (int i = 0; i < fld.length; i++) {
                if (fld[i].getType() == Integer.TYPE
                        && (fld[i].getModifiers() & Modifier.STATIC) != 0) {
                    int vl = 0;
                    try {
                        vl = fld[i].getInt(null);
                    } catch (IllegalAccessException ee) {
                        throw new RuntimeException(ee);
                    }
                    if (vl >= 0 && vl < 1000) {
                        if (fldsByN[vl] != null) {
                            System.err.println("Flds: " + fldsByN[vl].getName()
                                    + " & " + fld[i].getName());

                        }
                        fldsByN[vl] = fld[i];
                    }
                }

            }
        }
        System.out.println(s);
        for (int i = 0; i < a.length; i++)
            if (a[i] != 0) {
                System.out.println(i + "\t" + a[i]);
                if (examples) {
                    if (fldsByN[i] != null)
                        System.out.println(fldsByN[i].getName());
                    for (int j = 0; j < 0 && j < a[i]; j++) {
                        System.out.println("Ex: " + j);
                        for (int k = 0; k < 6; k++)
                            System.out
                                    .println("    "
                                            + fieldName[k]
                                            + " "
                                            + CategorySet
                                                    .catString(eExamples[i][j][k]));

                    }
                }
            }
    }

}
*/
/*
 * (c) Copyright 2003-2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
