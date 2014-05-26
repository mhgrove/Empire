/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * Copyright (c) 2010, Ultan O'Carroll
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

package com.clarkparsia.empire.jena;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.rdf.model.Alt;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.RSIterator;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceF;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple delegate for Jena model so that we can control jdbc and model commits in SDB.
 * 
 * @author 	uoccou
 * @see 	SDBModelWithStore
 * @since 	0.7
 * @version 0.7
 */
class AbstractDelegateModel implements Model {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Model mModel;

	/**
	 * Create a new AbstractDelegateModel
	 * @param m the model that will serve as the delegate
	 */
	public AbstractDelegateModel(Model m) {
		this.mModel = m;
	}

	/**
	 * @inheritDoc
	 */
	public Model abort() {
		return mModel.abort();
	}

	/**
	 * @inheritDoc
	 */
	public Model add(List<Statement> statements) {
		return mModel.add(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Model m, boolean suppressReifications) {
		return mModel.add(m, suppressReifications);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Model m) {
		return mModel.add(m);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, RDFNode o) {
		return mModel.add(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String o, boolean wellFormed) {
		return mModel.add(s, p, o, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String lex, RDFDatatype datatype) {
		return mModel.add(s, p, lex, datatype);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String o, String l) {
		return mModel.add(s, p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String o) {
		return mModel.add(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Statement s) {
		return mModel.add(s);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Statement[] statements) {
		return mModel.add(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(StmtIterator iter) {
		return mModel.add( iter );
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, boolean o) {
		return mModel.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, char o) {
		return mModel.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, double o) {
		return mModel.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, float o) {
		return mModel.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, int o) {
		return mModel.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, Literal o) {
		return mModel.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, long o) {
		return mModel.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, Object o) {
		return mModel.addLiteral( s, p, o );
	}

	/**
	 * @inheritDoc
	 */
	public RDFNode asRDFNode(Node n) {
		return mModel.asRDFNode( n );
	}

	/**
	 * @inheritDoc
	 */
	public Resource wrapAsResource(final Node theNode) {
		return mModel.wrapAsResource( theNode );
	}

	/**
	 * @inheritDoc
	 */
	public Statement asStatement(Triple t) {
		return mModel.asStatement( t );
	}

	/**
	 * @inheritDoc
	 */
	public Model begin() {
		return mModel.begin();
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		mModel.close();
	}

	/**
	 * @inheritDoc
	 */
	public Model commit() {
		return mModel.commit();
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p, RDFNode o) {
		return mModel.contains(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p, String o, String l) {
		return mModel.contains(s, p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p, String o) {
		return mModel.contains(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p) {
		return mModel.contains(s, p);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Statement s) {
		return mModel.contains( s );
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAll(Model model) {
		return mModel.containsAll(model);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAll(StmtIterator iter) {
		return mModel.containsAll( iter );
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAny(Model model) {
		return mModel.containsAny(model);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAny(StmtIterator iter) {
		return mModel.containsAny( iter );
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, boolean o) {
		return mModel.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, char o) {
		return mModel.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, double o) {
		return mModel.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, float o) {
		return mModel.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, int o) {
		return mModel.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, long o) {
		return mModel.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, Object o) {
		return mModel.containsLiteral( s, p, o );
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsResource(RDFNode r) {
		return mModel.containsResource( r );
	}

	/**
	 * @inheritDoc
	 */
	public Alt createAlt() {
		return mModel.createAlt();
	}

	/**
	 * @inheritDoc
	 */
	public Alt createAlt(String uri) {
		return mModel.createAlt( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Bag createBag() {
		return mModel.createBag();
	}

	/**
	 * @inheritDoc
	 */
	public Bag createBag(String uri) {
		return mModel.createBag( uri );
	}

	/**
	 * @inheritDoc
	 */
	public RDFList createList() {
		return mModel.createList();
	}

	/**
	 * @inheritDoc
	 */
	public RDFList createList(Iterator<? extends RDFNode> members) {
		return mModel.createList(members);
	}

	/**
	 * @inheritDoc
	 */
	public RDFList createList(RDFNode[] members) {
		return mModel.createList( members );
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(String v, boolean wellFormed) {
		return mModel.createLiteral(v, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(String v, String language) {
		return mModel.createLiteral(v, language);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(String v) {
		return mModel.createLiteral( v );
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, boolean o) {
		return mModel.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, char o) {
		return mModel.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, double o) {
		return mModel.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, float o) {
		return mModel.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, int o) {
		return mModel.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, long o) {
		return mModel.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, Object o) {
		return mModel.createLiteralStatement( s, p, o );
	}

	/**
	 * @inheritDoc
	 */
	public Property createProperty(String nameSpace, String localName) {
		return mModel.createProperty(nameSpace, localName);
	}

	/**
	 * @inheritDoc
	 */
	public Property createProperty(String uri) {
		return mModel.createProperty( uri );
	}

	/**
	 * @inheritDoc
	 */
	public ReifiedStatement createReifiedStatement(Statement s) {
		return mModel.createReifiedStatement(s);
	}

	/**
	 * @inheritDoc
	 */
	public ReifiedStatement createReifiedStatement(String uri, Statement s) {
		return mModel.createReifiedStatement( uri, s );
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource() {
		return mModel.createResource();
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(AnonId id) {
		return mModel.createResource(id);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(Resource type) {
		return mModel.createResource(type);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(ResourceF f) {
		return mModel.createResource(f);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(String uri, Resource type) {
		return mModel.createResource(uri, type);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(String uri, ResourceF f) {
		return mModel.createResource(uri, f);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(String uri) {
		return mModel.createResource( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Seq createSeq() {
		return mModel.createSeq();
	}

	/**
	 * @inheritDoc
	 */
	public Seq createSeq(String uri) {
		return mModel.createSeq( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, RDFNode o) {
		return mModel.createStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o,
									 boolean wellFormed) {
		return mModel.createStatement(s, p, o, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o,
									 String l, boolean wellFormed) {
		return mModel.createStatement(s, p, o, l, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o, String l) {
		return mModel.createStatement(s, p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o) {
		return mModel.createStatement( s, p, o );
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(boolean v) {
		return mModel.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Calendar d) {
		return mModel.createTypedLiteral(d);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(char v) {
		return mModel.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(double v) {
		return mModel.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(float v) {
		return mModel.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(int v) {
		return mModel.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(long v) {
		return mModel.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Object value, RDFDatatype dtype) {
		return mModel.createTypedLiteral(value, dtype);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Object value, String typeURI) {
		return mModel.createTypedLiteral(value, typeURI);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Object value) {
		return mModel.createTypedLiteral(value);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(String lex, RDFDatatype dtype) {
		return mModel.createTypedLiteral(lex, dtype);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(String lex, String typeURI) {
		return mModel.createTypedLiteral(lex, typeURI);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(String v) {
		return mModel.createTypedLiteral( v );
	}

	/**
	 * @inheritDoc
	 */
	public Model difference(Model model) {
		return mModel.difference(model);
	}

	/**
	 * @inheritDoc
	 */
	public void enterCriticalSection(boolean readLockRequested) {
		mModel.enterCriticalSection(readLockRequested);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object m) {
		return mModel.equals( m );
	}

	/**
	 * @inheritDoc
	 */
	public Object executeInTransaction(Command cmd) {
		return mModel.executeInTransaction( cmd );
	}

	/**
	 * @inheritDoc
	 */
	public String expandPrefix(String prefixed) {
		return mModel.expandPrefix( prefixed );
	}

	/**
	 * @inheritDoc
	 */
	public Alt getAlt(Resource r) {
		return mModel.getAlt(r);
	}

	/**
	 * @inheritDoc
	 */
	public Alt getAlt(String uri) {
		return mModel.getAlt( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Resource getAnyReifiedStatement(Statement s) {
		return mModel.getAnyReifiedStatement( s );
	}

	/**
	 * @inheritDoc
	 */
	public Bag getBag(Resource r) {
		return mModel.getBag(r);
	}

	/**
	 * @inheritDoc
	 */
	public Bag getBag(String uri) {
		return mModel.getBag( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Graph getGraph() {
		return mModel.getGraph();
	}

	/**
	 * @inheritDoc
	 */
	public Lock getLock() {
		return mModel.getLock();
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, String> getNsPrefixMap() {
		return mModel.getNsPrefixMap();
	}

	/**
	 * @inheritDoc
	 */
	public String getNsPrefixURI(String prefix) {
		return mModel.getNsPrefixURI( prefix );
	}

	/**
	 * @inheritDoc
	 */
	public String getNsURIPrefix(String uri) {
		return mModel.getNsURIPrefix( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Statement getProperty(Resource s, Property p) {
		return mModel.getProperty(s, p);
	}

	/**
	 * @inheritDoc
	 */
	public Property getProperty(String nameSpace, String localName) {
		return mModel.getProperty(nameSpace, localName);
	}

	/**
	 * @inheritDoc
	 */
	public Property getProperty(String uri) {
		return mModel.getProperty( uri );
	}

	/**
	 * @inheritDoc
	 */
	public RDFNode getRDFNode(Node n) {
		return mModel.getRDFNode( n );
	}

	/**
	 * @inheritDoc
	 */
	public RDFReader getReader() {
		return mModel.getReader();
	}

	/**
	 * @inheritDoc
	 */
	public RDFReader getReader(String lang) {
		return mModel.getReader( lang );
	}

	/**
	 * @inheritDoc
	 */
	public ReificationStyle getReificationStyle() {
		return mModel.getReificationStyle();
	}

	/**
	 * @inheritDoc
	 */
	public Statement getRequiredProperty(Resource s, Property p) {
		return mModel.getRequiredProperty( s, p );
	}

	/**
	 * @inheritDoc
	 */
	public Resource getResource(String uri, ResourceF f) {
		return mModel.getResource(uri, f);
	}

	/**
	 * @inheritDoc
	 */
	public Resource getResource(String uri) {
		return mModel.getResource( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Seq getSeq(Resource r) {
		return mModel.getSeq(r);
	}

	/**
	 * @inheritDoc
	 */
	public Seq getSeq(String uri) {
		return mModel.getSeq( uri );
	}

	/**
	 * @inheritDoc
	 */
	public RDFWriter getWriter() {
		return mModel.getWriter();
	}

	/**
	 * @inheritDoc
	 */
	public RDFWriter getWriter(String lang) {
		return mModel.getWriter( lang );
	}

	/**
	 * @inheritDoc
	 */
	public boolean independent() {
		return mModel.independent();
	}

	/**
	 * @inheritDoc
	 */
	public Model intersection(Model model) {
		return mModel.intersection( model );
	}

	/**
	 * @inheritDoc
	 */
	public boolean isClosed() {
		return mModel.isClosed();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isEmpty() {
		return mModel.isEmpty();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isIsomorphicWith(Model g) {
		return mModel.isIsomorphicWith( g );
	}

	/**
	 * @inheritDoc
	 */
	public boolean isReified(Statement s) {
		return mModel.isReified(s);
	}

	/**
	 * @inheritDoc
	 */
	public void leaveCriticalSection() {
		mModel.leaveCriticalSection();
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, boolean object) {
		return mModel.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, char object) {
		return mModel.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, double object) {
		return mModel.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, float object) {
		return mModel.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject, Property predicate, long object) {
		return mModel.listLiteralStatements( subject, predicate, object );
	}

	/**
	 * @inheritDoc
	 */
	public NsIterator listNameSpaces() {
		return mModel.listNameSpaces();
	}

	/**
	 * @inheritDoc
	 */
	public NodeIterator listObjects() {
		return mModel.listObjects();
	}

	/**
	 * @inheritDoc
	 */
	public NodeIterator listObjectsOfProperty(Property p) {
		return mModel.listObjectsOfProperty(p);
	}

	/**
	 * @inheritDoc
	 */
	public NodeIterator listObjectsOfProperty(Resource s, Property p) {
		return mModel.listObjectsOfProperty( s, p );
	}

	/**
	 * @inheritDoc
	 */
	public RSIterator listReifiedStatements() {
		return mModel.listReifiedStatements();
	}

	/**
	 * @inheritDoc
	 */
	public RSIterator listReifiedStatements(Statement st) {
		return mModel.listReifiedStatements( st );
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, boolean o) {
		return mModel.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, char o) {
		return mModel.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, double o) {
		return mModel.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, float o) {
		return mModel.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, long o) {
		return mModel.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, Object o) {
		return mModel.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, RDFNode o) {
		return mModel.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p) {
		return mModel.listResourcesWithProperty( p );
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements() {
		return mModel.listStatements();
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Resource s, Property p, RDFNode o) {
		return mModel.listStatements(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Resource subject, Property predicate, String object, String lang) {
		return mModel.listStatements(subject, predicate, object, lang);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Resource subject, Property predicate,
									   String object) {
		return mModel.listStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Selector s) {
		return mModel.listStatements( s );
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjects() {
		return mModel.listSubjects();
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p, RDFNode o) {
		return mModel.listSubjectsWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p, String o, String l) {
		return mModel.listSubjectsWithProperty(p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p, String o) {
		return mModel.listSubjectsWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p) {
		return mModel.listSubjectsWithProperty( p );
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping lock() {
		return mModel.lock();
	}

	/**
	 * @inheritDoc
	 */
	public Model notifyEvent(Object e) {
		return mModel.notifyEvent( e );
	}

	/**
	 * @inheritDoc
	 */
	public String qnameFor(String uri) {
		return mModel.qnameFor( uri );
	}

	/**
	 * @inheritDoc
	 */
	public Model query(Selector s) {
		return mModel.query( s );
	}

	/*
	 * @inritDoc
	 // sotty : apparently not defined in Jena 2.10.0, which is imported by empire-jena.	 
	public QueryHandler queryHandler() {
		return mModel.queryHandler();
	}
	*/

	/**
	 * @inheritDoc
	 */
	public Model read(InputStream in, String base, String lang) {
		return mModel.read(in, base, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(InputStream in, String base) {
		return mModel.read(in, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(Reader reader, String base, String lang) {
		return mModel.read(reader, base, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(Reader reader, String base) {
		return mModel.read(reader, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(String url, String base, String lang) {
		return mModel.read(url, base, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(String url, String lang) {
		return mModel.read(url, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(String url) {
		return mModel.read(url);
	}

	/**
	 * @inheritDoc
	 */
	public Model register(ModelChangedListener listener) {
		return mModel.register(listener);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(List<Statement> statements) {
		return mModel.remove(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Model m, boolean suppressReifications) {
		return mModel.remove(m, suppressReifications);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Model m) {
		return mModel.remove(m);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Resource s, Property p, RDFNode o) {
		return mModel.remove(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Statement s) {
		return mModel.remove(s);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Statement[] statements) {
		return mModel.remove(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(StmtIterator iter) {
		return mModel.remove(iter);
	}

	/**
	 * @inheritDoc
	 */
	public Model removeAll() {
		return mModel.removeAll();
	}

	/**
	 * @inheritDoc
	 */
	public Model removeAll(Resource s, Property p, RDFNode r) {
		return mModel.removeAll(s, p, r);
	}

	/**
	 * @inheritDoc
	 */
	public void removeAllReifications(Statement s) {
		mModel.removeAllReifications(s);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping removeNsPrefix(String prefix) {
		return mModel.removeNsPrefix(prefix);
	}

	/**
	 * @inheritDoc
	 */
	public void removeReification(ReifiedStatement rs) {
		mModel.removeReification(rs);
	}

	/**
	 * @inheritDoc
	 */
	public boolean samePrefixMappingAs(PrefixMapping other) {
		return mModel.samePrefixMappingAs(other);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping setNsPrefix(String prefix, String uri) {
		return mModel.setNsPrefix(prefix, uri);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping setNsPrefixes(Map<String, String> map) {
		return mModel.setNsPrefixes(map);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping setNsPrefixes(PrefixMapping other) {
		return mModel.setNsPrefixes(other);
	}

	/**
	 * @inheritDoc
	 */
	public String setReaderClassName(String lang, String className) {
		return mModel.setReaderClassName(lang, className);
	}

	/**
	 * @inheritDoc
	 */
	public String setWriterClassName(String lang, String className) {
		return mModel.setWriterClassName(lang, className);
	}

	/**
	 * @inheritDoc
	 */
	public String shortForm(String uri) {
		return mModel.shortForm(uri);
	}

	/**
	 * @inheritDoc
	 */
	public long size() {
		return mModel.size();
	}

	/**
	 * @inheritDoc
	 */
	public boolean supportsSetOperations() {
		return mModel.supportsSetOperations();
	}

	/**
	 * @inheritDoc
	 */
	public boolean supportsTransactions() {
		return mModel.supportsTransactions();
	}

	/**
	 * @inheritDoc
	 */
	public Model union(Model model) {
		return mModel.union(model);
	}

	/**
	 * @inheritDoc
	 */
	public Model unregister(ModelChangedListener listener) {
		return mModel.unregister(listener);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping withDefaultMappings(PrefixMapping map) {
		return mModel.withDefaultMappings(map);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(OutputStream out, String lang, String base) {
		return mModel.write(out, lang, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(OutputStream out, String lang) {
		return mModel.write(out, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(OutputStream out) {
		return mModel.write(out);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(Writer writer, String lang, String base) {
		return mModel.write(writer, lang, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(Writer writer, String lang) {
		return mModel.write(writer, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(Writer writer) {
		return mModel.write(writer);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
