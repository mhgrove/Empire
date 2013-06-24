/*
 * Copyright (c) 2009-2013 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.empire.jena;

import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Simple override of Jena Model for use with JPA so that pooled jdbc connection from DataSource and Jena Model
 * can be committed and closed in synchrony.
 *
 * @author  uoccou
 * @since   0.7
 * @version 1.0
 */
final class SDBModelWithStore extends AbstractDelegateModel {

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