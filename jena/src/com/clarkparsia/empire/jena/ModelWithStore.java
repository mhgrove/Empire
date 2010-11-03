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
 *
 */
public class ModelWithStore extends AbstractDelegateModel {

	private Connection sdbc = null;
	
	public ModelWithStore(Model m, Connection sdbc) {
		super(m);
		this.sdbc = sdbc;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void enterCriticalSection(boolean readLockRequested) {
		// TODO Auto-generated method stub
		//super.enterCriticalSection(readLockRequested);
		log.debug("spoofing critical section");
	}

	@Override
	public void leaveCriticalSection() {
		// TODO Auto-generated method stub
		//super.leaveCriticalSection();
		log.debug("end of spoof critical section");
	}
	
	public void close() {
		
		try {
			if ( null != sdbc && !sdbc.isClosed() )
        		sdbc.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.close();
	}
	public Model commit() {
		Model m = null;
		try {
			
			if ( null != sdbc && !sdbc.isClosed() )
				sdbc.commit();
			
			m = super.commit();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return m;
	}
	public Model begin() {
		Model m = super.begin();
		try {
			sdbc.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
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
