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

package com.clarkparsia.empire.sesame;

import java.util.Map;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceUtil;
import com.clarkparsia.empire.ds.TripleSource;
import com.google.common.collect.Maps;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since   0.8
 * @version 0.8
 */
public class TestSesameDS {
    @Test
    public void testDS() throws Exception{
        Map<String, Object> aMap = Maps.newHashMap();
        aMap.put("factory", "sesame");

        DataSource aSesameSource = Empire.get().persistenceProvider().createDataSource("test-sesame", aMap);

        TripleSource aTripleSrc = DataSourceUtil.asTripleSource(aSesameSource);

        // Sesame sources are triple sources
        assertTrue(aSesameSource == aTripleSrc);
    }
}
