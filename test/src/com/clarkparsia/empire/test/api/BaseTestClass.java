package com.clarkparsia.empire.test.api;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;

import javax.persistence.PrePersist;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;
import javax.persistence.PostUpdate;
import javax.persistence.PostLoad;

import java.net.URI;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 29, 2009 3:45:27 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class BaseTestClass implements SupportsRdfId {
	public boolean preUpdateCalled = false;
	public boolean postUpdateCalled = false;
	public boolean preRemoveCalled = false;
	public boolean postRemoveCalled = false;
	public boolean prePersistCalled = false;
	public boolean postPersistCalled = false;
	public boolean postLoadCalled = false;

	private SupportsRdfId mIdSupport = new SupportsRdfIdImpl();

	public URI getRdfId() {
		return mIdSupport.getRdfId();
	}

	public void setRdfId(final URI theId) {
		mIdSupport.setRdfId(theId);
	}

	public void clearState() {
		prePersistCalled = preRemoveCalled = preUpdateCalled = false;
		postLoadCalled = postPersistCalled = postRemoveCalled = postUpdateCalled = false;
	}

	@PrePersist
	public void onPrePersist() {
		prePersistCalled = true;
	}

	@PostPersist
	public void onPostPersist() {
		postPersistCalled = true;
	}

	@PreRemove
	public void onPreRemove() {
		preRemoveCalled = true;
	}

	@PostRemove
	public void onPostRemove() {
		postRemoveCalled = true;
	}

	@PreUpdate
	public void onPreUpdate() {
		preUpdateCalled = true;
	}

	@PostUpdate
	public void onPostUpdate() {
		postUpdateCalled = true;
	}

	@PostLoad
	public void onPostLoad() {
		postLoadCalled = true;
	}
}
