package com.clarkparsia.empire.test.api;

import com.clarkparsia.sesame.utils.ExtendedGraph;
import org.openrdf.model.Statement;
import org.openrdf.sesame.admin.DummyAdminListener;
import com.clarkparsia.empire.MutableDataSource;
import com.clarkparsia.empire.DataSourceException;
import org.openrdf.model.Graph;
import org.openrdf.sesame.config.AccessDeniedException;

import java.io.IOException;

/**
 * <p>Implementation of the MutableDataSource interface for testing</p>
 *
 * @author Michael Grove
 */
public class MutableTestDataSource extends TestDataSource implements MutableDataSource {

	public MutableTestDataSource() {
        super();
	}

	public MutableTestDataSource(final Graph theGraph) {
		super(theGraph);
	}

	/**
	 * @inheritDoc
	 */
	public void add(final Graph theGraph) throws DataSourceException {
		try {
			getRepository().addGraph(theGraph);
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove(final Graph theGraph) throws DataSourceException {
		try {
			getRepository().removeGraph(theGraph);
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}
}
