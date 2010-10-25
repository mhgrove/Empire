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
import javax.persistence.OneToOne;
import javax.persistence.CascadeType;

import java.util.List;
import java.util.ArrayList;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
@Namespaces({"space", "http://purl.org/net/schemas/space/"})
@Entity
@RdfsClass("space:Launch")
@NamedGraph(type = NamedGraph.NamedGraphType.Instance)
public class Launch extends BaseTestClass {
	@RdfProperty("space:spacecraft")
	private List<Spacecraft> spacecraft = new ArrayList<Spacecraft>();

	@RdfProperty("space:launched")
	private String launched;

	@RdfProperty("space:launchvehicle")
	private List<String> launchvehicle;

	@RdfProperty("space:launchsite")
	private LaunchSite launchSite;

	@RdfProperty("space:spacecraftOther")
	private Spacecraft otherSpacecraft;

	public Spacecraft getOtherSpacecraft() {
		return otherSpacecraft;
	}

	public void setOtherSpacecraft(final Spacecraft theOtherSpacecraft) {
		otherSpacecraft = theOtherSpacecraft;
	}

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
		if (!(o instanceof Launch)) {
			return false;
		}

		final Launch aLaunch = (Launch) o;

		if (getLaunchSite() != null ? !getLaunchSite().equals(aLaunch.getLaunchSite()) : aLaunch.getLaunchSite() != null) {
			return false;
		}
		if (getLaunched() != null ? !getLaunched().equals(aLaunch.getLaunched()) : aLaunch.getLaunched() != null) {
			return false;
		}
		if (!((getLaunchvehicle() == null && aLaunch.getLaunchvehicle() == null) ||
			(getLaunchvehicle() != null && aLaunch.getLaunchvehicle() != null && CollectionUtil.contentsEqual(getLaunchvehicle(), aLaunch.getLaunchvehicle())))) {
			return false;
		}
		if (getSpacecraft() != null ? !getSpacecraft().equals(aLaunch.getSpacecraft()) : aLaunch.getSpacecraft() != null) {
			return false;
		}
		if (!BasicUtils.equalsOrNull(getRdfId(), aLaunch.getRdfId())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getRdfId() == null ? super.hashCode() : getRdfId().hashCode();
	}
}
