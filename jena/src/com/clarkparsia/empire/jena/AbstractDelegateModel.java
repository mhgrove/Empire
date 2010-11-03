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
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;
/**
 * Simple delegate for Jena model so that we can control jdbc and model commits in SDB.
 * 
 * @author uoccou
 * @see ModelWithStore
 */
public class AbstractDelegateModel implements Model {
	protected Logger log = LoggerFactory.getLogger(this.getClass()) ;
	private Model m = null;
	public AbstractDelegateModel(Model m) {
		this.m = m;
	}
	public Model abort() {
		return m.abort();
	}
	public Model add(List<Statement> statements) {
		return m.add(statements);
	}
	public Model add(Model m, boolean suppressReifications) {
		return m.add(m, suppressReifications);
	}
	public Model add(Model m) {
		return m.add(m);
	}
	public Model add(Resource s, Property p, RDFNode o) {
		return m.add(s, p, o);
	}
	public Model add(Resource s, Property p, String o, boolean wellFormed) {
		return m.add(s, p, o, wellFormed);
	}
	public Model add(Resource s, Property p, String lex, RDFDatatype datatype) {
		return m.add(s, p, lex, datatype);
	}
	public Model add(Resource s, Property p, String o, String l) {
		return m.add(s, p, o, l);
	}
	public Model add(Resource s, Property p, String o) {
		return m.add(s, p, o);
	}
	public Model add(Statement s) {
		return m.add(s);
	}
	public Model add(Statement[] statements) {
		return m.add(statements);
	}
	public Model add(StmtIterator iter) {
		return m.add(iter);
	}
	public Model addLiteral(Resource s, Property p, boolean o) {
		return m.addLiteral(s, p, o);
	}
	public Model addLiteral(Resource s, Property p, char o) {
		return m.addLiteral(s, p, o);
	}
	public Model addLiteral(Resource s, Property p, double o) {
		return m.addLiteral(s, p, o);
	}
	public Model addLiteral(Resource s, Property p, float o) {
		return m.addLiteral(s, p, o);
	}
	public Model addLiteral(Resource s, Property p, int o) {
		return m.addLiteral(s, p, o);
	}
	public Model addLiteral(Resource s, Property p, Literal o) {
		return m.addLiteral(s, p, o);
	}
	public Model addLiteral(Resource s, Property p, long o) {
		return m.addLiteral(s, p, o);
	}
	public Model addLiteral(Resource s, Property p, Object o) {
		return m.addLiteral(s, p, o);
	}
	public RDFNode asRDFNode(Node n) {
		return m.asRDFNode(n);
	}
	public Statement asStatement(Triple t) {
		return m.asStatement(t);
	}
	public Model begin() {
		return m.begin();
	}
	public void close() {
		m.close();
	}
	public Model commit() {
		return m.commit();
	}
	public boolean contains(Resource s, Property p, RDFNode o) {
		return m.contains(s, p, o);
	}
	public boolean contains(Resource s, Property p, String o, String l) {
		return m.contains(s, p, o, l);
	}
	public boolean contains(Resource s, Property p, String o) {
		return m.contains(s, p, o);
	}
	public boolean contains(Resource s, Property p) {
		return m.contains(s, p);
	}
	public boolean contains(Statement s) {
		return m.contains(s);
	}
	public boolean containsAll(Model model) {
		return m.containsAll(model);
	}
	public boolean containsAll(StmtIterator iter) {
		return m.containsAll(iter);
	}
	public boolean containsAny(Model model) {
		return m.containsAny(model);
	}
	public boolean containsAny(StmtIterator iter) {
		return m.containsAny(iter);
	}
	public boolean containsLiteral(Resource s, Property p, boolean o) {
		return m.containsLiteral(s, p, o);
	}
	public boolean containsLiteral(Resource s, Property p, char o) {
		return m.containsLiteral(s, p, o);
	}
	public boolean containsLiteral(Resource s, Property p, double o) {
		return m.containsLiteral(s, p, o);
	}
	public boolean containsLiteral(Resource s, Property p, float o) {
		return m.containsLiteral(s, p, o);
	}
	public boolean containsLiteral(Resource s, Property p, int o) {
		return m.containsLiteral(s, p, o);
	}
	public boolean containsLiteral(Resource s, Property p, long o) {
		return m.containsLiteral(s, p, o);
	}
	public boolean containsLiteral(Resource s, Property p, Object o) {
		return m.containsLiteral(s, p, o);
	}
	public boolean containsResource(RDFNode r) {
		return m.containsResource(r);
	}
	public Alt createAlt() {
		return m.createAlt();
	}
	public Alt createAlt(String uri) {
		return m.createAlt(uri);
	}
	public Bag createBag() {
		return m.createBag();
	}
	public Bag createBag(String uri) {
		return m.createBag(uri);
	}
	public RDFList createList() {
		return m.createList();
	}
	public RDFList createList(Iterator<? extends RDFNode> members) {
		return m.createList(members);
	}
	public RDFList createList(RDFNode[] members) {
		return m.createList(members);
	}
	public Literal createLiteral(String v, boolean wellFormed) {
		return m.createLiteral(v, wellFormed);
	}
	public Literal createLiteral(String v, String language) {
		return m.createLiteral(v, language);
	}
	public Literal createLiteral(String v) {
		return m.createLiteral(v);
	}
	public Statement createLiteralStatement(Resource s, Property p, boolean o) {
		return m.createLiteralStatement(s, p, o);
	}
	public Statement createLiteralStatement(Resource s, Property p, char o) {
		return m.createLiteralStatement(s, p, o);
	}
	public Statement createLiteralStatement(Resource s, Property p, double o) {
		return m.createLiteralStatement(s, p, o);
	}
	public Statement createLiteralStatement(Resource s, Property p, float o) {
		return m.createLiteralStatement(s, p, o);
	}
	public Statement createLiteralStatement(Resource s, Property p, int o) {
		return m.createLiteralStatement(s, p, o);
	}
	public Statement createLiteralStatement(Resource s, Property p, long o) {
		return m.createLiteralStatement(s, p, o);
	}
	public Statement createLiteralStatement(Resource s, Property p, Object o) {
		return m.createLiteralStatement(s, p, o);
	}
	public Property createProperty(String nameSpace, String localName) {
		return m.createProperty(nameSpace, localName);
	}
	public Property createProperty(String uri) {
		return m.createProperty(uri);
	}
	public ReifiedStatement createReifiedStatement(Statement s) {
		return m.createReifiedStatement(s);
	}
	public ReifiedStatement createReifiedStatement(String uri, Statement s) {
		return m.createReifiedStatement(uri, s);
	}
	public Resource createResource() {
		return m.createResource();
	}
	public Resource createResource(AnonId id) {
		return m.createResource(id);
	}
	public Resource createResource(Resource type) {
		return m.createResource(type);
	}
	public Resource createResource(ResourceF f) {
		return m.createResource(f);
	}
	public Resource createResource(String uri, Resource type) {
		return m.createResource(uri, type);
	}
	public Resource createResource(String uri, ResourceF f) {
		return m.createResource(uri, f);
	}
	public Resource createResource(String uri) {
		return m.createResource(uri);
	}
	public Seq createSeq() {
		return m.createSeq();
	}
	public Seq createSeq(String uri) {
		return m.createSeq(uri);
	}
	public Statement createStatement(Resource s, Property p, RDFNode o) {
		return m.createStatement(s, p, o);
	}
	public Statement createStatement(Resource s, Property p, String o,
			boolean wellFormed) {
		return m.createStatement(s, p, o, wellFormed);
	}
	public Statement createStatement(Resource s, Property p, String o,
			String l, boolean wellFormed) {
		return m.createStatement(s, p, o, l, wellFormed);
	}
	public Statement createStatement(Resource s, Property p, String o, String l) {
		return m.createStatement(s, p, o, l);
	}
	public Statement createStatement(Resource s, Property p, String o) {
		return m.createStatement(s, p, o);
	}
	public Literal createTypedLiteral(boolean v) {
		return m.createTypedLiteral(v);
	}
	public Literal createTypedLiteral(Calendar d) {
		return m.createTypedLiteral(d);
	}
	public Literal createTypedLiteral(char v) {
		return m.createTypedLiteral(v);
	}
	public Literal createTypedLiteral(double v) {
		return m.createTypedLiteral(v);
	}
	public Literal createTypedLiteral(float v) {
		return m.createTypedLiteral(v);
	}
	public Literal createTypedLiteral(int v) {
		return m.createTypedLiteral(v);
	}
	public Literal createTypedLiteral(long v) {
		return m.createTypedLiteral(v);
	}
	public Literal createTypedLiteral(Object value, RDFDatatype dtype) {
		return m.createTypedLiteral(value, dtype);
	}
	public Literal createTypedLiteral(Object value, String typeURI) {
		return m.createTypedLiteral(value, typeURI);
	}
	public Literal createTypedLiteral(Object value) {
		return m.createTypedLiteral(value);
	}
	public Literal createTypedLiteral(String lex, RDFDatatype dtype) {
		return m.createTypedLiteral(lex, dtype);
	}
	public Literal createTypedLiteral(String lex, String typeURI) {
		return m.createTypedLiteral(lex, typeURI);
	}
	public Literal createTypedLiteral(String v) {
		return m.createTypedLiteral(v);
	}
	public Model difference(Model model) {
		return m.difference(model);
	}
	public void enterCriticalSection(boolean readLockRequested) {
		m.enterCriticalSection(readLockRequested);
	}
	public boolean equals(Object m) {
		return m.equals(m);
	}
	public Object executeInTransaction(Command cmd) {
		return m.executeInTransaction(cmd);
	}
	public String expandPrefix(String prefixed) {
		return m.expandPrefix(prefixed);
	}
	public Alt getAlt(Resource r) {
		return m.getAlt(r);
	}
	public Alt getAlt(String uri) {
		return m.getAlt(uri);
	}
	public Resource getAnyReifiedStatement(Statement s) {
		return m.getAnyReifiedStatement(s);
	}
	public Bag getBag(Resource r) {
		return m.getBag(r);
	}
	public Bag getBag(String uri) {
		return m.getBag(uri);
	}
	public Graph getGraph() {
		return m.getGraph();
	}
	public Lock getLock() {
		return m.getLock();
	}
	public Map<String, String> getNsPrefixMap() {
		return m.getNsPrefixMap();
	}
	public String getNsPrefixURI(String prefix) {
		return m.getNsPrefixURI(prefix);
	}
	public String getNsURIPrefix(String uri) {
		return m.getNsURIPrefix(uri);
	}
	public Statement getProperty(Resource s, Property p) {
		return m.getProperty(s, p);
	}
	public Property getProperty(String nameSpace, String localName) {
		return m.getProperty(nameSpace, localName);
	}
	public Property getProperty(String uri) {
		return m.getProperty(uri);
	}
	public RDFNode getRDFNode(Node n) {
		return m.getRDFNode(n);
	}
	public RDFReader getReader() {
		return m.getReader();
	}
	public RDFReader getReader(String lang) {
		return m.getReader(lang);
	}
	public ReificationStyle getReificationStyle() {
		return m.getReificationStyle();
	}
	public Statement getRequiredProperty(Resource s, Property p) {
		return m.getRequiredProperty(s, p);
	}
	public Resource getResource(String uri, ResourceF f) {
		return m.getResource(uri, f);
	}
	public Resource getResource(String uri) {
		return m.getResource(uri);
	}
	public Seq getSeq(Resource r) {
		return m.getSeq(r);
	}
	public Seq getSeq(String uri) {
		return m.getSeq(uri);
	}
	public RDFWriter getWriter() {
		return m.getWriter();
	}
	public RDFWriter getWriter(String lang) {
		return m.getWriter(lang);
	}
	public boolean independent() {
		return m.independent();
	}
	public Model intersection(Model model) {
		return m.intersection(model);
	}
	public boolean isClosed() {
		return m.isClosed();
	}
	public boolean isEmpty() {
		return m.isEmpty();
	}
	public boolean isIsomorphicWith(Model g) {
		return m.isIsomorphicWith(g);
	}
	public boolean isReified(Statement s) {
		return m.isReified(s);
	}
	public void leaveCriticalSection() {
		m.leaveCriticalSection();
	}
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, boolean object) {
		return m.listLiteralStatements(subject, predicate, object);
	}
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, char object) {
		return m.listLiteralStatements(subject, predicate, object);
	}
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, double object) {
		return m.listLiteralStatements(subject, predicate, object);
	}
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, float object) {
		return m.listLiteralStatements(subject, predicate, object);
	}
	public StmtIterator listLiteralStatements(Resource subject,
			Property predicate, long object) {
		return m.listLiteralStatements(subject, predicate, object);
	}
	public NsIterator listNameSpaces() {
		return m.listNameSpaces();
	}
	public NodeIterator listObjects() {
		return m.listObjects();
	}
	public NodeIterator listObjectsOfProperty(Property p) {
		return m.listObjectsOfProperty(p);
	}
	public NodeIterator listObjectsOfProperty(Resource s, Property p) {
		return m.listObjectsOfProperty(s, p);
	}
	public RSIterator listReifiedStatements() {
		return m.listReifiedStatements();
	}
	public RSIterator listReifiedStatements(Statement st) {
		return m.listReifiedStatements(st);
	}
	public ResIterator listResourcesWithProperty(Property p, boolean o) {
		return m.listResourcesWithProperty(p, o);
	}
	public ResIterator listResourcesWithProperty(Property p, char o) {
		return m.listResourcesWithProperty(p, o);
	}
	public ResIterator listResourcesWithProperty(Property p, double o) {
		return m.listResourcesWithProperty(p, o);
	}
	public ResIterator listResourcesWithProperty(Property p, float o) {
		return m.listResourcesWithProperty(p, o);
	}
	public ResIterator listResourcesWithProperty(Property p, long o) {
		return m.listResourcesWithProperty(p, o);
	}
	public ResIterator listResourcesWithProperty(Property p, Object o) {
		return m.listResourcesWithProperty(p, o);
	}
	public ResIterator listResourcesWithProperty(Property p, RDFNode o) {
		return m.listResourcesWithProperty(p, o);
	}
	public ResIterator listResourcesWithProperty(Property p) {
		return m.listResourcesWithProperty(p);
	}
	public StmtIterator listStatements() {
		return m.listStatements();
	}
	public StmtIterator listStatements(Resource s, Property p, RDFNode o) {
		return m.listStatements(s, p, o);
	}
	public StmtIterator listStatements(Resource subject, Property predicate,
			String object, String lang) {
		return m.listStatements(subject, predicate, object, lang);
	}
	public StmtIterator listStatements(Resource subject, Property predicate,
			String object) {
		return m.listStatements(subject, predicate, object);
	}
	public StmtIterator listStatements(Selector s) {
		return m.listStatements(s);
	}
	public ResIterator listSubjects() {
		return m.listSubjects();
	}
	public ResIterator listSubjectsWithProperty(Property p, RDFNode o) {
		return m.listSubjectsWithProperty(p, o);
	}
	public ResIterator listSubjectsWithProperty(Property p, String o, String l) {
		return m.listSubjectsWithProperty(p, o, l);
	}
	public ResIterator listSubjectsWithProperty(Property p, String o) {
		return m.listSubjectsWithProperty(p, o);
	}
	public ResIterator listSubjectsWithProperty(Property p) {
		return m.listSubjectsWithProperty(p);
	}
	public PrefixMapping lock() {
		return m.lock();
	}
	public Model notifyEvent(Object e) {
		return m.notifyEvent(e);
	}
	public String qnameFor(String uri) {
		return m.qnameFor(uri);
	}
	public Model query(Selector s) {
		return m.query(s);
	}
	public QueryHandler queryHandler() {
		return m.queryHandler();
	}
	public Model read(InputStream in, String base, String lang) {
		return m.read(in, base, lang);
	}
	public Model read(InputStream in, String base) {
		return m.read(in, base);
	}
	public Model read(Reader reader, String base, String lang) {
		return m.read(reader, base, lang);
	}
	public Model read(Reader reader, String base) {
		return m.read(reader, base);
	}
	public Model read(String url, String base, String lang) {
		return m.read(url, base, lang);
	}
	public Model read(String url, String lang) {
		return m.read(url, lang);
	}
	public Model read(String url) {
		return m.read(url);
	}
	public Model register(ModelChangedListener listener) {
		return m.register(listener);
	}
	public Model remove(List<Statement> statements) {
		return m.remove(statements);
	}
	public Model remove(Model m, boolean suppressReifications) {
		return m.remove(m, suppressReifications);
	}
	public Model remove(Model m) {
		return m.remove(m);
	}
	public Model remove(Resource s, Property p, RDFNode o) {
		return m.remove(s, p, o);
	}
	public Model remove(Statement s) {
		return m.remove(s);
	}
	public Model remove(Statement[] statements) {
		return m.remove(statements);
	}
	public Model remove(StmtIterator iter) {
		return m.remove(iter);
	}
	public Model removeAll() {
		return m.removeAll();
	}
	public Model removeAll(Resource s, Property p, RDFNode r) {
		return m.removeAll(s, p, r);
	}
	public void removeAllReifications(Statement s) {
		m.removeAllReifications(s);
	}
	public PrefixMapping removeNsPrefix(String prefix) {
		return m.removeNsPrefix(prefix);
	}
	public void removeReification(ReifiedStatement rs) {
		m.removeReification(rs);
	}
	public boolean samePrefixMappingAs(PrefixMapping other) {
		return m.samePrefixMappingAs(other);
	}
	public PrefixMapping setNsPrefix(String prefix, String uri) {
		return m.setNsPrefix(prefix, uri);
	}
	public PrefixMapping setNsPrefixes(Map<String, String> map) {
		return m.setNsPrefixes(map);
	}
	public PrefixMapping setNsPrefixes(PrefixMapping other) {
		return m.setNsPrefixes(other);
	}
	public String setReaderClassName(String lang, String className) {
		return m.setReaderClassName(lang, className);
	}
	public String setWriterClassName(String lang, String className) {
		return m.setWriterClassName(lang, className);
	}
	public String shortForm(String uri) {
		return m.shortForm(uri);
	}
	public long size() {
		return m.size();
	}
	public boolean supportsSetOperations() {
		return m.supportsSetOperations();
	}
	public boolean supportsTransactions() {
		return m.supportsTransactions();
	}
	public Model union(Model model) {
		return m.union(model);
	}
	public Model unregister(ModelChangedListener listener) {
		return m.unregister(listener);
	}
	public PrefixMapping withDefaultMappings(PrefixMapping map) {
		return m.withDefaultMappings(map);
	}
	public Resource wrapAsResource(Node n) {
		return m.wrapAsResource(n);
	}
	public Model write(OutputStream out, String lang, String base) {
		return m.write(out, lang, base);
	}
	public Model write(OutputStream out, String lang) {
		return m.write(out, lang);
	}
	public Model write(OutputStream out) {
		return m.write(out);
	}
	public Model write(Writer writer, String lang, String base) {
		return m.write(writer, lang, base);
	}
	public Model write(Writer writer, String lang) {
		return m.write(writer, lang);
	}
	public Model write(Writer writer) {
		return m.write(writer);
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

}
