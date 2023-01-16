package com.fiberg.wsio.annotation;

import com.fiberg.wsio.processor.WsIOType;

import java.lang.annotation.*;

/**
 * <p>Annotation used to specify if a parameter should be wrapped.</p>
 */
@Repeatable(WsIOElementWrappers.class)
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

	/** Prefix name of the element wrapper */
	String prefix() default "##default";

	/** Suffix name of the element wrapper */
	String suffix() default "##default";

	/** Target value of the element wrapper */
	WsIOType target() default WsIOType.BOTH;

}
