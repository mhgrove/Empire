/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.empire.test.util;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.test.TestJPA;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.util.DefaultEmpireModule;

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
		aMap.put("files", TestJPA.DATA_FILE);

		return Empire.get().persistenceProvider().createDataSource("test", aMap);
	}
}
