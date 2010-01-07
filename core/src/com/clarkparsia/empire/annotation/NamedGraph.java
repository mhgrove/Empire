package com.clarkparsia.empire.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Title: NamedGraph<br/>
 * Description: <p>Annotation for specifying the named graph instances of a Java object should be persisted to.  When
 * the type is {@link NamedGraph.NamedGraphType#Instance} the resulting RDF will be persisted to a graph whose
 * name is the same as the rdf:ID of the instance being saved.  When the type is {@link NamedGraph.NamedGraphType#Static}
 * you must also specify a value, and the specified URI value is what is used as the named graph uri during persistence.</p>
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
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com><br/>
 * Created: Dec 14, 2009 3:31:51 PM<br/>
 *
 * @author Michael Grove <mike@clarkparsia.com><br/>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedGraph {
	public enum NamedGraphType {
		Instance,
		Static,
	}
	public NamedGraphType type() default NamedGraphType.Instance;
	public String value() default "";
}
