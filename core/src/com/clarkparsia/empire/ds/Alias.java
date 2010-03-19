package com.clarkparsia.empire.ds;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since 0.6.3
 * @version 0.6.3
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {
	public String value();
}
