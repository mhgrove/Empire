/*
 * Copyright (c) 2005-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.common.web;

import java.io.IOException;
import java.net.URL;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since 1.0
 */
@Deprecated
public interface HttpResource {
	public HttpResource resource(String theName);

	public Response get() throws IOException;
	public Response delete() throws IOException;

	public Request initGet();
	public Request initPost();
	public Request initPut();
	public Request initDelete();

	public Request initRequest(Method theMethod);

	public URL url();
}
