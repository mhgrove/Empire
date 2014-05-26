/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.empire.util;

import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.api.MutableTestDataSourceFactory;
import com.google.common.base.Strings;
import junit.framework.Test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Utility methods for the test suite.</p>
 *
 * @author Michael Grove
 */
public class TestUtil {

    public static DataSource createTestSource() throws DataSourceException {
		Map<String, Object> aMap = new HashMap<String, Object>();
		aMap.put("factory", "test-source");
		aMap.put("files", lookupFile( "data/lite.nasa.nt" ).getAbsolutePath() );

		return new MutableTestDataSourceFactory().create(aMap);
	}

    public static File getProjectHome() {
        String home = System.getProperty("test.home", "");
        if (Strings.isNullOrEmpty(home)) {
            home = System.getProperty("user.dir");
            return new File( home );
        }
        else {
            return new File(home);
        }
    }

    public static void setConfigSystemProperty( String configSystemProperty ) {
        String path = null;
        File f = lookupFile( configSystemProperty );
        if ( f != null && f.exists() ) {
            path = f.getAbsolutePath();
        }
        if ( path != null ) {
            System.setProperty( "empire.configuration.file", path );
        } else {
            System.setProperty( "empire.configuration.file", configSystemProperty );
        }
    }



    public static String getTestDataDirPath( String dataDir ) {
        File d = lookupFile( dataDir );
        if ( d != null ) {
            return d.getAbsolutePath();
        } else {
            return dataDir;
        }
    }

    protected static File lookupFile( String base ) {
        //TODO a more robust implementation shuold not use Files, but more generalized Streams that can be looked up using CL.getResource()
        File f;
        f = new File( TestUtil.getProjectHome(), base );
        if ( f.exists() ) {
            return f;
        }
        f = new File( TestUtil.getProjectHome(), "test/" + base );
        if ( f.exists() ) {
            return f;
        }
        f = new File( TestUtil.getProjectHome(), "core/test/" + base );
        if ( f.exists() ) {
            return f;
        }
        f = new File( TestUtil.getProjectHome(), "../core/test/" + base );
        if ( f.exists() ) {
            return f;
        }
        return null;
    }
}
