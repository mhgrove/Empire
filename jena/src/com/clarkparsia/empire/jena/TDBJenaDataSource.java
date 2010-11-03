package com.clarkparsia.empire.jena;


import com.clarkparsia.empire.ds.DataSourceException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;

/**
 * Extension of JenaDataSource to allow TDB data to be synced to disk on commit.
 *  
 * Would also be possible in TransactionDataSource override if used something like 
 * <code>TDB.sync(((TDBModel)((JenaDataSource)DataSourceUtil.asTripleStore(mDataSource)).getModel()))</code>
 *  
 * @author uoccou
 *
 */
public class TDBJenaDataSource extends JenaDataSource {

	public TDBJenaDataSource(Model theModel) {
		super(theModel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void begin() throws DataSourceException {
		
		//no-op
	}

	@Override
	public void commit() throws DataSourceException {
		//sync to disk
		TDB.sync(getModel());

				
	}

	

	@Override
	public void rollback() throws DataSourceException {
		//no-op
	}

}
