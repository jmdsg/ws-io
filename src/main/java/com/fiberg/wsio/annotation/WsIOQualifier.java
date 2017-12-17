package com.fiberg.wsio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to identify a cloned object annotated with {@link WsIOClone}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOQualifier {

	/** Prefix name of the cloned class */
	String prefix() default "";

	/** Suffix name of the cloned class */
	String suffix() default "";

}
