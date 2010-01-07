package com.clarkparsia.empire;

/**
 * Title: SupportsRdfId<br/>
 * Description: Interface for anything that wants to support having an rdf:ID<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
public interface SupportsRdfId {
	/**
	 * Return the rdf:ID of this instance
	 * @return the rdf:ID
	 */
	public java.net.URI getRdfId();

	/**
	 * Set the rdf:ID for this object
	 * @param theId the new rdf:ID
	 */
	void setRdfId(java.net.URI theId);
}
