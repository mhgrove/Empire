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

package com.clarkparsia.empire.test.api;

import com.clarkparsia.empire.SupportsRdfId;

import java.net.URI;

/**
 * <p>Simple interface for testing the dynamic code generation</p>
 *
 * @author Michael Grove
 * @since 0.6.4
 * @version 0.6.4
 *
 * @see com.clarkparsia.empire.codegen.InstanceGenerator
 */
public interface TestInterface extends SupportsRdfId {
	public Integer getInt();
	public void setInt(Integer theInt);

	public String getString();
	public void setString(String theStr);

	public URI getURI();
	public void setURI(URI theURI);

	public TestInterface getObject();
	public void setObject(TestInterface theInterface);
}
