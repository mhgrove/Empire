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
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 30, 2009 3:30:32 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
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

            //validateRemove(theGraph);
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}

    private void validateRemove(Graph theGraph) {
        for (Statement aStmt : new ExtendedGraph(theGraph)) {
            if (getRepository().hasStatement(aStmt.getSubject(), aStmt.getPredicate(), aStmt.getObject())) {
                throw new IllegalStateException("Remove failed");
            }
        }
    }
}
