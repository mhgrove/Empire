package com.clarkparsia.empire.jena;

import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.enhanced.Personality;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

/**
 * Simple override of Jena Model for use with JPA so that pooled jdbc connection from DataSource and Jena Model
 * can be committed and closed in synchrony.
 * 
 * @author uoccou
 * @since 0.7
 * @version 0.7
 */
class SDBModelWithStore extends AbstractDelegateModel {

	/**
	 * JDBC connection to the actual store
	 */
	private Connection sdbc = null;

	/**
	 * Create a new ModelWithStore
	 *
	 * @param m the jena model of SDB
	 * @param sdbc the jdbc connection
	 */
	public SDBModelWithStore(Model m, Connection sdbc) {
		super(m);
		this.sdbc = sdbc;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void enterCriticalSection(boolean readLockRequested) {
		//super.enterCriticalSection(readLockRequested);
		log.debug("spoofing critical section");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void leaveCriticalSection() {
		//super.leaveCriticalSection();
		log.debug("end of spoof critical section");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void close() {

		try {
			if (null != sdbc && !sdbc.isClosed()) {
				sdbc.close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		super.close();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Model commit() {
		Model m = null;
		try {

			if (null != sdbc && !sdbc.isClosed()) {
				sdbc.commit();
			}

			m = super.commit();
		}
		catch (SQLException e) {
			log.error("SQL Exception trying to commit to the underlying JDBC connection", e);
		}
		return m;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Model begin() {
		Model m = super.begin();

		try {
			sdbc.setAutoCommit(false);
		}
		catch (SQLException e) {
			log.error("SQL Exception trying to disable auto commit", e);
		}

		return m;
	}

	public Connection getSDBConnection() {
		return sdbc;
	}

	public void setSDBConnection(Connection sdbc) {
		this.sdbc = sdbc;
	}
}
