package com.clarkparsia.empire.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Title: RdfClass<br/>
 * Description: <p>Annotation used to specify the rdf:type of the individual corresponding to instances of the Java class.</p>
 * <p>
 * Usage:<br/>
 * <code>
 * @RdfClass("foaf:Person")
 * @NamedGraph(type = NamedGraph.NamedGraphType.Instance)
 * public class Foo implements SupportsRdfId {
 *   ...
 * }
 * </code>
 * </p>
 * <p>All resulting instances of the class Foo will be typed as a foaf:Person.</p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2009 3:09:50 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfsClass {
	public String value();
}
