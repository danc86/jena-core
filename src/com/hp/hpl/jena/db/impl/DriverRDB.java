/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.util.Log;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.*;

//=======================================================================
/**
* Base database driver for implementing SpecializedGraphs.
* Different drivers are needed for different databases and different
* layout schemes.
* <p>
* This driver is a base implemention from which database-specific
* drivers can inherit. It is not generic in the sense that it will work
* on any minimal SQL store and so should be treated as if it were
* an abstract class.
* <p>The SQL statements which implement each of the functions are
* loaded in a separate file etc/[layout]_[database].sql from the classpath.
*
* @author hkuno modification of Jena1 code by Dave Reynolds (der)
* @version $Revision$ on $Date$
*/

public abstract class DriverRDB implements IRDBDriver {

//=======================================================================
// Cutomization variables
// =======================================================================
   /**
    * This Graph's db properties
    */
   protected DBPropDatabase m_dbProps;
    
   /**
   * Name of this class's PSet_TripleStore_XXX class
   */
  protected String m_psetClassName;

  /**
  * Name of this class's PSet_TripleStore_XXX class
  */
 protected String m_psetReifierClassName;

   /**
	* Cached name of this class's SpecializedGraph_XXX class
	*/
   protected String m_lsetClassName;
   
   /**
	* Cached name of this class's SpecializedGraphReifer_XXX class
	*/
   protected String m_lsetReifierClassName;
   
   /** The class name of the database driver (e.g. jdbc.sql.class)*/
   protected  String DRIVER_NAME;     
   // Dummy - needs replacing when instantiated?

   /** The name of the database type this driver supports */
   protected String DATABASE_TYPE;

   /** The maximum size of index key (or a component of a key) */
   protected int INDEX_KEY_LENGTH;
   /** The maximum size of an object that can be stored in a Statement table */

   protected boolean HAS_XACTS;
   /** true if graphs using this database instance use transactions.
    * note, this differs from m_transactionSupported because HAS_XACTS
    * is a user settable parameter. the underlying db engine may support
    * transactions but an application may prefer to run without transactions
    * for better performance.
    */
   
   protected boolean STRINGS_TRIMMED;
   /** true if the database engine will trim trailing spaces in strings. to
    *  prevent this, append EOS to strings that should not be trimmed.
    */
   
   protected String EOS = "";
   protected char	EOS_CHAR = ':';
   protected int	EOS_LEN = 0;
   /** EOS is appended to most RDB strings to deal with string trimming. if
    *  STRINGS_TRIMMED is false, EOS is null. otherwise, EOS is EOS_CHAR.
    *  EOS_LEN is the length of EOS (0 or 1).
    */
   
   
   protected boolean URI_COMPRESS;
   /** true if URI's are to be compressed by storing prefixes (an approximation
    *  of a namespace) in the JENA_PREFIX table. note that "short" prefixes are
    *  not stored, i.e., the prefix length not more than URI_COMPRESS_LENGTH.
    */
   
   protected int URI_COMPRESS_LENGTH;
   /** if URI_COMPRESS is true, compress prefixes that are longer than this.

   /** The maximum size of an object that can be stored in a Statement table */
   protected int LONG_OBJECT_LENGTH;

   /** The SQL type to use for storing ids (compatible with wrapDBID) */
   protected String ID_SQL_TYPE;
   
   /** Set to true if the insert operations already check for duplications */
   protected boolean SKIP_DUPLICATE_CHECK;

   /** Set to true if the insert operations allocate object IDs themselves */
   protected boolean SKIP_ALLOCATE_ID;
	
   /** Holds value of empty literal marker */
   protected String EMPTY_LITERAL_MARKER;
	
   /** The name of the sql definition file for this database/layout combo */
   protected String SQL_FILE;
   
   /** The name of the sql definition file for this database/layout combo */
   protected String DEFAULT_SQL_FILE = "etc/generic_generic.sql";


   /** Set to true if the insert operations should be done using the "proc" versions */
   protected boolean INSERT_BY_PROCEDURE;
      
   
// =======================================================================
//	Common variables
// =======================================================================
   /**
	* Holds base name of AssertedStatement table.
	* Every triple store has at least one tables for AssertedStatements.
	*/
   protected static final String TABLE_BASE_NAME = "JENA_";
  
   /** Set to true to enable cache of pre-prepared statements */
   protected boolean CACHE_PREPARED_STATEMENTS = true;

   /** The name of the layout type this driver supports */
   protected String LAYOUT_TYPE = "TripleStore";

   /** Default name of the table that holds system property graph asserted statements **/
   protected final String SYSTEM_STMT_TABLE = TABLE_BASE_NAME + "SYS_STMT";
   
   /** Name of the long literal table **/
   protected final String LONG_LIT_TABLE = "JENA_LONG_LIT";
   
   /** Name of the long URI table **/
   protected final String LONG_URI_TABLE = "JENA_LONG_URI";

   /** Name of the prefix table **/
   protected final String PREFIX_TABLE = "JENA_PREFIX";

    
   /** Name of the graph holding default properties (the one's that a newly-created
	*  graph will have by default **/
   protected final String DEFAULT_PROPS = "JENA_DEFAULT_GRAPH_PROPERTIES";
   
   /** Unique numeric identifier of the graph holding default properties **/
   protected final int DEFAULT_ID = 0;

        
   /** Driver version number */
   protected final String VERSION = "2.0alpha";
    

// =======================================================================
//	Instance variables
// =======================================================================

	/**
	 * Instance of SQLCache used by Driver for hard-coded db commands
	 */
	protected SQLCache m_sql = null;

    /** Cache a reference to the system property graph (java) **/
    protected SpecializedGraph m_sysProperties = null;
    
    protected IDBConnection m_dbcon = null;
    
    //===================================
    // for transaction support
    //===================================
    
    
    // caches whether or not underlying connection supports transactions
    private Boolean m_transactionsSupported;
    
	/** flag to indicate that there is a transaction active on the associated connection */
	protected boolean inTransaction = false;



//	=======================================================================
//	 Constructor
//	=======================================================================


    /**
     * Create a bare instance of the driver. It is not functional until a
     * database connection has been supplied via setConnection.
     */
    public DriverRDB() {
    }
    
//	=======================================================================
//	 Methods
//	=======================================================================
	
	/**
	 * Return the connection
	 */
	public IDBConnection getConnection() {
		return m_dbcon;
	}
	
	/**
	 * Return the specialized graph used to store system properties.
	 * (Constuct a new one if necessary).
	 */
	public SpecializedGraph getSystemSpecializedGraph() {
		
		if (m_sysProperties != null) {
			return m_sysProperties;
		}
		
		if( !isDBFormatOK() ) {
			// Format the DB
			return formatAndConstructSystemSpecializedGraph();
		}
        getDbInitTablesParams();
		// The database has already been formatted - just grab the properties
		IPSet pSet = createIPSetInstanceFromName(m_psetClassName, SYSTEM_STMT_TABLE);
		m_sysProperties = createLSetInstanceFromName(m_lsetClassName, pSet, DEFAULT_ID);
		m_dbProps = new DBPropDatabase(m_sysProperties);
		return m_sysProperties;		
	}
	
	/**
	 * Format the database and construct a brand new system specialized graph.
	 */
	protected SpecializedGraph formatAndConstructSystemSpecializedGraph() {

		try {
			String [] params = 	getDbInitTablesParams();
			m_sql.runSQLGroup("initDBtables", params);
			if (!SKIP_ALLOCATE_ID) {
				Iterator seqIt = getSequences().iterator();
				while (seqIt.hasNext()) {
						removeSequence((String)seqIt.next());
				}
			}
			m_sql.runSQLGroup("initDBgenerators");
//			m_sql.runSQLGroup("initDBprocedures");
		} catch (SQLException e) {
			com.hp.hpl.jena.util.Log.warning("Problem formatting database", e);
			throw new RDFRDBException("Failed to format database", e);
		}
		
		// Construct the system properties
		IPSet pSet = createIPSetInstanceFromName(m_psetClassName, SYSTEM_STMT_TABLE);
		m_sysProperties = createLSetInstanceFromName(m_lsetClassName, pSet, DEFAULT_ID);
						
		// The following call constructs a new set of database properties and
		// adds them to the m_sysProperties specialized graph.
		m_dbProps = new DBPropDatabase( m_sysProperties, m_dbcon.getDatabaseType(), 
		                                VERSION, String.valueOf(LONG_OBJECT_LENGTH));
			
		// Now we also need to construct the parameters that will be the
		// default settings for any graph added to this database
		DBPropGraph def_prop = new DBPropGraph( m_sysProperties, DEFAULT_PROPS, "generic");
		
		String reifTbl = createTable(DEFAULT_ID, true);
		String stmtTbl = createTable(DEFAULT_ID, false);
		
		def_prop.addGraphId(DEFAULT_ID);
		def_prop.addStmtTable(stmtTbl);
		def_prop.addReifTable(reifTbl);

		return m_sysProperties;		
	}
	
	abstract String[] getDbInitTablesParams();
	
	abstract String[] getCreateTableParams( int graphId, boolean isReif );
	
	abstract public int graphIdAlloc ( String graphName );	
	
	abstract public int getLastInsertID();
	
	
	/**
	 * Construct and return a new specialized graph.
	 * @param graphProperties A set of customization properties for the specialized graph.
	 */
	public List createSpecializedGraphs(DBPropGraph graphProperties) {
		
		String graphName = graphProperties.getName();
		String stmtTbl = null;
		String reifTbl = null;
		String dbSchema;
		int graphId = graphIdAlloc(graphName);
		graphProperties.addGraphId(graphId);
				
		dbSchema = graphProperties.getDBSchema();
		// use the default schema if:
		// 1) no schema is specified and we are creating the default (unnamed) graph
		// 2) a schema is specified and it is the default (unnamed) graph
		if ( ((dbSchema == null) && graphName.equals(GraphRDB.DEFAULT)) ||
			 ((dbSchema != null) && dbSchema.equals(GraphRDB.DEFAULT)) )
			dbSchema = DEFAULT_PROPS;  // default graph should use default tables
		if ( dbSchema != null ) {
			DBPropGraph schProp = DBPropGraph.findPropGraphByName(getSystemSpecializedGraph(),
												dbSchema );
			if ( schProp != null ) {
				reifTbl = schProp.getReifTable();
				stmtTbl = schProp.getStmtTable();
			}
			if ( (reifTbl == null) || (stmtTbl == null) )
				throw new RDFRDBException("Creating graph " + graphName +
					": referenced schema not found: " + dbSchema);
		} else {
			reifTbl = createTable(graphId, true);	
			stmtTbl = createTable(graphId, false);	
			if ( (reifTbl == null) || (stmtTbl == null) )
				throw new RDFRDBException("Creating graph " + graphName +
					": cannot create tables");
		}
		graphProperties.addStmtTable(stmtTbl);
		graphProperties.addReifTable(reifTbl);
			
		// Add the reifier first
		DBPropPSet pSetReifier = new DBPropPSet(m_sysProperties, m_psetReifierClassName, reifTbl);
		DBPropLSet lSetReifier = new DBPropLSet(m_sysProperties, "LSET_"+graphProperties.getName()+"_REIFIER", m_lsetReifierClassName);
		lSetReifier.setPSet(pSetReifier);
		graphProperties.addLSet(lSetReifier);
		
		// Now add support all all non-reified triples
		DBPropPSet pSet = new DBPropPSet(m_sysProperties, m_psetClassName, stmtTbl);
		DBPropLSet lSet = new DBPropLSet(m_sysProperties, "LSET_"+graphProperties.getName(), m_lsetClassName);
		lSet.setPSet(pSet);
		graphProperties.addLSet(lSet);

		// Note - there is an assumption here that the order in which we add
		// these will be maintained - that's not true for graphs in general,
		// but our properties are always stored in our persistent graphs and
		// we know they do maintain ordering.
		return recreateSpecializedGraphs( graphProperties );
	}
	
	/**
	 * Construct and return a list of specialized graphs to match those in the store.
	 * @param graphProperties A set of customization properties for the graph.
	 */
	public List recreateSpecializedGraphs(DBPropGraph graphProperties) {
		
		List result = new ArrayList();
		int dbGraphId = graphProperties.getGraphId();

		Iterator it = graphProperties.getAllLSets();
		while(it.hasNext() ) {
			DBPropLSet lSetProps = (DBPropLSet)it.next();
			DBPropPSet pSetProps = lSetProps.getPset();

			IPSet pSet = createIPSetInstanceFromName(pSetProps.getType(), pSetProps.getTable());		
			result.add( createLSetInstanceFromName( lSetProps.getType(), pSet, dbGraphId));		
		}
		
		return result;		
	}
	
    /**
     * Create a new IPSet instance of the named implementation class and set the db connection.
     * 
     * @param pName name of a class that implements IPSet.
     * @return an instance of the named class with the db connection set.
     */
	private IPSet createIPSetInstanceFromName(String className, String tblName) {
		IPSet pSet = null;		
		try {
			String tblname;
			// get PSet
			pSet = (IPSet) Class.forName(className).newInstance();
			pSet.setDriver(this);
			pSet.setMaxLiteral(LONG_OBJECT_LENGTH);
			pSet.setSQLType(ID_SQL_TYPE);
			pSet.setSkipDuplicateCheck(SKIP_DUPLICATE_CHECK);
			pSet.setSkipAllocateId(SKIP_ALLOCATE_ID);
			pSet.setEmptyLiteralMarker(EMPTY_LITERAL_MARKER);
			pSet.setSQLCache(m_sql);
			pSet.setInsertByProcedure(INSERT_BY_PROCEDURE);
			pSet.setCachePreparedStatements(CACHE_PREPARED_STATEMENTS);
			pSet.setASTname(tblName);
		} catch (Exception e) {
			Log.warning("Unable to create IPSet instance " + e);
		}
		return pSet;
	}	
		
	private SpecializedGraph createLSetInstanceFromName(String lSetName, IPSet pset, int dbGraphID) {
		SpecializedGraph sg = null;		
		try {
			Class cls = Class.forName(lSetName);
			Class[] params = {IPSet.class, Integer.class};
			java.lang.reflect.Constructor con = cls.getConstructor(params);
			Object[] args = {pset, new Integer(dbGraphID)};
			sg = (SpecializedGraph) con.newInstance(args);
		} catch (Exception e) {
			Log.severe("Unable to create instance of SpecializedGraph " + e);
		}
		return sg;
	}

	/**
	 * Remove the specialized graph, erasing all trace of a Graph.
	 * @param graphId The identity of the Graph which these specialized graphs should hold
	 * @param graphProperties The properties for the graph to be removed.
	 */
	public void removeSpecializedGraphs( DBPropGraph graphProperties,
		List specializedGraphs) {
			
		Iterator it = specializedGraphs.iterator();
		while (it.hasNext()){
		   removeSpecializedGraph((SpecializedGraph) it.next());
		}

		// remove from system properties table
		// It is sufficient just to remove the lSet properties (it will
		// take care of deleting any pset properties automatically).			
		m_dbProps.removeGraph(graphProperties);
	}
	
	
	/**
	 * Remove specialized graph from the datastore.
	 * @param graph is the graph to be removed.
	 */
	private void removeSpecializedGraph(SpecializedGraph graph) {
		graph.clear();		
	}

	/**
	 * Method setDatabaseProperties.
	 * 
	 * Sets the current properties for the database.
	 * 
	 * @param databaseProperties is a Graph containing a full set of database properties
	 */
	public void setDatabaseProperties(Graph databaseProperties) {
		SpecializedGraph toGraph = getSystemSpecializedGraph();
		// really need to start a transaction here

		// Here add code to check if the database has been used - if so,
		// it's too late to change the properties, so throw an exception

		toGraph.clear();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		toGraph.add(databaseProperties, complete);

		// Now test the properties to see if it's a valid set - if not,
		// throw an exception - it's okay to check some things later (there's
		// no guarantee that every error will be caught here).

		// end transaction here.
	}

		
	/**
	 * Method getDefaultModelProperties 
	 * 
	 * Return the default properties for a new model stored in this database.
	 * If none are stored, then load default properties into the database.
	 * @return Graph containg the default properties for a new model
	 */
	public DBPropGraph getDefaultModelProperties() {
		SpecializedGraph sg = getSystemSpecializedGraph();
		DBPropGraph result = DBPropGraph.findPropGraphByName(sg, DEFAULT_PROPS);
		if (result == null) {
			Log.severe("No default Model Properties found");
			// Construct the parameters that will be the
			// default settings for any graph added to this database
			//new DBPropGraph( m_sysProperties, "default", "generic");
			//result = DBPropGraph.findPropGraph(sg, "default");	
		}
		return result;
	}

	/**
	 * Test if the database has previously been formatted.
	 * 
	 * @return boolean true if database is correctly formatted, false on any error.
	 */
	public boolean isDBFormatOK() {
		boolean result = false;
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			ResultSet alltables = dbmd.getTables(null, null, "JENA%", tableTypes);
			result = alltables.next();
			alltables.close();
		} catch (Exception e1) {
			;// if anything goes wrong, the database is not formatted correctly;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graphRDB.IRDBDriver#cleanDB()
	 */
	public void cleanDB() {
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			ResultSet alltables = dbmd.getTables(null, null, "JENA%", tableTypes);
			List tablesPresent = new ArrayList(10);
			while (alltables.next()) {
				tablesPresent.add(alltables.getString("TABLE_NAME").toUpperCase());
			}
			alltables.close();
			Iterator it = tablesPresent.iterator();
			while (it.hasNext()) {
				m_sql.runSQLGroup("dropTable", (String) it.next());
			}
			if (!SKIP_ALLOCATE_ID) {
				Iterator seqIt = getSequences().iterator();
				while (seqIt.hasNext()) {
					removeSequence((String)seqIt.next());
				}
			}
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver", e1);
		}
	}
	
	/**
	 * Removes named sequence from the database, if it exists.
	 * @param seqName
	 */
	public void removeSequence(String seqName) {
		if (sequenceExists(seqName)) {
			try {
				m_sql.runSQLGroup("DropSequence",seqName);
			} catch (Exception e) {
				Log.warning("Unable to drop sequence " + seqName + ": " + e);
			}
		}
	}
	/**
	 * Check database and see if named sequence exists.
	 * @param seqName
	 */
	public boolean sequenceExists(String seqName) {
		Object[] args = {seqName};
		ResultSetIterator it = null;
		try {
		    it = m_sql.runSQLQuery("SelectSequenceName",args);
		} catch (Exception e) {
		  Log.severe("Unable to select sequence " + seqName + ": " + e);
			}
		if (it != null) {
			return (it.hasNext());
		}		
		return false;
	}

	/**
	 * Check database and see if named sequence exists.
	 * @param seqName
	 */
	public List getSequences() {
		List results =  new ArrayList(10);
		Object[] args = {};
		ResultSetIterator it = null;
		try {
		    it = m_sql.runSQLQuery("SelectJenaSequences",args);
		    while (it.hasNext()) {
		    	results.add((String)it.getSingleton());
		    }
		    it.close();
		} catch (Exception e) {
		  Log.severe("Unable to select Jena sequences: " + e);
		 }
		return results;
	}
	
	/**
	 * Initialise a database ready to store RDF tables.
	 * @throws RDFDBException if the is a problem opening the connection or an internal SQL error.
	 * @deprecated Since Jena 2.0 this call is no longer needed - formatting 
	 * happens automatically as a side effect of creating Models - there should
	 * be no need for an application to interact directly with the driver.
	 */
	public void formatDB() throws RDFRDBException {
	}
	
	/**
	 * Create a table for storing asserted or reified statements.
	 * 
	 * @param graphId the graph which the table is created.
	 * @param isReif true if table stores reified statements.
	 * @return the name of the new table 
	 * 
	 */
	public String createTable( int graphId, boolean isReif) { 	
		String opname = isReif ? "createReifStatementTable" : "createStatementTable";
		int i = 0;
		String params[];
		while ( true ) {
			params = getCreateTableParams(graphId, isReif);
			try {
				m_sql.runSQLGroup(opname, params);
				break;
			} catch (SQLException e) {
				i++;
				if ( i > 5 ) {
					com.hp.hpl.jena.util.Log.warning("Problem creating table", e);
					throw new RDFRDBException("Failed to create table: " + params[0], e);
				}
			}
		}
		return params[0];
	}



	/**
	 * Throws an UnsupportedOperation exception.
	 * 
	 * @param opName name of the operation that's not supported.
	 */
	private void notSupported(String opName)
		{ throw new UnsupportedOperationException(opName); }
		
		/**
	 * If underlying database connection supports transactions, call abort()
	 * on the connection, then turn autocommit on.
	 */
	public synchronized void abort() throws RDFRDBException {
		if (transactionsSupported()) {
			try {
				if (inTransaction) {
				  Connection c = m_sql.getConnection();
				  c.rollback();
				  c.commit();
				  c.setAutoCommit(true);
				  inTransaction = false;
				}
			} catch (SQLException e) {
				throw new JenaException("Transaction support failed: ", e);
			}
		} else {
		}
	}
        



        
	/**
	 * If the underlying database connection supports transactions,
	 * turn autocommit off, then begin a new transaction.
	 * Note that transactions are associated with connections, not with
	 * Models.  This 
	 */
	public synchronized void begin() throws  RDFRDBException {
	  if (transactionsSupported()) {
		try {
			if (!inTransaction) {
				// Starting a transaction could require us to lose any cached prepared statements
				// for some jdbc drivers, currently I think all the drivers we use are safe and
				// is a major performance hit so commented out for now.
			  //m_sql.flushPreparedStatementCache();
			  Connection c = m_sql.getConnection();
			  c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			  c.setAutoCommit(false);
			  inTransaction = true;
			}
		} catch (SQLException e) {
			throw new RDFRDBException("Transaction support failed: ", e);
		}
	} else
		{ notSupported("begin transaction"); }
	}
	
	/**
	 * If the underlying database connection supports transactions,
	 * call commit(), then turn autocommit on.
	 */
	public void commit() throws RDFRDBException{
		if (transactionsSupported()) {
			try {
				  if (inTransaction) {
				  	Connection c = m_sql.getConnection();
					c.commit();
					c.setAutoCommit(true);
					c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
					inTransaction =  false;
				   }
				} catch (SQLException e) {
						throw new RDFRDBException("Transaction support failed: ", e);
				}
		} else {
				  notSupported("commit transaction"); 
		}
	}
        
	/**
	 * Return a string identifying underlying database type.
	 *
	 */
	public String getDatabaseType() {
		return(DATABASE_TYPE);
	}

	/**
	 * Returns true if the underlying database supports transactions.
	 */
	public boolean transactionsSupported() { 
		if (m_transactionsSupported != null) {
			return(m_transactionsSupported.booleanValue());	
		}
		
		if (m_dbcon != null) {
			try {
				Connection c = m_sql.getConnection();
				if ( c != null) {
					m_transactionsSupported = new Boolean(c.getMetaData().supportsMultipleTransactions());
					return(m_transactionsSupported.booleanValue());
				}
			} catch (SQLException e) {
				Log.severe("SQL Exception caught " + e);
			}
		}
		return (false);
			
		}
        



    //--------------------------------------------------jena 1 backward compatability

    /**
     * Close the driver 
     * 
     * Nothing to do for now.
     * 
     * @throws RDFDBException if there is an access problem
     * @deprecated Since Jena 2.0 this call is no longer required - just 
     * close the DBConnection - there should be no need for an application
     * to interact directly with the driver.
     * 
     */

    public void close() throws RDFRDBException {
    }


    /**
     * Returns true if the database layout supports multiple RDF models
     * in the same database.
     * @deprecated Since Jena 2.0 all databases support multiple models.
     */

    public boolean supportsMultipleModels() {
    	return true;
    }

    /**
     * Returns true if the database layout supports implicit reification
     * of statements (i.e. statements can be treated as resources).
     * @deprecated Since Jena 2.0 the reification API has changed.  The
     * new API is supported in all models, but the old Jena 1 API is no
     * longer supported.  This call will return false to indicate
     * to old code that the old style of jena reification is not supported.
     */

    public boolean supportsJenaReification() {
    	return false;
    }
    	
	
	/*
	 * The following routines are responsible for encoding nodes
	 * as database structures. For each node type stored (currently,
	 * literals, URI, blank), there are two possible encodings
	 * depending on the node size. Small nodes may be stored
	 * within a statement table. If the node is long (will not
	 * fit within the statement table), it is be stored in a
	 * separate table for that node type.
	 * 
	 * In addition, for resources (URI, blank nodes), the URI
	 * may be optionally compressed. Below, the possibilites
	 * are enumerated.
	 * 
	 * Literal Encoding in Statement Tables
	 * 	Short Literal:	Lv:[langLen]:[datatypeLen]:[langString][datatypeString]value[:]
	 * 	Long Literal:	Lr:dbid
	 * Literal Encoding in Long Literal Table
	 * 	Literal:		Lv:[langLen]:[datatypeLen]:[langString][datatypeString]head[:] hash tail
	 * 
	 * Comments:
	 * 		L indicates a literal
	 * 		v indicates a value
	 * 		r indicates a reference to another table
	 * 		: is used as a delimiter. note that MySQL trims trailing white space for
	 * 			certain VARCHAR columns so an extra delimiter is appended when necessary
	 * 			for those columns. it is not required for dbid, however. 
	 * 		dbid references the long literal table
	 * 		langLen is the length of the language identifier for the literal
	 * 		langString is the language identifier
	 * 		datatypeLen is the length of the datatype for the literal
	 * 		datatypeString is the datatype for the literal
	 * 		value is the lexical form of the string
	 * 		head is a prefix of value that can be indexed
	 * 		hash is the CRC32 hash value for the tail
	 * 		tail is the remainder of the value that cannot be indexed
	 * 		
	 * 
	 * 
	 * URI Encoding in Statement Tables
	 * 	Short URI:	Uv:[pfx_dbid]:URI[:]
	 * 	Long URI:	Ur:[pfx_dbid]:dbid
	 * URI Encoding in Long URI Table
	 * 	URI:		Uv:head[:] hash tail
	 * 
	 * Comments:
	 * 		U indicates a URI
	 * 		pfx_dbid references the prefix table. if the prefix is too
	 * 			short (i.e., the length of the prefix is less than
	 * 			URI_COMPRESS_LENGTH), the URI is not compressed and
	 * 			pfx_dbid is null.
	 * 		URI is the complete URI
	 * 		other notation same as for literal encoding
	 * 
	 * Blank Node Encoding in Statement Tables
	 * 	Blank Node:	Bv:[pfx_dbid]:bnid
	 * 
	 * Comments:
	 * 		B indicates a blank node
	 * 		bnid is the blank node identifier
	 * 		other notation same as above
	 * 		blank node encoding should always fit within a statement table
	 * 		Note: currently, blank nodes are always stored uncompressed (pfix_dbid is null). 
	 * 
	 * Prefix Encoding in Prefix Table
	 * 	Prefix:	Pv:val[:] [hash] [tail]
	 * 
	 * Comments:
	 * 		P indicates a prefix
	 * 		other notation same as above
	 * 		hash and tail are only required for long prefixes.
	 * 
	 */
	 
	 
	 
	protected static String RDBCodeURI = "U";
	protected static String RDBCodeBlank = "B";
	protected static String RDBCodeLiteral = "L";
	protected static String RDBCodePrefix = "P";
	protected static String	RDBCodeValue = "v";
	protected static String RDBCodeRef = "r";
	protected static String RDBCodeDelim = ":";
	protected static char RDBCodeDelimChar = ':';

		
    
	/**
	* Convert a node to a string to be stored in a statement table.
	* @param Node The node to convert to a string. Must be a concrete node.
	* @param addIfLong If the node is a long object and is not in the database, add it.
	* @return the string or null if failure.
	*/
	public String nodeToRDBString ( Node node, boolean addIfLong ) throws RDFRDBException {
		String res = null;
		if ( node.isURI() ) {
			String uri = new String(((Node_URI) node).getURI());
			if ( uri.startsWith(RDBCodeURI) ) {
				throw new RDFRDBException ("URI Node looks like a blank node: " + uri );
			}
			// TODO: need to write special version of splitNamespace for rdb.
			//		or else, need a guarantee that splitNamespace never changes.
			//		the problem is that if the splitNamespace algorithm changes,
			//		then URI's may be encoded differently. so, URI's in existing
			//		databases may become inaccessible.
			int pos = 0;
			boolean noCompress;
			String pfx;
			String qname;
			if ( URI_COMPRESS == true ) {
				pos = Util.splitNamespace(uri);
				noCompress = (pos == uri.length()) || (pos <= URI_COMPRESS_LENGTH);
			} else
				noCompress = true;
			if ( noCompress ) {
				pfx = RDBCodeDelim + RDBCodeDelim;
				qname = uri;
			} else {
				DBIDInt pfxid = URItoPrefix(uri, pos, addIfLong);
				if ( pfxid == null ) return res;
				pfx = RDBCodeDelim + ((DBIDInt)pfxid).getIntID() + RDBCodeDelim;
				qname = uri.substring(pos);
			}
			int encodeLen = RDBCodeURI.length() + 1 + pfx.length() + EOS_LEN;
			boolean URIisLong = objectIsLong(encodeLen,qname);
			if ( URIisLong ) {
				int	dbid;
				// belongs in URI table
				DBIDInt URIid = getURIID(qname,addIfLong);
				if ( URIid == null ) return res;
				dbid = URIid.getIntID();
				res = new String(RDBCodeLiteral + RDBCodeRef + RDBCodeDelim + dbid);
			} else {
				res = RDBCodeURI + RDBCodeValue + pfx + qname + EOS;
			}
		} else if ( node.isLiteral() ){
			// TODO: may need to encode literal value when datatype is not a string.
			Node_Literal litNode = (Node_Literal) node;
			LiteralLabel ll = litNode.getLiteral();
			String lval = ll.getLexicalForm();
			String lang = ll.language();
			String dtype = ll.getDatatypeURI();
			String ld = litLangTypeToRDBString(lang,dtype);
			int encodeLen = RDBCodeLiteral.length() + 2 + ld.length() + EOS_LEN;
			boolean litIsLong = objectIsLong(encodeLen,lval);		
			if ( litIsLong ) {
				int	dbid;
				// belongs in literal table
				DBIDInt lid = getLiteralID(litNode,addIfLong);
				if ( lid == null ) return res;
				dbid = lid.getIntID();
				res = new String(RDBCodeLiteral + RDBCodeRef + RDBCodeDelim + dbid);
			} else {
				res = new String(RDBCodeLiteral + RDBCodeValue + RDBCodeDelim + ld + lval + EOS);
			}    		
		} else if ( node.isBlank() ) {
			// TODO: prefix compression for blank nodes.
			res = new String(RDBCodeBlank + RDBCodeValue + RDBCodeDelim + RDBCodeDelim
							+ node.getBlankNodeId().toString()+ EOS);
		}else {
			throw new RDFRDBException ("Expected Concrete Node, got " + node.toString() );	
		}
		return res;
	}
	
	/**
	* Convert an RDB string to the node that it encodes. Return null if failure.
	* @param RDBstring The string to convert to a node.
	* @return The node or null if failure.
	*/
	public Node RDBStringToNode ( String RDBString ) throws RDFRDBException {	
		Node res = null;
		int len = RDBString.length();
		if ( len < 3 ) 
			throw new RDFRDBException("Bad RDBString Header: " + RDBString);
		String nodeType = RDBString.substring(0,1);
		String valType = RDBString.substring(1,2);
		if ( (!(valType.equals(RDBCodeRef) || valType.equals(RDBCodeValue))) ||
				(RDBString.charAt(2) != RDBCodeDelimChar) || (len < 4) )
				throw new RDFRDBException("Bad RDBString Header: " + RDBString);

		int pos = 3;
		int npos;
		
		if ( nodeType.equals(RDBCodeURI) ) {
			ParseInt pi = new ParseInt(pos);
			String prefix = "";
			RDBStringParseInt(RDBString, pi, false);
			if ( pi.val != null ) {
				if ( URI_COMPRESS == false )
					throw new RDFRDBException("Bad URI: Prefix Compression Disabled: " + RDBString);
				prefix = IDtoPrefix(pi.val.intValue());
				if ( prefix == null )
					throw new RDFRDBException("Bad URI Prefix: " + RDBString);
			}
			pos = pi.pos + 1;
			String qname = RDBString.substring(pos,len - EOS_LEN);
			if ( valType.equals(RDBCodeRef) ) {
				qname = IDtoURI(qname);
				if ( qname == null )
					throw new RDFRDBException("Bad URI: " + RDBString);
			}
			res = Node.createURI(prefix + qname);
			
		} else if ( nodeType.equals(RDBCodeLiteral) ) {
			ParseInt pi = new ParseInt(pos);
			String litString = null;
			if ( valType.equals(RDBCodeRef) ) {
				RDBStringParseInt(RDBString,pi,true);
				if ( pi.val != null )
					litString = IDtoLiteral(pi.val.intValue());
				if ( litString == null )
					throw new RDFRDBException("Bad Literal Reference: " + RDBString);
			} else
				litString = RDBString.substring(pos,len-EOS_LEN);
			len = litString.length();
			String lang;
			String dtype;
			int langLen = 0;
			int dtypeLen = 0;
			LiteralLabel llabel;
			pi.pos = 0;
			RDBStringParseInt(litString, pi, false);
			if ( pi.val == null ) langLen = 0; 
			else langLen = pi.val.intValue(); 
			pi.pos = pi.pos + 1;
			RDBStringParseInt(litString, pi, false);	
			if ( pi.val == null ) dtypeLen = 0;
			else dtypeLen = pi.val.intValue();
			pos = pi.pos + 1;	
			if ( (pos + langLen + dtypeLen) > len )
					throw new RDFRDBException("Malformed Literal: " + litString);	
			lang = litString.substring(pos,pos+langLen);
			pos = pos + langLen;
			dtype = litString.substring(pos,pos+dtypeLen);
			pos = pos + dtypeLen;
			
			String val = litString.substring(pos);
			
			if ( (dtype == null) || (dtype.equals(""))  ) {
				llabel = new LiteralLabel(val, lang == null ? "" : lang);
			} else {
				RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtype);
				llabel = new LiteralLabel(val, lang == null ? "" : lang, dt);
			}	 
			res = Node.createLiteral(llabel);
			
		} else if ( nodeType.equals(RDBCodeBlank) ) {
			// TODO: implement prefix compression for blank nodes
			String bstr = RDBString.substring(4,len-EOS_LEN);
			res = Node.createAnon( new AnonId (bstr) );
		} else
			throw new RDFRDBException ("Invalid RDBString Prefix, " + RDBString );	
		return res;
	}
	
	class ParseInt {
		int	pos;
		Integer val;	
		ParseInt(int p) {pos = p;}
	}

	protected void RDBStringParseInt ( String RDBString, ParseInt pi, boolean toEnd ) {
		int npos = toEnd ? RDBString.length() : RDBString.indexOf(RDBCodeDelimChar,pi.pos);
		if ( npos < 0 ) {
			throw new RDFRDBException("Bad RDB String: " + RDBString);
		}
		String intStr = RDBString.substring(pi.pos,npos);
		pi.pos = npos;
		if ( intStr.equals("") )
			pi.val = null;
		else try {
			pi.val = new Integer(intStr);
		} catch (NumberFormatException e1) {
			throw new RDFRDBException("Bad RDB String: " + RDBString);
		} 
		return;
	}
	
	

	DBIDInt URItoPrefix ( String uri, int pos, boolean add ) {
		RDBLongObject	lobj = PrefixToLongObject(uri,pos);
		return getLongObjectID(lobj, PREFIX_TABLE, add);
	}
	
	protected RDBLongObject PrefixToLongObject ( String prefix, int split ) {
		RDBLongObject	res = new RDBLongObject();
		int				headLen;
		int				avail;

		res.head = RDBCodePrefix + RDBCodeValue + RDBCodeDelim;
		headLen = res.head.length();
		avail = INDEX_KEY_LENGTH - (headLen + EOS_LEN);
		if ( split > avail ) {
			res.head = res.head + prefix.substring(0,avail) + EOS;
			res.tail = prefix.substring(avail);
			res.hash = stringToHash(res.tail);
		} else {
			res.head = res.head + prefix;
			res.tail = "";
		}
		res.head = res.head + EOS;
		return res;	
	}

	/**
	* Encode a literal node's lang and datatype as a string of the
	* form ":[langLen]:[datatypeLen]:[langString][dataTypeString]"
	* @return the string.
	*/
	public String litLangTypeToRDBString ( String lang, String dtype ) throws RDFRDBException {
		String res = RDBCodeDelim;
		res = ((lang == null) ? "" : Integer.toString(lang.length())) + RDBCodeDelim;
		res = res + ((dtype == null) ? "" : Integer.toString(dtype.length())) + RDBCodeDelim;
		res = res + (lang == null ? "" : lang) + (dtype == null ? "" : dtype);
		return res;
	}
	
	/**
	* Check if an object is long, i.e., it exceeds the length
	* limit for storing in a statement table.
	* @return true if literal is long, else false.
	*/
	protected boolean objectIsLong ( int encodingLen, String objAsString ) {
		return ( (encodingLen + objAsString.length()) > LONG_OBJECT_LENGTH);
	}
	
	class RDBLongObject {
		String		head;		/* prefix of long object that can be indexed */
		long		hash;		/* hash encoding of tail */
		String		tail;		/* remainder of long object */
	}
	
	protected RDBLongObject literalToLongObject ( Node_Literal node ) {
		RDBLongObject	res = new RDBLongObject();
		int				headLen;
		int				avail;
		LiteralLabel 	l = node.getLiteral();
		String 			lang = l.language();
		String 			dtype = l.getDatatypeURI();
		String 			val = l.getLexicalForm();
		String			langType = litLangTypeToRDBString(lang,dtype);

		res.head = RDBCodeLiteral + RDBCodeValue + RDBCodeDelim + langType;
		headLen = res.head.length();
		avail = INDEX_KEY_LENGTH - (headLen + EOS_LEN);
		if ( val.length() > avail ) {
			res.head = res.head + val.substring(0,avail);
			res.tail = val.substring(avail);
			res.hash = stringToHash(res.tail);
		} else {
			res.head = res.head + val;
			res.tail = "";
		}
		res.head = res.head + EOS;
		return res;
	}
	
		
	protected long stringToHash ( String str ) {
		CRC32 checksum = new CRC32();
		checksum.update(str.getBytes());
		return checksum.getValue();
	}
	
	/**
	 * Return the database ID for the URI, if it exists
	 */
	public DBIDInt getURIID(String qname, boolean add) throws RDFRDBException {
		RDBLongObject	lobj = URIToLongObject (qname);
		return getLongObjectID(lobj, LONG_URI_TABLE, add);
	}

	protected RDBLongObject URIToLongObject ( String qname ) {
		RDBLongObject	res = new RDBLongObject();
		int				headLen;
		int				avail;

		res.head = RDBCodeURI + RDBCodeValue + RDBCodeDelim;
		headLen = res.head.length();
		avail = INDEX_KEY_LENGTH - (headLen + EOS_LEN);
		if ( qname.length() > avail ) {
			res.head = res.head + qname.substring(0,avail) + EOS;
			res.tail = qname.substring(avail);
			res.hash = stringToHash(res.tail);
		} else {
			res.head = res.head + qname;
			res.tail = "";
		}
		res.head = res.head + EOS;
		return res;	
	}
			
	
	/**
	 * Return the database ID for the literal, if it exists
	 */
	public DBIDInt getLiteralID(Node_Literal lnode, boolean add) throws RDFRDBException {
		RDBLongObject	lobj = literalToLongObject (lnode);
		return getLongObjectID(lobj, LONG_LIT_TABLE, add);
	}
			
	public DBIDInt getLongObjectID(RDBLongObject lobj, String table, boolean add) throws RDFRDBException {
		try {
			String opName = "getLongObjectID";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opName, table); 
			ps.setString(1,lobj.head);
			ps.setLong(2,lobj.hash);
			ResultSet rs = ps.executeQuery();
			DBIDInt result = null;
			if (rs.next()) {
				result = wrapDBID(rs.getObject(1));
			} else {
				if ( add )
					result = addRDBLongObject(lobj, table);
			}
		 //   m_sql.returnPreparedSQLStatement(ps, opName);
			return result;
		} catch (SQLException e1) {
			// /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
			throw new RDFRDBException("Failed to find literal", e1);
		}
	}
 
	/**
	 * Insert a long object into the database.  
	 * This assumes the object is not already in the database.
	 * @return the db index of the added literal 
	 */
	public DBIDInt addRDBLongObject(RDBLongObject lobj, String table) throws RDFRDBException {
		try {
			String opname = "insertLongObject";           			
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opname, table);
			int argi = 1;
			 ps.setString(argi++, lobj.head);
			 if ( lobj.tail.length() > 0 ) {
			 	ps.setLong(argi++, lobj.hash);
			 	ps.setString(argi++, lobj.tail);
			 } else {
			 	ps.setNull(argi++,java.sql.Types.BIGINT);
				ps.setNull(argi++,java.sql.Types.VARCHAR);     
			 }
/*			if (isBlob || (len == 0) ) {
				// First convert the literal to a UTF-16 encoded byte array
				// (this wouldn't be needed for jdbc 2.0 drivers but not all db's have them)
				byte[] temp = lit.getBytes("UTF-8");
				int lenb = temp.length;
				//System.out.println("utf-16 len = " + lenb);
				byte[] litData = new byte[lenb + 4];
				litData[0] = (byte)(lenb & 0xff);
				litData[1] = (byte)((lenb >> 8) & 0xff);
				litData[2] = (byte)((lenb >> 16) & 0xff);
				litData[3] = (byte)((lenb >> 24) & 0xff);
				System.arraycopy(temp, 0, litData, 4, lenb);
                
				// Oracle has its own way to insert Blobs
				if (isBlob && m_driver.getDatabaseType().equalsIgnoreCase("Oracle")) {
					//TODO fix to use Blob
					// For now, we do not support Blobs under Oracle
					throw new RDFRDBException("Oracle driver does not currently support large literals.");
				} else {
					ps.setBinaryStream(argi++, new ByteArrayInputStream(litData), litData.length);
				}
			} 
*/            
			ps.executeUpdate();
			return wrapDBID(new Integer(getLastInsertID()));
		} catch (Exception e1) {
			/* DEBUG */ System.out.println("Problem on long object (l=" + lobj.head + ") " + e1 );
			// System.out.println("ID is: " + id);
			throw new RDFRDBException("Failed to add long object ", e1);
		}
	}
	
	/**
	 * Return the prefix string that has the given prefix id.
	 * @param prefixID - the dbid of the prefix.
	 * @return the prefix string or null if it does not exist.
	 */
	protected String IDtoPrefix ( int prefixID ) {
		return IDtoString ( prefixID, PREFIX_TABLE, RDBCodePrefix);
	}
	
	/**
 	* Return the URI string that has the given database id.
 	* @param uriID - the dbid of the uri, as a string.
 	* @return the uri string or null if it does not exist.
 	*/
	protected String IDtoURI ( String uriID ) {
		return IDtoString ( uriID, LONG_URI_TABLE, RDBCodeURI);
	}

	/**
	* Return the long literal string that has the given database id.
	* @param litID - the dbid of the literal..
	* @return the long literal string or null if it does not exist.
	*/
	protected String IDtoLiteral ( int litID ) {
		return IDtoString ( litID, LONG_LIT_TABLE, RDBCodeLiteral);
	}
	

	
	protected String IDtoString ( String dbidAsString, String table, String RDBcode ) {
		int	dbID;
		String res = null;
		try {
			dbID = Integer.parseInt(dbidAsString);
		} catch (NumberFormatException e1) {
			throw new RDFRDBException("Invalid Object ID: " + dbidAsString);
		}
		return IDtoString (dbID, table, RDBcode);
	}

	protected String IDtoString ( int dbID, String table, String RDBcode ) {
		String res = null;
		RDBLongObject lobj = IDtoLongObject(dbID, table);
		if ( lobj == null )
			throw new RDFRDBException("Invalid Object ID: " + dbID);
		// debug check
		if ( !lobj.head.substring(0,3).equals(RDBcode + RDBCodeValue + RDBCodeDelim) )
			throw new RDFRDBException("Malformed URI in Database: " + lobj.head);
		res = lobj.head.substring(3,lobj.head.length() - EOS_LEN);
		res = res + lobj.tail;	
		return res;
	}

	
	protected RDBLongObject IDtoLongObject ( int dbid, String table ) {
		RDBLongObject	res = null;
		try {
			String opName = "getLongObject";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opName, table); 
			ps.setInt(1,dbid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				res = new RDBLongObject();
				res.head = rs.getString(1);
				res.tail = rs.getString(2);			
			}
			return res;
		} catch (SQLException e1) {
			// /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
			throw new RDFRDBException("Failed to find literal", e1);
		}		
	}
	
	protected RDBLongObject IDtoLongObject ( String idAsString, String table ) {
		RDBLongObject res = null;
		int dbid;
		try {
			dbid = Integer.parseInt(idAsString);
		} catch (NumberFormatException e1) {
			throw new RDFRDBException("Invalid Object ID: " + idAsString);
		}
		return IDtoLongObject(dbid,table);
	}
    
 
	/**
	 * Convert the raw SQL object used to store a database identifier into a java object
	 * which meets the DBIDInt interface.
	 */
	public DBIDInt wrapDBID(Object id) throws RDFRDBException {
		if (id instanceof Number) {
			return new DBIDInt(((Number)id).intValue());
		} else if (id == null) {
			return null;
		} else {
			throw new RDFRDBException("Unexpected DB identifier type: " + id);
			//return null;
		}
	}

}


/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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
 */
