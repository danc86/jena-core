/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id$
*/

package com.hp.hpl.jena.regression;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class NewRegression extends ModelTestBase
    {
    public NewRegression( String name )
        { super( name ); }

    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite( NewRegression.class ); 
        result.addTest( NewRegressionLiterals.suite() );
        result.addTest( NewRegressionResources.suite() );
        result.addTest( NewRegressionStatements.suite() );
        result.addTest( NewRegressionContainers.suite() );
        result.addTest( NewRegressionAddAndContains.suite() );
        result.addTest( NewRegressionGet.suite() );
        result.addTest( NewRegressionObjects.suite() );
        result.addTest( NewRegressionStatements.suite() );
        result.addTest( NewRegressionAddModel.suite() );
        result.addTest( NewRegressionListSubjects.suite() );
        result.addTest( NewRegressionSelector.suite() );
        result.addTest( NewRegressionSeq.suite() );
        result.addTest( NewRegressionSet.suite() );
        result.addTest( NewRegressionResourceMethods.suite() );
        result.addTest( NewRegressionStatementMethods.suite() );
        result.addTest( NewRegressionBagMethods.suite() );
        result.addTest( NewRegressionAltMethods.suite() );
        return result;
        }
    
    public void testNothing()
        {}    

    }




//    /** test load from xml file
//     * @param m the model implementation under test
//     */
//    public void test18(Model m) {
//        String  test = "Test18";
//        if (test.equals( test )) return;
//        String  testURI = "http://aldabaran.hpl.hp.com/rdftest/test18/";
//        String  subject1 = testURI + "1";
//        String  object1 =
//   "<foo bar=\"bar\"><bar>abc<foobar/>def&lt;&gt;&apos;&quot;&amp;</bar></foo>";
//        String RDFSchemaURI = "http://lists.w3.org/Archives/Public/www-archive/"
//                            + "2001Sep/att-0064/00-rdfschema.rdf";
//        int     n = 0;
//
//        try {
//            System.out.println("Beginning " + test);
//            m.read(ResourceReader.getInputStream("modules/rdf/rdfschema.html"),
//                                      RDFSchemaURI);
//            n++; if (m.size() != 124) error(test, n);
//   //         n++; m.write(new PrintWriter(System.out));
//
//            StmtIterator iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//
//            m.read(ResourceReader.getInputStream("modules/rdf/embeddedxml.xml"), "");
//            n++;
// /* I'd like to test for the exactly correct value here, but can't since the
//  * exactly correct value is not defined.
//            if (! m.contains(m.createResource(subject1),
//                             RDF.value, object1)) error(test, n++);
//  * So instead lets do some rough checks its right */
//            String xml = m.getResource(subject1)
//                          .getRequiredProperty(RDF.value)
//                          .getString();
//            n++; if ( xml.indexOf("&lt;") == -1) error(test, n);
//            n++; if ( xml.indexOf("&gt;") == -1) error(test, n);
//            n++; if ( xml.indexOf("&amp;") == -1) error(test, n);
//            n++; if ((xml.indexOf("'bar'") == -1) &&
//                     (xml.indexOf("\"bar\"") == -1)) error(test, n);
//
//            m.createResource()
//             .addProperty(RDF.value, "can't loose");
//  //          m.write(new PrintWriter(System.out));
//
//            iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//            n++;
//            m.read(ResourceReader.getInputStream("modules/rdf/testcollection.rdf"), "");
//            if (m.size() != 24) error(test, (int) m.size());
//
//            iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//
//            try {
//                m.read(System.getProperty("com.hp.hpl.jena.regression.testURL",
//                                          RDFSchemaURI));
// //               n++; m.write(new PrintWriter(System.out));
//                n++; if ((m.size() != 124) && (m.size() != 125)) {
//                    System.out.println("size = " + m.size());
//                      error(test, n);
//                }
//                if (! m.contains(RDF.Property, RDF.type, RDFS.Class))
//                    error(test, n);
//            } catch (JenaException rdfx) {
//                Throwable th = rdfx.getCause();
//                if ( th instanceof NoRouteToHostException
//                 || th instanceof UnknownHostException
//                 || th instanceof IOException
//                 || th instanceof ConnectException) {
//                    logger.warn( "Cannot access public internet- part of test not executed" );
//                } else {
//                    throw rdfx;
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test moving things between models
//     * @param m the model implementation under test
//     */
//    public void test19(Model m1, Model m2) {
//        String  test = "Test19";
//        int     n = 0;
//
//        try {
//            Statement stmt;
//            StmtIterator sIter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                Resource r11 = m1.createResource();
//                Resource r12 = m2.createResource(new ResTestObjF());
//                long size1 = m1.size();
//                long size2 = m2.size();
//
//                r11.addProperty(RDF.value, 1);
//                n++; if (! (m1.size() == ++size1)) error(test, n);
//                n++; if (! (m2.size() == size2)) error(test,n);
//
//                stmt = m2.createStatement(r11, RDF.value, r12);
//                n++; if (! (stmt.getSubject().getModel() == m2)) error(test,n);
//                n++; if (! (stmt.getResource().getModel() == m2)) error(test,n);
//
//                m1.add(stmt);
//                n++; if (! (m1.size() == ++size1)) error(test, n);
//                n++; if (! (m2.size() == size2)) error(test,n);
//
//                sIter = m1.listStatements(
//                                    new SimpleSelector(r11, RDF.value, r12));
//                n++; if (! sIter.hasNext()) error(test, n);
//                n++; stmt = sIter.nextStatement();
//                n++; if (! (stmt.getSubject().getModel() == m1)) error(test,n);
//                n++; if (! (stmt.getResource().getModel() == m1)) error(test,n);
//                sIter.close();
//
//
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//   /** Empty the passed in model
//     * @param m the model implementation under test
//     */
//    public void test20(Model m) {
//        String  test = "Test20";
//        int     n = 0;
//
//        try {
////            System.out.println("Beginning " + test);
//            Statement s1 = null;
//            Statement s2 = null;
//
//            try {
//                n=100;
//                n++; s1 = m.createStatement(m.createResource(),
//                                            RDF.type,
//                                            RDFS.Class);
//                n++; if (s1.isReified()) error(test,n);
//                n++; m.add(s1);
//                n++; if (s1.isReified()) error(test,n);
//                n++; s2 = m.createStatement(m.createResource(),
//                                            RDF.type,
//                                            RDFS.Class);
//                n++; if (s2.isReified()) error(test,n);
//                n++; m.add(s2);
//                n++; if (s2.isReified()) error(test,n);
///*
//                n++; m.add(s1, RDF.value, new LiteralImpl("foo"));
//                n++; if (!s1.isReified()) error(test,n);
//
//                n++; m.add(s1, RDF.value, s2);
//                n++; if (!s2.isReified()) error(test,n);
// */
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** Testing for miscellaneous bugs
//     * @param m the model implementation under test
//     */
//    public void test97(Model m) {
//        String  test = "Test97";
//        int     n = 0;
//
//        try {
//
////            System.out.println("Beginning " + test);
//
//                /*
//                    the _null_ argument to LiteralImpl was preserved only for backward
//                    compatability. It was be logged and has now become an exception.
//                    (Brian and Chris had a discussion about this and agreed).
//                */
//                // Node.nullLiteralsGenerateWarnings();
//                try
//                    {
//                   n=100; m.query(new SimpleSelector(null,
//                                                   null,
//                                                   new LiteralImpl( Node.createLiteral( null, "", false ), (ModelCom) m)));
//                    error( test, n );
//                    }
//                catch (NullPointerException e)
//                        {}
//                try
//                    {
//                   n=101; m.query(new SimpleSelector(null,
//                                                   null,
//                                                   new LiteralImpl( Node.createLiteral( null, "en", false ), (ModelCom) m)));
//                    error( test, n );
//                    }
//                catch (NullPointerException e)
//                    {}
//                // end of nullLiteralsGenerateWarnings code
//
//               n=102;
//               StmtIterator iter
//                            = m.listStatements(new SimpleSelector(null,
//                                                                null,
//                                                                (String) null));
//               while (iter.hasNext()) {
//                   RDFNode o = iter.nextStatement().getObject();
//               }
//
//               n=103;
//               iter = m.listStatements(new SimpleSelector(null,
//                                                        null,
//                                                        (Object) null));
//               while (iter.hasNext()) {
//                   RDFNode o = iter.nextStatement().getObject();
//               }
//
//            } catch (Exception e) {
//                error(test, n, e);
//            }
////        System.out.println("End of " + test);
//    }
//
//    /** Empty the passed in model
//     * @param m the model implementation under test
//     */
//    public void test99(Model m) {
//        String  test = "Test5";
//        int     n = 0;
//
//        try {
//            StmtIterator iter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                n++; iter = m.listStatements();
//                while (iter.hasNext()) {
//                    iter.nextStatement();
//                    n++;    iter.remove();
//                }
//                n++; iter.close();
//                n++; if (! (m.size()==0)) error(test,999);
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//


//    public void testBag(Model m, Bag bag1, Bag bag2, Bag bag3,
//                          String test, int n) {
//        int num = 10;
//        NodeIterator nIter;
//
//        try {
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//
//                    for (int i=0; i<num; i++) {
//                        bag1.add(i);
//                    }
//                n++; nIter=bag1.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                     nIter.close();
//                n=(n/100+1)*100;
//                n++; nIter = bag1.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        bag2.add(i);
//                    }
//                n++; nIter=bag2.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//               n=(n/100+1)*100;
//                n++; nIter = bag2.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//              n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        bag3.add(i);
//                    }
//                n++; nIter=bag3.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;;
//                n++; nIter = bag3.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }
//
//    public void testAlt(Model m, Alt alt1, Alt alt2, Alt alt3, Alt alt4,
//                         String test, int n) {
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal    tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResource = m.createResource();
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            String     lang     = "fr";
//            int        num=10;
//            Statement stmt;
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//               n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        alt1.add(i);
//                    }
//                n++; nIter=alt1.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                n++; nIter = alt1.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        alt2.add(i);
//                    }
//                n++; nIter=alt2.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=550;
//                n++; nIter = alt2.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=580;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        alt3.add(i);
//                    }
//                n++; nIter=alt3.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                n++; nIter = alt3.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                n=(n/100+1)*100;
//                n++; if (! (alt4.setDefault(tvLiteral)
//                               .getDefault().equals(tvLiteral)))
//                       error(test,n);
//                n++; if (! (alt4.setDefault(tvLiteral)
//                               .getDefaultLiteral().equals(tvLiteral)))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvResource)
//                               .getDefaultResource().equals(tvResource))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvByte)
//                               .getDefaultByte()== tvByte))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvShort)
//                               .getDefaultShort()==tvShort))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvInt)
//                               .getDefaultInt()==tvInt))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvLong)
//                               .getDefaultLong()==tvLong))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvChar)
//                               .getDefaultChar()==tvChar))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvFloat)
//                               .getDefaultFloat()==tvFloat))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvDouble)
//                               .getDefaultDouble()==tvDouble))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvString)
//                               .getDefaultString().equals(tvString))
//                       error(test,n);
//                n++; if (!  alt4.getDefaultLanguage().equals(""))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvString, lang)
//                               .getDefaultString().equals(tvString))
//                       error(test,n);
//                n++; if (!  alt4.getDefaultLanguage().equals(lang))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvResObj)
//                               .getDefaultResource(new ResTestObjF())
//                               .equals(tvResObj))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvLitObj)
//                               .getDefaultObject(new LitTestObjF())
//                               .equals(tvLitObj))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvAlt)
//                               .getDefaultAlt()
//                               .equals(tvAlt))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvBag)
//                               .getDefaultBag()
//                               .equals(tvBag))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvSeq)
//                               .getDefaultSeq()
//                               .equals(tvSeq))
//                       error(test,n);
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }
//
//    public void testSeq(Model m, Seq seq1, Seq seq2, Seq seq3, Seq seq4,
//                           Seq seq5, Seq seq6, Seq seq7, String test, int n) {
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal    tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResource = m.createResource();
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            String     lang     = "fr";
//            int        num=10;
//            Statement stmt;
//
//            {
//
//                     for (int i=0; i<num; i++) {
//                        seq1.add(i);
//                    }
//                n++; if (! (seq1.size()==num)) error(test,n);
//                n++; nIter = seq1.iterator();
//                    for (int i=0; i<num; i++) {
//                        if ( ! (((Literal) nIter.nextNode()).getInt() == i))
//                            error(test, 320+i);
//                    }
//                    nIter.close();
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//                n=(n/100)*100 + 100;
//                n++; nIter=seq1.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                n++; nIter = seq1.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                        seq2.add(i);
//                    }
//                n++; nIter=seq2.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                n++; nIter = seq2.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//               n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                        seq3.add(i);
//                    }
//                n++; nIter=seq3.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                n++; nIter = seq3.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                n=(n/100)*100 + 100;
//                n++; seq4.add(tvBoolean);
//                n++; if (!  (seq4.getBoolean(1)==tvBoolean)) error(test,n);
//                n++; seq4.add(tvByte);
//                n++; if (!  (seq4.getByte(2)==tvByte)) error(test,n);
//                n++; seq4.add(tvShort);
//                n++; if (!  (seq4.getShort(3)==tvShort)) error(test,n);
//                n++; seq4.add(tvInt);
//                n++; if (!  (seq4.getInt(4)==tvInt)) error(test,n);
//                n++; seq4.add(tvLong);
//                n++; if (!  (seq4.getLong(5)==tvLong)) error(test,n);
//                n++; seq4.add(tvChar);
//                n++; if (!  (seq4.getChar(6)==tvChar)) error(test,n);
//                n++; seq4.add(tvFloat);
//                n++; if (!  (seq4.getFloat(7)==tvFloat)) error(test,n);
//                n++; seq4.add(tvDouble);
//                n++; if (!  (seq4.getDouble(8)==tvDouble)) error(test,n);
//                n++; seq4.add(tvString);
//                n++; if (!  (seq4.getString(9).equals(tvString))) error(test,n);
//                n++; if (!  (seq4.getLanguage(9).equals(""))) error(test,n);
//                n++; seq4.add(tvString, lang);
//                n++; if (!  (seq4.getString(10).equals(tvString))) error(test,n);
//                n++; if (!  (seq4.getLanguage(10).equals(lang))) error(test,n);
//                n++; seq4.add(tvLitObj);
//                n++; if (!  (seq4.getObject(11, new LitTestObjF())
//                                .equals(tvLitObj))) error(test,n);
//                n++; seq4.add(tvResource);
//                n++; if (!  (seq4.getResource(12).equals(tvResource))) error(test,n);
//                n++; seq4.add(tvLiteral);
//                n++; if (!  (seq4.getLiteral(13).equals(tvLiteral))) error(test,n);
//                n++; seq4.add(tvResObj);
//                n++; if (!  (seq4.getResource(14, new ResTestObjF())
//                                .equals(tvResObj))) error(test,n);
//                n++; seq4.add(tvBag);
//                n++; if (!  (seq4.getBag(15).equals(tvBag))) error(test,n);
//                n++; seq4.add(tvAlt);
//                n++; if (!  (seq4.getAlt(16).equals(tvAlt))) error(test,n);
//                n++; seq4.add(tvSeq);
//                n++; if (!  (seq4.getSeq(17).equals(tvSeq))) error(test,n);
//                n++; try {
//                        seq4.getInt(18); error(test,n);
//                    } catch (SeqIndexBoundsException e) {
//                        // as required
//                    }
//                n++; try {
//                        seq4.getInt(0); error(test,n);
//                    } catch (SeqIndexBoundsException e) {
//                        // as required
//                    }
//            }
//
//            {
//                n=(n/100)*100 + 100;
//                for (int i=0; i<num; i++) {
//                    seq5.add(i);
//                }
//
//                     try {
//                n++;        seq5.add(0, false); error(test,n);
//                     } catch (SeqIndexBoundsException e) {
//                        // as required
//                     }
//                     seq5.add(num+1, false);
//                     if (seq5.size()!=num+1) error(test,n);
//                     seq5.remove(num+1);
//                     try {
//                n++;        seq5.add(num+2, false); error(test,n);
//                     } catch (SeqIndexBoundsException e) {
//                        // as required
//                     }
//
//               n=(n/100)*100 + 100;
//                    int size = seq5.size();
//                    for (int i=1; i<=num-1; i++) {
//               n++;     seq5.add(i, 1000+i);
//               n++;     if (! (seq5.getInt(i)==1000+i)) error(test,n);
//               n++;     if (! (seq5.getInt(i+1)==0)) error(test, n);
//               n++;     if (! (seq5.size()==(size+i))) error(test,n);
//               n++;     if (! (seq5.getInt(size)==(num-i-1))) error(test,n);
//                    }
//               n=(n/100)*100 + 100;
//                    seq6.add(m.createResource());
//                    seq6.add(1, tvBoolean);
//               n++; if (! (seq6.getBoolean(1)==tvBoolean)) error(test,n);
//                    seq6.add(1, tvByte);
//               n++; if (! (seq6.getByte(1)==tvByte)) error(test,n);
//                    seq6.add(1, tvShort);
//               n++; if (! (seq6.getShort(1)==tvShort)) error(test,n);
//                    seq6.add(1, tvInt);
//               n++; if (! (seq6.getInt(1)==tvInt)) error(test,n);
//                    seq6.add(1, tvLong);
//               n++; if (! (seq6.getLong(1)==tvLong)) error(test,n);
//                    seq6.add(1, tvChar);
//               n++; if (! (seq6.getChar(1)==tvChar)) error(test,n);
//                    seq6.add(1, tvFloat);
//               n++; if (! (seq6.getFloat(1)==tvFloat)) error(test,n);
//                    seq6.add(1, tvDouble);
//               n++; if (! (seq6.getDouble(1)==tvDouble)) error(test,n);
//                    seq6.add(1, tvString);
//               n++; if (! (seq6.getString(1).equals(tvString))) error(test,n);
//                    seq6.add(1, tvString, lang);
//               n++; if (! (seq6.getString(1).equals(tvString))) error(test,n);
//                    seq6.add(1, tvResource);
//               n++; if (! (seq6.getResource(1).equals(tvResource))) error(test,n);
//                    seq6.add(1, tvLiteral);
//               n++; if (! (seq6.getLiteral(1).equals(tvLiteral))) error(test,n);
//                    seq6.add(1, tvLitObj);
//               n++; if (! (seq6.getObject(1, new LitTestObjF())
//                              .equals(tvLitObj))) error(test,n);
//
//               n=(n/100)*100 + 100;
//               n++; if (! (seq6.indexOf(tvLitObj)==1)) error(test,n);
//               n++; if (! (seq6.indexOf(tvLiteral)==2)) error(test,n);
//               n++; if (! (seq6.indexOf(tvResource)==3)) error(test,n);
//               n++; if (! (seq6.indexOf(tvString,lang)==4)) error(test,n);
//               n++; if (! (seq6.indexOf(tvString)==5)) error(test,n);
//               n++; if (! (seq6.indexOf(tvDouble)==6)) error(test,n);
//               n++; if (! (seq6.indexOf(tvFloat)==7)) error(test,n);
//               n++; if (! (seq6.indexOf(tvChar)==8)) error(test,n);
//               n++; if (! (seq6.indexOf(tvLong)==9)) error(test,n);
//               n++; if (! (seq6.indexOf(tvInt)==10)) error(test,n);
//               n++; if (! (seq6.indexOf(tvShort)==11)) error(test,n);
//               n++; if (! (seq6.indexOf(tvByte)==12)) error(test,n);
//               n++; if (! (seq6.indexOf(tvBoolean)==13)) error(test,n);
//               n++; if (! (seq6.indexOf(1234543)==0)) error(test,n);
//
//               n=(n/100)*100 + 100;
//                   for (int i=0; i<num; i++) {
//                       seq7.add(i);
//                   }
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvBoolean);
//              n++; if (! (seq7.getBoolean(5)==tvBoolean)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvByte);
//              n++; if (! (seq7.getByte(5)==tvByte)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvShort);
//              n++; if (! (seq7.getShort(5)==tvShort)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvInt);
//              n++; if (! (seq7.getInt(5)==tvInt)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//             n=(n/100)*100 + 100;
//                   seq7.set(5, tvLong);
//              n++; if (! (seq7.getLong(5)==tvLong)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvChar);
//              n++; if (! (seq7.getChar(5)==tvChar)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvFloat);
//              n++; if (! (seq7.getFloat(5)==tvFloat)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvDouble);
//              n++; if (! (seq7.getDouble(5)==tvDouble)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvString);
//              n++; if (! (seq7.getString(5).equals(tvString))) error(test,n);
//              n++; if (! (seq7.getLanguage(5).equals(""))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//                   seq7.set(5, tvString,lang);
//              n++; if (! (seq7.getString(5).equals(tvString))) error(test,n);
//              n++; if (! (seq7.getLanguage(5).equals(lang))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvLiteral);
//              n++; if (! (seq7.getLiteral(5).equals(tvLiteral))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvResource);
//              n++; if (! (seq7.getResource(5).equals(tvResource))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvLitObj);
//              n++; if (! (seq7.getObject(5, new LitTestObjF()))
//                             .equals(tvLitObj)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvResObj);
//              n++; if (! (seq7.getResource(5, new ResTestObjF())
//                             .equals(tvResObj))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//
//        }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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

