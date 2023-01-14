package com.fiberg.wsio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to specify if a parameter should be wrapped.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOElementWrapper {

	/** Name of the element wrapper */
	String name() default "##default";

	/** Namespace of the element wrapper */
	String namespace() default "##default";

	/** Inner required of the element wrapper */
	boolean required() default false;

	/** Inner nillable of the element wrapper */
	boolean nillable() default false;

}
