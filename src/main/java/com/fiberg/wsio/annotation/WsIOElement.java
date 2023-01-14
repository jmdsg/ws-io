package com.fiberg.wsio.annotation;

import jakarta.xml.bind.annotation.XmlElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotation used to specify if a parameter should be wrapped.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOElement {

	/** Name of the element */
	String name() default "##default";

	/** Nillable of the element */
	boolean nillable() default false;

	/** Required of the element */
	boolean required() default false;

	/** Namespace of the element */
	String namespace() default "##default";

	/** Default value of the element */
	String defaultValue() default "\u0000";

	/** Type of the element */
	Class<?> type() default XmlElement.DEFAULT.class;

}
