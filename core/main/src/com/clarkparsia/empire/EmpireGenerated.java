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

import org.openrdf.model.Model;

/**
 * Interface implemented by all generated beans that track which RDF statements were used to describe the particular instance
 * in RDF, and which were used to populate the fields of this bean.  This is for Empire internal use only.
 * 
 * @author Blazej Bulka <blazej@clarkparsia.com>
 */
public interface EmpireGenerated {
	/**
	 * Gets the graph containing all the RDF statements for that instance (regardless whether they were used
	 * to populate the fields of this bean).
	 * 
	 * @return the graph
	 */
	public Model getAllTriples();
	
	public void setAllTriples(Model aGraph);
	
	/**
	 * Gets the graph containing the RDF statements for that instance that were used to populate this bean.
	 * 
	 * @return the graph
	 */
	public Model getInstanceTriples();
	
	public void setInstanceTriples(Model aGraph);
	
	/**
	 * Returns the original class/interface that was extended by Empire
	 * 
	 * @return the Java Class object
	 */
	public Class getInterfaceClass();
}
