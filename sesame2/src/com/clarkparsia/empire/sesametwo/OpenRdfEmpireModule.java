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

package com.clarkparsia.empire.sesametwo;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.clarkparsia.empire.ds.DataSourceFactory;
import com.clarkparsia.empire.util.EmpireModule;

/**
 * <p>Guice module for the Sesame 2.x plugin.</p>
 *
 * @author Michael Grove
 * @since 0.6
 * @version 0.6.1
 */
public class OpenRdfEmpireModule extends AbstractModule implements EmpireModule {

	/**
	 * @inheritDoc
	 */
	protected void configure() {
		Multibinder.newSetBinder(binder(), DataSourceFactory.class)
				.addBinding().to(RepositoryDataSourceFactory.class);
	}
}
