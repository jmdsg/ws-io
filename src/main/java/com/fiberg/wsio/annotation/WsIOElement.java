package com.fiberg.wsio.annotation;

import jakarta.xml.bind.annotation.XmlElement;

import java.lang.annotation.*;

/**
 * <p>Annotation used to specify if a parameter should be wrapped.</p>
 */
@Repeatable(WsIOElements.class)
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

	/** Prefix name of the element */
	String prefix() default "##default";

	/** Suffix name of the element */
	String suffix() default "##default";

}
