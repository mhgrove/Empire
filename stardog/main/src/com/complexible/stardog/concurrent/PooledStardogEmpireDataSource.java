package com.complexible.stardog.concurrent;

import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.QueryException;
import com.clarkparsia.empire.ds.ResultSet;
import com.complexible.common.base.Option;
import com.complexible.common.openrdf.model.Graphs;
import com.complexible.common.openrdf.util.AdunaIterations;
import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.ConnectionPool;
import com.complexible.stardog.api.ConnectionPoolConfig;
import com.complexible.stardog.empire.StardogEmpireDataSource;
import org.openrdf.model.Graph;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Anand on 3/3/15.
 */
public class PooledStardogEmpireDataSource extends StardogEmpireDataSource{

    private ConnectionPool pool;
    private ThreadLocal<Connection> tlCurrentConnection = new ThreadLocal<>();
    private ThreadLocal<Boolean> tlConnected = new ThreadLocal<>();
    private ConnectionConfiguration connectionConfiguration;

    public static final String STARDOG_POOL_MIN = "STARDOG_POOL_MIN";
    public static final String STARDOG_POOL_MAX = "STARDOG_POOL_MAX";
    public static final String STARDOG_POOL_EXP = "STARDOG_POOL_EXP";
    public static final String STARDOG_POOL_BLOCK = "STARDOG_POOL_BLOCK";

    public static final Integer STARDOG_POOL_MIN_DEF = new Integer(10);
    public static final Integer STARDOG_POOL_MAX_DEF = new Integer(100);
    public static final Long STARDOG_POOL_EXP_DEF = new Long(60000);
    public static final Long STARDOG_POOL_BLOCK_DEF = new Long(60000);

    public PooledStardogEmpireDataSource(ConnectionConfiguration theConfiguration) {
        super(theConfiguration);
        this.connectionConfiguration = theConfiguration;
        ConnectionPoolConfig aConfig = ConnectionPoolConfig
                .using(theConfiguration)		      // use my connection configuration to spawn new connections
                .minPool(theConfiguration.get(Option.create(STARDOG_POOL_MIN, STARDOG_POOL_MIN_DEF)))			      // the number of objects to start my pool with
                .maxPool(theConfiguration.get(Option.create(STARDOG_POOL_MAX, STARDOG_POOL_MAX_DEF)))			      // the maximum number of objects that can be in the pool (leased or idle)
                .expiration(theConfiguration.get(Option.create(STARDOG_POOL_EXP, STARDOG_POOL_EXP_DEF)), TimeUnit.MILLISECONDS)	      // Connections can expire after being idle for 1 hr.
                .blockAtCapacity(theConfiguration.get(Option.create(STARDOG_POOL_BLOCK, STARDOG_POOL_BLOCK_DEF)), TimeUnit.MILLISECONDS); // I want obtain to block for at most 1 min while trying to obtain a connection.
        pool = aConfig.create();
    }

    public Connection getCurrentConnection() throws StardogException {
        Connection connection = tlCurrentConnection.get();
        if(connection == null) {
            long start = System.currentTimeMillis();
            connection = pool.obtain();
//            System.out.println("CONNECTION CREATION TOOK : " + (System.currentTimeMillis() - start) + " : " + Integer.toHexString(connection.hashCode()));
            tlCurrentConnection.set(connection);
        }
        return connection;
    }

    public void releaseConnection() throws StardogException {
        Connection connection = tlCurrentConnection.get();
        if(connection != null) {
            pool.release(connection);
            tlCurrentConnection.set(null);
        }
    }

    private void setConnectionStatus(boolean option) {
        tlConnected.set(Boolean.valueOf(option));
        setConnected(option);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void connect() throws ConnectException {
        try {
            getCurrentConnection();
            setConnectionStatus(true);
        }
        catch (StardogException e) {
            throw new ConnectException(e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void disconnect() {
        try {
            releaseConnection();
            setConnectionStatus(false);
            pool.shutdown();
        }
        catch (StardogException e) {
            // TODO: log me
            System.err.println(e.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public ResultSet selectQuery(final String theQuery) throws QueryException {
        assertConnected();

        try {
            final TupleQueryResult aResults = getCurrentConnection().select(theQuery).execute();
            return new ResultSet() {
                @Override
                public void close() {
                    AdunaIterations.closeQuietly(aResults);
                }

                @Override
                public boolean hasNext() {
                    try {
                        return aResults.hasNext();
                    }
                    catch (QueryEvaluationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public BindingSet next() {
                    try {
                        return aResults.next();
                    }
                    catch (QueryEvaluationException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        catch (StardogException e) {
            throw new QueryException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Graph graphQuery(final String theQuery) throws QueryException {
        assertConnected();

        GraphQueryResult aResult = null;

        try {
            aResult = getCurrentConnection().graph(theQuery).execute();
            return Graphs.newGraph(aResult);
        }
        catch (QueryEvaluationException e) {
            throw new QueryException(e);
        }
        catch (StardogException e) {
            throw new QueryException(e);
        }
        finally {
            if (aResult != null) {
                try {
                    aResult.close();
                }
                catch (QueryEvaluationException e) {
                    // TODO: log me
                    System.err.println("There was an error closing a query result: " + e.getMessage());
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean ask(final String theQuery) throws QueryException {
        assertConnected();

        try {
            return getCurrentConnection().ask(theQuery).execute();
        }
        catch (StardogException e) {
            throw new QueryException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Graph describe(final String theQuery) throws QueryException {
        return graphQuery(theQuery);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void add(final Graph theGraph) throws DataSourceException {
        assertConnected();
        try {
            getCurrentConnection().add().graph(theGraph);
        }
        catch (StardogException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void remove(final Graph theGraph) throws DataSourceException {
        assertConnected();
        try {
            getCurrentConnection().remove().graph(theGraph);
        }
        catch (StardogException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void begin() throws DataSourceException {
        assertConnected();

        try {
            getCurrentConnection().begin();
        }
        catch (StardogException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void commit() throws DataSourceException {
        assertConnected();

        try {
            getCurrentConnection().commit();
            releaseConnection();
        }
        catch (StardogException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void rollback() throws DataSourceException {
        assertConnected();

        try {
            getCurrentConnection().rollback();
            releaseConnection();
        }
        catch (StardogException e) {
            throw new DataSourceException(e);
        }
    }
}
