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

package com.clarkparsia.empire.impl;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.collections.CollectionUtil;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.ResultSet;
import com.clarkparsia.empire.Dialect;
import static com.clarkparsia.empire.util.EmpireUtil.asPrimaryKey;
import com.clarkparsia.empire.util.BeanReflectUtil;
import com.clarkparsia.empire.annotation.RdfGenerator;
import com.clarkparsia.empire.annotation.AnnotationChecker;
import com.clarkparsia.openrdf.ExtBindingSet;

import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Implementation of the JPA {@link Query} interface for RDF based query languages.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.6.5
 */
public class RdfQuery implements Query {
	/**
	 * The logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(RdfQuery.class.getName());

	/**
	 * Variable parameter token in queries
	 */
	public static final String VARIABLE_TOKEN = "??";

	/**
	 * Regex for finding the variable token(s) in a query string
	 */
	public static final String VT_RE = "\\?\\?";

	/**
	 * The default name expected to be used in queries to denote what is to be returned as objects from the result
     * set of the query.  Can by changed by specifying a QueryHint with the key {@link #HINT_PROJECTION_VAR}
	 */
    protected static final String MAGIC_PROJECTION_VAR = "result";

    /**
     * Key of the {@link javax.persistence.QueryHint} to specify a different projection var
     * than specified by the default {@link #MAGIC_PROJECTION_VAR}
     */
    public static final String HINT_PROJECTION_VAR = "projection-var";

    /**
     * Key of the {@link javax.persistence.QueryHint} to specify the bean/entity class to be returned by the query.
     */
    public static final String HINT_ENTITY_CLASS = "entity-class";

	/**
	 * The DataSource the query will be executed against
	 */
	private DataSource mSource;

	/**
	 * The raw query string
	 */
	private String mQuery;

	/**
	 * The bean class, this is the type of objects returned by this query
	 */
	private Class mClass;

	/**
	 * Map of parameter index (not string index, their numbered index, eg the first parameter (1), the second (2))
	 * to the value of that parameter
	 */
	private Map<Integer, Value> mIndexedParameters = new HashMap<Integer, Value>();

	/**
	 * Map of parameter names to their values
	 */
	private Map<String, Value> mNamedParameters = new HashMap<String, Value>();

	/**
	 * The current limit of the query, or -1 for no limit
	 */
	private int mLimit = -1;

	/**
	 * The current result set offset, or -1 for no offset
	 */
	private int mOffset = -1;

	/**
	 * Whether or not the query results are distinct, the default is true.
	 */
	private boolean mIsDistinct = true;

	/**
	 * Whether or not this is a construct query.
	 */
	private boolean mIsConstruct = false;

	/**
	 * The map of asserted query hints.
	 */
	private Map<String, Object> mHints = new HashMap<String, Object>();

	/**
	 * The dialect of the query represented by this query object.
	 */
	private Dialect mQueryDialect;

    /**
	 * Create a new RdfQuery
	 * @param theSource the data source the query is run against
	 * @param theQueryString the query string
	 */
	public RdfQuery(final DataSource theSource, String theQueryString) {
		// TODO: support for ASK and DESCRIBE

		mSource = theSource;

		mQuery = theQueryString;

		mQueryDialect = theSource.getQueryFactory().getDialect();

		mQueryDialect.validateQueryFormat(getQueryString(), getProjectionVarName());

		// trying to guess if this is a construct query or not.  this is not foolproof, but since the only way of
		// definitely specifying this right now is to cast a query object as an RdfQuery and use setConstruct, that
		// is not ideal.  so we'll take a crack guessing it here.
		if (getQueryString().trim().startsWith("construct")) {
			setConstruct(true);
		}

		parseParameters();
	}

	/**
	 * Returns the class of Java beans returned as the results of the executed query.  When no bean class is specified,
	 * raw {@link BindingSet} objects are returned.
	 * @return the class, or null if one is not specified.
	 */
	public Class getBeanClass() {
        if (mClass != null) {
		    return mClass;
        }
        else if (getHints().containsKey(HINT_ENTITY_CLASS)) {
			Object aValue = getHints().get(HINT_ENTITY_CLASS);
            if (aValue instanceof Class) {
                return (Class) aValue;
            }
            else {
                try {
                    return BeanReflectUtil.loadClass(aValue.toString());
                }
                catch (ClassNotFoundException e) {
                    LOGGER.error("Invalid Entity class query set, value not found: " + aValue);
                    return null;
                }
            }
        }
        else {
            return null;
        }
	}

	/**
	 * Sets the class of Java beans returned by executions of this query.
	 * @param theClass the bean class
	 * @return this query object
	 */
	public Query setBeanClass(Class<?> theClass) {
		mClass = theClass;

		return this;
	}

	/**
	 * Return the DataSource the query will be run against.
	 * @return the source
	 * @see DataSource
	 */
	DataSource getSource() {
		return mSource;
	}

	/**
	 * Set the DataSource the query will be run against
	 * @param theSource the new source
	 */
	void setSource(final DataSource theSource) {
		mSource = theSource;
	}

	/**
	 * Return the raw query string as provided by the user.  This will contain un-escaped variables and is likely
	 * to be missing its type (select | construct).
	 * @return the un-modified query string
	 */
	protected String getQueryString() {
		return mQuery;
	}

	/**
	 * Return the result set limit for this query
	 * @return the limit
	 */
	public int getMaxResults() {
		return mLimit;
	}

	/**
	 * Return the current offset of this query
	 * @return the offset index
	 */
	public int getFirstResult() {
		return mOffset;
	}

	/**
	 * Set whether or not to enable the distinct modifier for this query
	 * @param theDistinct true to enable, false otherwise
	 * @return this query instance
	 */
	public Query setDistinct(boolean theDistinct) {
		mIsDistinct = theDistinct;

		return this;
	}

	/**
	 * Return whether or not the distinct modifier is enabled for this query
	 * @return true if the results will be distinct, false otherwise
	 */
	public boolean isDistinct() {
		return mIsDistinct;
	}

	/**
	 * Set whether or not this query object represents a construct query.
	 * @param theConstruct true to set this as a construct query, false otherwise
	 * @return this query instance
	 * @see #isConstruct
	 */
	public Query setConstruct(boolean theConstruct) {
		mIsConstruct = theConstruct;
		return this;
	}

	/**
	 * Return whether or not this is a construct query.  If this is an instance of a construct query, getSingleResult
	 * will return a {@link Graph} and getResultList will return a List with a single element which is an instance of
	 * Graph.  Otherwise, when it's a select query, these will return a single
	 * {@link BindingSet}, or a list of Bindings (or instances of
	 * the Bean class, when specified) respectively.
	 * @return true if this is a construct query, false otherwise.
	 */
	public boolean isConstruct() {
		return mIsConstruct;
	}

	/**
	 * @inheritDoc
	 */
	@SuppressWarnings("unchecked")
	public List getResultList() {
		List aList = new ArrayList();

		try {
			if (isConstruct()) {
				Graph aGraph = getSource().graphQuery(query());
				aList.add(aGraph);
			}
			else {
				ResultSet aResults = getSource().selectQuery(query());

				if (getBeanClass() != null) {
					// for now, by convention, for this to work like the JPQL stuff where you do something like
					// "from Product pr join pr.poc as p where p.id = ?" and expect to get a list of Product instances
					// back as the result set, you *MUST* have a var in the projection called 'result' which is
					// the URI of the things you want to get back; when you don't do this, we prefix your partial query
					// with this string
					for (BindingSet aBS : CollectionUtil.iterable(aResults)) {
						ExtBindingSet aBinding = new ExtBindingSet(aBS);

						Object aObj;

						String aVarName = getProjectionVarName();

						if (aBinding.getValue(aVarName) instanceof URI && AnnotationChecker.isValid(getBeanClass())) {
							aObj = RdfGenerator.fromRdf(getBeanClass(),
														asPrimaryKey(aBinding.getValue(aVarName)),
														getSource());
                        }
                        else {
                            aObj = new RdfGenerator.ValueToObject(getSource(), null,
                                                                  getBeanClass(), null).apply(aBinding.getValue(aVarName));
                        }

						if (aObj == null || !getBeanClass().isInstance(aObj)) {
							throw new PersistenceException("Cannot bind query result to bean: " + mClass);
						}
						else {
							aList.add(aObj);
						}
					}
				}
				else {
					aList.addAll(CollectionUtil.list( (Iterable<BindingSet>) aResults));
				}

				aResults.close();
			}
		}
		catch (Exception e) {
			throw new PersistenceException(e);
		}

		return aList;
	}

	/**
	 * Returns the name of the projection variable that is to represent the return value of the query.  By default
	 * this is {@link #MAGIC_PROJECTION_VAR} but you can override this by setting the {@link #HINT_PROJECTION_VAR}
	 * QueryHint value.
	 * @return the name of the projection variable to grab
	 */
	protected String getProjectionVarName() {
        if (getHints().containsKey(HINT_PROJECTION_VAR)) {
            return getHints().get(HINT_PROJECTION_VAR).toString();
        }
        else {
            return MAGIC_PROJECTION_VAR;
        }
    }

	/**
	 * @inheritDoc
	 */
	public Object getSingleResult() {
		List aResults = getResultList();

		if (aResults == null || aResults.isEmpty()) {
			throw new NoResultException();
		}
		else if (aResults.size() > 1) {
			throw new NonUniqueResultException();
		}

		return aResults.get(0);
	}

	/**
	 * @inheritDoc
	 */
	public int executeUpdate() {
		throw new UnsupportedOperationException("Update operations are not supported.");
	}

	/**
	 * @inheritDoc
	 */
	public Query setMaxResults(final int theLimit) {
		mLimit = theLimit;

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public Query setFirstResult(final int theOffset) {
		mOffset = theOffset;

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public Query setHint(final String theName, final Object theObj) {
		mHints.put(theName, theObj);

		return this;
	}

	/**
	 * Return a map of the current query hints
	 * @return the query hints
	 */
	protected Map<String, Object> getHints() {
		return mHints;
	}

	/**
	 * @inheritDoc
	 */
	public Query setParameter(final String theName, final Object theObj) {
		validateParameterName(theName);

		mNamedParameters.put(theName, validateParameterValue(theObj));

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public Query setParameter(final String theName, final Date theDate, final TemporalType theTemporalType) {
		Calendar aCal = Calendar.getInstance();
		aCal.setTime(theDate);

		return setParameter(theName, aCal, theTemporalType);
	}

	/**
	 * @inheritDoc
	 */
	public Query setParameter(final String theName, final Calendar theCalendar, final TemporalType theTemporalType) {
		validateParameterName(theName);

		Value aValue = asValue(theCalendar, theTemporalType);

		mNamedParameters.put(theName, aValue);

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public Query setParameter(final int theIndex, final Object theValue) {
		validateParameterIndex(theIndex);

		mIndexedParameters.put(theIndex, validateParameterValue(theValue));

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public Query setParameter(final int theIndex, final Date theDate, final TemporalType theTemporalType) {
		validateParameterIndex(theIndex);

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public Query setParameter(final int theIndex, final Calendar theCalendar, final TemporalType theTemporalType) {
		validateParameterIndex(theIndex);

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public Query setFlushMode(final FlushModeType theFlushModeType) {
		if (theFlushModeType != FlushModeType.AUTO) {
			throw new IllegalArgumentException("Commit style flush mode not supported");
		}

		return this;
	}

	/**
	 * Return the given date object with the specified temporal type as a {@link Value}
	 * @param theDate the date
	 * @param theTemporalType the type to extract from the date
	 * @return the time w.r.t to the TemportalType as a Value
	 */
	private Value asValue(final Calendar theDate, final TemporalType theTemporalType) {
		Value aValue = null;

		switch (theTemporalType) {
			case DATE:
				aValue = ValueFactoryImpl.getInstance().createLiteral(BasicUtils.date(theDate.getTime()), XMLSchema.DATE);
				break;
			case TIME:
				aValue = ValueFactoryImpl.getInstance().createLiteral(BasicUtils.datetime(theDate.getTime()), XMLSchema.TIME);
				break;
			case TIMESTAMP:
				aValue = ValueFactoryImpl.getInstance().createLiteral("" + theDate.getTime().getTime(), XMLSchema.TIME);
				break;
		}

		return aValue;
	}

	/**
	 * Validate that a parameter with the given name exists
	 * @param theName the parameter name to validate
	 * @throws IllegalArgumentException thrown if a parameter with the given name does not exist
	 */
	private void validateParameterName(String theName) {
		if (!mNamedParameters.containsKey(theName)) {
			throw new IllegalArgumentException("Parameter with name '" + theName + "' does not exist");
		}
	}

	/**
	 * Validate that the specified instance is a {@link Value} or can be
	 * {@link com.clarkparsia.empire.annotation.RdfGenerator.AsValueFunction turned into one}
	 * @param theValue the instance to validate
	 * @return the validated value
	 */
	private Value validateParameterValue(Object theValue) {
		if (!(theValue instanceof Value)) {
			try {
				return new RdfGenerator.AsValueFunction().apply(theValue);
			}
			catch (RuntimeException e) {
				// this is currently what is thrown when the function cannot transform the value
				throw new IllegalArgumentException(e);
			}
		}
		else {
			return (Value) theValue;
		}
	}

	/**
	 * Validate that a parameter at the given index exists
	 * @param theIndex the index to validate
	 * @throws IllegalArgumentException if a parameter at the given index does not exist
	 */
	private void validateParameterIndex(int theIndex) {
		if (!mIndexedParameters.containsKey(theIndex)) {
			throw new IllegalArgumentException("Parameter at index " + theIndex + " does not exist.");
		}
	}

	/**
	 * Validate that all parameter variables in the query have values.
	 * @throws IllegalStateException if there are unescaped parameter variables in th query
	 */
	private void validateVariables() {
		// note: null values for an index/name means the user never set a value for the parameter in the query
		// which means we have an invalid query!

		for (Integer aIndex : mIndexedParameters.keySet()) {
			if (mIndexedParameters.get(aIndex) == null) {
				throw new IllegalStateException("Not all parameters in query were replaced with values, query is invalid. Parameter at index " + aIndex + " was not set.");
			}
		}

		for (String aName : mNamedParameters.keySet()) {
			if (mNamedParameters.get(aName) == null) {
				throw new IllegalStateException("Not all parameters in query were replaced with values, query is invalid.  Parameter named " + aName + " was not set.");
			}
		}
	}

	/**
	 * Given the query string fragment, replace all variable parameter tokens with the values specified by the user
	 * through the various setParameter methods.
	 * @param theQuery the query fragment
	 * @return the query string with all parameter variables replaced
	 * @see #setParameter
	 */
	private String insertVariables(String theQuery) {
		String aBuffer = theQuery;

		for (String aName : mNamedParameters.keySet()) {
			aBuffer = aBuffer.replaceAll(VT_RE + aName, mQueryDialect.asQueryString(mNamedParameters.get(aName)));
		}

		int aIndex = 1;
		while (aBuffer.indexOf(VARIABLE_TOKEN) != -1) {
			aBuffer = aBuffer.replaceFirst(VT_RE, mQueryDialect.asQueryString(mIndexedParameters.get(aIndex++)));
		}

		return aBuffer;
	}

	/**
	 * Given a query fragment from {@link #getQueryString} pull out all the variable parameters
	 */
	private void parseParameters() {
		mNamedParameters.clear();
		mIndexedParameters.clear();

		String aUnamedVarRegex = VT_RE + "[^a-zA-Z0-9_\\-][\\.\\w]?";

		Matcher aMatcher = Pattern.compile(aUnamedVarRegex).matcher(getQueryString());

		// i'm pretty sure the JPA stuff is 1-indexed rather than the normal 0-indexed
		int aIndex = 1;
		while (aMatcher.find()) {
			mIndexedParameters.put(aIndex++, null);
		}

		String aNamedVarRegex = VT_RE + "[a-zA-Z0-9_\\-]+";

		aMatcher = Pattern.compile(aNamedVarRegex).matcher(getQueryString());

		while (aMatcher.find()) {
			mNamedParameters.put(getQueryString().substring(aMatcher.start() + VARIABLE_TOKEN.length(), aMatcher.end()), null);
		}
	}

	/**
	 * Return a valid, executable query instance from the specified query fragment, and user specified settings such
	 * as parameter values, limit, offset, etc.
	 * @return a valid query that can be run against a DataSource
	 */
	protected String query() {
		// use some regexs to look for and remove limits and offsets specified in the query string and store them locally
		// these will get postfixed to the query later on.
		boolean containsLimit = Pattern.compile("limit(\\s)*[0-9]{1,}[^}]*").matcher(getQueryString()).find();
		boolean containsOffset = Pattern.compile("offset(\\s)*[0-9]{1,}[^}]*").matcher(getQueryString()).find();

		if (containsLimit) {
			String aLimitGrabRegex = "limit(\\s)*[0-9]{1,}";
			Matcher m = Pattern.compile(aLimitGrabRegex).matcher(getQueryString());
			m.find();
			setMaxResults(Integer.parseInt(m.group(0).split(" ")[1]));
			mQuery = mQuery.replaceAll(aLimitGrabRegex, "");
		}

		if (containsOffset) {
			String aOffsetGrabRegex = "offset(\\s)*[0-9]{1,}";
			Matcher m = Pattern.compile(aOffsetGrabRegex).matcher(getQueryString());
			m.find();
			setFirstResult(Integer.parseInt(m.group(0).split(" ")[1]));
			mQuery = mQuery.replaceAll(aOffsetGrabRegex, "");
		}

		validateVariables();

		// TODO: should we get the values for the keywords used here (select, distinct, construct, limit, offset) from
		// the subclass rather than hard coding them?  or will these be the same for all rdf based query languages?

		StringBuffer aQuery = new StringBuffer(insertVariables(getQueryString()).trim());

        if (!aQuery.toString().startsWith(mQueryDialect.patternKeyword()) && !aQuery.toString().startsWith("select") && !aQuery.toString().startsWith("construct")) {
            aQuery.insert(0, mQueryDialect.patternKeyword());
        }

        StringBuffer aStart = new StringBuffer();
		if (!getQueryString().toLowerCase().startsWith("select") && !getQueryString().toLowerCase().startsWith("construct")) {
			aStart.insert(0, isConstruct() ? "construct " : "select ").append(isDistinct() ? " distinct " : "").append(" ");
			if (isConstruct()) {
				aStart.append(" * ");
			}
			else {
				aStart.append(mQueryDialect.asProjectionVar(getProjectionVarName())).append(" ");
			}
		}

        aQuery.insert(0, aStart.toString());

		if (getMaxResults() != -1) {
			aQuery.append(" limit ").append(getMaxResults());
		}

		if (getFirstResult() != -1) {
			aQuery.append(" offset ").append(getFirstResult());
		}

		mQueryDialect.insertNamespaces(aQuery);

		return aQuery.toString();
	}
}
