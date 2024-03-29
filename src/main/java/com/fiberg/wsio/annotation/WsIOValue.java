package com.fiberg.wsio.annotation;

import com.fiberg.wsio.enumerate.WsIOType;

import java.lang.annotation.*;

/**
 * <p>Annotation used to specify if a parameter should be used as a value.</p>
 */
@Repeatable(WsIOValues.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface WsIOValue {

	/** Prefix name of the value */
	String prefix() default "##default";

	/** Suffix name of the value */
	String suffix() default "##default";

	/** Target value of the value */
	WsIOType target() default WsIOType.BOTH;

}
