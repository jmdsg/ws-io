package com.fiberg.wsio.annotation;

import jakarta.jws.WebParam;

import java.lang.annotation.*;

/**
 * <p>Annotation used to specify a param data.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface WsIOParam {

	/** Name of the param */
	String name() default "";

	/** Part name of the param */
	String partName() default "";

	/** Namespace of the param */
	String targetNamespace() default "";

	/** Header of the param */
	WebParam.Mode mode() default WebParam.Mode.IN;

	/** Mode of the param */
	boolean header() default false;

}
