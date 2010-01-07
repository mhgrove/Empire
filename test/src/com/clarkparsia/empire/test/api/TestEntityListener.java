package com.clarkparsia.empire.test.api;

import javax.persistence.PrePersist;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;
import javax.persistence.PostUpdate;
import javax.persistence.PostLoad;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Jan 5, 2010 10:34:44 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class TestEntityListener {
	public static boolean preUpdateCalled = false;
	public static boolean postUpdateCalled = false;
	public static boolean preRemoveCalled = false;
	public static boolean postRemoveCalled = false;
	public static boolean prePersistCalled = false;
	public static boolean postPersistCalled = false;
	public static boolean postLoadCalled = false;

	public static void clearState() {
		prePersistCalled = preRemoveCalled = preUpdateCalled = false;
		postLoadCalled = postPersistCalled = postRemoveCalled = postUpdateCalled = false;
	}

	@PrePersist
	public void onPrePersist(Object theObj) {
		prePersistCalled = true;
	}

	@PostPersist
	public void onPostPersist(Object theObj) {
		postPersistCalled = true;
	}

	@PreRemove
	public void onPreRemove(Object theObj) {
		preRemoveCalled = true;
	}

	@PostRemove
	public void onPostRemove(Object theObj) {
		postRemoveCalled = true;
	}

	@PreUpdate
	public void onPreUpdate(Object theObj) {
		preUpdateCalled = true;
	}

	@PostUpdate
	public void onPostUpdate(Object theObj) {
		postUpdateCalled = true;
	}

	@PostLoad
	public void onPostLoad(Object theObj) {
		postLoadCalled = true;
	}
}
