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

package com.clarkparsia.empire.ds;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Simple annotation to be placed on instances of {@link DataSourceFactory} to provide
 * a short name or alias for the factory when reading in configuration files.</p>
 * <p>Usage:
 * <pre><code>
 * @Alias("my_factory")
 * public MyFactory implements DataSourceFactory {
 *   ... implementation ...
 * }
 * </code></pre>
 * </p>
 * <p>This would allow you to refer to your implementation with the alias "my_factory" in your configuration files.</p>
 *
 * @author Michael Grove
 * @since 0.6.3
 * @version 0.7
 *
 * @see com.clarkparsia.empire.config.ConfigKeys
 * @see DataSourceFactory
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {
	public String value();
}
