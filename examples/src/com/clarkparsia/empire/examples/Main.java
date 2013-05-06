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

package com.clarkparsia.empire.examples;

import com.clarkparsia.empire.Empire;
import com.clarkparsia.empire.impl.RdfQuery;
import com.clarkparsia.empire.sesametwo.OpenRdfEmpireModule;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import java.net.URI;
import java.util.Date;
import java.util.Arrays;
import java.util.List;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class Main {
	public static void main(String[] args) {
		// Alternatively you can use:  -Dempire.configuration.file=examples/examples.empire.config.properties
		// if so, comment out this line.
		System.setProperty("empire.configuration.file", "examples/examples.empire.config.properties");

		// loads Sesame bindings for Empire
		Empire.init(new OpenRdfEmpireModule());

		// create an EntityManager for the specified persistence context
		EntityManager aManager = Persistence.createEntityManagerFactory("oreilly")
											.createEntityManager();

		// this retrieves a particular book from the database
		Book aBook = aManager.find(Book.class, URI.create("urn:x-domain:oreilly.com:product:9780596514129.IP"));

		// prints: Switching to the Mac: The Missing Manual, Leopard Edition
		System.err.println(aBook.getTitle());

		// prints: O'Reilly Media / Pogue Press
		System.err.println(aBook.getPublisher());

		// creating a new book:
		Book aNewBook = new Book();
		aNewBook.setIssued(new Date());
		aNewBook.setTitle("How to use Empire");
		aNewBook.setPublisher("Clark & Parsia");
		aNewBook.setPrimarySubjectOf(URI.create("http://github.com/clarkparsia/Empire"));

		// grab the ebook manifestation
		Manifestation aEBook = aManager.find(Manifestation.class, URI.create("urn:x-domain:oreilly.com:product:9780596104306.EBOOK"));

		// and we'll use it as the embodiment of our new book.
		aNewBook.setEmbodiments(Arrays.asList(aEBook));

		// save the new book to the database
		aManager.persist(aNewBook);

		Book aNewBookCopy = aManager.find(Book.class, aNewBook.getRdfId());

		// true!
		System.err.println(aNewBook.equals(aNewBookCopy));

		// lets edit our book...
		// maybe we changed the title and published as a PDF
		aNewBook.setTitle("Return of the Empire");

		// create a new manifestation
		Manifestation aPDFManifestation = new Manifestation();
		aPDFManifestation.setIssued(new Date());
		// set the dc:type attribute
		aPDFManifestation.setType(URI.create("http://purl.oreilly.com/product-types/PDF"));

		aNewBook.setEmbodiments(Arrays.asList(aPDFManifestation));

		// now save our edits
		aManager.merge(aNewBook);

		// print the new information we just saved
		System.err.println(aNewBook.getTitle());
		System.err.println(aNewBook.getEmbodiments());

		// and importantly, verify that the new manifestation was also saved due to the cascaded merge operation
		// specified in the Book class via the @OneToMany annotation

		// true!
		System.err.println(aManager.contains(aPDFManifestation));

		// the copy of the book contains the old information
		System.err.println(aNewBookCopy.getTitle());
		System.err.println(aNewBookCopy.getEmbodiments());

		// but can be refreshed...
		aManager.refresh(aNewBookCopy);

		// and now contains the correct, up-to-date information
		System.err.println(aNewBookCopy.getTitle());
		System.err.println(aNewBookCopy.getEmbodiments());

		// now we can delete our new book
		aManager.remove(aNewBook);

		// false!
		System.err.println(aManager.contains(aNewBook));

		// but the new manifestation still exists, since we did not specify that deletes should cascade...

		// true!
		System.err.println(aManager.contains(aPDFManifestation));

		// Lastly, we can use the query API to run arbitrary sparql queries
		// create a jpql-style partial SPARQL query (JPQL is currently unsupported)
		Query aQuery = aManager.createQuery("where { ?result frbr:embodiment ?manifest." +
											"		 ?foo <http://purl.org/goodrelations/v1#typeOfGood> ?manifest . " +
											"        ?foo <http://purl.org/goodrelations/v1#hasPriceSpecification> ?price. " +
											"        ?price <http://purl.org/goodrelations/v1#hasCurrencyValue> ?value. " +
											"        ?price <http://purl.org/goodrelations/v1#hasCurrency> \"USD\"@en." +
											"        filter(?value > ??min). }");

		// this query should return instances of type Book
		aQuery.setHint(RdfQuery.HINT_ENTITY_CLASS, Book.class);

		// set the parameter in the query to the value for the min price
		// parameters are prefixed with ??
		aQuery.setParameter("min", 30);

		// now execute the query to get the list of all books which are $30 USD
		List aResults = aQuery.getResultList();

		// 233 results
		System.err.println("Num Results:  " + aResults.size());

		// print the titles of the first five results
		for (int i = 0; i < 5; i++) {
			Book aBookResult = (Book) aResults.get(i);
			System.err.println(aBookResult.getTitle());
		}

		/*
		 * Switching to the Mac: The Missing Manual, Leopard Edition
		 * O'Reilly Media / Pogue Press
		 * true
		 * Return of the Empire
		 * [http://purl.oreilly.com/product-types/PDF]
		 * true
		 * How to use Empire
		 * [http://purl.oreilly.com/product-types/EBOOK]
		 * Return of the Empire
		 * [http://purl.oreilly.com/product-types/PDF]
		 * false
		 * true
		 *
		 */
	}
}
