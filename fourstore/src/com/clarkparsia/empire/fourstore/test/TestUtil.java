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

package com.clarkparsia.empire.fourstore.test;

import com.clarkparsia.fourstore.impl.StoreFactory;

import com.clarkparsia.fourstore.api.Store;
import com.clarkparsia.fourstore.api.StoreException;

import java.net.URL;

import java.io.File;
import java.io.StringWriter;

import com.clarkparsia.openrdf.OpenRdfUtil;
import com.clarkparsia.openrdf.OpenRdfIO;

import com.clarkparsia.openrdf.ExtRepository;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.rio.RDFFormat;

/**
 * <p>Takes a 4Store database and partitions its instances into separate named graphs, one for each instance.  This
 * is required (for testing) because 4Store does not support the deletion of arbitrary triples, you can only delete
 * a named graph.  So to delete a single individual from the kb, you would have to delete an entire named graph,
 * so having it in it's own named graph makes this possible.</p>
 *
 * @author Michael Grove
 */
public class TestUtil {

	public static void prepFourStore() throws Exception {
		String aLocalDataFile = "/Users/mhgrove/Desktop/nasa.nt";
		String aDefaultGraphURI = "file:///home/mgrove/nasa.nt";
		URL aURL = new URL("http://localhost:4040");

		Store aStore = StoreFactory.create(aURL);

		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		OpenRdfIO.addData(aRepo, new File(aLocalDataFile));

		for (Resource aRes : aRepo.getSubjects(null, null)) {
			if (aRes instanceof org.openrdf.model.URI) {
				org.openrdf.model.URI aURI = (org.openrdf.model.URI) aRes;

				StringWriter aWriter = new StringWriter();

				try {
					OpenRdfIO.writeGraph(aRepo.describe(aURI), aWriter, RDFFormat.TURTLE);

					try {
						aStore.delete(aURI);
					}
					catch (StoreException e) {
						System.err.println("delete error: " + e.getMessage());
					}

					aStore.add(aWriter.toString(), RDFFormat.TURTLE, aURI);
				}
				catch (StoreException e) {
					e.printStackTrace();
				}
			}
			else {
				System.err.println("don't know what to do with: " + aRes);
			}
		}

		aStore.delete(ValueFactoryImpl.getInstance().createURI(aDefaultGraphURI));
		
	}

	public static void main(String[] args) throws Exception {
		prepFourStore();
	}

}
