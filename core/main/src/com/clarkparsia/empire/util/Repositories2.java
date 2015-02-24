package com.clarkparsia.empire.util;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author  Fernando Hernandez
 * @since   0.8.7
 * @version 0.8.7
 */
public class Repositories2 {

	private Repositories2() {
		throw new AssertionError();
	}

	/**
	 * Create a simple in-memory {@link Repository} which is already initialized
	 *
	 * @return an in memory Repository
	 */
	public static Repository createInMemoryRepo() {
		try {
			Repository aRepo = new SailRepository(new MemoryStore());

			aRepo.initialize();

			return aRepo;
		}
		catch (RepositoryException e) {
			// impossible?
			throw new AssertionError(e);
		}
	}
}
