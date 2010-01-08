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

package com.clarkparsia.empire.test.api;

import javax.persistence.PrePersist;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;
import javax.persistence.PostUpdate;
import javax.persistence.PostLoad;

/**
 * <p>Support class for testing EntityListener operations</p>
 *
 * @author Michael Grove
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
