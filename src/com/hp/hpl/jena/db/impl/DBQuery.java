/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id$
*/

package com.hp.hpl.jena.db.impl;

import java.util.ArrayList;
import java.util.List;

/**
	@author hedgehog
*/

public class DBQuery 
	{
	int argCnt;         // number of arguments to query
	String argType;     // list of argument types
	List argIndex;		// index of argument in input
	int varCnt;         // number of variables in query
	int aliasCnt;        // number of tables aliases (scans) in from clause
	String stmt;        // query string
	VarIndex[] binding;  // list of VarIndex
	int[] resList;		// indexes of result columns in mapping
	int graphId;        // id of graph to query
	String table;   // name of table to query
	IPSet pset;		// pset to be queried
	IRDBDriver driver;  // driver for store
	boolean qryOnlyStmt; // if true, ignore reified statements
	boolean qryOnlyReif; // if true, ignore asserted statements
	boolean qryFullReif; // if true, ignore partially reified statements
	DriverRDB.GenSQLAnd ga;

	boolean isMultiModel;   // true if graph is multi-model
	boolean isSingleValued; // true if property table is single-valued
	boolean isCacheable;    // true if it is safe to cache compiled query
	boolean isReifier;      // true if query is over a reifier specialized graph

	
	public DBQuery ( SpecializedGraph sg, List varList,
		boolean queryOnlyStmt,  boolean queryOnlyReif, boolean queryFullReif ) {
		pset = sg.getPSet();
		argCnt = 0;
		argType = "";
		argIndex = new ArrayList();	
		aliasCnt = 0;
		stmt = "";
		graphId = sg.getGraphId();
		table = pset.getTblName();
		isMultiModel = true;  // for now
		isSingleValued = false;  // for now
		isCacheable = true;
		isReifier = sg instanceof SpecializedGraphReifier;
		driver = pset.driver();
		ga = new IRDBDriver.GenSQLAnd();
		qryOnlyStmt = queryOnlyStmt;
		qryOnlyReif = queryOnlyReif;
		qryFullReif = queryFullReif;
		// add result variables to mapping
		binding = new VarIndex[varList.size()];
		for ( varCnt=0; varCnt<varList.size(); varCnt++ ) {
			binding[varCnt] = (VarIndex) varList.get(varCnt);
		}

	}
	
	public VarIndex getBinding ( int i ) {
		return binding[i];
	}
		
	public void newAlias() {
		aliasCnt++;
	}
	
}		

/*
    (c) Copyright Hewlett-Packard Company 2002
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
