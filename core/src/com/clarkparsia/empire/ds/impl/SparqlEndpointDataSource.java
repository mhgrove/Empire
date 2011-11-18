package com.clarkparsia.empire.ds.impl;

import com.clarkparsia.empire.ds.ResultSet;
import com.clarkparsia.empire.ds.QueryException;

import com.clarkparsia.empire.impl.RdfQueryFactory;

import com.clarkparsia.empire.impl.sparql.SPARQLDialect;

import com.clarkparsia.openrdf.util.AdunaIterations;

import com.clarkparsia.openrdf.OpenRdfIO;

import com.clarkparsia.common.web.ParameterList;
import com.clarkparsia.common.web.Request;
import com.clarkparsia.common.web.HttpResourceImpl;
import com.clarkparsia.common.web.HttpResource;
import com.clarkparsia.common.web.HttpHeaders;
import com.clarkparsia.common.web.MimeTypes;
import com.clarkparsia.common.web.Response;

import java.net.ConnectException;
import java.net.URL;

import java.io.IOException;

import org.openrdf.model.Graph;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;

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

		return new AbstractResultSet(AdunaIterations.iterator(executeSPARQLQuery(theQuery, TupleQueryResult.class))) {
			public void close() {
				// no-op
			}
		};
	}

	private <T> T executeSPARQLQuery(String theQuery, Class<T> clazz) throws QueryException {
		Response aResponse = null;
	
		try {
			aResponse = createSPARQLQueryRequest(theQuery).execute();

			if (aResponse.hasErrorCode()) {
				throw responseToException(theQuery, aResponse);
			}
			else {
				try {
					if (Boolean.class.equals(clazz)) {
						return (T) parseBooleanResult(aResponse);
					}
					else {
						return (T) parseTupleResult(aResponse);
					}
				}
				catch (Exception e) {
					throw new QueryException("Could not parse SPARQL-XML results", e);
				}
			}
		}
		catch (IOException e) {
			throw new QueryException(e);
		}
		finally {
			if (aResponse != null) {
				try {
					aResponse.close();
				}
				catch (IOException e) {
					System.err.println("There was an error while closing the http connection: " + e.getMessage());
				}
			}
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

		return executeSPARQLQuery(theQuery, Boolean.class);
	}

	private Boolean parseBooleanResult(Response theResponse) throws QueryResultParseException, UnsupportedQueryResultFormatException, IOException {
		return QueryResultIO.parse(theResponse.getContent(), BooleanQueryResultFormat.SPARQL);
	}
	
	private TupleQueryResult parseTupleResult(Response theResponse) throws QueryResultParseException, TupleQueryResultHandlerException, UnsupportedQueryResultFormatException, IOException {
		return QueryResultIO.parse(theResponse.getContent(), TupleQueryResultFormat.SPARQL);
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

		Request aQueryRequest;
		Response aResponse = null;
		try {

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

			aResponse = aQueryRequest.execute();

			if (aResponse.hasErrorCode()) {
				throw responseToException(theQuery, aResponse);
			}
			else {
				try {
					return OpenRdfIO.readGraph(aResponse.getContent(), RDFFormat.TURTLE);
				}
				catch (RDFParseException e) {
					throw new QueryException("Error while parsing rdf/xml-formatted query results", e);
				}
			}
		}
		catch (IOException e) {
			throw new QueryException(e);
		}
		finally {
			if (aResponse != null) {
				try {
					aResponse.close();
				}
				catch (IOException e) {
					System.err.println("There was an error while closing the http connection: " + e.getMessage());
				}
			}
		}
	}
}
