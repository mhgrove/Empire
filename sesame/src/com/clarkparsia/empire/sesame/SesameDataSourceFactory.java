package com.clarkparsia.empire.sesame;

import com.clarkparsia.empire.DataSourceFactory;
import com.clarkparsia.empire.DataSource;
import com.clarkparsia.empire.DataSourceException;

import java.util.Map;

import com.clarkparsia.utils.BasicUtils;

import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.UnknownRepositoryException;
import org.openrdf.sesame.config.ConfigurationException;

import org.openrdf.sesame.repository.SesameService;
import org.openrdf.sesame.repository.local.LocalService;

/**
 * Title: SesameEntityManagerFactory<br/>
 * Description: Provides a factory for creating EntityManagers backed by SesameDataSources<br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 17, 2009 9:39:16 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class SesameDataSourceFactory implements DataSourceFactory {

	/**
	 * Configuration key for the URL of the sesame service
	 */
	public static final String URL = "url";

	/**
	 * Configuration key for the name of the sesame repository
	 */
	public static final String REPO = "repo";

	/**
	 * Configuration key for the username of the sesame repository
	 */
	public static final String USER = "username";

	/**
	 * Configuration key for the password of the sesame repository
	 */
	public static final String PASSWORD = "password";

	/**
	 * @inheritDoc
	 */
	public boolean canCreate(final Map<String, String> theMap) {
		String aURL = theMap.get(URL);
		String aRepo = theMap.get(REPO);
		String aUser = theMap.get(USER);
		String aPW = theMap.get(PASSWORD);

		return (aURL == null || (aURL != null && BasicUtils.isURL(aURL)))
				&& (aRepo != null && !aRepo.equals(""))
				&& ((aUser == null && aPW == null) || (aUser != null && aPW != null && !aUser.equals("") && !aPW.equals("")));
	}

	/**
	 * @inheritDoc
	 */
	public DataSource create(final Map<String, String> theMap) throws DataSourceException {
		if (!canCreate(theMap)) {
			throw new DataSourceException("Invalid parameter map");
		}

		String aUser = theMap.get(USER);
		String aPW = theMap.get(PASSWORD);
		String aURL = theMap.get(URL);

		SesameService aService = null;

		try {
			aService = aURL != null ? Sesame.getService(new java.net.URL(aURL)) : Sesame.getService();

			if (aUser != null && aPW != null) {
				aService.login(aUser, aPW);
			}

			return new SesameDataSource(aService.getRepository(theMap.get(REPO)));
		}
		catch (UnknownRepositoryException e) {
			if (aService instanceof LocalService) {
				try {
					return new SesameDataSource(((LocalService)aService).createRepository(theMap.get(REPO), false));
				}
				catch (ConfigurationException ex) {
					throw new DataSourceException(ex);
				}
			}
			else {
				throw new DataSourceException(e);	
			}
		}
		catch (Exception e) {
			throw new DataSourceException(e);
		}
	}
}
