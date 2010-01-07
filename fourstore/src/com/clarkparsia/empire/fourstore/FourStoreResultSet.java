package com.clarkparsia.empire.fourstore;

import com.clarkparsia.empire.impl.AbstractResultSet;
import com.clarkparsia.sesame.utils.query.Binding;
import com.clarkparsia.utils.collections.CollectionUtil;
import com.clarkparsia.utils.Function;

import java.util.Map;
import java.util.HashMap;

import org.openrdf.model.Value;
import fourstore.impl.sesame.FourStoreToSesame;

/**
 * Title: FourStoreResultSet<br/>
 * Description: Implementation of an Empire ResultSet interface backed by a 4Store result set.<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 6, 2010 9:38:05 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class FourStoreResultSet extends AbstractResultSet {

	/**
	 * Create a new FourStoreResultSet
	 * @param theResults the FourStore results that will back this ResultSet
	 */
	FourStoreResultSet(final fourstore.api.results.ResultSet theResults) {
		super(new CollectionUtil.TransformingIterator<fourstore.api.results.Binding, Binding>(theResults.iterator(),
																							   new ToSesameBinding()));
	}

	/**
	 * @inheritDoc
	 */
	public void close() {
		// no-op
	}

	/**
	 * Function to transform a FourStore API binding to a Sesame binding
	 */
	private static class ToSesameBinding implements Function<fourstore.api.results.Binding, Binding> {

		/**
		 * @inheritDoc
		 */
		public Binding apply(final fourstore.api.results.Binding theIn) {
			Map<String, Value> aMap = new HashMap<String, Value>();

			for (String aVar : theIn.variables()) {
				aMap.put(aVar, FourStoreToSesame.toSesameValue(theIn.get(aVar)));
			}

			return new Binding(aMap);
		}
	}
}
