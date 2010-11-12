package com.clarkparsia.empire.jena;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Simple delegate for Jena model so that we can control jdbc and model commits in SDB.
 * 
 * @author uoccou
 * @see SDBModelWithStore
 * @since 0.7
 * @version 0.7
 */
class AbstractDelegateModel implements Model {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	private Model m = null;

	/**
	 * Create a new AbstractDelegateModel
	 * @param m the model that will serve as the delegate
	 */
	public AbstractDelegateModel(Model m) {
		this.m = m;
	}

	/**
	 * @inheritDoc
	 */
	public Model abort() {
		return m.abort();
	}

	/**
	 * @inheritDoc
	 */
	public Model add(List<Statement> statements) {
		return m.add(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Model m, boolean suppressReifications) {
		return m.add(m, suppressReifications);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Model m) {
		return m.add(m);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, RDFNode o) {
		return m.add(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String o, boolean wellFormed) {
		return m.add(s, p, o, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String lex, RDFDatatype datatype) {
		return m.add(s, p, lex, datatype);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String o, String l) {
		return m.add(s, p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Resource s, Property p, String o) {
		return m.add(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Statement s) {
		return m.add(s);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(Statement[] statements) {
		return m.add(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model add(StmtIterator iter) {
		return m.add(iter);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, boolean o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, char o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, double o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, float o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, int o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, Literal o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, long o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model addLiteral(Resource s, Property p, Object o) {
		return m.addLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public RDFNode asRDFNode(Node n) {
		return m.asRDFNode(n);
	}

	/**
	 * @inheritDoc
	 */
	public Statement asStatement(Triple t) {
		return m.asStatement(t);
	}

	/**
	 * @inheritDoc
	 */
	public Model begin() {
		return m.begin();
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		m.close();
	}

	/**
	 * @inheritDoc
	 */
	public Model commit() {
		return m.commit();
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p, RDFNode o) {
		return m.contains(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p, String o, String l) {
		return m.contains(s, p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p, String o) {
		return m.contains(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Resource s, Property p) {
		return m.contains(s, p);
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(Statement s) {
		return m.contains(s);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAll(Model model) {
		return m.containsAll(model);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAll(StmtIterator iter) {
		return m.containsAll(iter);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAny(Model model) {
		return m.containsAny(model);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAny(StmtIterator iter) {
		return m.containsAny(iter);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, boolean o) {
		return m.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, char o) {
		return m.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, double o) {
		return m.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, float o) {
		return m.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, int o) {
		return m.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, long o) {
		return m.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsLiteral(Resource s, Property p, Object o) {
		return m.containsLiteral(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsResource(RDFNode r) {
		return m.containsResource(r);
	}

	/**
	 * @inheritDoc
	 */
	public Alt createAlt() {
		return m.createAlt();
	}

	/**
	 * @inheritDoc
	 */
	public Alt createAlt(String uri) {
		return m.createAlt(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Bag createBag() {
		return m.createBag();
	}

	/**
	 * @inheritDoc
	 */
	public Bag createBag(String uri) {
		return m.createBag(uri);
	}

	/**
	 * @inheritDoc
	 */
	public RDFList createList() {
		return m.createList();
	}

	/**
	 * @inheritDoc
	 */
	public RDFList createList(Iterator<? extends RDFNode> members) {
		return m.createList(members);
	}

	/**
	 * @inheritDoc
	 */
	public RDFList createList(RDFNode[] members) {
		return m.createList(members);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(String v, boolean wellFormed) {
		return m.createLiteral(v, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(String v, String language) {
		return m.createLiteral(v, language);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(String v) {
		return m.createLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, boolean o) {
		return m.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, char o) {
		return m.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, double o) {
		return m.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, float o) {
		return m.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, int o) {
		return m.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, long o) {
		return m.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createLiteralStatement(Resource s, Property p, Object o) {
		return m.createLiteralStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Property createProperty(String nameSpace, String localName) {
		return m.createProperty(nameSpace, localName);
	}

	/**
	 * @inheritDoc
	 */
	public Property createProperty(String uri) {
		return m.createProperty(uri);
	}

	/**
	 * @inheritDoc
	 */
	public ReifiedStatement createReifiedStatement(Statement s) {
		return m.createReifiedStatement(s);
	}

	/**
	 * @inheritDoc
	 */
	public ReifiedStatement createReifiedStatement(String uri, Statement s) {
		return m.createReifiedStatement(uri, s);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource() {
		return m.createResource();
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(AnonId id) {
		return m.createResource(id);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(Resource type) {
		return m.createResource(type);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(ResourceF f) {
		return m.createResource(f);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(String uri, Resource type) {
		return m.createResource(uri, type);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(String uri, ResourceF f) {
		return m.createResource(uri, f);
	}

	/**
	 * @inheritDoc
	 */
	public Resource createResource(String uri) {
		return m.createResource(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Seq createSeq() {
		return m.createSeq();
	}

	/**
	 * @inheritDoc
	 */
	public Seq createSeq(String uri) {
		return m.createSeq(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, RDFNode o) {
		return m.createStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o,
									 boolean wellFormed) {
		return m.createStatement(s, p, o, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o,
									 String l, boolean wellFormed) {
		return m.createStatement(s, p, o, l, wellFormed);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o, String l) {
		return m.createStatement(s, p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(Resource s, Property p, String o) {
		return m.createStatement(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(boolean v) {
		return m.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Calendar d) {
		return m.createTypedLiteral(d);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(char v) {
		return m.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(double v) {
		return m.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(float v) {
		return m.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(int v) {
		return m.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(long v) {
		return m.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Object value, RDFDatatype dtype) {
		return m.createTypedLiteral(value, dtype);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Object value, String typeURI) {
		return m.createTypedLiteral(value, typeURI);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(Object value) {
		return m.createTypedLiteral(value);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(String lex, RDFDatatype dtype) {
		return m.createTypedLiteral(lex, dtype);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(String lex, String typeURI) {
		return m.createTypedLiteral(lex, typeURI);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createTypedLiteral(String v) {
		return m.createTypedLiteral(v);
	}

	/**
	 * @inheritDoc
	 */
	public Model difference(Model model) {
		return m.difference(model);
	}

	/**
	 * @inheritDoc
	 */
	public void enterCriticalSection(boolean readLockRequested) {
		m.enterCriticalSection(readLockRequested);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object m) {
		return m.equals(m);
	}

	/**
	 * @inheritDoc
	 */
	public Object executeInTransaction(Command cmd) {
		return m.executeInTransaction(cmd);
	}

	/**
	 * @inheritDoc
	 */
	public String expandPrefix(String prefixed) {
		return m.expandPrefix(prefixed);
	}

	/**
	 * @inheritDoc
	 */
	public Alt getAlt(Resource r) {
		return m.getAlt(r);
	}

	/**
	 * @inheritDoc
	 */
	public Alt getAlt(String uri) {
		return m.getAlt(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Resource getAnyReifiedStatement(Statement s) {
		return m.getAnyReifiedStatement(s);
	}

	/**
	 * @inheritDoc
	 */
	public Bag getBag(Resource r) {
		return m.getBag(r);
	}

	/**
	 * @inheritDoc
	 */
	public Bag getBag(String uri) {
		return m.getBag(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Graph getGraph() {
		return m.getGraph();
	}

	/**
	 * @inheritDoc
	 */
	public Lock getLock() {
		return m.getLock();
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, String> getNsPrefixMap() {
		return m.getNsPrefixMap();
	}

	/**
	 * @inheritDoc
	 */
	public String getNsPrefixURI(String prefix) {
		return m.getNsPrefixURI(prefix);
	}

	/**
	 * @inheritDoc
	 */
	public String getNsURIPrefix(String uri) {
		return m.getNsURIPrefix(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Statement getProperty(Resource s, Property p) {
		return m.getProperty(s, p);
	}

	/**
	 * @inheritDoc
	 */
	public Property getProperty(String nameSpace, String localName) {
		return m.getProperty(nameSpace, localName);
	}

	/**
	 * @inheritDoc
	 */
	public Property getProperty(String uri) {
		return m.getProperty(uri);
	}

	/**
	 * @inheritDoc
	 */
	public RDFNode getRDFNode(Node n) {
		return m.getRDFNode(n);
	}

	/**
	 * @inheritDoc
	 */
	public RDFReader getReader() {
		return m.getReader();
	}

	/**
	 * @inheritDoc
	 */
	public RDFReader getReader(String lang) {
		return m.getReader(lang);
	}

	/**
	 * @inheritDoc
	 */
	public ReificationStyle getReificationStyle() {
		return m.getReificationStyle();
	}

	/**
	 * @inheritDoc
	 */
	public Statement getRequiredProperty(Resource s, Property p) {
		return m.getRequiredProperty(s, p);
	}

	/**
	 * @inheritDoc
	 */
	public Resource getResource(String uri, ResourceF f) {
		return m.getResource(uri, f);
	}

	/**
	 * @inheritDoc
	 */
	public Resource getResource(String uri) {
		return m.getResource(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Seq getSeq(Resource r) {
		return m.getSeq(r);
	}

	/**
	 * @inheritDoc
	 */
	public Seq getSeq(String uri) {
		return m.getSeq(uri);
	}

	/**
	 * @inheritDoc
	 */
	public RDFWriter getWriter() {
		return m.getWriter();
	}

	/**
	 * @inheritDoc
	 */
	public RDFWriter getWriter(String lang) {
		return m.getWriter(lang);
	}

	/**
	 * @inheritDoc
	 */
	public boolean independent() {
		return m.independent();
	}

	/**
	 * @inheritDoc
	 */
	public Model intersection(Model model) {
		return m.intersection(model);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isClosed() {
		return m.isClosed();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isEmpty() {
		return m.isEmpty();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isIsomorphicWith(Model g) {
		return m.isIsomorphicWith(g);
	}

	/**
	 * @inheritDoc
	 */
	public boolean isReified(Statement s) {
		return m.isReified(s);
	}

	/**
	 * @inheritDoc
	 */
	public void leaveCriticalSection() {
		m.leaveCriticalSection();
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, boolean object) {
		return m.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, char object) {
		return m.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, double object) {
		return m.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject,
											  Property predicate, float object) {
		return m.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listLiteralStatements(Resource subject, Property predicate, long object) {
		return m.listLiteralStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public NsIterator listNameSpaces() {
		return m.listNameSpaces();
	}

	/**
	 * @inheritDoc
	 */
	public NodeIterator listObjects() {
		return m.listObjects();
	}

	/**
	 * @inheritDoc
	 */
	public NodeIterator listObjectsOfProperty(Property p) {
		return m.listObjectsOfProperty(p);
	}

	/**
	 * @inheritDoc
	 */
	public NodeIterator listObjectsOfProperty(Resource s, Property p) {
		return m.listObjectsOfProperty(s, p);
	}

	/**
	 * @inheritDoc
	 */
	public RSIterator listReifiedStatements() {
		return m.listReifiedStatements();
	}

	/**
	 * @inheritDoc
	 */
	public RSIterator listReifiedStatements(Statement st) {
		return m.listReifiedStatements(st);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, boolean o) {
		return m.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, char o) {
		return m.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, double o) {
		return m.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, float o) {
		return m.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, long o) {
		return m.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, Object o) {
		return m.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p, RDFNode o) {
		return m.listResourcesWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listResourcesWithProperty(Property p) {
		return m.listResourcesWithProperty(p);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements() {
		return m.listStatements();
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Resource s, Property p, RDFNode o) {
		return m.listStatements(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Resource subject, Property predicate, String object, String lang) {
		return m.listStatements(subject, predicate, object, lang);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Resource subject, Property predicate,
									   String object) {
		return m.listStatements(subject, predicate, object);
	}

	/**
	 * @inheritDoc
	 */
	public StmtIterator listStatements(Selector s) {
		return m.listStatements(s);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjects() {
		return m.listSubjects();
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p, RDFNode o) {
		return m.listSubjectsWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p, String o, String l) {
		return m.listSubjectsWithProperty(p, o, l);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p, String o) {
		return m.listSubjectsWithProperty(p, o);
	}

	/**
	 * @inheritDoc
	 */
	public ResIterator listSubjectsWithProperty(Property p) {
		return m.listSubjectsWithProperty(p);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping lock() {
		return m.lock();
	}

	/**
	 * @inheritDoc
	 */
	public Model notifyEvent(Object e) {
		return m.notifyEvent(e);
	}

	/**
	 * @inheritDoc
	 */
	public String qnameFor(String uri) {
		return m.qnameFor(uri);
	}

	/**
	 * @inheritDoc
	 */
	public Model query(Selector s) {
		return m.query(s);
	}

	/**
	 * @inheritDoc
	 */
	public QueryHandler queryHandler() {
		return m.queryHandler();
	}

	/**
	 * @inheritDoc
	 */
	public Model read(InputStream in, String base, String lang) {
		return m.read(in, base, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(InputStream in, String base) {
		return m.read(in, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(Reader reader, String base, String lang) {
		return m.read(reader, base, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(Reader reader, String base) {
		return m.read(reader, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(String url, String base, String lang) {
		return m.read(url, base, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(String url, String lang) {
		return m.read(url, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model read(String url) {
		return m.read(url);
	}

	/**
	 * @inheritDoc
	 */
	public Model register(ModelChangedListener listener) {
		return m.register(listener);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(List<Statement> statements) {
		return m.remove(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Model m, boolean suppressReifications) {
		return m.remove(m, suppressReifications);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Model m) {
		return m.remove(m);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Resource s, Property p, RDFNode o) {
		return m.remove(s, p, o);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Statement s) {
		return m.remove(s);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(Statement[] statements) {
		return m.remove(statements);
	}

	/**
	 * @inheritDoc
	 */
	public Model remove(StmtIterator iter) {
		return m.remove(iter);
	}

	/**
	 * @inheritDoc
	 */
	public Model removeAll() {
		return m.removeAll();
	}

	/**
	 * @inheritDoc
	 */
	public Model removeAll(Resource s, Property p, RDFNode r) {
		return m.removeAll(s, p, r);
	}

	/**
	 * @inheritDoc
	 */
	public void removeAllReifications(Statement s) {
		m.removeAllReifications(s);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping removeNsPrefix(String prefix) {
		return m.removeNsPrefix(prefix);
	}

	/**
	 * @inheritDoc
	 */
	public void removeReification(ReifiedStatement rs) {
		m.removeReification(rs);
	}

	/**
	 * @inheritDoc
	 */
	public boolean samePrefixMappingAs(PrefixMapping other) {
		return m.samePrefixMappingAs(other);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping setNsPrefix(String prefix, String uri) {
		return m.setNsPrefix(prefix, uri);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping setNsPrefixes(Map<String, String> map) {
		return m.setNsPrefixes(map);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping setNsPrefixes(PrefixMapping other) {
		return m.setNsPrefixes(other);
	}

	/**
	 * @inheritDoc
	 */
	public String setReaderClassName(String lang, String className) {
		return m.setReaderClassName(lang, className);
	}

	/**
	 * @inheritDoc
	 */
	public String setWriterClassName(String lang, String className) {
		return m.setWriterClassName(lang, className);
	}

	/**
	 * @inheritDoc
	 */
	public String shortForm(String uri) {
		return m.shortForm(uri);
	}

	/**
	 * @inheritDoc
	 */
	public long size() {
		return m.size();
	}

	/**
	 * @inheritDoc
	 */
	public boolean supportsSetOperations() {
		return m.supportsSetOperations();
	}

	/**
	 * @inheritDoc
	 */
	public boolean supportsTransactions() {
		return m.supportsTransactions();
	}

	/**
	 * @inheritDoc
	 */
	public Model union(Model model) {
		return m.union(model);
	}

	/**
	 * @inheritDoc
	 */
	public Model unregister(ModelChangedListener listener) {
		return m.unregister(listener);
	}

	/**
	 * @inheritDoc
	 */
	public PrefixMapping withDefaultMappings(PrefixMapping map) {
		return m.withDefaultMappings(map);
	}

	/**
	 * @inheritDoc
	 */
	public Resource wrapAsResource(Node n) {
		return m.wrapAsResource(n);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(OutputStream out, String lang, String base) {
		return m.write(out, lang, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(OutputStream out, String lang) {
		return m.write(out, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(OutputStream out) {
		return m.write(out);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(Writer writer, String lang, String base) {
		return m.write(writer, lang, base);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(Writer writer, String lang) {
		return m.write(writer, lang);
	}

	/**
	 * @inheritDoc
	 */
	public Model write(Writer writer) {
		return m.write(writer);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
