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

package com.clarkparsia.empire.test.api.nasa;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.NamedGraph;
import com.clarkparsia.empire.test.api.BaseTestClass;

import javax.persistence.Entity;

import java.util.List;
import java.util.ArrayList;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
@Namespaces({"", "http://purl.org/net/schemas/space/"})
@Entity
@RdfsClass("Launch")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Launch extends BaseTestClass {
	@RdfProperty("spacecraft")
	private List<Spacecraft> spacecraft = new ArrayList<Spacecraft>();

	@RdfProperty("launched")
	private String launched;

	@RdfProperty("launchvehicle")
	private List<String> launchvehicle;

	@RdfProperty("launchsite")
	private LaunchSite launchSite;

	public List<Spacecraft> getSpacecraft() {
		return spacecraft;
	}

	public void setSpacecraft(final List<Spacecraft> theSpacecraft) {
		spacecraft = theSpacecraft;
	}

	public String getLaunched() {
		return launched;
	}

	public void setLaunched(final String theLaunched) {
		launched = theLaunched;
	}

	public List<String> getLaunchvehicle() {
		return launchvehicle;
	}

	public void setLaunchvehicle(final List<String> theLaunchvehicle) {
		launchvehicle = theLaunchvehicle;
	}

	public LaunchSite getLaunchSite() {
		return launchSite;
	}

	public void setLaunchSite(final LaunchSite theLaunchSite) {
		launchSite = theLaunchSite;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Launch aLaunch = (Launch) o;

		if (launchSite != null ? !launchSite.equals(aLaunch.launchSite) : aLaunch.launchSite != null) {
			return false;
		}
		if (launched != null ? !launched.equals(aLaunch.launched) : aLaunch.launched != null) {
			return false;
		}
		if (!((launchvehicle == null && aLaunch.launchvehicle == null) ||
			(launchvehicle != null && aLaunch.launchvehicle != null && CollectionUtil.contentsEqual(launchvehicle, aLaunch.launchvehicle)))) {
			return false;
		}
		if (spacecraft != null ? !spacecraft.equals(aLaunch.spacecraft) : aLaunch.spacecraft != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), aLaunch.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getRdfId().hashCode();
	}
}
