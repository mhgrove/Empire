package com.clarkparsia.empire.ds.impl;

import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.QueryException;

import com.clarkparsia.empire.impl.RdfQueryFactory;
import com.clarkparsia.empire.impl.RdfQuery;

import com.clarkparsia.empire.impl.sparql.SPARQLDialect;

import com.clarkparsia.utils.web.HttpResource;
import com.clarkparsia.utils.web.ParameterList;
import com.clarkparsia.utils.web.Request;
import com.clarkparsia.utils.web.HttpHeaders;
import com.clarkparsia.utils.web.MimeTypes;
import com.clarkparsia.utils.web.Response;
import com.clarkparsia.utils.web.HttpResourceImpl;
import com.clarkparsia.utils.io.Encoder;

import com.clarkparsia.openrdf.query.results.SparqlXmlResultSetParser;

import com.clarkparsia.openrdf.OpenRdfIO;

import java.net.ConnectException;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.openrdf.model.Graph;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import org.openrdf.query.resultio.TupleQueryResultFormat;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>Simple implementation of the DataSource interface for a generic read-only sparql endpoint.</p>
 *
 * @author Michael Grove
 * @version 0.6.5
 * @since 0.7
 */
public class SparqlEndpointDataSource extends AbstractDataSource {

	/**
	 * Constant for the query requests
	 */
	private static final String PARAM_QUERY = "query";

	/**
	 * The URL of the endpoint
	 */
	private URL mURL;

	/**
	 * Whether or not to use HTTP GET requests for queries
	 */
	private boolean mUseGetForQueries = true;

	/**
	 * Create a new SparqlEndpointDataSource
	 * @param theURL the URL of the sparql endpoint.
	 */
	public SparqlEndpointDataSource(final URL theURL) {
		this(theURL, true, SPARQLDialect.instance());
	}

	/**
	 * Create a new SparqlEndpointDataSource
	 * @param theURL the URL of the sparql endpoint
	 * @param theDialect the sparql query dialect to use
	 */
	public SparqlEndpointDataSource(final URL theURL, SPARQLDialect theDialect) {
		this(theURL, true, theDialect);
	}

	/**
	 * Create a new SparqlEndpointDataSource
	 * @param theURL the URL of the sparql endpoint.
	 * @param theUseGetForQueries whether or not to use HTTP GET requests for queries
	 */
	public SparqlEndpointDataSource(final URL theURL, final boolean theUseGetForQueries) {
		mURL = theURL;
		mUseGetForQueries = theUseGetForQueries;

		setQueryFactory(new RdfQueryFactory(this, SPARQLDialect.instance()));
	}

	/**
	 * Create a new SparqlEndpointDataSource
	 * @param theURL the URL of the sparql endpoint.
	 * @param theUseGetForQueries whether or not to use HTTP GET requests for queries
	 * @param theDialect the query dialect to use for the endpoint
	 */
	public SparqlEndpointDataSource(final URL theURL, final boolean theUseGetForQueries, SPARQLDialect theDialect) {
		mURL = theURL;
		mUseGetForQueries = theUseGetForQueries;

		setQueryFactory(new RdfQueryFactory(this, theDialect));
	}

	/**
	 * @inheritDoc
	 */
	public void connect() throws ConnectException {
		setConnected(true);
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() {
		setConnected(false);
	}

	/**
	 * Return the URL of this SPARQL endpoint
	 * @return the endpoint URL
	 */
	public URL getURL() {
		return mURL;
	}

	/**
	 * @inheritDoc
	 */
	public ResultSet selectQuery(final String theQuery) throws QueryException {
		assertConnected();

		return new AbstractResultSet(executeSPARQLQuery(theQuery).bindingSet()) {
			public void close() {
				// no-op
			}
		};
	}

	private SparqlXmlResultSetParser executeSPARQLQuery(String theQuery) throws QueryException {
		try {
			Response aResponse = createSPARQLQueryRequest(theQuery).execute();

			if (aResponse.hasErrorCode()) {
				throw responseToException(theQuery, aResponse);
			}
			else {
				try {
					return parseResults(aResponse);
				}
				catch (SAXException e) {
					throw new QueryException("Could not parse SPARQL-XML results", e);
				}
			}
		}
		catch (IOException e) {
			throw new QueryException(e);
		}
	}

	private Request createSPARQLQueryRequest(String theQuery) {
		HttpResource aRes = new HttpResourceImpl(mURL);

		Request aQueryRequest;

		// auto prefix queries w/ rdf and rdfs namespaces
		ParameterList aParams = new ParameterList()
				.add(PARAM_QUERY, theQuery);

		if (mUseGetForQueries) {
			aQueryRequest = aRes.initGet()
					.addHeader(HttpHeaders.Accept.getName(), TupleQueryResultFormat.SPARQL.getDefaultMIMEType())
					.setParameters(aParams);
		}
		else {
			aQueryRequest = aRes.initPost()
					.addHeader(HttpHeaders.ContentType.getName(), MimeTypes.FormUrlEncoded.getMimeType())
					.addHeader(HttpHeaders.Accept.getName(), TupleQueryResultFormat.SPARQL.getDefaultMIMEType())
					.setBody(aParams.getURLEncoded());
		}

		return aQueryRequest;
	}


	/**
	 * Given a response, return it as a QueryException by parsing out the errore message and content
	 * @param theQuery the query being executed that caused the error
	 * @param theResponse the response which indicate a server error
	 * @return the Response as an Exception
	 */
	private QueryException responseToException(String theQuery, Response theResponse) {
		return new QueryException("Error evaluating query: " + theQuery + "\n(" + theResponse.getResponseCode() + ") " + theResponse.getMessage() + "\n\n" + theResponse.getContent());
	}

	/**
	 * @inheritDoc
	 */
	public boolean ask(final String theQuery) throws QueryException {
		assertConnected();

		return executeSPARQLQuery(theQuery).booleanResult();
	}

	private SparqlXmlResultSetParser parseResults(Response theResponse) throws SAXException, IOException {
		SparqlXmlResultSetParser aHandler = new SparqlXmlResultSetParser();

		XMLReader aParser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();

		aParser.setContentHandler(aHandler);
		aParser.setFeature("http://xml.org/sax/features/validation", false);

		aParser.parse(new InputSource(new ByteArrayInputStream(theResponse.getContent().getBytes(Encoder.UTF8.name()))));

		return aHandler;
	}

	/**
	 * @inheritDoc
	 */
	public Graph describe(final String theQuery) throws QueryException {
		return graphQuery(theQuery);
	}

	/**
	 * @inheritDoc
	 */
	public Graph graphQuery(final String theQuery) throws QueryException {
		assertConnected();

		HttpResource aRes = new HttpResourceImpl(mURL);

		ParameterList aParams = new ParameterList()
				.add(PARAM_QUERY, theQuery);

		try {
			Request aQueryRequest;

			if (mUseGetForQueries) {
				aQueryRequest = aRes.initGet()
						.addHeader(HttpHeaders.Accept.getName(), RDFFormat.TURTLE.getDefaultMIMEType())
						.setParameters(aParams);
			}
			else {
				aQueryRequest = aRes.initPost()
						.addHeader(HttpHeaders.ContentType.getName(), MimeTypes.FormUrlEncoded.getMimeType())
						.addHeader(HttpHeaders.Accept.getName(), RDFFormat.TURTLE.getDefaultMIMEType())
						.setBody(aParams.getURLEncoded());
			}

			Response aResponse = aQueryRequest.execute();

			if (aResponse.hasErrorCode()) {
				throw responseToException(theQuery, aResponse);
			}
			else {
				try {
					return OpenRdfIO.readGraph(new StringReader(aResponse.getContent()), RDFFormat.TURTLE);
				}
				catch (RDFParseException e) {
					throw new QueryException("Error while parsing rdf/xml-formatted query results", e);
				}
			}
		}
		catch (IOException e) {
			throw new QueryException(e);
		}
	}
}
