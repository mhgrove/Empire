/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.empire.test.lazyload;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;
import com.google.common.collect.ComparisonChain;

@Namespaces({
				"empire",  "https://github.com/mhgrove/Empire/",
				"dcterms", "http://purl.org/dc/terms/" })
@Entity
@RdfsClass("empire:Event")
public class Event extends BaseRdfEntity implements Comparable<Event>, SupportsRdfId {
    public enum Status {
        New(0x10), Running(0x11), Complete(0x21), Failed(0x41), Canceled(0x42);

        public final int id;

        Status(int id) {
            this.id = id;
        }

        public static Status getStatus(int value) {
            return (value == New.id)? New:
                   (value == Running.id)? Running:
                   (value == Complete.id)? Complete:
                   (value == Failed.id)? Failed:
                   (value == Canceled.id)? Canceled: null;
        }
    }

	private final static SimpleDateFormat FMT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	@RdfProperty("dcterms:subject")
	private String subject;
	@RdfProperty("dcterms:description")
	private String parameters;
	@RdfProperty("datalift:eventStatus")
	private int status = Status.New.id;
	@RdfProperty("dcterms:title")
	private String outcome;
	@RdfProperty("dcterms:created")
	private Date startDate = new Date();
	@RdfProperty("dcterms:issued")
	private Date endDate;

	protected Event() {
		// Default constructor for persistence.
	}
	protected Event(String subject, String parameters) {
		this(subject, parameters, null, null);
	}
	protected Event(String subject, String parameters,
						Status status,  String outcome) {
		if ((subject == null) || (subject.length() == 0)) {
			throw new IllegalArgumentException("subject");
		}
		if (status == null) {
			status = Status.New;
		}
		this.subject = subject;
		this.parameters = parameters;
		this.update(status, outcome);
	}

	public String getSubject() { return this.subject; }

	public String getParameters() { return this.parameters; }

	public Status getStatus() { return Status.getStatus(this.status); }

	public String getOutcome() { return this.outcome; }

	public Date getStartDate() { return this.startDate; }

	public Date getEndDate() { return this.endDate; }

	public final void update(Status status, String outcome) {
		if (status == null) {
			throw new IllegalArgumentException("status");
		}
		this.status = status.id;
		this.endDate = new Date();
		this.outcome = outcome;
	}

	@Override
	protected final void setId(String id) {
		// NOP
	}

	@Override
	public int compareTo(Event e) {
		return ComparisonChain.start()
			.compare(getStartDate(), e.getStartDate())
			.compare(getEndDate(), e.getEndDate())
			.compare(getParameters(), e.getParameters())
			.compare(getStatus(), e.getStatus())
			.compare(getSubject(), e.getSubject())
			.result();
	}

	//-------------------------------------------------------------------------
	// Object contract support
	//-------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(64);
		b.append("\n\t\t")
			.append(this.getClass().getSimpleName())
			.append(" {").append(this.getStatus())
			.append(", \"").append(this.getParameters())
			.append("\", ").append(FMT.format(this.getStartDate()));
		if ((this.getEndDate() != null) &&
			(! this.getEndDate().equals(this.getStartDate()))) {
			b.append(" - ").append(FMT.format(this.getEndDate()));
		}
		if (this.getOutcome() != null) {
			b.append(", \"").append(this.getOutcome()).append('"');
		}
		return b.append(" }").toString();
	}
}
