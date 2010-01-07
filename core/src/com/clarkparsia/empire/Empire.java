package com.clarkparsia.empire;

import javax.persistence.EntityManager;

/**
 * Title: Empire<br/>
 * Description: Access class for the RDF ORM/JPA layer to get the local {@link EntityManager} instance.
 * Why Empire?  Because it rules.<br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 11, 2009 12:00:03 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public class Empire {

	/**
	 * A thread-local reference to an instance of Empire
	 */
	private static ThreadLocal<Empire> mLocalInst = new ThreadLocal<Empire>();

	/**
	 * The current EntityManager
	 */
	private EntityManager mEntityManager;

	/**
	 * Create a new Empire instance with the given {@link EntityManager}
	 * @param theEntityManager the entity manager
	 */
	private Empire(final EntityManager theEntityManager) {
		mEntityManager = theEntityManager;
	}

	/**
	 * Return whether or not Empire has been initialized for the local thread context
	 * @return true if it has been initialized, false otherwise
	 */
	public static boolean isInitialized() {
		return mLocalInst.get() != null;
	}

	/**
	 * Return the thread local entity manager
	 * @return the entity manager
	 */
	static Empire get() {
		Empire aEmpire = mLocalInst.get();
		if (aEmpire == null) {
			throw new IllegalStateException("Empire not initialized.");
		}

		return aEmpire;
	}

	/**
	 * Return the current Empire EntityManager
	 * @return the current EntityManager
	 */
	public static EntityManager em() {
		return get().getEntityManager();
	}

	/**
	 * Create an instance of Empire using the specified {@link EntityManager}.  All subsequent operations will be performed
	 * on the given EntityManager
	 * @param theManager the enw EntityManager
	 * @return this instance
	 */
	public static Empire create(EntityManager theManager) {
		if (mLocalInst.get() != null) {
			mLocalInst.get().em().close();

			mLocalInst.remove();
		}

		Empire aEmpire = new Empire(theManager);

		mLocalInst.set(aEmpire);

		return aEmpire;
	}

	/**
	 * Return the current {@link EntityManager}.
	 * @return the entity manager
	 */
	private EntityManager getEntityManager() {
		return mEntityManager;
	}

	/**
	 * Close Empire
	 */
	public static void close() {
		if (mLocalInst.get() != null) {
			if (mLocalInst.get().em().isOpen()) {
				mLocalInst.get().em().close();
			}

			mLocalInst.remove();
		}
	}
}
