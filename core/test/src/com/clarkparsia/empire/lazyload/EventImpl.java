package com.clarkparsia.empire.lazyload;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfProperty;
import com.clarkparsia.empire.annotation.RdfsClass;

import javax.persistence.Entity;
import java.text.SimpleDateFormat;
import java.util.Date;

@Namespaces({
    "empire",  "https://github.com/mhgrove/Empire/",
    "dcterms", "http://purl.org/dc/terms/" })
@Entity
@RdfsClass("empire:Event")
public class EventImpl extends BaseRdfEntity
                       implements Event, Comparable<Event>
{
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

    protected EventImpl() {
        // Default constructor for persistence.
    }
    protected EventImpl(String subject, String parameters) {
        this(subject, parameters, null, null);
    }
    protected EventImpl(String subject, String parameters,
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

    @Override
    public final String getSubject() { return this.subject; }

    @Override
    public String getParameters() { return this.parameters; }

    @Override
    public Status getStatus() { return Status.getStatus(this.status); }

    @Override
    public String getOutcome() { return this.outcome; }

    @Override
    public Date getStartDate() { return this.startDate; }

    @Override
    public Date getEndDate() { return this.endDate; }

    @Override
    public final void update(Status status, String outcome) {
        if (status == null) {
            throw new IllegalArgumentException("status");
        }
        this.status = status.id;
        this.endDate = new Date();
        this.outcome = outcome;
    }

    @Override
    protected void setId(String id) {
        // NOP
    }

    @Override
    public int compareTo(Event e) {
        // Older start date first.
        int n = this.getStartDate().compareTo(e.getStartDate());
        if (n == 0) {
            // Older end date first.
            if (this.getEndDate() != null) {
                n = (e.getEndDate() != null)?
                                this.getEndDate().compareTo(e.getEndDate()): -1;
            }
            else {
                n = (e.getEndDate() != null)? 1: 0;
            }
        }
        // Distinguish events by their URI, to avoid sets to remove some.
        if ((n == 0) && (e instanceof EventImpl)) {
            n = String.valueOf(this.getRdfId()).compareTo(
                            String.valueOf(((EventImpl)e).getRdfId()));
        }
        return n;
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
