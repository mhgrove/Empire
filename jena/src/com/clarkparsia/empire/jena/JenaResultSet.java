package com.clarkparsia.empire.jena;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

import com.clarkparsia.empire.jena.util.JenaSesameUtils;

import com.clarkparsia.sesame.utils.query.Binding;

import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.collections.CollectionUtil;

import org.openrdf.model.Value;

import java.util.HashMap;
import java.util.Map;

import com.clarkparsia.empire.impl.AbstractResultSet;

/**
 * Title: JenaResultSet<br/>
 * Description: Implementation of an Empire ResultSet backed by a Jena ResultSet<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 6, 2010 2:41:28 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class JenaResultSet extends AbstractResultSet {
	/**
	 * The Jena Results
	 */
	private QueryExecution mQueryExec;

	/**
	 * Create a new JenaResultSet
	 * @param theQueryExec The query execution context, so it can be closed when this result set has been used.
	 * @param theResults the Jena result set to back this ResultSet instance
	 */
	public JenaResultSet(final QueryExecution theQueryExec, final ResultSet theResults) {
		super(new CollectionUtil.TransformingIterator<QuerySolution, Binding>(theResults, new ToSesameBinding()));

		mQueryExec = theQueryExec;
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		mQueryExec.close();
	}

	/**
	 * Function to convert from Jena QuerySolutions to Sesame query Bindings
	 */
	private static class ToSesameBinding implements Function<QuerySolution, Binding> {

		/**
		 * @inheritDoc
		 */
		public Binding apply(QuerySolution theIn) {
			Map<String, Value> aMap = new HashMap<String, Value>();

			for (String aVar : CollectionUtil.iterable(theIn.varNames())) {
				aMap.put(aVar, JenaSesameUtils.asSesameValue(theIn.get(aVar)));
			}

			return new Binding(aMap);
		}
	}

}
