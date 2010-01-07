package com.clarkparsia.empire.fourstore.test;

import fourstore.impl.StoreFactory;
import fourstore.api.Store;
import fourstore.api.StoreException;
import fourstore.api.Format;

import java.net.URL;
import java.net.URI;
import java.io.File;
import java.io.StringWriter;

import com.clarkparsia.sesame.repository.ExtendedSesameRepository;
import com.clarkparsia.sesame.utils.SesameUtils;
import com.clarkparsia.sesame.utils.SesameIO;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.admin.DummyAdminListener;
import org.openrdf.model.Resource;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 6, 2010 12:24:54 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class TestUtil {

	public static void prepFourStore() throws Exception {
		String aLocalDataFile = "/Users/mhgrove/Desktop/nasa.nt";
		String aDefaultGraphURI = "file:///home/mgrove/nasa.nt";
		URL aURL = new URL("http://localhost:4040");

		Store aStore = StoreFactory.create(aURL);

		ExtendedSesameRepository aRepo = new ExtendedSesameRepository(SesameUtils.createInMemSource());

		aRepo.addData(new File(aLocalDataFile), "", RDFFormat.NTRIPLES, true, new DummyAdminListener());

		for (Resource aRes : aRepo.getSubjects(null, null)) {
			if (aRes instanceof org.openrdf.model.URI) {
				org.openrdf.model.URI aURI = (org.openrdf.model.URI) aRes;
System.err.println("current: " + aURI);
				StringWriter aWriter = new StringWriter();

				try {
					SesameIO.writeGraph(aRepo.describe(aURI), aWriter, RDFFormat.TURTLE);

					try {
						aStore.delete(URI.create(aURI.getURI()));
					}
					catch (StoreException e) {
						System.err.println("delete error: " + e.getMessage());
					}

					aStore.add(aWriter.toString(), Format.Turtle, URI.create(aURI.getURI()));
				}
				catch (StoreException e) {
					e.printStackTrace();
				}
			}
			else {
				System.err.println("don't know what to do with: " + aRes);
			}
		}

		aStore.delete(URI.create(aDefaultGraphURI));
		
	}

	public static void main(String[] args) throws Exception {
		prepFourStore();
	}

}
