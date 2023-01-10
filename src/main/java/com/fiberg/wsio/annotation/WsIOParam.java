package com.fiberg.wsio.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to specify a param data.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface WsIOParam {

	/** Name of the param */
	String name() default "";

	/** Namespace od the param */
	String targetNamespace() default "";

	/** Required of the param */
	boolean required() default false;

	/** Nillable of the param */
	boolean nillable() default false;

}
