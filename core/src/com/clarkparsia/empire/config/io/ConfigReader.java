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

package com.clarkparsia.empire.config.io;

import com.clarkparsia.empire.config.EmpireConfiguration;
import com.clarkparsia.empire.EmpireException;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

/**
 * <p>Interface for reading Empire configuration files.</p>
 *
 * @author Michael Grove
 * @since 0.6.2
 * @version 0.6.2
 */
public interface ConfigReader {

	/**
	 * Read the Empire configuration information from the specified InputStream
	 * @param theStream the stream to read from
	 * @return the Empire config
	 * @throws IOException if there is an error while reading from the stream
	 * @throws EmpireException if the configuration is incomplete or invalid
	 */
	public EmpireConfiguration read(InputStream theStream) throws IOException, EmpireException;

	/**
	 * Read the Empire configuration information from the specified Reader
	 * @param theStream the stream to read from
	 * @return the Empire config
	 * @throws IOException if there is an error while reading from the reader
	 * @throws EmpireException if the configuration is incompete or invalid
	 */
	public EmpireConfiguration read(Reader theStream) throws IOException, EmpireException;
}
