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

package com.clarkparsia.empire;

import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.clarkparsia.empire.util.TestUtil;
import com.clarkparsia.empire.util.TestModule;
import com.clarkparsia.empire.ds.DataSource;
import com.clarkparsia.empire.ds.DataSourceException;
import com.clarkparsia.empire.ds.TripleSource;
import com.clarkparsia.empire.ds.DataSourceUtil;
import com.clarkparsia.empire.util.DefaultEmpireModule;

/**
 * <p>Tests for the DataSource APIs</p>
 *
 * @author  Michael Grove
 * @version 0.7
 * @since   0.7
 */
public class TestDS {
	@Test
	public void testTripleSourceCreate() throws DataSourceException {
		Empire.init(new DefaultEmpireModule(), new TestModule());
		
		DataSource aTestSource = TestUtil.createTestSource();

		// this is a test source, so it should not be a TripleSource...

		assertFalse(aTestSource instanceof TripleSource);

		TripleSource aTripleSrc = DataSourceUtil.asTripleSource(aTestSource);

		assertTrue(aTestSource != aTripleSrc);
	}

	@Test(expected=DataSourceException.class)
	public void testInvalidCreateTripleSource() throws DataSourceException {
		DataSourceUtil.asTripleSource(null);
	}

	@Test @Ignore
	public void testDataSourceOps() {
		// TODO: test DataSourceUtil operations like getType, exists, etc.
	}

	@Test @Ignore
	public void testTripleSourceAccess() {
		// TODO: verify triple store level operations work
	}

	@Test @Ignore
	public void testDelegateSource() {
		// TODO: implement me
	}
}
