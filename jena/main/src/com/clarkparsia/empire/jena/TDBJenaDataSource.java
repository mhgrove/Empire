package com.clarkparsia.empire.jena;

import com.clarkparsia.empire.ds.DataSourceException;

import com.clarkparsia.empire.ds.SupportsTransactions;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;

/**
 * Extension of JenaDataSource to allow TDB data to be synced to disk on commit.
 * <p/>
 * Would also be possible in TransactionDataSource override if used something like
 * <code>TDB.sync(((TDBModel)((JenaDataSource)DataSourceUtil.asTripleStore(mDataSource)).getModel()))</code>
 *
 * @author uoccou
 * @version 0.7
 * @since 0.7
 */
class TDBJenaDataSource extends JenaDataSource implements SupportsTransactions {

	public TDBJenaDataSource(Model theModel) {
		super(theModel);
	}

	/**
	 * @inheritDoc
	 */
	public void begin() throws DataSourceException {
		//no-op
	}

	/**
	 * @inheritDoc
	 */
	public void commit() throws DataSourceException {
		//sync to disk
		TDB.sync(getModel());
	}

	/**
	 * @inheritDoc
	 */
	public void rollback() throws DataSourceException {
		//no-op
	}
}
